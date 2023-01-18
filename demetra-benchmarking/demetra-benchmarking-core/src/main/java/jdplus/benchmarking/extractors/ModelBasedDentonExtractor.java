/*
 * Copyright 2022 National Bank of Belgium
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
package jdplus.benchmarking.extractors;

import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import demetra.tempdisagg.univariate.TemporalDisaggregationDictionaries;
import jdplus.tempdisagg.univariate.ResidualsDiagnostics;
import jdplus.stats.likelihood.LikelihoodStatistics;
import demetra.timeseries.TsData;
import jdplus.tempdisagg.univariate.ModelBasedDentonResults;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(InformationExtractor.class)
public class ModelBasedDentonExtractor extends InformationMapping<ModelBasedDentonResults> {

    public ModelBasedDentonExtractor() {
        set(TemporalDisaggregationDictionaries.TARGET, TsData.class, source -> source.getTarget());
        set(TemporalDisaggregationDictionaries.INDICATOR, TsData.class, source -> source.getIndicator());
        set(TemporalDisaggregationDictionaries.DISAGG, TsData.class, source -> source.getDisaggregatedSeries());
        set(TemporalDisaggregationDictionaries.LDISAGG, TsData.class, 
               source -> source.getDisaggregatedSeries()
                        .fn(source.getStdevDisaggregatedSeries(), (a, b) -> a - 2 * b));
        set(TemporalDisaggregationDictionaries.UDISAGG, TsData.class, 
               source -> source.getDisaggregatedSeries()
                        .fn(source.getStdevDisaggregatedSeries(), (a, b) -> a + 2 * b));
        set(TemporalDisaggregationDictionaries.EDISAGG, TsData.class, source -> source.getStdevDisaggregatedSeries());
        set(TemporalDisaggregationDictionaries.LFBIRATIO, TsData.class, source -> source.getAggregatedBiRatios());
        set(TemporalDisaggregationDictionaries.BIRATIO, TsData.class, source -> source.getBiRatios());
        set(TemporalDisaggregationDictionaries.EBIRATIO, TsData.class, source -> source.getStdevBiRatios());
        set(TemporalDisaggregationDictionaries.LBIRATIO, TsData.class, 
               source -> source.getBiRatios()
                        .fn(source.getStdevBiRatios(), (a, b) -> a - 2 * b));
        set(TemporalDisaggregationDictionaries.UBIRATIO, TsData.class, 
               source -> source.getBiRatios()
                        .fn(source.getStdevBiRatios(), (a, b) -> a + 2 * b));
        set(TemporalDisaggregationDictionaries.RES, TsData.class, source -> source.getResiduals());
        delegate(TemporalDisaggregationDictionaries.LIKELIHOOD, LikelihoodStatistics.class, source -> source.getLikelihood());
        delegate(TemporalDisaggregationDictionaries.RES, ResidualsDiagnostics.class, source -> source.getResidualsDiagnostics());
    }

    @Override
    public Class getSourceClass() {
        return ModelBasedDentonResults.class;
    }

}
