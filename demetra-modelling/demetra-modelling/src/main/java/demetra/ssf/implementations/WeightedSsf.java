/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.ssf.implementations;

import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.Ssf;
import java.util.function.IntToDoubleFunction;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class WeightedSsf{

    public ISsf of(ISsf original, IntToDoubleFunction weights){
        return new Ssf(original.getInitialization(), original.getDynamics(), WeightedMeasurement.of(original.getMeasurement(), weights));
    }

    public ISsf of(ISsf original, double weight){
        return new Ssf(original.getInitialization(), original.getDynamics(), WeightedMeasurement.of(original.getMeasurement(), weight));
    }
}
