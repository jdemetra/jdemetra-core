/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package demetra.timeseries;

import internal.Tripwire;

/**
 *
 * @author Philippe Charles
 * @param <P> period type
 * @param <V> value type
 */
public interface TimeObservation<P extends ITimePeriod, V> {

    P getPeriod();

    V getValue();

    interface OfDouble<P extends ITimePeriod> extends TimeObservation<P, Double> {

        double getDoubleValue();

        @Override
        public default Double getValue() {
            if (Tripwire.ENABLED) {
                Tripwire.trip(getClass(), "{0} calling TimeObservation.OfDouble.getValue()");
            }
            return getDoubleValue();
        }
    }
}
