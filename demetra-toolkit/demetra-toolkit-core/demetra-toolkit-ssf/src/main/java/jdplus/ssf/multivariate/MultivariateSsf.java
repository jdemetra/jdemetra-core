/*
 * Copyright 2013-2014 National Bank of Belgium
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
package jdplus.ssf.multivariate;

import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.ISsfInitialization;
import jdplus.ssf.StateComponent;

/**
 *
 * @author Jean Palate
 */
public class MultivariateSsf implements IMultivariateSsf {

    private final ISsfInitialization initializer;
    private final ISsfDynamics dynamics;
    private final ISsfMeasurements measurements;
    
    /**
     *
     * @param initializer
     * @param dynamics
     * @param measurements
     */
    public MultivariateSsf(final ISsfInitialization initializer, final ISsfDynamics dynamics, final ISsfMeasurements measurements) {
        this.initializer = initializer;
        this.dynamics = dynamics;
        this.measurements = measurements;
    }

   public MultivariateSsf(final StateComponent state, final ISsfMeasurements measurements) {
        this.initializer = state.initialization();
        this.dynamics = state.dynamics();
        this.measurements = measurements;
    }

   @Override
    public ISsfInitialization initialization() {
        return initializer;
    }

    @Override
    public ISsfMeasurements measurements() {
        return measurements;
    }

    @Override
    public ISsfDynamics dynamics() {
        return dynamics;
    }

    @Override
    public boolean isTimeInvariant() {
        return dynamics.isTimeInvariant() && measurements.isTimeInvariant();
    }

}
