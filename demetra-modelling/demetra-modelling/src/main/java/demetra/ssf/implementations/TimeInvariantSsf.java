/*
 * Copyright 2016 National Bank of Belgium
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
package demetra.ssf.implementations;

import demetra.ssf.univariate.ISsf;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.univariate.ISsfMeasurement;
import demetra.ssf.univariate.Ssf;
import demetra.ssf.ISsfInitialization;
import demetra.ssf.univariate.ISsfError;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class TimeInvariantSsf {

    public static Ssf of(ISsf ssf) {
        if (!ssf.isTimeInvariant()) {
            return null;
        }
        ISsfError e = ssf.measurementError();
        if (e != null) {
            e = MeasurementError.of(e.at(0));
        }
        return Ssf.builder()
                .initialization(ssf.initialization())
                .dynamics(TimeInvariantDynamics.of(ssf.getStateDim(), ssf.dynamics()))
                .loading(TimeInvariantLoading.of(ssf.getStateDim(), ssf.loading()))
                .measurementError(e)
                .build();
    }

    public String toString(ISsf ssf) {
        StringBuilder builder = new StringBuilder();
        builder.append("Initialization").append(System.lineSeparator());
        builder.append(ssf.initialization());
        builder.append("Measurement").append(System.lineSeparator());
        builder.append(ssf.measurement());
        builder.append("Dynamics").append(System.lineSeparator());
        builder.append(ssf.dynamics());
        return builder.toString();
    }

}
