/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
 */
package demetra.stats.tests;

import demetra.design.Development;
import demetra.design.Immutable;
import demetra.dstats.IDistribution;
import demetra.dstats.ProbabilityType;
import demetra.stats.StatException;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable
public class StatisticalTest {

    private final IDistribution distribution;
    private final double value;
    private final TestType type;
    private final boolean asymptotical;

    /**
     * Creates new TestStatistic
     *
     * @param dist
     * @param val
     * @param type
     * @param asymptotical
     */
    StatisticalTest(final IDistribution dist, final double val,
            final TestType type, final boolean asymptotical) {
        distribution = dist;
        value = val;
        this.type = type;
        this.asymptotical = asymptotical;
    }

    /**
     *
     * @return
     */
    public IDistribution getDistribution() {
        return distribution;
    }

    /**
     *
     * @return
     */
    public double getPValue() {
        try {
            switch (type) {
                case TwoSided:
                    if (!distribution.isSymmetrical()) {
                        throw new StatException("misspecified test");
                    }
                    double mean = distribution.getExpectation();
                    return 2 * distribution.getProbability(value, value < mean
                            ? ProbabilityType.Lower : ProbabilityType.Upper);
                case Lower:
                    return distribution.getProbability(value, ProbabilityType.Lower);
                case Upper:
                    return distribution.getProbability(value, ProbabilityType.Upper);
                default:
                    return -1;
            }
        } catch (Exception e) {
            return Double.NaN;
        }

    }

    /**
     *
     * @return
     */
    public TestType getType() {
        return type;
    }

    /**
     *
     * @return
     */
    public double getValue() {
        return value;
    }

    /**
     *
     * @return
     */
    public boolean isAsymptotical() {
        return asymptotical;
    }

    /**
     *
     * @param threshold
     * @return
     */
    public boolean isSignificant(double threshold) {
        return getPValue() < threshold;
    }
    
}
