/*
 * Copyright 2013-2014 National Bank copyOf Belgium
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
package demetra.ssf.multivariate;

import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.maths.matrices.LowerTriangularMatrix;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.State;
import demetra.ssf.StateInfo;

/**
 *
 * @author Jean Palate
 */
public class MultivariateOrdinaryFilter {

    public static interface Initializer {

        int initialize(State state, IMultivariateSsf ssf, IMultivariateSsfData data);
    }

    private final Initializer initializer;
    private State state;
    private MultivariateUpdateInformation updinfo;
    private ISsfMeasurements measurements;
    private ISsfDynamics dynamics;
    private IMultivariateSsfData data;

    /**
     *
     */
    public MultivariateOrdinaryFilter() {
        initializer = null;
    }

    /**
     *
     * @param initializer
     */
    public MultivariateOrdinaryFilter(final Initializer initializer) {
        this.initializer = initializer;
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
     * P(t+1|t) = T(t)P(t|t)T'(t)
     * @param pos
     */
     protected void pred(int pos) {
        dynamics.TX(pos, state.a());
        dynamics.TVT(pos, state.P());
        dynamics.addV(pos, state.P());
    }

    /**
     * Computes: e(t)=y(t) - Z(t)a(t|t-1)) F(t)=Z(t)P(t|t-1)Z'(t)+H(t) F(t) =
     * L(t)L'(t) E(t) = e(t)L'(t)^-1 K(t)= P(t|t-1)Z'(t)L'(t)^-1
     *
     * Not computed for missing values
     * @param pos
     */
    protected void error(int pos) {
        int nobs = countMeasurements(pos);
        if (nobs == 0) {
            updinfo = null;
        } else {
            updinfo = new MultivariateUpdateInformation(dynamics.getStateDim(), nobs);
            Matrix L = updinfo.getCholeskyFactor();
            // K = PZ'(ZPZ'+H)^-1/2
            // computes (ZP)' in K'. Missing values are set to 0 
            // Z~v x r, P~r x r, K~r x v
            Matrix K = updinfo.getK();
            ZM(pos, state.P(), K.transpose());
            // computes ZPZ'; results in pe_.L
            ZM(pos, K, L);
            addH(pos, L);
            SymmetricMatrix.reenforceSymmetry(L);

            // pe_L contains the Cholesky factor !!!
            SymmetricMatrix.lcholesky(L, State.ZERO);

            // We put in K  PZ'*(ZPZ'+H)^-1/2 = PZ'* F^-1 = PZ'*(LL')^-1/2 = PZ'(L')^-1
            // K L' = PZ' or L K' = ZP
            LowerTriangularMatrix.rsolve(L, K.transpose(), State.ZERO);
            DataBlock U = updinfo.getTransformedPredictionErrors();
            for (int i = 0, j = 0; i < measurements.getCount(pos); ++i) {
                if (!data.isMissing(pos, i)) {
                    double y = data.get(pos, i);
                    U.set(j, y - measurements.ZX(pos, i, state.a()));
                    ++j;
                }
            }
            // E = e*L'^-1 or E L' = e or L*E' = e'
            LowerTriangularMatrix.rsolve(L, U, State.ZERO);
        }
    }

    /**
     * Updates the state vector and its covariance a(t|t) = a(t|t-1) + e(t)
     */
    protected void update() {
        if (updinfo == null) {
            return;
        }
        int n = updinfo.getK().getColumnsCount();
        // P = P - (M)* F^-1 *(M)' --> Symmetric
        // PZ'(LL')^-1 ZP' =PZ'L'^-1*L^-1*ZP'
        // A = a + (M)* F^-1 * v
//        for (int i = 0; i < n; ++i) {
//            state.P().addXaXt(-1, updinfo.getK().column(i));//, state_.K.column(i));
//            state.a().addAY(updinfo.getTransformedPredictionErrors().get(i), updinfo.getK().column(i));
//        }
        Matrix P = state.P();
        Matrix K = updinfo.getK();
        DataBlock U = updinfo.getTransformedPredictionErrors();
        for (int i = 0; i < n; ++i) {
            P.addXaXt(-1, K.column(i));//, state_.K.column(i));
            state.a().addAY(U.get(i), K.column(i));
        }
    }

    /**
     *
     * @return
     */
    public State getState() {
        return state;
    }

    private int initialize(IMultivariateSsf ssf, IMultivariateSsfData data) {
        this.data = data;
        measurements = ssf.getMeasurements();
        dynamics = ssf.getDynamics();
        if (initializer == null) {
            state = State.of(dynamics);
            return state == null ? -1 : 0;
        } else {
            state = new State(dynamics.getStateDim());
            return initializer.initialize(state, ssf, data);
        }
    }

    /**
     *
     * @param ssf
     * @param data
     * @param rslts
     * @return
     */
    public boolean process(final IMultivariateSsf ssf, final IMultivariateSsfData data, final IMultivariateFilteringResults rslts) {
        int t=initialize(ssf, data);
        if (t < 0){
            return false;
        }
        if (rslts != null) {
            rslts.open(ssf, this.data);
        }
        int end = data.getCount();
        while (t < end) {
            if (rslts != null) {
                rslts.save(t, state, StateInfo.Forecast);
            }
            error(t);
            if (rslts != null) {
                rslts.save(t, updinfo);
            }
            update();
            if (rslts != null) {
                rslts.save(t, state, StateInfo.Concurrent);
            }
            pred(t++);
        }
        if (rslts != null) {
            rslts.close();
        }
        return true;
    }

}
