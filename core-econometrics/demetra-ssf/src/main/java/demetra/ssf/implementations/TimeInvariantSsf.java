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

/**
 *
 * @author Jean Palate
 */
public class TimeInvariantSsf extends Ssf{
    public static ISsf of(ISsf ssf){
        TimeInvariantDynamics td=TimeInvariantDynamics.of(ssf.getStateDim(), ssf.getDynamics());
        if (td == null)
            return null;
        TimeInvariantMeasurement tm=TimeInvariantMeasurement.of(ssf.getStateDim(), ssf.getMeasurement());
        return new TimeInvariantSsf(ssf.getInitialization(), td, tm);
    }
    
    private TimeInvariantSsf(final ISsfInitialization initializer, final ISsfDynamics dynamics, ISsfMeasurement measurement) {
        super(initializer, dynamics, measurement);
    }
    
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append("Initialization").append(System.lineSeparator());
        builder.append(getInitialization());
        builder.append("Measurement").append(System.lineSeparator());
        builder.append(getMeasurement());
        builder.append("Dynamics").append(System.lineSeparator());
        builder.append(getDynamics());
        return builder.toString();
    }
}
