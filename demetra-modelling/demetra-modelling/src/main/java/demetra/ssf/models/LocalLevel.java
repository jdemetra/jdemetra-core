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
package demetra.ssf.models;

import demetra.ssf.implementations.Measurement;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.Ssf;

/**
 * Usual local linear trend 
 * y(t)=l(t)+n(t)
 * l(t+1)=l(t)+u(t) 
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class LocalLevel{
    
    public ISsf of(double lvar) {
        return new Ssf(new RandomWalk.Initialization(lvar), new RandomWalk.Dynamics(lvar), Measurement.create(0));
    }


}
