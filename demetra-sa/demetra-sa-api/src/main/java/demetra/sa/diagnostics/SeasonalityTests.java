/*
 * Copyright 2020 National Bank of Belgium
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
package demetra.sa.diagnostics;

import demetra.arima.SarimaOrders;
import demetra.data.DoubleSeq;
import demetra.design.Algorithm;
import demetra.stats.OneWayAnova;
import demetra.stats.StatisticalTest;
import nbbrd.service.Mutability;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;

/**
 *
 * @author palatej
 */
public class SeasonalityTests {

    private final SeasonalityTestsLoader.Factory FACTORY = new SeasonalityTestsLoader.Factory();

    public void seFactory(Factory factory) {
        FACTORY.set(factory);
    }

    public Factory getFactory() {
        return FACTORY.get();
    }

    public OneWayAnova stableSeasonalityTest(final DoubleSeq x, int period) {
        return getFactory().stableSeasonalityTest(x, period);
    }

    public OneWayAnova evolutiveSeasonalityTest(final DoubleSeq x, int period, int startPos, double xbar) {
        return getFactory().evolutiveSeasonalityTest(x, period, startPos, xbar);
    }

    public CombinedSeasonalityTest combinedTest(DoubleSeq x, int period, int startperiod, double xbar) {
        return getFactory().combinedTest(x, period, startperiod, xbar);
    }

    public StatisticalTest friedmanTest(DoubleSeq data, int period) {
        return getFactory().qsTest(data, period, period);
    }

    public StatisticalTest kruskalWallisTest(DoubleSeq data, int period) {
        return getFactory().kruskalWallisTest(data, period);
    }

    public StatisticalTest qsTest(DoubleSeq data, int period, int nlags) {
        return getFactory().qsTest(data, period, nlags);
    }

    public StatisticalTest fTest(DoubleSeq data, int period, SarimaOrders.Prespecified model) {
        return getFactory().fTest(data, period, model);
    }

    @Algorithm
    @ServiceDefinition(quantifier = Quantifier.SINGLE, mutability = Mutability.CONCURRENT)
    public static interface Factory {

        // Parametric tests. Should be applied on series corrected for trend
        public OneWayAnova stableSeasonalityTest(final DoubleSeq x, int period);

        public OneWayAnova evolutiveSeasonalityTest(final DoubleSeq x, int period, int startPos, double xbar);

        public CombinedSeasonalityTest combinedTest(DoubleSeq x, int period, int startperiod, double xbar);

        // Non parametric tests. Should be applied on series corrected for trend
        public StatisticalTest friedmanTest(DoubleSeq data, int period);

        public StatisticalTest kruskalWallisTest(DoubleSeq data, int period);

        // Should be applied on series corrected for trend
        public StatisticalTest qsTest(DoubleSeq data, int period, int nlags);

        public StatisticalTest fTest(DoubleSeq data, int period, SarimaOrders.Prespecified model);

    }

}
