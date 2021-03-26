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
package demetra.x13.r;

import com.google.protobuf.InvalidProtocolBufferException;
import demetra.processing.ProcResults;
import demetra.sa.EstimationPolicyType;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.regression.ModellingContext;
import demetra.util.r.Dictionary;
import demetra.x13.X13Spec;
import demetra.x13.io.protobuf.SpecProto;
import demetra.x13.io.protobuf.X13Output;
import demetra.x13.io.protobuf.X13Protos;
import demetra.x13.io.protobuf.X13ResultsProto;
import java.util.LinkedHashMap;
import java.util.Map;
import jdplus.x13.X13Factory;
import jdplus.x13.X13Kernel;
import jdplus.x13.X13Results;
import jdplus.x13.extractors.X13Extractor;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class X13 {

    @lombok.Value
    public static class Results implements ProcResults {

        private X13Results core;

        public byte[] buffer() {
            return X13ResultsProto.convert(core).toByteArray();
        }

        public RegArima.Results preprocessing() {
            return new RegArima.Results(core.getPreprocessing());
        }

        public X11.Results decomposition() {
            return new X11.Results(core.getDecomposition());
        }

        @Override
        public boolean contains(String id) {
            return X13Extractor.getMapping().contains(id);
        }

        @Override
        public Map<String, Class> getDictionary() {
            Map<String, Class> dic = new LinkedHashMap<>();
            X13Extractor.getMapping().fillDictionary(null, dic, true);
            return dic;
        }

        @Override
        public <T> T getData(String id, Class<T> tclass) {
            return X13Extractor.getMapping().getData(core, id, tclass);
        }
    }

    public Results process(TsData series, String defSpec) {
        X13Spec spec = X13Spec.fromString(defSpec);
        X13Kernel kernel = X13Kernel.of(spec, null);
        X13Results estimation = kernel.process(series.cleanExtremities(), null);
        return new Results(estimation);
    }

    public Results process(TsData series, X13Spec spec, Dictionary dic) {
        ModellingContext context = dic == null ? null : dic.toContext();
        X13Kernel kernel = X13Kernel.of(spec, context);
        X13Results estimation = kernel.process(series.cleanExtremities(), null);
        return new Results(estimation);
    }

    public X13Spec refreshSpec(X13Spec currentSpec, X13Spec domainSpec, String policy, TsDomain domain) {
        return X13Factory.INSTANCE.refreshSpec(currentSpec, domainSpec, EstimationPolicyType.valueOf(policy), domain);
    }

    public byte[] toBuffer(X13Spec spec) {
        return SpecProto.convert(spec).toByteArray();
    }

    public X13Spec specOf(byte[] buffer) {
        try {
            X13Protos.Spec spec = X13Protos.Spec.parseFrom(buffer);
            return SpecProto.convert(spec);
        } catch (InvalidProtocolBufferException ex) {
            return null;
        }
    }

    public X13Output fullProcess(TsData series, X13Spec spec, Dictionary dic) {
        ModellingContext context = dic == null ? null : dic.toContext();
        X13Kernel tramoseats = X13Kernel.of(spec, context);
        X13Results estimation = tramoseats.process(series.cleanExtremities(), null);

        return X13Output.builder()
                .estimationSpec(spec)
                .result(estimation)
                .resultSpec(estimation == null ? null : X13Factory.INSTANCE.generateSpec(spec, estimation))
                .build();
    }

    public X13Output fullProcess(TsData series, String defSpec) {
        X13Spec spec = X13Spec.fromString(defSpec);
        return fullProcess(series, spec, null);
    }

    public byte[] toBuffer(X13Output output) {
        return output.convert().toByteArray();
    }

}
