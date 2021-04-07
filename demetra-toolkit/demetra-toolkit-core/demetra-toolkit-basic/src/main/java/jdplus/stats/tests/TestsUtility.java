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
package jdplus.stats.tests;

import demetra.stats.TestType;
import demetra.stats.ProbabilityType;
import demetra.dstats.ContinuousDistribution;
import demetra.dstats.DStatException;
import demetra.stats.OneWayAnova;
import demetra.stats.StatisticalTest;
import jdplus.dstats.F;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class TestsUtility {

    public double pvalue(ContinuousDistribution distribution, double value, TestType type) {
        try {
            switch (type) {
                case TwoSided:
                    if (!distribution.isSymmetrical()) {
                        throw new DStatException("misspecified test");
                    }
                    double mean = distribution.getExpectation();
                    return 2 * distribution.getProbability(value, value < mean
                            ? ProbabilityType.Lower : ProbabilityType.Upper);
                case Lower:
                    return distribution.getProbability(value, ProbabilityType.Lower);
                case Upper:
                    return distribution.getProbability(value, ProbabilityType.Upper);
                default:
                    return 0;
            }
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    public StatisticalTest testOf(double value, ContinuousDistribution dist, TestType type) {
        return new StatisticalTest(value, TestsUtility.pvalue(dist, value, type), dist.getDescription());
    }

    public StatisticalTest ofAnova(OneWayAnova anova) {
        F f = new F(anova.getDfm(), anova.getDfr());
        return testOf(anova.ftest(), f, TestType.Upper);
    }

}
