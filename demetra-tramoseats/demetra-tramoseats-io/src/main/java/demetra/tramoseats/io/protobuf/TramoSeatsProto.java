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
package demetra.tramoseats.io.protobuf;

import demetra.sa.io.protobuf.SaProtosUtility;
import demetra.tramoseats.TramoSeatsSpec;

/**
 *
 * @author PALATEJ
 */
public class TramoSeatsProto {

    public TramoSeatsProtos.Spec convert(TramoSeatsSpec spec) {
        return TramoSeatsProtos.Spec.newBuilder()
                .setTramo(TramoProto.convert(spec.getTramo()))
                .setSeats(DecompositionProto.convert(spec.getSeats()))
                .setBenchmarking(SaProtosUtility.convert(spec.getBenchmarking()))
                .build();
    }
    
    public TramoSeatsSpec convert(TramoSeatsProtos.Spec spec) {
        return TramoSeatsSpec.builder()
                .tramo(TramoProto.convert(spec.getTramo()))
                .seats(DecompositionProto.convert(spec.getSeats()))
                .benchmarking(SaProtosUtility.convert(spec.getBenchmarking()))
                .build();
    }
    
}
