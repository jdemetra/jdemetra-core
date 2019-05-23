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
package demetra.dstats.spi;

import demetra.design.Algorithm;
import demetra.design.ServiceDefinition;
import demetra.stats.ProbabilityType;
import demetra.util.ServiceLookup;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class Distributions {

    private final AtomicReference<Processor> PROCESSOR = ServiceLookup.firstMutable(Processor.class);

    public void setProcessor(Processor processor) {
        PROCESSOR.set(processor);
    }

    public Processor getProcessor() {
        return PROCESSOR.get();
    }

    public Processor.Distribution normal() {
        return PROCESSOR.get().normal(0, 1);
    }

    public Processor.Distribution normal(double mean, double stde) {
        return PROCESSOR.get().normal(mean, stde);
    }

    public Processor.Distribution chi2(double degreesOfFreedom) {
        return PROCESSOR.get().chi2(degreesOfFreedom);
    }

    public Processor.Distribution t(double degreesOfFreedom) {
        return PROCESSOR.get().t(degreesOfFreedom);
    }

    public Processor.Distribution f(double degreesOfFreedomOnNum, double degreesOfFreedomOnDenom) {
        return PROCESSOR.get().f(degreesOfFreedomOnNum, degreesOfFreedomOnDenom);
    }

    public Processor.Distribution gamma(double alpha, double beta) {
        return PROCESSOR.get().gamma(alpha, beta);
    }

    @ServiceDefinition
    @Algorithm
    public static interface Processor {

        static interface Distribution {

            double random();

            double probability(double x, ProbabilityType type);

            double probabilityInverse(double p, ProbabilityType type);

            double density(double x);
        }

        Distribution normal(double mean, double stdev);

        Distribution chi2(double degreesOfFreedom);

        Distribution t(double degreesOfFreedom);

        Distribution f(double degreesOfFreedomOnNum, double degreesOfFreedomOnDenom);

        Distribution gamma(double alpha, double beta);

    }

}
