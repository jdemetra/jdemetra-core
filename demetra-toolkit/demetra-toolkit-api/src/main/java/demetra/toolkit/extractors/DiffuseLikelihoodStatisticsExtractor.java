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
package demetra.toolkit.extractors;

import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import demetra.likelihood.DiffuseLikelihoodStatistics;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@ServiceProvider(InformationExtractor.class)
public class DiffuseLikelihoodStatisticsExtractor extends InformationMapping<DiffuseLikelihoodStatistics> {

    private final String LL = "ll", LLC = "adjustedll", SSQ = "ssqerr", AIC = "aic", BIC = "bic", AICC = "aicc", HQ = "hannanquinn",
            NPARAMS = "nparams", NOBS = "nobs", DF = "df", NDIFFUSE = "ndiffuse";

    public DiffuseLikelihoodStatisticsExtractor() {
        set(LL, Double.class, source -> source.getLogLikelihood());
        set(LLC, Double.class, source -> source.getAdjustedLogLikelihood());
        set(SSQ, Double.class, source -> source.getSsqErr());
        set(NPARAMS, Integer.class, source -> source.getEstimatedParametersCount());
        set(NOBS, Integer.class, source -> source.getObservationsCount());
        set(NDIFFUSE, Integer.class, source -> source.getDiffuseCount());
        set(DF, Integer.class, source -> source.getObservationsCount() - source.getEstimatedParametersCount() - source.getDiffuseCount());
        set(AIC, Double.class, source -> source.aic());
        set(AICC, Double.class, source -> source.aicc());
        set(BIC, Double.class, source -> source.bic());
        set(HQ, Double.class, source -> source.hannanQuinn());

     }

    @Override
    public Class getSourceClass() {
        return DiffuseLikelihoodStatistics.class;
    }

}
