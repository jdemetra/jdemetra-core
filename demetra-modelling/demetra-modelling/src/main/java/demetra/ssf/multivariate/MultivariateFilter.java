/*
 * Copyright 2016-2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
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

import demetra.ssf.State;
import demetra.ssf.StateInfo;

/**
 *
 * @author Jean Palate
 */
public class MultivariateFilter {
    public static interface Initializer {

        int initialize(State state, IMultivariateSsf ssf, IMultivariateSsfData data);
    }

    private final Initializer initializer;
    private State state;
    private MultivariateUpdateInformation updinfo;
    private IMultivariateSsf ssf;
    private IMultivariateSsfData data;

    /**
     *
     */
    public MultivariateFilter() {
        initializer = null;
    }

    /**
     *
     * @param initializer
     */
    public MultivariateFilter(final Initializer initializer) {
        this.initializer = initializer;
    }


    /**
     * Computes a(t+1|t), P(t+1|t) from a(t|t), P(t|t) a(t+1|t) = T(t)a(t|t)
     * P(t+1|t) = T(t)P(t|t)T'(t)
     * @param pos
     */
     protected MultivariateUpdateInformation next(int t) {
         return ssf.next(t, state, data.get(t));
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
        int end = data.getCount();
        while (t < end) {
            if (rslts != null) {
                rslts.save(t, state, StateInfo.Forecast);
            }
            this.updinfo = next(t);
            if (rslts != null) {
                rslts.save(t, updinfo);
            }
            ++t;
        }
        if (rslts != null) {
            rslts.close();
        }
        return true;
    }

     
}
