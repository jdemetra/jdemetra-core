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

import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import demetra.math.Constants;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.GeneralMatrix;
import jdplus.math.matrices.LowerTriangularMatrix;
import jdplus.math.matrices.QuadraticForm;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.ISsfInitialization;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.State;
import jdplus.ssf.StateInfo;
import jdplus.ssf.univariate.ISmoothingResults;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.univariate.ISsfData;
import jdplus.ssf.univariate.OrdinarySmoother;

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
    private FastMatrix N, Nc, Rd, B, V, RNA, S;
    private FastMatrix Psi;
    private DataBlock delta;
    private boolean missing, hasinfo, calcvar = true;

    public boolean process(final ISsf ssf, final ISsfData data, ISmoothingResults sresults) {
        DefaultQFilteringResults fresults = AkfToolkit.filter(ssf, data, true, true);
        return process(ssf, data.length(), fresults, sresults);
    }

    public boolean process(ISsf ssf, final int endpos, DefaultQFilteringResults results, ISmoothingResults sresults) {
        frslts = results;
        srslts = sresults;
        initFilter(ssf);
        initSmoother(ssf, endpos);
        int t = results.getCollapsingPosition();
        if (t == 0) {
            QAugmentation q = results.getAugmentation();
            // delta = S(s+B'*R), psi = S - S*B'*N*B*S 
            // delta = a'^-1*a^-1(-a*b' + B'*R)
            // delta = - (b * a^-1)' + a'^-1*a^-1*B'*r = a'^-1 * (a^-1*B'*r - b)
            // Psi = = a'^-1*(I - a^-1*B'*N*B*a'^-1)* a^-1
            FastMatrix B = q.B(); // B*a^-1'
            S = q.a().deepClone();
            delta = q.b().deepClone();
            delta.chs();
            LowerTriangularMatrix.solvexL(S, delta);
            if (N != null) {
                Psi = FastMatrix.identity(S.getColumnsCount());
                LowerTriangularMatrix.solveXL(S, Psi);
                LowerTriangularMatrix.solveLtX(S, Psi);
            }
            return processNoCollapsing(endpos);
        }
        ordinarySmoothing(ssf, results.getCollapsingPosition(), endpos);
        if (t > 0) {
            calcSmoothedDiffuseEffects(results.getAugmentation());
            while (--t >= 0) {
                iterate(t);
                if (hasinfo) {
                    srslts.saveSmoothation(t, uc, ucVariance);
                    srslts.saveR(t, Rc, Nc);
                    srslts.save(t, state, StateInfo.Smoothed);
                }
            }
        }

        return true;
    }

    public boolean process(ISsf ssf, final int endpos, DefaultAugmentedFilteringResults results, FastMatrix S, DoubleSeq delta, ISmoothingResults sresults) {
        frslts = results;
        srslts = sresults;
        initFilter(ssf);
        initSmoother(ssf, endpos);
        this.delta = DataBlock.of(delta);
        this.S = S;
        if (N != null) {
            Psi = FastMatrix.identity(S.getColumnsCount());
            LowerTriangularMatrix.solveXL(this.S, Psi);
            LowerTriangularMatrix.solveLtX(this.S, Psi);
        }
        return processNoCollapsing(endpos);
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
        Rd = FastMatrix.make(dim, nd);
        B = FastMatrix.make(dim, nd);

        if (calcvar) {
            N = FastMatrix.square(dim);
            Nc = FastMatrix.square(dim);
            V = FastMatrix.make(dim, nd);
            RNA = FastMatrix.make(dim, nd);
        }
    }

    private void loadInfo(int pos) {
        missing = frslts.isMissing(pos);
        if (!missing) {
            err = frslts.error(pos);
            errVariance = frslts.errorVariance(pos);
            E.copy(frslts.E(pos));
            M.copy(frslts.M(pos));
            // T*P*Z/f
            if (errVariance != 0) {
                K.copy(frslts.M(pos));
                dynamics.TX(pos, K);
                K.div(errVariance);
            }
        }

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
        }

    }

    private void updateA(int pos) {
        // a(t) + P*r(t-1) + (A(t)+P*R(t-1))*d
        DataBlock a = state.a();
        // normal iteration
        a.addProduct(R, calcvar ? state.P().columnsIterator() : frslts.P(pos).columnsIterator());
        // diffuse correction
        mcorrection(a, B);
    }

    private void updateP() {
        // P(t|y)=var(a(t) + P*r(t-1) + (A(t)+P*R(t-1))*d)
        // B(t)=(A(t)+P*R(t-1)), C(t)=R(t-1)+N(t-1)*A(t)
        // P(t|y)=P(t)-P(t)N(t-1)P(t)+B(t)*psi*B'(t)-B(t)*S*C't)*P(t)-P(t)*C(t)*B'(t)
        FastMatrix P = state.P();
        // normal iteration
        FastMatrix PNP = SymmetricMatrix.XtSX(N, P);
        P.sub(PNP);
        // diffuse correction
        vcorrection(P, B, V);
        P.apply(z -> Math.abs(z) < State.ZERO ? 0 : z);
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
//        N.apply(z -> Math.abs(z) < State.ZERO ? 0 : z);

        FastMatrix A = frslts.B(pos);
        // Rd(t-1)+N(t-1)*A(t)
        FastMatrix NA = GeneralMatrix.AB(N, A);
        NA.add(Rd);
        Nc.set(0);
        vcorrection(Nc, Rd.deepClone(), NA);
        Nc.chs();
        Nc.add(N);
        SymmetricMatrix.reenforceSymmetry(Nc);
//        Nc.apply(z -> Math.abs(z) < State.ZERO ? 0 : z);
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
//        Rc.apply(z -> Math.abs(z) < State.ZERO ? 0 : z);
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
            if (calcvar) {
                FastMatrix A = frslts.B(pos);
                // N*A
                FastMatrix NA = GeneralMatrix.AB(N, A);
                NA.add(Rd);
                DataBlock C = DataBlock.make(U.length());
                C.product(K, NA.columnsIterator());
                C.chs();
                ucVariance = 1 / errVariance + QuadraticForm.apply(N, K) - vcorrection(U.deepClone(), C);
                if (ucVariance < Constants.MACHEP) {
                    ucVariance = 0;
                }
                if (ucVariance == 0) {
                    if (Math.abs(uc) < Constants.getEpsilon()) {
                        uc = 0;
//                    } else {
//                        throw new SsfException(SsfException.INCONSISTENT);
                    }
                }
            }
//        } else {
//            u = -R.dot(K);
//            // apply the same to the colums of Rd
//            U.product(K, Rd.columnsIterator());
//            U.chs();
//            uc = u + U.dot(delta);
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

    private void ordinarySmoothing(ISsf ssf, final int startpos, final int endpos) {
        OrdinarySmoother smoother = OrdinarySmoother
                .builder(ssf)
                .calcVariance(calcvar)
                .build();
        smoother.process(startpos, endpos, frslts, srslts);
        // updates R, N
        R.copy(smoother.getFinalR());
        Rc.copy(smoother.getFinalR());
        if (calcvar) {
            N.copy(smoother.getFinalN());
            Nc.copy(smoother.getFinalN());
        }
    }

    private void calcSmoothedDiffuseEffects(QAugmentation q) {
        // computes the smoothed diffuse effects and their covariance...
        // delta = S(s+B'*R), psi = S - S*B'*N*B*S 
        // delta = a'^-1*a^-1(-a*b' + B'*R)
        // delta = - (b * a^-1)' + a'^-1*a^-1*B'*r = a'^-1 * (a^-1*B'*r - b)
        // Psi = = a'^-1*(I - a^-1*B'*N*B*a'^-1)* a^-1
        FastMatrix B = q.B(); // B*a^-1'
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
    private void mcorrection(DataBlock x, FastMatrix B) {
        x.addProduct(B.rowsIterator(), delta);
    }

    /**
     * V+=B*psi*B'-B*S*C'-C*S*B') Attention ! B, C are modified on exit. Make a
     * copy if necessary
     *
     * @param V
     * @param B
     * @param C
     */
    private void vcorrection(FastMatrix V, FastMatrix B, FastMatrix C) {
        FastMatrix BPsiB = SymmetricMatrix.XSXt(Psi, B);
        V.add(BPsiB);
        LowerTriangularMatrix.solveXLt(S, B);
        LowerTriangularMatrix.solveXLt(S, C);
        // compute B*C'
        FastMatrix UV = GeneralMatrix.ABt(B, C);
        V.sub(UV);
        V.subTranspose(UV);
        SymmetricMatrix.reenforceSymmetry(V);
    }

    private double vcorrection(DataBlock B, DataBlock C) {
        double v = QuadraticForm.apply(Psi, B);
        LowerTriangularMatrix.solveLx(S, B);
        LowerTriangularMatrix.solveLx(S, C);
        // compute B*C'
        return v - 2 * B.dot(C);
    }

    private boolean processNoCollapsing(int endpos) {
        int t = endpos;
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
}
