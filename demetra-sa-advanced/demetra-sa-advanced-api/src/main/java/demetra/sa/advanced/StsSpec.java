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

import demetra.processing.AlgorithmDescriptor;
import demetra.sa.SaSpecification;
import demetra.sa.benchmarking.SaBenchmarkingSpec;
import demetra.sts.BsmEstimationSpec;
import demetra.sts.BsmSpec;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.Builder(toBuilder = true,  builderClassName="Builder")
public class StsSpec implements SaSpecification{
    
    public static final String METHOD = "sts";
     public static final String VERSION = "3.0.0";
    
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, METHOD, VERSION);
    
    public static final StsSpec DEF=builder().build();

    public static Builder builder() {
        return new Builder()
                .preprocessing(PreprocessingSpec.TRAMO)
                .bsm(BsmSpec.DEFAULT)
                .bsmEstimation(BsmEstimationSpec.DEFAULT)
                .benchmarking(SaBenchmarkingSpec.DEFAULT_DISABLED);
    }
    
   @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return DESCRIPTOR;
    }
    
    @lombok.NonNull
    private PreprocessingSpec preprocessing;
    @lombok.NonNull
    private BsmSpec bsm;
    @lombok.NonNull
    private BsmEstimationSpec bsmEstimation;
    @lombok.NonNull
    private SaBenchmarkingSpec benchmarking;

}
