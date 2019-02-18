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
import demetra.likelihood.ConcentratedLikelihood;
import demetra.likelihood.LikelihoodStatistics;
import demetra.maths.MatrixType;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class ConcentratedLikelihoodDescriptor {

    private final String LL = "ll", LDET="ldet", SSQ = "ssqerr", SER = "ser", SIGMA = "sigma", COEF = "coeff", VAR = "cvar", RES="residuals";

    private final InformationMapping<ConcentratedLikelihood> MAPPING = new InformationMapping<>(ConcentratedLikelihood.class);

    static {
        MAPPING.set(SER, Double.class, source -> source.ser());
        MAPPING.set(SIGMA, Double.class, source -> source.sigma());
        MAPPING.set(COEF, double[].class, source -> source.coefficients().toArray());
        MAPPING.set(VAR, MatrixType.class, source -> source.unscaledCovariance());
        MAPPING.set(LL, Double.class, source -> source.logLikelihood());
        MAPPING.set(LDET, Double.class, source -> source.logDeterminant());
        MAPPING.set(SSQ, Double.class, source -> source.ssq());
        MAPPING.set(RES, double[].class, source -> source.e().toArray());
    }

    public InformationMapping<ConcentratedLikelihood> getMapping() {
        return MAPPING;
    }
}
