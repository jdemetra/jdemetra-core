/*
 * Copyright 2022 National Bank of Belgium
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
package demetra.sa.advanced;

import demetra.sts.Component;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.Builder(toBuilder = true,  builderClassName="Builder")
public class SeasonalSpecificSpec {
    public static enum EstimationMethod{
        Iterative,
        ErrorVariance,
        LikelihoodGradient
    }
    
    public Builder builder(int[] noisy){
        return new Builder()
                .noisyPeriods(noisyPeriods)
                .noisyComponent(Component.Noise)
                .method(EstimationMethod.LikelihoodGradient)
                .step(0.5);
    }
    
    @lombok.NonNull
    private int[] noisyPeriods; // pre-specified periods
    private Component noisyComponent;
    // search method
    private double step;
    private EstimationMethod method;
    
}
