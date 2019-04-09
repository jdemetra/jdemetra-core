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


package demetra.stats.tests.seasonal;

import demetra.data.DoubleSeqCursor;
import demetra.data.DeprecatedDoubles;
import demetra.design.Development;
import demetra.dstats.F;
import demetra.stats.tests.FTest;
import demetra.stats.tests.StatisticalTest;
import demetra.stats.tests.TestType;
import demetra.data.DoubleSeq;


/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SeasonalityTest  {
    /**
     * 
     * @param ts Ts should contain full
     * @param period
     * @param mul
     * @return
     */
    public static FTest evolutiveSeasonality(final DoubleSeq ts, int period, boolean mul)
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
        
	return new FTest(ssb, ny - 1, ssr, (ny - 1)
		* (period - 1));
    }

    /**
     * 
     * @param ts
     * @param period
     * @return
     */
    public static FTest stableSeasonality(DoubleSeq ts, int period)
    {
	// compute mean
	double mm = DeprecatedDoubles.average(ts);

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
	return new FTest(SSM, period - 1, SSR,
		n - period);
    }

}
