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
import jdplus.stats.likelihood.LikelihoodStatistics;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@ServiceProvider(InformationExtractor.class)
public class LikelihoodStatisticsExtractor extends InformationMapping<LikelihoodStatistics> {

    public LikelihoodStatisticsExtractor() {
        set(LikelihoodDictionaries.LL, Double.class, source -> source.getLogLikelihood());
        set(LikelihoodDictionaries.LLC, Double.class, source -> source.getAdjustedLogLikelihood());
        set(LikelihoodDictionaries.SSQ, Double.class, source -> source.getSsqErr());
        set(LikelihoodDictionaries.NPARAMS, Integer.class, source -> source.getEstimatedParametersCount());
        set(LikelihoodDictionaries.NOBS, Integer.class, source -> source.getObservationsCount());
        set(LikelihoodDictionaries.NEFFECTIVEOBS, Integer.class, source -> source.getEffectiveObservationsCount());
        set(LikelihoodDictionaries.DF, Integer.class, source -> source.getEffectiveObservationsCount() - source.getEstimatedParametersCount());
        set(LikelihoodDictionaries.AIC, Double.class, source -> source.getAIC());
        set(LikelihoodDictionaries.AICC, Double.class, source -> source.getAICC());
        set(LikelihoodDictionaries.BIC, Double.class, source -> source.getBIC());
        set(LikelihoodDictionaries.BICC, Double.class, source -> source.getBICC());
        set(LikelihoodDictionaries.BIC2, Double.class, source -> source.getBIC2());
        set(LikelihoodDictionaries.HQ, Double.class, source -> source.getHannanQuinn());
    }

    @Override
    public Class getSourceClass() {
        return LikelihoodStatistics.class;
    }
}
