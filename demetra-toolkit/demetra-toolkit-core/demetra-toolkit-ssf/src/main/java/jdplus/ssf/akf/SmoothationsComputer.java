/*
 * Copyright 2022 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.ssf.akf;

import demetra.data.DoubleSeqCursor;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.data.DataBlockStorage;
import jdplus.math.matrices.GeneralMatrix;
import jdplus.math.matrices.LowerTriangularMatrix;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.MatrixStorage;
import jdplus.math.matrices.MatrixTransformation;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.ISsfInitialization;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.State;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.univariate.ISsfData;

/**
 *
 * @author Jean Palate
 */
public class SmoothationsComputer {

    private AugmentedState state;
    private ISsfDynamics dynamics;
    private ISsfLoading loading;
    private DataBlockStorage allR;
    private MatrixStorage allRvar;
    private DefaultQFilteringResults frslts;

    private double err, errVariance;
    private double u, uc, ucVariance;
    private DataBlock M, K, E, U, R, Rc;
    private FastMatrix N, Nc, Rd, S;
//    private FastMatrix V;
    private DataBlock delta;
    private boolean missing, hasinfo;

    public boolean process(ISsf ssf, final ISsfData data) {
        int n = data.length();
        frslts = DefaultQFilteringResults.light();
        frslts.prepare(ssf, 0, n);
        AugmentedFilter filter = new AugmentedFilter(false);
        filter.process(ssf, data, frslts);
        initFilter(ssf);
        int dim = ssf.getStateDim();
        allR = new DataBlockStorage(dim, n);
        allRvar = new MatrixStorage(dim, dim, n);

        initSmoother(ssf);
        while (--n >= 0) {
            iterate(n);
            if (hasinfo) {
                allR.save(n, Rc);
                allRvar.save(n, Nc);
            }
        }

        return true;
    }

    private void initSmoother(ISsf ssf) {
        ISsfInitialization initialization = ssf.initialization();
        int dim = initialization.getStateDim();
        int nd = initialization.getDiffuseDim();
        state = new AugmentedState(dim, nd);

        R = DataBlock.make(dim);
        Rc = DataBlock.make(dim);
        M = DataBlock.make(dim);
        K = DataBlock.make(dim);
        E = DataBlock.make(nd);
        U = DataBlock.make(nd);
        Rd = FastMatrix.make(dim, nd);

        N = FastMatrix.square(dim);
        Nc = FastMatrix.square(dim);
//            V = FastMatrix.make(dim, nd);

        // computes the smoothed diffuse effects and their covariance...
        QAugmentation q = frslts.getAugmentation();
//        FastMatrix B = q.B(); // B*a^-1'
        // Psi = = a'^-1* a^-1
        S = q.a().deepClone(); // 
        // delta=-a'^-1 * b
        delta = q.b().deepClone();
        LowerTriangularMatrix.solvexL(S, delta);
        delta.chs();
        FastMatrix is = LowerTriangularMatrix.inverse(S);
        FastMatrix C = SymmetricMatrix.LtL(is);
    }

    private void loadInfo(int pos) {
        err = frslts.error(pos);
        errVariance = frslts.errorVariance(pos);
        missing = !Double.isFinite(err);

        E.copy(frslts.E(pos));
        // P*Z
        M.copy(frslts.M(pos));
        // T*P*Z/f
        if (errVariance != 0) {
            K.copy(frslts.M(pos));
            dynamics.TX(pos, K);
            K.div(errVariance);
        }
        DataBlock fa = frslts.a(pos);
        hasinfo = fa != null;
        if (!hasinfo) {
            return;
        }
        state.a().copy(fa);
    }

    private void iterate(int pos) {
        loadInfo(pos);
        iterateSmoothation(pos);
        iterateR(pos);
        iterateN(pos);
    }

    private void xL(int pos, DataBlock x) {
        // xL = x(T-KZ) = x(T-Tc/f*Z) = xT - ((xT)*c)/f * Z
        // compute xT
        dynamics.XT(pos, x);
        // compute q=xT*c
        double q = x.dot(M);
        // remove q/f*Z
        loading.XpZd(pos, x, -q / errVariance);
    }

    private void XL(int pos, DataBlockIterator X) {
        while (X.hasNext()) {
            xL(pos, X.next());
        }
    }

    /**
     *
     */
    private void iterateN(int pos) {
        if (!missing) {
            // rc(t-1)=r(t-1)+d*R(t-1) 
            // Nc(t-1)=
            // N(t-1) = Z'(t)*Z(t)/f(t) + L'(t)*N(t)*L(t)
            XL(pos, N.rowsIterator());
            XL(pos, N.columnsIterator());
            loading.VpZdZ(pos, N, 1 / errVariance);
        } else {
            dynamics.MT(pos, N);
            dynamics.TtM(pos, N);
        }
        SymmetricMatrix.reenforceSymmetry(N);
        N.apply(z -> Math.abs(z) < State.ZERO ? 0 : z);

        FastMatrix W = Rd.deepClone();
        LowerTriangularMatrix.solveXLt(S, W);
        Nc.copy(N);
        GeneralMatrix.aAB_p_bC(-1, W, W, 1, Nc, MatrixTransformation.None, MatrixTransformation.Transpose);
        SymmetricMatrix.reenforceSymmetry(Nc);
        Nc.apply(z -> Math.abs(z) < State.ZERO ? 0 : z);
    }

    /**
     *
     */
    private void iterateR(int pos) {
        // r(t-1)=u(t)Z(t)+r(t)T(t)
        // R(t-1)=U(t)Z(t)+R(t)T(t)
        // rc(t-1)=r(t-1)+d*R(t-1) [=uc(t)Z(t)+rc(t)T(t)]
        dynamics.XT(pos, R);
        dynamics.TtM(pos, Rd);
        if (!missing && errVariance != 0) {
            // RT
            loading.XpZd(pos, R, u);
            DataBlockIterator rcols = Rd.columnsIterator();
            DoubleSeqCursor ucur = U.cursor();
            while (rcols.hasNext()) {
                loading.XpZd(pos, rcols.next(), ucur.getAndNext());
            }
        }
        Rc.copy(R);
        Rc.addProduct(Rd.rowsIterator(), delta);
        Rc.apply(z -> Math.abs(z) < State.ZERO ? 0 : z);
    }

    private void iterateSmoothation(int pos) {
        // u = v(t)/f(t)-K'(t)*R(t)
        if (missing) {
            u = Double.NaN;
            uc = Double.NaN;
            U.set(Double.NaN);
            ucVariance = Double.NaN;
            return;
        }

        if (errVariance != 0) {
            u = err / errVariance - R.dot(K);
            // apply the same to the colums of Rd
            U.product(K, Rd.columnsIterator());
            U.chs();
            U.addAY(1 / errVariance, E);
            uc = u + U.dot(delta);
//            if (calcvar) {
//                FastMatrix A = frslts.B(pos + 1);
//                // N*A
//                FastMatrix NA = GeneralMatrix.AB(N, A);
//                NA.add(Rd);
//                DataBlock C = DataBlock.make(U.length());
//                C.product(K, NA.columnsIterator());
//                C.chs();
//                ucVariance = 1 / errVariance + QuadraticForm.apply(Nc, K);
//                if (ucVariance < State.ZERO) {
//                    ucVariance = 0;
//                }
//                if (ucVariance == 0) {
//                    if (Math.abs(uc) < State.ZERO) {
//                        uc = 0;
//                    } else {
//                        throw new SsfException(SsfException.INCONSISTENT);
//                    }
//                }
//            }
        } else {
            u = -R.dot(K);
            // apply the same to the colums of Rd
            U.product(K, Rd.columnsIterator());
            U.chs();
            uc = u + U.dot(delta);
        }
    }

    private void initFilter(ISsf ssf) {
        dynamics = ssf.dynamics();
        loading = ssf.loading();
    }

    public DefaultQFilteringResults getFilteringResults() {
        return frslts;
    }
    
    public DataBlock R(int pos){
        return allR.block(pos);
    }

    public FastMatrix Rvar(int pos){
        return allRvar.matrix(pos);
    }
 
}
