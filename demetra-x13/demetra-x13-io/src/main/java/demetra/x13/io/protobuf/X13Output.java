/*
 * Copyright 2020 National Bank of Belgium
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
package demetra.x13.io.protobuf;

import demetra.processing.ProcessingLog;
import demetra.x13.X13Spec;
import java.util.Map;
import jdplus.x13.X13Results;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder
public class X13Output {
    X13Results result;

    @lombok.NonNull
    X13Spec estimationSpec;
    
    X13Spec resultSpec;

    @lombok.Singular
    Map<String, Object> details;
    
    ProcessingLog logs;
    
    public X13Protos.X13Output convert(){
        X13Protos.X13Output.Builder builder = 
                X13Protos.X13Output.newBuilder()
                .setEstimationSpec(SpecProto.convert(estimationSpec));
        
        if (result != null){
            builder.setResult(X13ResultsProto.convert(result))
                    .setResultSpec(SpecProto.convert(resultSpec));
        }
        // TODO detail and logs
        
        return builder.build();
    }
}
