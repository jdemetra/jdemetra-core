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
package jdplus.ssf.univariate;

import demetra.math.Constants;
import jdplus.data.DataBlock;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.SsfException;
import jdplus.ssf.State;
import jdplus.ssf.StateInfo;
import jdplus.ssf.UpdateInformation;

/**
 * Ordinary Kalman filter for univariate time series
 *
 * @author Jean Palate
 */
public class OrdinaryFilter {

    public static interface Initializer {

        int initializeFilter(State state, ISsf ssf, ISsfData data);
    }

    private final Initializer initializer;
    private State state;
    private UpdateInformation updinfo;
    private ISsfLoading loading;
    private ISsfError error;
    private ISsfDynamics dynamics;
    private boolean missing;

    /**
     *
     * @param initializer
     */
    public OrdinaryFilter(Initializer initializer) {
        this.initializer = initializer;
    }

    public OrdinaryFilter() {
        this.initializer = null;
    }

    /**
     * Computes the update information:
     *
     * @param t
     * @param data
     * @return
     */
    protected boolean error(int t, ISsfData data) {
        missing = data.isMissing(t);
        if (missing) {
            // pe_ = null;
            updinfo.setMissing();
            return false;
        } else {
            // K = PZ'/f
            // computes PZ' (=ZP)'  in M.  
            DataBlock M = updinfo.M();
            loading.ZM(t, state.P(), M);
            // compute ZPZ'
            double v = loading.ZX(t, M);
            // check that ZPZ' is non negative (P semi-definite positive)
            if (v < -Constants.getEpsilon()) {
                throw new SsfException();
            }
            if (v < Constants.getEpsilon()) {
                v = 0;
            }
            if (error != null) {
                v += error.at(t);
            }
            updinfo.setVariance(v);
            double y = data.get(t);
            double e = y - loading.ZX(t, state.a());
            if (v == 0) {
                if (Math.abs(e) < State.ZERO) {
                    e = 0;
                } else {
                    throw new SsfException(SsfException.INCONSISTENT);
                }
            }
            updinfo.set(e);
            return true;
        }
    }

    /**
     * Retrieves the final state (which is a(N|N-1))
     *
     * @return
     */
    public State getFinalState() {
        return state;
    }

    private int initialize(ISsf ssf, ISsfData data) {
        loading = ssf.loading();
        error = ssf.measurementError();
        dynamics = ssf.dynamics();
        updinfo = new UpdateInformation(ssf.getStateDim());
        if (initializer == null) {
            state = State.of(ssf);
            return state == null ? -1 : 0;
        } else {
            state = new State(ssf.getStateDim());
            return initializer.initializeFilter(state, ssf, data);
        }
    }

    /**
     *
     * @param ssf
     * @param data
     * @param rslts
     * @return
     */
    public boolean process(final ISsf ssf, final ISsfData data, final IFilteringResults rslts) {
        // intialize the state with a(0|-1)
        int t = initialize(ssf, data);
        if (t < 0) {
            return false;
        }
        int end = data.length();
        while (t < end) {
            if (rslts != null) {
                rslts.save(t, state, StateInfo.Forecast);
            }
            if (error(t, data)) {
                if (rslts != null) {
                    rslts.save(t, updinfo);
                }
                state.update(updinfo);
            } else if (rslts != null) {
                rslts.save(t, updinfo);
            }
            if (rslts != null) {
                rslts.save(t, state, StateInfo.Concurrent);
            }
            state.next(t++, dynamics);
        }
        return true;
    }

}
