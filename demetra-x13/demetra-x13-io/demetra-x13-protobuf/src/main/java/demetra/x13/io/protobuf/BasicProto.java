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

import com.google.protobuf.InvalidProtocolBufferException;
import demetra.regarima.BasicSpec;
import demetra.toolkit.io.protobuf.ToolkitProtosUtility;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class BasicProto {

    public void fill(BasicSpec spec, RegArimaSpec.BasicSpec.Builder builder) {
        builder.setSpan(ToolkitProtosUtility.convert(spec.getSpan()))
                .setPreliminaryCheck(spec.isPreliminaryCheck())
                .setPreprocessing(spec.isPreprocessing());
    }

    public RegArimaSpec.BasicSpec convert(BasicSpec spec) {
        RegArimaSpec.BasicSpec.Builder builder = RegArimaSpec.BasicSpec.newBuilder();
        fill(spec, builder);
        return builder.build();
    }

    public BasicSpec convert(RegArimaSpec.BasicSpec spec) {
        return BasicSpec.builder()
                .span(ToolkitProtosUtility.convert(spec.getSpan()))
                .preprocessing(spec.getPreprocessing())
                .preliminaryCheck(spec.getPreliminaryCheck())
                .build();

    }
}
