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
package demetra.highfreq;

import demetra.processing.AlgorithmDescriptor;
import demetra.processing.ProcSpecification;
import lombok.NonNull;
import nbbrd.design.Development;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class ExtendedAirlineDecompositionSpec implements ProcSpecification {
    @NonNull
    private ExtendedAirlineModellingSpec preprocessing;
    @NonNull
    private DecompositionSpec decomposition;
    
    
    public static final ExtendedAirlineDecompositionSpec DEFAULT=builder()
            .preprocessing(ExtendedAirlineModellingSpec.DEFAULT)
            .decomposition(DecompositionSpec.DEFAULT)
            .build();
    
    public static final String METHOD = "extendedairline";
    public static final String FAMILY = "Decomposition";
    public static final String VERSION = "0.1.0.0";


    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return new AlgorithmDescriptor(FAMILY, METHOD, VERSION);
    }
    
    @Override
    public String display(){
        return "Extended airline";
    }
    
}
