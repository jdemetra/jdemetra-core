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
package demetra.descriptors.stats;

import demetra.information.InformationMapping;
import demetra.likelihood.LikelihoodStatistics;
import demetra.likelihood.LikelihoodStatistics;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class LikelihoodStatisticsDescriptor {

    private final String LL = "ll", LLC = "adjustedll", SSQ = "ssqerr", AIC = "aic", BIC = "bic", AICC = "aicc", BICC = "bicc", BIC2 = "bic2", HQ = "hannanquinn",
            NPARAMS = "nparams", NOBS = "nobs", NEFFECTIVEOBS = "neffective";

    private final InformationMapping<LikelihoodStatistics> MAPPING = new InformationMapping<>(LikelihoodStatistics.class);

    static {
        MAPPING.set(AIC, Double.class, source -> source.getAIC());
        MAPPING.set(AICC, Double.class, source -> source.getAICC());
        MAPPING.set(BIC, Double.class, source -> source.getBIC());
        MAPPING.set(BICC, Double.class, source -> source.getBICC());
        MAPPING.set(BIC2, Double.class, source -> source.getBIC2());
        MAPPING.set(HQ, Double.class, source -> source.getHannanQuinn());
        MAPPING.set(LL, Double.class, source -> source.getLogLikelihood());
        MAPPING.set(LLC, Double.class, source -> source.getAdjustedLogLikelihood());
        MAPPING.set(SSQ, Double.class, source -> source.getSsqErr());

        MAPPING.set(NPARAMS, Integer.class, source -> source.getEstimatedParametersCount());
        MAPPING.set(NOBS, Integer.class, source -> source.getObservationsCount());
        MAPPING.set(NEFFECTIVEOBS, Integer.class, source -> source.getEffectiveObservationsCount());

    }

    public InformationMapping<LikelihoodStatistics> getMapping() {
        return MAPPING;
    }
}
