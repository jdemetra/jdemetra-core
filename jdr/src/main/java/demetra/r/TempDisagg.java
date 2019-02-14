/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.r;

import demetra.benchmarking.descriptors.TemporalDisaggregationDescriptor;
import demetra.data.AggregationType;
import demetra.data.ParameterSpec;
import demetra.information.InformationMapping;
import demetra.processing.ProcResults;
import demetra.ssf.SsfAlgorithm;
import demetra.tempdisagg.univariate.TemporalDisaggregation;
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
public class TempDisagg {

    public Results process(TsData y, boolean constant, boolean trend, TsData[] indicators,
            String model, String aggregation, double rho, boolean fixedrho, double truncatedRho, boolean diffuseregs,
            String algorithm, int obspos, int freq) {
        TemporalDisaggregationSpec.Builder builder = TemporalDisaggregationSpec.builder()
                .constant(constant)
                .trend(trend)
                .residualsModel(TemporalDisaggregationSpec.Model.valueOf(model))
                .aggregationType(AggregationType.valueOf(aggregation))
                .parameter(fixedrho ? ParameterSpec.fixed(rho) : ParameterSpec.initial(rho))
                .truncatedParameter(truncatedRho <= -1 ? null : truncatedRho)
                .algorithm(SsfAlgorithm.valueOf(algorithm))
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
            return new Results(TemporalDisaggregation.process(y, all, builder.build()));
        } else {
            return new Results(TemporalDisaggregation.process(y, indicators, builder.build()));
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
