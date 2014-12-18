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
package ec.tstoolkit.stats;

import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.dstats.Normal;
import ec.tstoolkit.dstats.TestType;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class KurtosisTest extends StatisticalTest
{

    DescriptiveStatistics stats;

    /**
     * 
     */
    public KurtosisTest()
    {
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isValid() {
	return stats != null;
    }

    private void test() {
	int n = stats.getObservationsCount();
	Normal dist = new Normal();
	dist.setMean(3);
	dist.setStdev(Math.sqrt(24.0 / n));
	m_dist = dist;
	m_val = stats.getKurtosis();
	m_type = TestType.TwoSided;
	m_asympt = true;
    }

    /**
     * 
     * @param stats
     */
    public void test(DescriptiveStatistics stats)
    {
	this.stats = stats;
	test();
    }

    /**
     * 
     * @param data
     */
    public void test(IReadDataBlock data)
    {
	stats = new DescriptiveStatistics(data);
	test();
    }
}
