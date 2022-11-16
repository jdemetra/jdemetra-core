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
package demetra.tramoseats.r;

import com.google.protobuf.InvalidProtocolBufferException;
import demetra.sa.EstimationPolicyType;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.regression.ModellingContext;
import demetra.tramoseats.TramoSeatsSpec;
import demetra.tramoseats.io.protobuf.SpecProto;
import demetra.tramoseats.io.protobuf.TramoSeatsOutput;
import demetra.tramoseats.io.protobuf.TramoSeatsProtos;
import demetra.tramoseats.io.protobuf.TramoSeatsResultsProto;
import jdplus.tramoseats.TramoSeatsFactory;
import jdplus.tramoseats.TramoSeatsKernel;
import jdplus.tramoseats.TramoSeatsResults;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class TramoSeats {

    public byte[] toBuffer(TramoSeatsResults rslts) {
        return TramoSeatsResultsProto.convert(rslts).toByteArray();
    }

    public TramoSeatsResults process(TsData series, String defSpec) {
        TramoSeatsSpec spec = TramoSeatsSpec.fromString(defSpec);
        TramoSeatsKernel kernel = TramoSeatsKernel.of(spec, null);
        return kernel.process(series.cleanExtremities(), null);
    }

    public TramoSeatsResults process(TsData series, TramoSeatsSpec spec, ModellingContext context) {
        TramoSeatsKernel kernel = TramoSeatsKernel.of(spec, context);
        return kernel.process(series.cleanExtremities(), null);
    }

    public TramoSeatsSpec refreshSpec(TramoSeatsSpec currentSpec, TramoSeatsSpec domainSpec, TsDomain domain, String policy) {
        return TramoSeatsFactory.getInstance().refreshSpec(currentSpec, domainSpec, EstimationPolicyType.valueOf(policy), domain);
    }

    public byte[] toBuffer(TramoSeatsSpec spec) {
        return SpecProto.convert(spec).toByteArray();
    }

    public TramoSeatsSpec specOf(byte[] buffer) {
        try {
            TramoSeatsProtos.Spec spec = TramoSeatsProtos.Spec.parseFrom(buffer);
            return SpecProto.convert(spec);
        } catch (InvalidProtocolBufferException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    public TramoSeatsOutput fullProcess(TsData series, TramoSeatsSpec spec, ModellingContext context) {
        TramoSeatsKernel tramoseats = TramoSeatsKernel.of(spec, context);
        TramoSeatsResults estimation = tramoseats.process(series.cleanExtremities(), null);

        return TramoSeatsOutput.builder()
                .estimationSpec(spec)
                .result(estimation)
                .resultSpec(estimation == null ? null : TramoSeatsFactory.getInstance().generateSpec(spec, estimation.getPreprocessing().getDescription()))
                .build();
    }

    public TramoSeatsOutput fullProcess(TsData series, String defSpec) {
        TramoSeatsSpec spec = TramoSeatsSpec.fromString(defSpec);
        return fullProcess(series, spec, null);
    }

    public byte[] toBuffer(TramoSeatsOutput output) {
        return output.convert().toByteArray();
    }

}
