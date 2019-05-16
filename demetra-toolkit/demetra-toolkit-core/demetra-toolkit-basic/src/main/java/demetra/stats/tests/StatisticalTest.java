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
import demetra.stats.ProbabilityType;
import demetra.stats.StatException;
import demetra.stats.TestResult;
import jdplus.dstats.Distribution;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
public class StatisticalTest {

    private Distribution distribution;
    private double value;
    private TestType type;
    private boolean asymptotical;

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
     * @param threshold
     * @return
     */
    public boolean isSignificant(double threshold) {
        return getPValue() < threshold;
    }

    public TestResult toSummary(){
        return new TestResult(value, getPValue(), getDistribution().toString());
    }
}
