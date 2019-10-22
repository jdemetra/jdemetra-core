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

package jdplus.stats.tests.seasonal;

import demetra.design.Development;
import jdplus.stats.tests.AnovaTest;
import jdplus.stats.tests.StatisticalTest;
import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class CombinedSeasonalityTest {

    /**
     * 
     * @param ts Ts should contain full
     * @param period
     * @param mul
     * @return
     */
    public static AnovaTest evolutiveSeasonality(final DoubleSeq ts, int period, boolean mul)
    {
	// determine "full cycles"

        int n=ts.length();
        int ny=n/period;
        if (ny == 0)
            return null;
	double xbar = mul ? 1 : 0;


	// determine dimensions of marginal means vectors

	double[] mc = new double[period];
	double[] my = new double[ny];
	double m = 0.0;

	double[] tmp = new double[ny * period];

        DoubleSeqCursor reader=ts.cursor();
	for (int i = 0; i < tmp.length; i++)
	    tmp[i] = Math.abs(reader.getAndNext() - xbar);

	for (int i = 0, mm = 0; i < ny; i++) {
	    for (int j = 0; j < period; j++, mm++) {
		mc[j] += tmp[mm];
		my[i] += tmp[mm];
	    }
	    m += my[i];
	}

	m /= period * ny;
	for (int i = 0; i < period; i++)
	    mc[i] /= ny;
	for (int i = 0; i < ny; i++)
	    my[i] /= period;

	double ss = 0.0, ssa = 0.0, ssb = 0.0;
	for (int i = 0, ll = 0; i < ny; i++)
	    for (int j = 0; j < period; j++, ll++)
		ss += (tmp[ll] - m) * (tmp[ll] - m);

	for (int i = 0; i < ny; i++)
	    ssb += ((my[i] - m) * (my[i] - m));
	ssb *= period;
	for (int i = 0; i < period; i++)
	    ssa += ((mc[i] - m) * (mc[i] - m));
	ssa *= ny;

	double ssr = ss - ssa - ssb;
        if (ssr <0)
            ssr=0;
        
	return new AnovaTest(ssb, ny - 1, ssr, (ny - 1)
		* (period - 1));
    }
    /**
     * 
     * @param ts
     * @param period
     * @return
     */
    
    public static AnovaTest stableSeasonality(DoubleSeq ts, int period)
    {
	// compute mean
	double mm = ts.average();

	// compute total SSQ
	double SSQ = 0.0;
        int n=ts.length();
        DoubleSeqCursor reader = ts.cursor();
	for (int i = 0; i < n; i++){
            double cur=reader.getAndNext();
	    SSQ += (cur - mm) * (cur - mm);
        }
        
	// compute SS of seasonality factors

	double SSM = 0;
        for (int i=0; i<period; ++i){
            double s=0;
            int nc=0;
            for (int j=i; j<n; j+=period){
                s+=ts.get(j);
                ++nc;
            }
	    double mmj = s / nc;
	    SSM += (mmj - mm) * (mmj - mm) * nc;
	}

	double SSR = SSQ - SSM;
        if (SSR<0)
            SSR=0;
	return new AnovaTest(SSM, period - 1, SSR,
		n - period);
    }
    /**
     * 
     */
    public static enum IdentifiableSeasonality
    {
	// / <summary>
	// / No identifiable seasonality
	// / </summary>

        /**
         *
         */
        None,
	// / <summary>
	// / Probably no identifiable seasonality
	// / </summary>
        /**
         *
         */
        ProbablyNone,
	// / <summary>
	// / Identifiable seasonality present
	// / </summary>
        /**
         *
         */
        Present
    }

    private double thfs = 0.001;

    private double thfm = 0.05;

    private double thkw = 0.001;

    private final AnovaTest stable, evolutive;

    private final KruskalWallis kruskallwallis;

    /**
     * 
     * @param ts
     * @param mul
     */
    public CombinedSeasonalityTest(DoubleSeq ts, int period, boolean mul)
    {
	kruskallwallis = new KruskalWallis(ts, period);
	stable = stableSeasonality(ts, period);
	evolutive = evolutiveSeasonality(ts, period, mul);

    }

    /**
     * 
     * @return
     */
    public AnovaTest getEvolutiveSeasonality()
    {
	return evolutive;
    }

    /**
     * 
     * @return
     */
    public double getMovingSeasonalityAcceptanceLevel()
    {
	return thfm;
    }

    /**
     * 
     * @return
     */
    public double getNonParametricAcceptanceLevel()
    {
	return thkw;
    }

    /**
     * 
     * @return
     */
    public KruskalWallis getNonParametricTestForStableSeasonality()
    {
	return kruskallwallis;
    }

    /**
     * 
     * @return
     */
    public AnovaTest getStableSeasonality()
    {
	return stable;
    }

    // / <summary>
    // / The property sets/gets the acceptance level for the stable seasonality
    // F-value
    // / </summary>
    /**
     * 
     * @return
     */
    public double getStableSeasonalityAcceptanceLevel()
    {
	return thfs;
    }

    /**
     * 
     * @return
     */
    public IdentifiableSeasonality getSummary()
    {
        StatisticalTest stest=stable.asTest(), etest=evolutive.asTest();
	double ps = stest.getPValue(), pm = etest.getPValue();
	if (ps >= thfs)
	    return IdentifiableSeasonality.None;
	boolean resfm = (pm < thfm);

	double fs = stest.getValue(), fm = etest.getValue();
	double T1 = 7.0 / fs;
	double T2 = fs == 0 ? 9 : 3.0 * fm / fs;
	if (T1 > 9)
	    T1 = 9;
	if (T2 > 9)
	    T2 = 9;

	if (resfm) {
	    double T = Math.sqrt((T1 + T2) / 2.0);
	    if (T >= 1.0)
		return IdentifiableSeasonality.None;
	}

	if (T1 >= 1.0 || T2 >= 1.0)
	    return IdentifiableSeasonality.ProbablyNone;

	if (kruskallwallis.build().getPValue() >= thkw)
	    return IdentifiableSeasonality.ProbablyNone;
	return IdentifiableSeasonality.Present;
    }

    /**
     * 
     * @return
     */
    public double mvalue()
    {
	double fs = stable.asTest().getValue(), fm = evolutive.asTest().getValue();
	double T1 = 7.0 / fs;

	double T2 = fs == 0 ? 9 : 3.0 * fm / fs;
	if (T1 > 9)
	    T1 = 9;
	if (T2 > 9)
	    T2 = 9;
	return Math.sqrt(.5 * (T1 + T2));
    }

    /**
     * 
     * @param value
     */
    public void setMovingSeasonalityAcceptanceLevel(double value)
    {
	thfm = value;
    }

    // / <summary>
    // / The property sets/gets the acceptance level for the Kruskal-Wallis
    // seasonality test F-value
    // / </summary>
    /**
     * 
     * @param value
     */
    public void setNonParametricAcceptanceLevel(double value)
    {
	thkw = value;
    }

    /**
     * 
     * @param value
     */
    public void setStableSeasonalityAcceptanceLevel(double value)
    {
	thfs = value;
    }
    // / <summary>
    // / The property sets/gets the acceptance level for the moving seasonality
    // F-value
    // / </summary>
}
