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
public class DoornikHansenTest extends StatisticalTest
{

    DescriptiveStatistics stats;

    /**
     * 
     */
    public DoornikHansenTest()
    {
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isValid() {
	return stats != null && stats.getDataCount() > 7;
    }

    // calculate correction for Skewness (D'Agostino)
    private void test() {

	double sk = stats.getSkewness();
	double n = stats.getDataCount();
	if (n <= 7)
	    return;
	double b = (3 * (n * n + 27 * n - 70) * (n + 1) * (n + 3))
		/ ((n - 2) * (n + 5) * (n + 7) * (n + 9));
	double w2 = -1 + Math.sqrt(2 * (b - 1));
	double ds = 1.0 / (Math.sqrt(0.5 * Math.log(w2)));
	double y = sk
		* Math.sqrt((w2 - 1) * (n + 1) * (n + 3) / (12 * (n - 2)));
	double z1 = ds * Math.log(y + Math.sqrt(y * y + 1.0));

	// calculate transformation of kurtosis from gamma to X2 distribution
	// using Wilson-Hilferty
	// cubed root transformation
	double kr = stats.getKurtosis();
	double dk = (n - 3) * (n + 1) * (n * n + 15 * n - 4);
	double a = (n - 2) * (n + 5) * (n + 7) * (n * n + 27 * n - 70.0)
		/ (dk * 6);
	double c = (n - 7) * (n + 5) * (n + 7) * (n * n + 2 * n - 5) / (dk * 6);
	double k = (n + 5) * (n + 7)
		* (n * n * n + n * n * 37 + 11 * n - 313.0) / (dk * 12);
	double alpha = a + c * sk * sk;
	double chi = 2 * k * (kr - 1.0 - sk * sk);
	double z2 = Math.sqrt(alpha * 9)
		* (Math.pow(chi / (2 * alpha), 1 / 3.0) - 1 + 1 / (9 * alpha));

	m_val = z1 * z1 + z2 * z2;
	Chi2 chi2 = new Chi2();
	chi2.setDegreesofFreedom(2);
	m_dist = chi2;
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
