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
package demetra.ssf.univariate;

import demetra.ssf.ISsfDynamics;
import demetra.ssf.implementations.TimeInvariantSsf;

/**
 *
 * @author Jean Palate
 */
public class Ssf implements ISsf {

    protected final ISsfMeasurement measurement;
    protected final ISsfDynamics dynamics;

    /**
     *
     * @param dynamics
     * @param measurement
     */
    public Ssf(final ISsfDynamics dynamics, ISsfMeasurement measurement) {
        this.dynamics = dynamics;
        this.measurement = measurement;
    }

    /**
     *
     * @return
     */
    @Override
    public int getStateDim() {
        return dynamics.getStateDim();
    }

    @Override
    public ISsfMeasurement getMeasurement() {
        return measurement;
    }

    @Override
    public ISsfDynamics getDynamics() {
        return dynamics;
    }

    @Override
    public boolean isTimeInvariant() {
        return dynamics.isTimeInvariant() && measurement.isTimeInvariant();
    }

    @Override
    public String toString() {
        if (isTimeInvariant()) {
            return TimeInvariantSsf.of(this).toString();
        } else {
            return super.toString();
        }
    }

}
