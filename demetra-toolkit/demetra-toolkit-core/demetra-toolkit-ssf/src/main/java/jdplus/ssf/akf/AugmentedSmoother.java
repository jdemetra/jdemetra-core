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
import jdplus.maths.matrices.LowerTriangularMatrix;
import jdplus.maths.matrices.SymmetricMatrix;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.StateInfo;
import jdplus.ssf.univariate.ISmoothingResults;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.univariate.ISsfData;
import jdplus.ssf.univariate.OrdinarySmoother;
import demetra.data.DoubleSeqCursor;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.ssf.ISsfInitialization;
import jdplus.ssf.ISsfLoading;
import jdplus.maths.matrices.FastMatrix;

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

    private double e, f;
    private DataBlock C, E, R;
    private CanonicalMatrix N, Rd, U, V, RNA, S;
    private CanonicalMatrix Psi;
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
        C = DataBlock.make(dim);
        E = DataBlock.make(nd);
        Rd = CanonicalMatrix.make(dim, nd);
        U = CanonicalMatrix.make(dim, nd);

        if (calcvar) {
            N = CanonicalMatrix.square(dim);
            V = CanonicalMatrix.make(dim, nd);
            RNA = CanonicalMatrix.make(dim, nd);
        }
    }

    private void loadInfo(int pos) {
        e = frslts.error(pos);
        f = frslts.errorVariance(pos);
        E.copy(frslts.E(pos));
        C.copy(frslts.M(pos));
        missing = !Double.isFinite(e);
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
        iterateR(pos);
        calcU(pos);
        updateA(pos);
        if (calcvar) {
            // P = P-PNP
            iterateN(pos);
            calcV();
            updateP();
        }
    }

    // 
    private void calcU(int pos) {
        // U = A + PR
        DataBlockIterator ucolumns = U.columnsIterator();
        DataBlockIterator rcolumns = Rd.columnsIterator();
        DataBlockIterator acolumns = calcvar ? state.B().columnsIterator() : frslts.B(pos).columnsIterator();
        DataBlockIterator prows = calcvar ? state.P().rowsIterator() : frslts.P(pos).rowsIterator();
        while (ucolumns.hasNext() && rcolumns.hasNext() && acolumns.hasNext()) {
            prows.reset();
            DataBlock uc = ucolumns.next();
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
        a.addProduct(U.rowsIterator(), delta);
    }

    private void updateP() {
        CanonicalMatrix P = state.P();
        // normal iteration
        CanonicalMatrix PNP = SymmetricMatrix.XtSX(N, P);
        P.sub(PNP);
        // diffuse correction
        CanonicalMatrix UPsiU = SymmetricMatrix.XSXt(Psi, U);
        P.add(UPsiU);
        LowerTriangularMatrix.rsolve(S, U.transpose());
        LowerTriangularMatrix.rsolve(S, V.transpose());
        // compute U*V'
        CanonicalMatrix UV = CanonicalMatrix.square(U.getRowsCount());
        UV.product(U, V.transpose());
        P.sub(UV);
        P.sub(UV.transpose());
        SymmetricMatrix.reenforceSymmetry(P);
    }

    private void xL(int pos, DataBlock x) {
        // xL = x(T-KZ) = x(T-Tc/f*Z) = xT - ((xT)*c)/f * Z
        // compute xT
        dynamics.XT(pos, x);
        // compute q=xT*c
        double q = x.dot(C);
        // remove q/f*Z
        loading.XpZd(pos, x, -q / f);
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
        if (!missing && f != 0) {
            // N(t-1) = Z'(t)*Z(t)/f(t) + L'(t)*N(t)*L(t)
            XL(pos, N.rowsIterator());
            XL(pos, N.columnsIterator());
            loading.VpZdZ(pos, N, 1 / f);
        } else {
            dynamics.MT(pos, N);
            dynamics.MT(pos, N.transpose());
        }
        SymmetricMatrix.reenforceSymmetry(N);
    }

    /**
     *
     */
    private void iterateR(int pos) {
        // R(t-1)=v(t)/f(t)Z(t)+R(t)L(t)
        //   = v/f*Z + R*(T-TC/f*Z)
        //  = (v - RT*C)/f*Z + RT
        dynamics.XT(pos, R);
        dynamics.MT(pos, Rd.transpose());
        if (!missing && f != 0) {
            // RT
            double c = (e - R.dot(C)) / f;
            loading.XpZd(pos, R, c);
            // apply the same to the colums copyOf Rd
            DataBlockIterator rcols = Rd.columnsIterator();
            DoubleSeqCursor cell = E.cursor();
            while (rcols.hasNext()) {
                DataBlock rcol = rcols.next();
                c = (cell.getAndNext() - rcol.dot(C)) / f;
                loading.XpZd(pos, rcol, c);
            }
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
        if (calcvar) {
            N.copy(smoother.getFinalN());
        }
    }

    private void calcSmoothedDiffuseEffects() {
        // computes the smoothed diffuse effects and their covariance...

        QAugmentation q = frslts.getAugmentation();
        // delta = S(s+B'*R), psi = Psi= S - S*B'*N*B*S 
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
        LowerTriangularMatrix.lsolve(S, delta);
        // B'NB 
        if (N != null) {
            // we have to make a copy copyOf B
//            Matrix A = B.clone();
//            // a^-1*B' =C <-> B'=aC
//            LowerTriangularMatrix.rsolve(S, A.all().transpose());
//            Psi = SymmetricMatrix.quadraticForm(N, A);
            Psi = SymmetricMatrix.XtSX(N, B);
            Psi.chs();
            Psi.diagonal().add(1);
            // B*a^-1* =C <->B =Ca
            LowerTriangularMatrix.lsolve(S, Psi);
            // a'^-1*B = C <-> B' = C'a
            LowerTriangularMatrix.lsolve(S, Psi.transpose());
        }
    }

    public IAugmentedFilteringResults getFilteringResults() {
        return frslts;
    }
}
