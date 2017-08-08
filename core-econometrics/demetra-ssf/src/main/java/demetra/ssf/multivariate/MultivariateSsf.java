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
package demetra.ssf.multivariate;

import demetra.ssf.ISsfDynamics;
import demetra.ssf.ISsfInitialization;
import demetra.ssf.implementations.Measurement;
import demetra.ssf.implementations.Measurements;
import demetra.ssf.univariate.ISsf;

/**
 *
 * @author Jean Palate
 */
public class MultivariateSsf implements IMultivariateSsf {
    
    public static MultivariateSsf proxy(ISsf ssf){
        return new MultivariateSsf(ssf.getInitialization(), ssf.getDynamics(), Measurements.proxy(ssf.getMeasurement()));
    }

    protected final ISsfInitialization initialization;
    protected final ISsfDynamics dynamics;
    protected final ISsfMeasurements measurements;

    /**
     *
     * @param dynamics
     * @param measurement
     */
    protected MultivariateSsf(final ISsfInitialization initialization, final ISsfDynamics dynamics, ISsfMeasurements measurements) {
        this.initialization=initialization;
        this.dynamics=dynamics;
        this.measurements=measurements;
    }

     @Override
    public ISsfInitialization getInitialization() {
        return initialization;
    }

   @Override
    public ISsfMeasurements getMeasurements() {
        return measurements;
    }

    @Override
    public ISsfDynamics getDynamics() {
        return dynamics;
    }

    @Override
    public boolean isTimeInvariant() {
        return dynamics.isTimeInvariant() && measurements.isTimeInvariant();
    }
}
