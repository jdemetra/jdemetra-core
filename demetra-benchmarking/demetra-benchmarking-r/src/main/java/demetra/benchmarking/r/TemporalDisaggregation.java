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
import demetra.ssf.SsfAlgorithm;
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

    public Results process(TsData y, boolean constant, boolean trend, TsData[] indicators,
            String model, int freq, String aggregation, int obspos,
            double rho, boolean fixedrho, double truncatedRho,
            String algorithm, boolean zeroinit, boolean diffuseregs) {
        TemporalDisaggregationSpec.Builder builder = TemporalDisaggregationSpec.builder()
                .constant(constant)
                .trend(trend)
                .residualsModel(TemporalDisaggregationSpec.Model.valueOf(model))
                .aggregationType(AggregationType.valueOf(aggregation))
                .parameter(fixedrho ? ParameterSpec.fixed(rho) : ParameterSpec.initial(rho))
                .truncatedParameter(truncatedRho <= -1 ? null : truncatedRho)
                .algorithm(SsfAlgorithm.valueOf(algorithm))
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

    @lombok.Value
    public static class Results implements ProcResults {

        TemporalDisaggregationResults results;

        @Override
        public boolean contains(String id) {
            return MAPPING.contains(id);
        }

        @Override
        public Map<String, Class> getDictionary() {
            Map<String, Class> dic = new LinkedHashMap<>();
            MAPPING.fillDictionary(null, dic, true);
            return dic;
        }

        @Override
        public <T> T getData(String id, Class<T> tclass) {
            return MAPPING.getData(results, id, tclass);
        }

        public static final InformationMapping<TemporalDisaggregationResults> getMapping() {
            return MAPPING;
        }

        private static final InformationMapping<TemporalDisaggregationResults> MAPPING;

        static {
            MAPPING = TemporalDisaggregationDescriptor.getMapping();
        }
    }
}
