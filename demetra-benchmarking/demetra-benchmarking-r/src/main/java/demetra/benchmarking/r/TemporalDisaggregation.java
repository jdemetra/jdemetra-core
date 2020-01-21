/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demetra.benchmarking.r;

import demetra.benchmarking.descriptors.TemporalDisaggregationDescriptor;
import demetra.data.AggregationType;
import demetra.data.ParameterSpec;
import demetra.information.InformationMapping;
import demetra.processing.ProcResults;
import demetra.ssf.SsfInitialization;
import demetra.tempdisagg.univariate.TemporalDisaggregationResults;
import demetra.tempdisagg.univariate.TemporalDisaggregationSpec;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class TemporalDisaggregation {

    public static class Results implements ProcResults {

        private final TemporalDisaggregationResults core;

        private Results(TemporalDisaggregationResults results) {
            this.core = results;
        }

        public static InformationMapping<TemporalDisaggregationResults> getMapping() {
            return TemporalDisaggregationDescriptor.getMapping();
        }

        @Override
        public boolean contains(String id) {
            return TemporalDisaggregationDescriptor.getMapping().contains(id);
        }

        @Override
        public Map<String, Class> getDictionary() {
            Map<String, Class> dic = new LinkedHashMap<>();
            TemporalDisaggregationDescriptor.getMapping().fillDictionary(null, dic, true);
            return dic;
        }

        @Override
        public <T> T getData(String id, Class<T> tclass) {
            return TemporalDisaggregationDescriptor.getMapping().getData(core, id, tclass);
        }
    }

    public Results process(TsData y, boolean constant, boolean trend, TsData[] indicators,
            String model, int freq, String aggregation, int obspos,
            double rho, boolean fixedrho, double truncatedRho, boolean zeroinit,
            String algorithm, boolean diffuseregs) {
        TemporalDisaggregationSpec.Builder builder = TemporalDisaggregationSpec.builder()
                .constant(constant)
                .trend(trend)
                .residualsModel(TemporalDisaggregationSpec.Model.valueOf(model))
                .aggregationType(AggregationType.valueOf(aggregation))
                .parameter(fixedrho ? ParameterSpec.fixed(rho) : ParameterSpec.initial(rho))
                .truncatedParameter(truncatedRho <= -1 ? null : truncatedRho)
                .algorithm(SsfInitialization.valueOf(algorithm))
                .zeroInitialization(zeroinit)
                .diffuseRegressors(diffuseregs)
                .rescale(true);
        if (aggregation.equals("UserDefined")) {
            builder.observationPosition(obspos);
        }
        if (indicators == null) {
            TsUnit unit = TsUnit.ofAnnualFrequency(freq);
            TsPeriod start = TsPeriod.of(unit, y.getStart().start());
            TsPeriod end = TsPeriod.of(unit, y.getDomain().end());
            TsDomain all = TsDomain.of(start, start.until(end) + 2 * freq);
            return new Results(demetra.tempdisagg.univariate.TemporalDisaggregation.process(y, all, builder.build()));
        } else {
            for (int i = 0; i < indicators.length; ++i) {
                indicators[i] = indicators[i].cleanExtremities();
            }
            return new Results(demetra.tempdisagg.univariate.TemporalDisaggregation.process(y, indicators, builder.build()));
        }
    }

}
