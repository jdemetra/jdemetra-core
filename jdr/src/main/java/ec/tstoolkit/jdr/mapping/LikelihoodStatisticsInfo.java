/*
 * Copyright 2017 National Bank of Belgium
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
package ec.tstoolkit.jdr.mapping;

import demetra.information.InformationMapping;
import ec.tstoolkit.arima.estimation.LikelihoodStatistics;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class LikelihoodStatisticsInfo {

    public final String 
            NP = "np",
            NEFFOBS = "neffectiveobs",
            ADJLVAL = "adjustedlogvalue",
            LVAL = "logvalue",
            SSQERR = "ssqerr",
            SER = "ser",
            SERML = "ser-ml",
            AIC = "aic",
            AICC = "aicc",
            BIC = "bic",
            BICC = "bicc";

    // MAPPING
    public static InformationMapping<LikelihoodStatistics> getMapping() {
        return MAPPING;
    }

    private static final InformationMapping<LikelihoodStatistics> MAPPING = new InformationMapping<>(LikelihoodStatistics.class);

    static {

        // Likelihood
        MAPPING.set(NEFFOBS, Integer.class, source -> source.effectiveObservationsCount);
        MAPPING.set(NP, Integer.class, source -> source.estimatedParametersCount);
        MAPPING.set(LVAL, Double.class, source -> source.logLikelihood);
        MAPPING.set(ADJLVAL, Double.class, source -> source.adjustedLogLikelihood);
        MAPPING.set(SSQERR, Double.class, source -> source.SsqErr);
        MAPPING.set(AIC, Double.class, source -> source.AIC);
        MAPPING.set(AICC, Double.class, source -> source.AICC);
        MAPPING.set(BIC, Double.class, source -> source.BIC);
        MAPPING.set(BICC, Double.class, source -> source.BICC);
        MAPPING.set(SER, Double.class,
                source -> Math.sqrt(source.SsqErr / (source.effectiveObservationsCount - source.estimatedParametersCount + 1)));
        MAPPING.set(SERML, Double.class,
                source -> Math.sqrt(source.SsqErr / (source.effectiveObservationsCount)));
    }

}
