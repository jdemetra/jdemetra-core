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
package jdplus.modelling.extractors;

import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import demetra.toolkit.dictionaries.LikelihoodDictionaries;
import jdplus.stats.likelihood.DiffuseLikelihoodStatistics;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@ServiceProvider(InformationExtractor.class)
public class DiffuseLikelihoodStatisticsExtractor extends InformationMapping<DiffuseLikelihoodStatistics> {


    public DiffuseLikelihoodStatisticsExtractor() {
        set(LikelihoodDictionaries.LL, Double.class, source -> source.getLogLikelihood());
        set(LikelihoodDictionaries.LLC, Double.class, source -> source.getAdjustedLogLikelihood());
        set(LikelihoodDictionaries.SSQ, Double.class, source -> source.getSsqErr());
        set(LikelihoodDictionaries.NPARAMS, Integer.class, source -> source.getEstimatedParametersCount());
        set(LikelihoodDictionaries.NOBS, Integer.class, source -> source.getObservationsCount());
        set(LikelihoodDictionaries.NDIFFUSE, Integer.class, source -> source.getDiffuseCount());
        set(LikelihoodDictionaries.DF, Integer.class, source -> source.getObservationsCount() - source.getEstimatedParametersCount() - source.getDiffuseCount());
        set(LikelihoodDictionaries.AIC, Double.class, source -> source.aic());
        set(LikelihoodDictionaries.AICC, Double.class, source -> source.aicc());
        set(LikelihoodDictionaries.BIC, Double.class, source -> source.bic());
        set(LikelihoodDictionaries.HQ, Double.class, source -> source.hannanQuinn());

     }

    @Override
    public Class getSourceClass() {
        return DiffuseLikelihoodStatistics.class;
    }

}
