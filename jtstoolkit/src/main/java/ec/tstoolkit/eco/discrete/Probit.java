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

package ec.tstoolkit.eco.discrete;

import ec.tstoolkit.dstats.Normal;
import ec.tstoolkit.dstats.ProbabilityType;

/**
 *
 * @author Jean Palate
 */
public class Probit implements ICumulativeDistributionFunction {

    @Override
    public double cdf(double x) {
        return m_n.getProbability(x, ProbabilityType.Lower);
    }

    @Override
    public double dcdf(double x) {
        return Math.exp(-.5 * x * x) / c_sqrt2pi;
    }

    @Override
    public double d2cdf(double x) {
        return -x / c_sqrt2pi * Math.exp(-.5 * x * x);
    }
    private Normal m_n = new Normal();
    private static final double c_sqrt2pi = 2.5066282746310005;
}
