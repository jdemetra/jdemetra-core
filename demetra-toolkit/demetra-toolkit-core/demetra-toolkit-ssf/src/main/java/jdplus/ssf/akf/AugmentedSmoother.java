/*
 * Copyright 2016 National Bank copyOf Belgium
 *  
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions copyOf the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy copyOf the Licence at:
 *  
 * http://ec.europa.eu/idabc/eupl
 *  
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.ssf.akf;

import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.math.matrices.LowerTriangularMatrix;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.StateInfo;
import jdplus.ssf.univariate.ISmoothingResults;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.univariate.ISsfData;
import jdplus.ssf.univariate.OrdinarySmoother;
import demetra.data.DoubleSeqCursor;
import jdplus.math.matrices.GeneralMatrix;
import jdplus.ssf.ISsfInitialization;
import jdplus.ssf.ISsfLoading;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.QuadraticForm;
import jdplus.ssf.SsfException;
import jdplus.ssf.State;

/**
 *
 * @author Jean Palate
 */
public class AugmentedSmoother {

    private AugmentedState state;
    private ISsfDynamics dynamics;
    private ISsfLoading loading;
    private ISmoothingResults srslts;
    private DefaultAugmentedFilteringResults frslts;

    private double err, errVariance, u, uc, ucVariance;
    private DataBlock M, K, E, U, R, Rc;
    private Matrix N, Nc, Rd, B, V, RNA, S;
    private Matrix Psi;
    private DataBlock delta;
    private boolean missing, hasinfo, calcvar = true;

    public boolean process(final ISsf ssf, final ISsfData data, ISmoothingResults sresults) {
        DefaultAugmentedFilteringResults fresults = AkfToolkit.filter(ssf, data, true);
        return process(ssf, data.length(), fresults, sresults);
    }

    public boolean process(ISsf ssf, final int endpos, DefaultAugmentedFilteringResults results, ISmoothingResults sresults) {
        frslts = results;
        srslts = sresults;
        initFilter(ssf);
        initSmoother(ssf, endpos);
        ordinarySmoothing(ssf, endpos);
        calcSmoothedDiffuseEffects();
        int t = frslts.getCollapsingPosition();
        while (--t >= 0) {
            iterate(t);
            if (hasinfo) {
                srslts.saveSmoothation(t, uc, ucVariance);
                srslts.saveR(t, Rc, Nc);
                srslts.save(t, state, StateInfo.Smoothed);
            }
        }

        return true;
    }

    public ISmoothingResults getResults() {
        return srslts;
    }

    private void initSmoother(ISsf ssf, int end) {
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
        Rd = Matrix.make(dim, nd);
        B = Matrix.make(dim, nd);

        if (calcvar) {
            N = Matrix.square(dim);
            Nc = Matrix.square(dim);
            V = Matrix.make(dim, nd);
            RNA = Matrix.make(dim, nd);
        }
    }

    private void loadInfo(int pos) {
        err = frslts.error(pos);
        errVariance = frslts.errorVariance(pos);

        E.copy(frslts.E(pos));
        // P*Z
        M.copy(frslts.M(pos));
        // T*P*Z/f
        K.copy(frslts.M(pos));
        dynamics.TX(pos, K);
        K.div(errVariance);

        missing = !Double.isFinite(err);
        DataBlock fa = frslts.a(pos);
        hasinfo = fa != null;
        if (!hasinfo) {
            return;
        }
        state.a().copy(fa);
        if (calcvar) {
            state.restoreB(frslts.B(pos));
            state.P().copy(frslts.P(pos));
        }
    }

    private void iterate(int pos) {
        loadInfo(pos);
        iterateSmoothation(pos);
        iterateR(pos);
        calcB(pos);
        updateA(pos);
        if (calcvar) {
            // P = P-PNP
            iterateN(pos);
            calcV();
            updateP();
        }
    }

    // 
    private void calcB(int pos) {
        // B = A + PR
        DataBlockIterator bcolumns = B.columnsIterator();
        DataBlockIterator rcolumns = Rd.columnsIterator();
        DataBlockIterator acolumns = calcvar ? state.B().columnsIterator() : frslts.B(pos).columnsIterator();
        DataBlockIterator prows = calcvar ? state.P().rowsIterator() : frslts.P(pos).rowsIterator();
        while (bcolumns.hasNext() && rcolumns.hasNext() && acolumns.hasNext()) {
            prows.reset();
            DataBlock uc = bcolumns.next();
            uc.product(prows, rcolumns.next());
            uc.add(acolumns.next());
        }
    }

    private void calcV() {
        // V =  PR + PNA = P(R+NA)

        // RNA = R + NA
        DataBlockIterator rnacolumns = RNA.columnsIterator();
        DataBlockIterator rcolumns = Rd.columnsIterator();
        DataBlockIterator acolumns = state.B().columnsIterator();
        while (rnacolumns.hasNext() && rcolumns.hasNext() && acolumns.hasNext()) {
            DataBlock rnac = rnacolumns.next();
            rnac.product(N.rowsIterator(), acolumns.next());
            rnac.add(rcolumns.next());
        }

        DataBlockIterator vcolumns = V.columnsIterator();
        rnacolumns.reset();
        while (vcolumns.hasNext() && rnacolumns.hasNext()) {
            vcolumns.next().product(state.P().rowsIterator(), rnacolumns.next());
        };

    }

    private void updateA(int pos) {
        DataBlock a = state.a();
        // normal iteration
        a.addProduct(R, calcvar ? state.P().columnsIterator() : frslts.P(pos).columnsIterator());
        // diffuse correction
        mcorrection(a, B);
    }

    private void updateP() {
        Matrix P = state.P();
        // normal iteration
        Matrix PNP = SymmetricMatrix.XtSX(N, P);
        P.sub(PNP);
        // diffuse correction
        vcorrection(P, B, V);
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
        if (!missing && uc != 0) {
            // N(t-1) = Z'(t)*Z(t)/f(t) + L'(t)*N(t)*L(t)
            XL(pos, N.rowsIterator());
            XL(pos, N.columnsIterator());
            loading.VpZdZ(pos, N, 1 / errVariance);
        } else {
            dynamics.MT(pos, N);
            dynamics.TtM(pos, N);
        }
        SymmetricMatrix.reenforceSymmetry(N);
        if (uc != 0) {
            Matrix A = frslts.B(pos + 1).deepClone();
            // N*A
            Matrix NA = GeneralMatrix.AB(N, A);
            NA.add(Rd);
            Nc.copy(N);
            vcorrection(Nc, Rd.deepClone(), NA);
            SymmetricMatrix.reenforceSymmetry(Nc);
            Nc.apply(z -> Math.abs(z) < State.ZERO ? 0 : z);
        }
    }

    /**
     *
     */
    private void iterateR(int pos) {
        // R(t-1)=u(t)Z(t)+R(t)T(t)
        dynamics.XT(pos, R);
        dynamics.TtM(pos, Rd);
        if (!missing) {
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
        }

        if (errVariance != 0) {
            u = err / errVariance - R.dot(K);
            // apply the same to the colums of Rd
            U.product(K, Rd.columnsIterator());
            U.chs();
            U.addAY(1 / errVariance, E);
            uc = u + U.dot(delta);
            if (calcvar) {
                Matrix A = frslts.B(pos + 1);
                // N*A
                Matrix NA = GeneralMatrix.AB(N, A);
                NA.add(Rd);
                DataBlock C = DataBlock.make(U.length());
                C.product(K, NA.columnsIterator());
                ucVariance = 1 / errVariance + QuadraticForm.apply(N, K) - vcorrection(U.deepClone(), C);
            }
            if (ucVariance < State.ZERO) {
                ucVariance = 0;
            }
            if (ucVariance == 0) {
                if (Math.abs(uc) < State.ZERO) {
                    uc = 0;
                } else {
                    throw new SsfException(SsfException.INCONSISTENT);
                }
            }
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

    public void setCalcVariances(boolean b) {
        calcvar = b;
    }

    public boolean isCalcVariances() {
        return calcvar;
    }

    private void ordinarySmoothing(ISsf ssf, final int endpos) {
        OrdinarySmoother smoother = OrdinarySmoother
                .builder(ssf)
                .calcVariance(calcvar)
                .rescaleVariance(false)
                .build();
        smoother.process(frslts.getCollapsingPosition(), endpos, frslts, srslts);
        // updates R, N
        R.copy(smoother.getFinalR());
        Rc.copy(smoother.getFinalR());
        if (calcvar) {
            N.copy(smoother.getFinalN());
            Nc.copy(smoother.getFinalN());
        }
    }

    private void calcSmoothedDiffuseEffects() {
        // computes the smoothed diffuse effects and their covariance...
        QAugmentation q = frslts.getAugmentation();
        // delta = S(s+B'*R), psi = Psi= S - S*B'*N*B*S 
        // delta = a'^-1*a^-1(-a*b' + B'*R)
        // delta = - (b * a^-1)' + a'^-1*a^-1*B'*r = a'^-1 * (a^-1*B'*r - b)
        // Psi = = a'^-1*(I - a^-1*B'*N*B*a'^-1)* a^-1
        Matrix B = q.B(); // B*a^-1'
        S = q.a().deepClone();
        // computes a^-1*B'*r (or r*B*a^-1')
        delta = DataBlock.make(B.getColumnsCount());
        delta.product(B.columnsIterator(), R);
        // t1 = - b*a^-1 <-> -t1*a=b
        delta.sub(q.b());
        LowerTriangularMatrix.solvexL(S, delta);
        // B'NB 
        if (N != null) {
            Psi = SymmetricMatrix.XtSX(N, B);
            Psi.chs();
            Psi.diagonal().add(1);
            // B*a^-1* =C <->B =Ca
            LowerTriangularMatrix.solveXL(S, Psi);
            // a'^-1*B = C <-> B' = C'a
            LowerTriangularMatrix.solveLtX(S, Psi);
        }
    }

    public DefaultAugmentedFilteringResults getFilteringResults() {
        return frslts;
    }

    /**
     * x+=b*delta
     *
     * @param x
     * @param B
     */
    private void mcorrection(DataBlock x, Matrix B) {
        x.addProduct(B.rowsIterator(), delta);
    }

    /**
     * V+=B*psi*B'-B*S*C'+C*S*B')
     * Attention ! B, C are modified on exit. Make a copy if necessary
     *
     * @param V
     * @param B
     * @param C
     */
    private void vcorrection(Matrix V, Matrix B, Matrix C) {
        Matrix BPsiB = SymmetricMatrix.XSXt(Psi, B);
        V.add(BPsiB);
        LowerTriangularMatrix.solveXLt(S, B);
        LowerTriangularMatrix.solveXLt(S, C);
        // compute B*C'
        Matrix UV = GeneralMatrix.ABt(B, C);
        V.sub(UV);
        V.subTranspose(UV);
        SymmetricMatrix.reenforceSymmetry(V);
    }

    private double vcorrection(DataBlock B, DataBlock C) {
        double v = QuadraticForm.apply(Psi, B);
        LowerTriangularMatrix.solveLx(S, B);
        LowerTriangularMatrix.solveLx(S, C);
        // compute B*C'
        return v + 2 * B.dot(C);
    }
}
