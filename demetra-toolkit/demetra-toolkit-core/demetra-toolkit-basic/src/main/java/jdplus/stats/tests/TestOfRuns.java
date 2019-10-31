/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.stats.tests;

import demetra.design.Development;
import jdplus.dstats.Chi2;
import jdplus.dstats.Normal;
import demetra.stats.DescriptiveStatistics;
import demetra.stats.StatException;
import demetra.data.DoubleSeq;


/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class TestOfRuns 
{
    private final DescriptiveStatistics stats;
    private boolean mean = true;
    
    public TestOfRuns(DoubleSeq data)
    {
        this.stats=DescriptiveStatistics.of(data);
    }
    
    public TestOfRuns(final DescriptiveStatistics stats)
    {
        this.stats=stats;
    }
   
    /**
     * Should we use mean or median
     * @param mean
     * @return 
     */
    public TestOfRuns useMean(boolean mean){
        if (this.mean == mean)
            return this;
        clear();
        this.mean=mean;
        return this;
    }

    private double refValue;
    private int plus, above, nruns;
    private int[] runLengths;
    private double[] obs;


    /**
     * 
     * @return
     */
    public boolean isUsingMean()
    {
	return mean;
    }

    /**
     * 
     * @return
     */
    public int getBelowNormalCount()
    {
	return above;
    }

    /**
     * 
     * @return
     */
    public int getAboveNormalCount()
    {
	return plus;
    }

    
    public double getReferenceValue(){
        return refValue;
    }
    
    private void clear(){
	plus = 0;
	above = 0;
	nruns = 0;
        obs=null;
        runLengths=null;
    }

    private void races() {
        if (runLengths != null)
            return;
	if (mean)
	    refValue = stats.getAverage();
	else
	    refValue = stats.getMedian();
	obs = stats.observations().toArray();
	runLengths = new int[obs.length];
 	int n = obs.length;
	if (n == 0)
	    throw new StatException(StatException.NO_DATA);
	boolean prev = obs[0] >= refValue;
	if (prev)
	    ++plus;
	else
	    ++above;
	nruns = 1;
	int curlength = 1;
	for (int i = 1; i < n; ++i) {
	    boolean cur = obs[i] >= refValue;
	    if (cur)
		++plus;
	    else
		++above;

	    if (cur != prev) {
		++nruns;
		prev = cur;
		++runLengths[curlength - 1];
		curlength = 1;
	    } else
		++curlength;
	}
	++runLengths[curlength - 1];
    }

    /**
     * 
     * @param length
     * @return
     */
    public int runsCount(final int length)
    {
	return (length <= 0) ? nruns : runLengths[length - 1];
    }

    public StatisticalTest testLength() {
	races();
	int n = obs.length;
	double x = 0, p = plus, m = above;
	double fp = p / n, fm = m / n, e = n / (p / m + m / p), xp = fm, xm = fp;
	for (int i = 0; i < n; ++i) {
	    xp *= fp;
	    xm *= fm;
	    // compute E(y=i+1)
	    double ei = e * (xp + xm);
	    if (runLengths[i] == 0)
		x += ei;
	    else if (ei != 0)
		x += (runLengths[i] - ei) / ei * (runLengths[i] - ei);
	    else
		x += 999999;
	}
	Chi2 dist = new Chi2(n);
        return new StatisticalTest(dist, x, TestType.Upper, true);
    }

    public StatisticalTest testNumber() {
	races();
	double n = obs.length;
	double mp = above * plus;
	double E = 1 + 2 * mp / n;
	double V = 2 * mp * (2 * mp - n) / (n * n * (n - 1));
	if (V < 1e-9)
	    V = 1e-9;
	Normal dist = new Normal();
       return new StatisticalTest(dist, (nruns - E) / Math.sqrt(V), TestType.TwoSided, true);
    }

}
