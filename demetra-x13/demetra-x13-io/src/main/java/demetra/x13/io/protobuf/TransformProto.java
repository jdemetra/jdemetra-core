/*
 * Copyright 2021 National Bank of Belgium
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

import demetra.modelling.io.protobuf.ModellingProtosUtility;
import demetra.regarima.TransformSpec;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class TransformProto {

    public void fill(TransformSpec spec, X13Protos.RegArimaSpec.TransformSpec.Builder builder) {
        builder.setTransformation(ModellingProtosUtility.convert(spec.getFunction()))
                .setAdjust(ModellingProtosUtility.convert(spec.getAdjust()))
                .setAicdiff(spec.getAicDiff());
    }

    public X13Protos.RegArimaSpec.TransformSpec convert(TransformSpec spec) {
        X13Protos.RegArimaSpec.TransformSpec.Builder builder = X13Protos.RegArimaSpec.TransformSpec.newBuilder();
        fill(spec, builder);
        return builder.build();
    }

    public TransformSpec convert(X13Protos.RegArimaSpec.TransformSpec spec) {
        return TransformSpec.builder()
                .function(ModellingProtosUtility.convert(spec.getTransformation()))
                .adjust(ModellingProtosUtility.convert(spec.getAdjust()))
                .aicDiff(spec.getAicdiff())
                .build();

    }
}
