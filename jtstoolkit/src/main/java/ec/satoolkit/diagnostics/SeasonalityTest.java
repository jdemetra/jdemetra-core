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


package ec.satoolkit.diagnostics;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.dstats.F;
import ec.tstoolkit.dstats.TestType;
import ec.tstoolkit.stats.StatisticalTest;
import ec.tstoolkit.timeseries.simplets.PeriodIterator;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataBlock;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SeasonalityTest extends StatisticalTest {
    /**
     * 
     * @param ts
     * @param mul
     * @return
     */
    public static SeasonalityTest evolutiveSeasonality(TsData ts, boolean mul)
    {
	// determine begin and endpoints because we need full years

	double xbar = mul ? 1 : 0;

	// compute average on full years...
	PeriodIterator bi = PeriodIterator.fullYears(ts);

	// determine dimensions of marginal means vectors

	int freq = ts.getFrequency().intValue();
	double[] mc = new double[freq];
	if (!bi.hasMoreElements())
	    return null;
	TsDataBlock cur = bi.nextElement();
	int yy = cur.data.getLength();
	double[] my = new double[yy];
	double m = 0.0;

	double[] tmp = new double[yy * freq];
	int f = cur.start.minus(ts.getStart());

	double[] pdata = ts.getValues().internalStorage();

	for (int i = 0; i < tmp.length; i++)
	    tmp[i] = Math.abs(pdata[f++] - xbar);

	for (int i = 0, mm = 0; i < yy; i++) {
	    for (int j = 0; j < freq; j++, mm++) {
		mc[j] += tmp[mm];
		my[i] += tmp[mm];
	    }
	    m += my[i];
	}

	m /= freq * yy;
	for (int i = 0; i < freq; i++)
	    mc[i] /= yy;
	for (int i = 0; i < yy; i++)
	    my[i] /= freq;

	double ss = 0.0, ssa = 0.0, ssb = 0.0;
	for (int i = 0, ll = 0; i < yy; i++)
	    for (int j = 0; j < freq; j++, ll++)
		ss += (tmp[ll] - m) * (tmp[ll] - m);

	for (int i = 0; i < yy; i++)
	    ssb += ((my[i] - m) * (my[i] - m));
	ssb *= freq;
	for (int i = 0; i < freq; i++)
	    ssa += ((mc[i] - m) * (mc[i] - m));
	ssa *= yy;

	double ssr = ss - ssa - ssb;
        if (ssr <0)
            ssr=0;
	SeasonalityTest sr = new SeasonalityTest(ssb, yy - 1, ssr, (yy - 1)
		* (freq - 1));
	return sr;
    }

    /**
     * 
     * @param ts
     * @return
     */
    public static SeasonalityTest stableSeasonality(TsData ts)
    {
	double[] pdata = ts.getValues().internalStorage();
	// compute mean
	double mm = 0;
	for (int i = 0; i < pdata.length; i++)
	    mm += pdata[i];
	mm /= pdata.length;

	// compute total SSQ
	double SSQ = 0.0;
	for (int i = 0; i < pdata.length; i++)
	    SSQ += (pdata[i] - mm) * (pdata[i] - mm);

	// compute SS of seasonality factors
	PeriodIterator bi = new PeriodIterator(ts);

	double SSM = 0;
	while (bi.hasMoreElements()) {
	    TsDataBlock block = bi.nextElement();
	    DataBlock rc = block.data;
	    double mmj = rc.sum() / rc.getLength();
	    SSM += (mmj - mm) * (mmj - mm) * rc.getLength();
	}

	double SSR = SSQ - SSM;
        if (SSR<0)
            SSR=0;
	int freq = ts.getFrequency().intValue();
	SeasonalityTest st = new SeasonalityTest(SSM, freq - 1, SSR,
		pdata.length - freq);
	return st;
    }

    private double m_ssr, m_ssm;

    private int m_dfm, m_dfr;

    // / <summary>
    // / Constructor
    // / </summary>
    // / <param name="SSM">Inter-period Sum of Squares</param>
    // / <param name="dfm">Degrees of freedom of SSM</param>
    // / <param name="SSR">Sum of Squares of residuals</param>
    // / <param name="dfr">Degrees of freedomof SSR</param>
    SeasonalityTest(double SSM, int dfm, double SSR, int dfr) {
	m_ssm = SSM;
	m_ssr = SSR;

	m_dfm = dfm;
	m_dfr = dfr;

	F f = new F();
	f.setDFNum(m_dfm);
	f.setDFDenom(m_dfr);
	m_dist = f;
	m_val = (m_ssm / m_dfm) * (m_dfr / m_ssr);
	m_type = TestType.Upper;
    }

    // / <summary>
    // / The peroperty sets/gets the degrees of freedom of the inter-period SS.
    // This is
    // / equal to Frequency-1
    // / </summary>
    /**
     * 
     * @return
     */
    public int getDFInterPeriod()
    {
	return m_dfm;
    }

    // / <summary>
    // / The property sets/gets the degrees of freedom of the residual SS. This
    // is equal
    // / to NumberOfObs-Frequency
    // / </summary>
    /**
     * 
     * @return
     */
    public int getDFResidual()
    {
	return m_dfr;
    }

    /**
     * 
     * @return
     */
    public int getDFTot()
    {
	return m_dfm + m_dfr;
    }

    // / <summary>
    // / The property sets/gets the Inter-period Sum of Squares
    // / </summary>
    /**
     * 
     * @return
     */
    public double getSSM()
    {
	return m_ssm;
    }

    /**
     * 
     * @return
     */
    public double getSSQ()
    {
	return m_ssr + m_ssm;
    }

    // / <summary>
    // / The property sets/get the Sum of Squares of the residuals (total SS -
    // SSM)
    // / </summary>
    /**
     * 
     * @return
     */
    public double getSSR()
    {
	return m_ssr;
    }
}
