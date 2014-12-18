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
import ec.tstoolkit.dstats.Normal;
import ec.tstoolkit.dstats.TestType;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class TestofUpDownRuns extends StatisticalTest
{

    private static double dfact(double x, final int k) {
	for (int i = 2; i <= k; ++i)
	    if (x == 0)
		return 0;
	    else
		x /= i;
	return x;
    }

    // 31/05/2007. Correction for statsc...
    DescriptiveStatistics stats;

    // private bool m_bEqual=false;
    // private int m_k=1, m_r;
    private int m_r;

    private int[] m_nr;

    private RunsTestKind m_kind = RunsTestKind.Number;

    private double[] obs;

    /**
     * 
     */
    public TestofUpDownRuns()
    {
    }

    private void calcRuns() {
	int n = obs.length;
	m_nr = new int[n - 1];
	m_r = 1;
	if (n < 2)
	    return;
	boolean up = obs[1] >= obs[0];
	int curlength = 1;
	for (int i = 2; i < n; ++i) {
	    boolean curup = obs[i] >= obs[i - 1];
	    if (up != curup) {
		++m_r;
		up = curup;
		++m_nr[curlength - 1];
		curlength = 1;
	    } else
		++curlength;
	}
	++m_nr[curlength - 1];
    }

    /**
     * 
     * @return
     */
    public RunsTestKind getKind()
    {
	return m_kind;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isValid() {
	return stats != null && stats.getObservationsCount() > 10;// +m_k;
    }

    /**
     * 
     * @param length
     * @return
     */
    public int runsCount(final int length)
    {
	return (length <= 0) ? m_r : m_nr[length - 1];
    }

    /**
     * 
     * @param value
     */
    public void setKind(final RunsTestKind value)
    {
	if (m_kind != value) {
	    m_kind = value;
	    updateResults();
	}
    }

    private void test() {
	obs = stats.observations();
	calcRuns();
	updateResults();
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

    private void testLength() {
	int n = obs.length;
	double x = 0;
	for (int i = 1; i < n; ++i) {
	    double ei = 0;
	    if (i != n - 1) {
		ei = 2 * (n * (i * i + 3 * i + 1) - (i * i * i + 3 * i * i - i - 4));
		ei = dfact(ei, i + 3);
	    } else
		ei = dfact(2.0, n);

	    double oi = m_nr[i - 1];
	    if (oi == 0)
		x += ei;
	    else if (ei != 0)
		x += (oi - ei) / ei * (oi - ei);
	    else
		x += 999999;
	}

	Chi2 dist = new Chi2();
	dist.setDegreesofFreedom(n - 1);
	m_dist = dist;
	m_val = x;
	m_type = TestType.Upper;
	m_asympt = true;
    }

    private void testNumber() {
	double n = obs.length;
	double E = (2 * n - 1) / 3;
	double V = (16 * n - 29) / 90;

	Normal dist = new Normal();
	m_dist = dist;
	m_val = (m_r - E) / Math.sqrt(V);
	m_type = TestType.TwoSided;
	m_asympt = true;
    }

    private void updateResults() {
	if (m_kind == RunsTestKind.Number)
	    testNumber();
	else
	    testLength();
    }
}
