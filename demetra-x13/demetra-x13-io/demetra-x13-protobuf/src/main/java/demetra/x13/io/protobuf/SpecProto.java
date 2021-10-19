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

import demetra.sa.io.protobuf.SaProtosUtility;
import demetra.x13.X13Spec;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class SpecProto {

    public X13Protos.Spec convert(X13Spec spec) {
        return X13Protos.Spec.newBuilder()
                .setRegarima(RegArimaProto.convert(spec.getRegArima()))
                .setX11(X11Proto.convert(spec.getX11()))
                .setBenchmarking(SaProtosUtility.convert(spec.getBenchmarking()))
                .build();
    }

    public X13Spec convert(X13Protos.Spec spec) {
        return X13Spec.builder()
                .regArima(RegArimaProto.convert(spec.getRegarima()))
                .x11(X11Proto.convert(spec.getX11()))
                .benchmarking(SaProtosUtility.convert(spec.getBenchmarking()))
                .build();
    }
}
