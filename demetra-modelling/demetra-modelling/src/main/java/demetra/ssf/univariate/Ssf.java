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

import demetra.ssf.ISsfLoading;
import demetra.design.BuilderPattern;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.implementations.TimeInvariantSsf;
import demetra.ssf.ISsfInitialization;
import demetra.ssf.SsfComponent;
import demetra.ssf.SsfException;
import demetra.ssf.implementations.MeasurementError;

/**
 *
 * @author Jean Palate
 */
public class Ssf implements ISsf {
    
    public static Builder builder(){
        return new Builder();
    }

    @BuilderPattern(Ssf.class)
    public static class Builder {

        private ISsfInitialization initializer;
        private ISsfLoading loading;
        private ISsfError error;
        private ISsfDynamics dynamics;
        
        public Builder initialization(ISsfInitialization initializer){
            this.initializer=initializer;
            return this;
        }
        
        public Builder dynamics(ISsfDynamics dynamics){
            this.dynamics=dynamics;
            return this;
        }

        public Builder loading(ISsfLoading loading){
            this.loading=loading;
            return this;
        }

        public Builder measurementError(ISsfError error){
            this.error=error;
            return this;
        }

        public Builder measurementError(double evar){
            this.error=MeasurementError.of(evar);
            return this;
        }

        public Ssf build(){
            if (dynamics == null || loading == null)
                throw new SsfException(SsfException.MODEL);
            return new Ssf(initializer, dynamics, new Measurement(loading, error));
        }
    }

    private final ISsfInitialization initializer;
    private final ISsfMeasurement measurement;
    private final ISsfDynamics dynamics;
    
    public static Ssf of(SsfComponent cmp, double measurementError){
        return new Ssf(cmp.initialization(), cmp.dynamics(), 
                new Measurement(cmp.loading(), MeasurementError.of(measurementError)));
    }

    /**
     *
     * @param initializer
     * @param dynamics
     * @param measurement
     */
    public Ssf(final ISsfInitialization initializer, final ISsfDynamics dynamics, ISsfMeasurement measurement) {
        this.initializer = initializer;
        this.dynamics = dynamics;
        this.measurement = measurement;
    }

    @Override
    public ISsfInitialization initialization() {
        return initializer;
    }

    @Override
    public ISsfMeasurement measurement() {
        return measurement;
    }

    @Override
    public ISsfDynamics dynamics() {
        return dynamics;
    }

    @Override
    public boolean isTimeInvariant() {
        return dynamics.isTimeInvariant() && measurement.isTimeInvariant();
    }

    @Override
    public String toString() {
        if (isTimeInvariant()) {
            return TimeInvariantSsf.toString(this);
        } else {
            return super.toString();
        }
    }

}
