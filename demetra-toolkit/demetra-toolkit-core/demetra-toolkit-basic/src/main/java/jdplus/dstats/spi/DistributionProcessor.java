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
package jdplus.dstats.spi;

import demetra.dstats.spi.Distributions;
import jdplus.random.MersenneTwister;
import jdplus.random.RandomNumberGenerator;
import demetra.stats.ProbabilityType;
import jdplus.dstats.ContinuousDistribution;

/**
 *
 * @author Jean Palate
 */
class DProcessor implements Distributions.Processor.Distribution{
    
   
    private final ContinuousDistribution distribution;
    private final RandomNumberGenerator rng=MersenneTwister.fromSystemNanoTime();
     
    DProcessor(final ContinuousDistribution distribution){
        this.distribution=distribution;
    }

    @Override
    public double random() {
        return distribution.random(rng);
    }

    @Override
    public double probability(double x, ProbabilityType type) {
        return distribution.getProbability(x, type);
    }

    @Override
    public double probabilityInverse(double p, ProbabilityType type) {
        return distribution.getProbabilityInverse(p, type);
    }
    
    @Override
    public double density(double x) {
        return distribution.getDensity(x);
    }
}
