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
import ec.tstoolkit.dstats.Chi2;
import ec.tstoolkit.dstats.TestType;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class BowmanShentonTest extends StatisticalTest
{

    DescriptiveStatistics stats;

    /**
     * 
     */
    public BowmanShentonTest()
    {
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isValid() {
	return stats != null && stats.getObservationsCount() > 10;
    }

    private void test() {
	int n = stats.getObservationsCount();
	double m3 = stats.getSkewness();
	double m4 = stats.getKurtosis() - 3.0;
	m_val = n / 6.0 * m3 * m3 + n / 24.0 * m4 * m4;
	Chi2 chi = new Chi2();
	chi.setDegreesofFreedom(2);
	m_dist = chi;
	m_type = TestType.Upper;
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
