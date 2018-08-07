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
package demetra.ssf.akf;

import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.maths.matrices.LowerTriangularMatrix;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.State;
import demetra.ssf.StateInfo;
import demetra.ssf.multivariate.IMultivariateSsf;
import demetra.ssf.multivariate.IMultivariateSsfData;
import demetra.ssf.multivariate.ISsfMeasurements;
import demetra.ssf.ISsfInitialization;

/**
 *
 * @author Jean Palate
 */
public class MultivariateAugmentedFilter {

    private AugmentedState state;
    private AugmentedPredictionErrors perrors;
    private IMultivariateSsf ssf;
    private ISsfMeasurements measurements;
    private ISsfDynamics dynamics;
    private IMultivariateSsfData data;
    private final boolean collapsing;
    private int collapsingPos = -1;

    /**
     *
     */
    public MultivariateAugmentedFilter() {
        collapsing = false;
    }

    public MultivariateAugmentedFilter(final boolean collapsing) {
        this.collapsing = collapsing;
    }

    private int countMeasurements(int pos) {
        int n = 0;
        for (int i = 0; i < measurements.getCount(pos); ++i) {
            if (!data.isMissing(pos, i)) {
                ++n;
            }
        }
        return n;
    }

    /**
     * Computes zm = Z * M
     *
     * @param M
     * @param zm
     */
    private void ZM(int pos, Matrix M, Matrix zm) {
        DataBlockIterator zrows = zm.rowsIterator();
        int imax = measurements.getCount(pos);
        for (int i = 0; i < imax; ++i) {
            if (!data.isMissing(pos, i)) {
                measurements.ZM(pos, i, M, zrows.next());
                if (!zrows.hasNext()) {
                    return;
                }
            }
        }
    }

    private void addH(int pos, Matrix P) {
        int nm = measurements.getCount(pos);
        Matrix H = Matrix.square(nm);
        measurements.H(pos, H);
        for (int i = 0, r = 0; i < nm; ++i) {
            if (!data.isMissing(pos, i)) {
                for (int j = 0, c = 0; j < i; ++j) {
                    if (!data.isMissing(pos, j)) {
                        double h = H.get(i, j);
                        P.add(r, c, h);
                        P.add(c, r, h);
                        ++c;
                    }
                }
                P.add(r, r, H.get(i, i));
                ++r;
            }
        }
    }

    /**
     * Computes a(t+1|t), P(t+1|t) from a(t|t), P(t|t) a(t+1|t) = T(t)a(t|t)
     * P(t+1|t) = T(t)P(t|t)T'(t) The same transformation is applied on state.B
     * (diffuse constraints)
     *
     * @param pos
     */
    protected void pred(int pos) {
        DataBlock a = state.a();
        dynamics.TX(pos, a);
        dynamics.TM(pos, state.B());
        dynamics.TVT(pos, state.P());
        dynamics.addV(pos, state.P());
    }

    /**
     * Computes: e(t)=y(t) - Z(t)a(t|t-1)) F(t)=Z(t)P(t|t-1)Z'(t)+H(t) F(t) =
     * L(t)L'(t) E(t) = e(t)L'(t)^-1 K(t)= P(t|t-1)Z'(t)L'(t)^-1
     *
     * Not computed for missing values
     */
    private boolean error(int pos) {
        int nobs = countMeasurements(pos);
        if (nobs == 0) {
            perrors = null;
            return false;
        } else {
        ISsfInitialization initialization = ssf.initialization();
            perrors = new AugmentedPredictionErrors(initialization.getStateDim(), nobs, initialization.getDiffuseDim());
            Matrix L = perrors.getCholeskyFactor();
            // K = PZ'(ZPZ'+H)^-1/2
            // computes (ZP)' in K'. 
            // Z~v x r, P~r x r, K~r x v
            // F = ZPZ'+H ~ v x v
            // L = F^-1/2 ~ v x v
            Matrix F = L, K = perrors.getK();
            // K' = ZP or K = PZ'
            ZM(pos, state.P(), K.transpose());
            // computes ZPZ'; results in L
            ZM(pos, K, F);
            addH(pos, L);
            // to avoid numerical problems
            SymmetricMatrix.reenforceSymmetry(L);

            // L contains now the Cholesky factor !!!
            SymmetricMatrix.lcholesky(L, State.ZERO);

            // We put in K  PZ'*(ZPZ'+H)^-1/2 = PZ'* L'^-1
            // K L' = PZ' or L K' = ZP
            LowerTriangularMatrix.rsolve(L, K.transpose(), State.ZERO);
            DataBlock U = perrors.getTransformedPredictionErrors();
            for (int i = 0, j = 0; i < measurements.getCount(pos); ++i) {
                if (!data.isMissing(pos, i)) {
                    double y = data.get(pos, i);
                    U.set(j, y - measurements.ZX(pos, i, state.a()));
                    ++j;
                }
            }
            // U = e*L'^-1 or U L' = e or L*U' = e'
            LowerTriangularMatrix.rsolve(L, U, State.ZERO);
            Matrix E = perrors.E();
            // E is ndiffuse x nobs. Each column contains the diffuse effects
            // on the corresponding variable
            ZM(pos, state.B(), E.transpose());
            E.chs();
            DataBlockIterator erows = E.rowsIterator();
            while (erows.hasNext()) {
                LowerTriangularMatrix.rsolve(L, erows.next(), State.ZERO);
            };
            return true;
        }
    }

    /**
     * Updates the state vector and its covariance a(t|t) = a(t|t-1) + e(t)
     */
    protected void update() {
        if (perrors == null) {
            return;
        }
        int n = perrors.getK().getColumnsCount();
        // P = P - (M)* F^-1 *(M)' --> Symmetric
        // PZ'(LL')^-1 ZP' =PZ'L'^-1*L^-1*ZP'
        // A = a + (M)* F^-1 * v
        Matrix P = state.P();
        DataBlock U = perrors.getTransformedPredictionErrors();
        Matrix K = perrors.getK();
        for (int i = 0; i < n; ++i) {
//            for (int j = 0; j < n; ++j) {
//                P.addXaYt(-1, K.column(i), K.column(j));//, state_.K.column(i));
//            }
            state.a().addAY(U.get(i), K.column(i));
            P.addXaXt(-1, K.column(i));//, state_.K.column(i));
        }
        DataBlockIterator acols = state.B().columnsIterator();
        int apos=0;
        while (acols.hasNext()) {
            DataBlock row = perrors.E().row(apos++);
            for (int i = 0; i < n; ++i) {
                acols.next().addAY(row.get(i), K.column(i));
            }
        }
    }

    /**
     *
     * @return
     */
    public AugmentedState getState() {
        return state;
    }

    public int getCollapsingPosition() {
        return collapsingPos;
    }

    private boolean initState() {
        state = AugmentedState.of(ssf);
        return (state != null);
    }

    /**
     *
     * @param ssf
     * @param data
     * @param rslts
     * @return
     */
    public boolean process(final IMultivariateSsf ssf, final IMultivariateSsfData data, final IMultivariateAugmentedFilteringResults rslts) {
        measurements = ssf.measurements();
        dynamics = ssf.dynamics();
        this.data = data;
        if (!initState()) {
            return false;
        }
        rslts.open(ssf, data);
        int t = 0, end = data.getCount();

        while (t < end) {
            if (collapse(rslts)) {
                collapsingPos = t;
                break;
            }
            rslts.save(t, state, StateInfo.Forecast);
            if (error(t)) {
                rslts.save(t, perrors);
                update();
            }
            rslts.save(t, state, StateInfo.Concurrent);
            pred(t++);
        }
        rslts.close();
        return true;
    }

    protected boolean collapse(IMultivariateAugmentedFilteringResults decomp) {
        if (!collapsing) {
            return false;
        }
        // update the state vector
        if (!decomp.collapse(state)) {
            return false;
        }
        return true;
    }

}
