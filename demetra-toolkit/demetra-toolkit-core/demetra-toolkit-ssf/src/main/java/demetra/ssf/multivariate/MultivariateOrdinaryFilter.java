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
import demetra.data.DeprecatedDoubles;
import demetra.maths.matrices.Matrix;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.State;
import demetra.ssf.StateInfo;
import demetra.data.DoubleSeq;

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
    private IMultivariateSsf ssf;
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
        int dim = ssf.getStateDim();
        DoubleSeq x=data.get(pos);
        int nmissing = x.count(y -> Double.isInfinite(y));
        int nobs = x.length() - nmissing;
        if (nobs == 0)
            updinfo=null;
        int[] obs;
        if (nmissing != 0) {
            obs = new int[nobs];
            DeprecatedDoubles.search(x, y -> Double.isFinite(y), obs);
        } else {
            obs = null;
        }
        updinfo = new MultivariateUpdateInformation(dim, nobs);
        updinfo.compute(ssf, pos, state, x, obs);
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
        this.ssf=ssf;
        measurements = ssf.measurements();
        dynamics = ssf.dynamics();
        if (initializer == null) {
            state = State.of(ssf);
            return state == null ? -1 : 0;
        } else {
            state = new State(ssf.getStateDim());
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
        int end = data.getObsCount();
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
