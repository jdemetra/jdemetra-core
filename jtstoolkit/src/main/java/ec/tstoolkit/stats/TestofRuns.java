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
public class TestofRuns extends StatisticalTest
{
    // 31/05/2007. Correction for m_wnc...

    DescriptiveStatistics stats;

    private boolean m_bMean = true;

    private double m_ref;

    private int m_p, m_m, m_r;

    private int[] m_nr;

    private RunsTestKind m_kind = RunsTestKind.Number;

    private double[] m_wnc;

    /**
     * 
     */
    public TestofRuns()
    {
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
    public int getMCount()
    {
	return m_m;
    }

    /**
     * 
     * @return
     */
    public int getPCount()
    {
	return m_p;
    }

    /**
     * 
     * @return
     */
    public boolean isUseMean()
    {
	return m_bMean;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isValid() {
	return stats != null && stats.getObservationsCount() > 10;
    }

    private void prepare() {
	m_p = 0;
	m_m = 0;
	m_r = 0;
	if (m_bMean)
	    m_ref = stats.getAverage();
	else
	    m_ref = stats.getMedian();
	m_wnc = stats.observations();

	m_nr = new int[m_wnc.length];
    }

    private void races() {
	int n = m_wnc.length;
	if (n == 0)
	    return;
	boolean prev = m_wnc[0] >= m_ref;
	if (prev)
	    ++m_p;
	else
	    ++m_m;
	m_r = 1;
	int curlength = 1;
	for (int i = 1; i < n; ++i) {
	    boolean cur = m_wnc[i] >= m_ref;
	    if (cur)
		++m_p;
	    else
		++m_m;

	    if (cur != prev) {
		++m_r;
		prev = cur;
		++m_nr[curlength - 1];
		curlength = 1;
	    } else
		++curlength;
	}
	++m_nr[curlength - 1];
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

    /**
     * 
     * @param value
     */
    public void setUseMean(final boolean value)
    {
	if (m_bMean != value) {
	    m_bMean = value;
	    test();
	}
    }

    private void test() {
	prepare();
	races();
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
	int n = m_wnc.length;
	double x = 0, p = m_p, m = m_m;
	double fp = p / n, fm = m / n, e = n / (p / m + m / p), xp = fm, xm = fp;
	for (int i = 0; i < n; ++i) {
	    xp *= fp;
	    xm *= fm;
	    // compute E(y=i+1)
	    double ei = e * (xp + xm);
	    if (m_nr[i] == 0)
		x += ei;
	    else if (ei != 0)
		x += (m_nr[i] - ei) / ei * (m_nr[i] - ei);
	    else
		x += 999999;
	}
	Chi2 dist = new Chi2();
	dist.setDegreesofFreedom(n);
	m_dist = dist;
	m_val = x;
	m_type = TestType.Upper;
	m_asympt = true;
    }

    private void testNumber() {
	double n = m_wnc.length;
	double mp = m_m * m_p;
	double E = 1 + 2 * mp / n;
	double V = 2 * mp * (2 * mp - n) / (n * n * (n - 1));
	if (V < 1e-9)
	    V = 1e-9;
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
