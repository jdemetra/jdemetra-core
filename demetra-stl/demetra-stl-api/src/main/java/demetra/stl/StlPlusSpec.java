/*
 * Copyright 2023 National Bank of Belgium
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
package demetra.stl;

import demetra.modelling.regular.ModellingSpec;
import demetra.processing.AlgorithmDescriptor;
import demetra.sa.SaSpecification;
import static demetra.sa.SaSpecification.FAMILY;
import demetra.sa.benchmarking.SaBenchmarkingSpec;
import nbbrd.design.LombokWorkaround;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class StlPlusSpec implements SaSpecification{
    public static final String METHOD = "stlplus";
    public static final String VERSION_V3 = "3.0.0";
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, METHOD, VERSION_V3);

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return DESCRIPTOR;
    }

    @lombok.NonNull
    private ModellingSpec preprocessing;
    
    private StlSpec stl;
    
    @lombok.NonNull
    private SaBenchmarkingSpec benchmarking;

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .preprocessing(ModellingSpec.FULL)
                .stl(null)
                .benchmarking(SaBenchmarkingSpec.DEFAULT_DISABLED);
    }


    @Override
    public String display(){
        return SMETHOD;
    }
    
    private static final String SMETHOD = "STL+";
    
    public static final StlPlusSpec FULL=StlPlusSpec.builder()
            .preprocessing(ModellingSpec.FULL)
            .stl(null)
            .benchmarking(SaBenchmarkingSpec.DEFAULT_DISABLED)
            .build();
    
    public static final StlPlusSpec DEFAULT=StlPlusSpec.builder()
            .preprocessing(ModellingSpec.DEFAULT)
            .stl(null)
            .benchmarking(SaBenchmarkingSpec.DEFAULT_DISABLED)
            .build();
    
}
