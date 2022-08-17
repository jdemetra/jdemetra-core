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

    public final String DISAGG = "disagg", EDISAGG = "edisagg", BIRATIO = "biratio", EBIRATIO = "ebiratio",
            RES="residuals", RESDIAGS = "residualsstats", LIKELIHOOD = "ll";

    public ModelBasedDentonExtractor() {
        set(DISAGG, TsData.class, source -> source.getDisaggregatedSeries());
        set(EDISAGG, TsData.class, source -> source.getStdevDisaggregatedSeries());
        set(BIRATIO, TsData.class, source -> source.getBiRatios());
        set(EBIRATIO, TsData.class, source -> source.getStdevBiRatios());
        set(RES, TsData.class, source -> source.getResiduals());
        delegate(LIKELIHOOD, LikelihoodStatistics.class, source -> source.getLikelihood());
        delegate(RESDIAGS, ResidualsDiagnostics.class, source -> source.getResidualsDiagnostics());
        
    }

    @Override
    public Class getSourceClass() {
        return ModelBasedDentonResults.class;
    }

}
