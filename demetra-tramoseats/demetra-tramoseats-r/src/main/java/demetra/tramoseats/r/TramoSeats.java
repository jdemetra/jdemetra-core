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
import demetra.processing.ProcResults;
import demetra.sa.EstimationPolicyType;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.regression.ModellingContext;
import demetra.tramoseats.TramoSeatsSpec;
import demetra.tramoseats.io.protobuf.SpecProto;
import demetra.tramoseats.io.protobuf.TramoSeatsOutput;
import demetra.tramoseats.io.protobuf.TramoSeatsProtos;
import demetra.tramoseats.io.protobuf.TramoSeatsResultsProto;
import demetra.util.r.Dictionary;
import java.util.LinkedHashMap;
import java.util.Map;
import jdplus.tramoseats.TramoSeatsFactory;
import jdplus.tramoseats.TramoSeatsKernel;
import jdplus.tramoseats.TramoSeatsResults;
import jdplus.tramoseats.extractors.TramoSeatsExtractor;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class TramoSeats {

    @lombok.Value
    public static class Results implements ProcResults {

        private TramoSeatsResults core;

        public byte[] buffer() {
            return TramoSeatsResultsProto.convert(core).toByteArray();
        }

        public Tramo.Results preprocessing() {
            return new Tramo.Results(core.getPreprocessing());
        }

        public Seats.Results decomposition() {
            return new Seats.Results(core.getDecomposition());
        }

        @Override
        public boolean contains(String id) {
            return TramoSeatsExtractor.getMapping().contains(id);
        }

        @Override
        public Map<String, Class> getDictionary() {
            Map<String, Class> dic = new LinkedHashMap<>();
            TramoSeatsExtractor.getMapping().fillDictionary(null, dic, true);
            return dic;
        }

        @Override
        public <T> T getData(String id, Class<T> tclass) {
            return TramoSeatsExtractor.getMapping().getData(core, id, tclass);
        }
    }

    public Results process(TsData series, String defSpec) {
        TramoSeatsSpec spec = TramoSeatsSpec.fromString(defSpec);
        TramoSeatsKernel kernel = TramoSeatsKernel.of(spec, null);
        TramoSeatsResults estimation = kernel.process(series.cleanExtremities(), null);
        return new Results(estimation);
    }

    public Results process(TsData series, TramoSeatsSpec spec, Dictionary dic) {
        ModellingContext context = dic == null ? null : dic.toContext();
        TramoSeatsKernel kernel = TramoSeatsKernel.of(spec, context);
        TramoSeatsResults estimation = kernel.process(series.cleanExtremities(), null);
        return new Results(estimation);
    }

    public TramoSeatsSpec refreshSpec(TramoSeatsSpec currentSpec, TramoSeatsSpec domainSpec, TsDomain domain, String policy) {
        return TramoSeatsFactory.INSTANCE.refreshSpec(currentSpec, domainSpec, EstimationPolicyType.valueOf(policy), domain);
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

    public TramoSeatsOutput fullProcess(TsData series, TramoSeatsSpec spec, Dictionary dic) {
        ModellingContext context = dic == null ? null : dic.toContext();
        TramoSeatsKernel tramoseats = TramoSeatsKernel.of(spec, context);
        TramoSeatsResults estimation = tramoseats.process(series.cleanExtremities(), null);

        return TramoSeatsOutput.builder()
                .estimationSpec(spec)
                .result(estimation)
                .resultSpec(estimation == null ? null : TramoSeatsFactory.INSTANCE.generateSpec(spec, estimation.getPreprocessing().getDescription()))
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
