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
package demetra.stats;

import demetra.design.Development;
import demetra.dstats.IDistribution;
import demetra.dstats.ProbabilityType;
import demetra.dstats.TestType;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class StatisticalTest {

    /**
     *
     */
    final IDistribution m_dist;
    /**
     *
     */
    final double m_val;
    /**
     *
     */
    final TestType m_type;
    /**
     *
     */
    final boolean m_asympt;
    boolean m_computed;


    /** Creates new TestStatistic
     * @param dist
     * @param val
     * @param type
     * @param asymptotical
     */
    public StatisticalTest(final IDistribution dist, final double val,
            final TestType type, final boolean asymptotical) {
        m_dist = dist;
        m_val = val;
        m_type = type;
        m_asympt = asymptotical;
    }
    
    /**
     * 
     * @return
     */
    public IDistribution getDistribution() {
        return m_dist;
    }

    /**
     * 
     * @return
     */
    public double getPValue() {
        try {
            switch (m_type) {
                case TwoSided:
                    if (!m_dist.isSymmetrical()) {
                        throw new StatException("misspecified test");
                    }
                    double mean = m_dist.getExpectation();
                    return 2 * m_dist.getProbability(m_val,
                            m_val < mean ? ProbabilityType.Lower
                            : ProbabilityType.Upper);
                case Lower:
                    return m_dist.getProbability(m_val, ProbabilityType.Lower);
                case Upper:
                    return m_dist.getProbability(m_val, ProbabilityType.Upper);
                default:
                    return -1;
            }
        }
        catch (Exception e) {
            return Double.NaN;
        }

    }


    /**
     * 
     * @return
     */
    public TestType getType() {
        return m_type;
    }

    /**
     * 
     * @return
     */
    public double getValue() {
        return m_val;
    }

    /**
     * 
     * @return
     */
    public boolean isAsymptotical() {
        return m_asympt;
    }

    /**
     * 
     * @return
     */
    public boolean isSignificant(double threshold) {
        return getPValue() < threshold;
    }

    /**
     * 
     * @return
     */
    public boolean isValid() {
        return true;
    }

}