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

import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.dstats.Chi2;
import ec.tstoolkit.dstats.TestType;
import ec.tstoolkit.stats.StatisticalTest;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.Arrays;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class KruskalWallisTest extends StatisticalTest {

    /**
     * 
     * @param tsdata
     */
    public KruskalWallisTest(TsData tsdata)
    {
	int freq = tsdata.getFrequency().intValue();
	if (freq <= 1)
	    return;
	int pos = tsdata.getStart().getPosition();
	double[] data = tsdata.internalStorage();

	Item[] items = new Item[data.length];
	int N = 0;
	int[] nk = new int[freq];
	int j = 0;
	for (int i = 0; i < items.length; ++i) {
	    //
	    double d = data[i];
	    if (Double.isFinite(d)) {
		int k = (pos + i) % freq;
		items[j++] = new Item(k, d);
		nk[k]++;
	    }
	}
	N = j;
	Arrays.sort(items, 0, N);

	double[] S = new double[freq];

	for (int i = 0; i < N;) {
	    int j0 = i, j1 = i + 1;
	    while (j1 < N && items[j0].val == items[j1].val)
		++j1;
	    int n = j1 - j0;
	    if (n == 1)
		S[items[i].pos] += i + 1;
	    else {
		double dpos = j0 + .5 * (n + 1);
		for (int jcur = j0; jcur < j1; ++jcur)
		    S[items[jcur].pos] += dpos;
	    }
	    i = j1;
	}

	double h = 0;
	for (int i = 0; i < freq; ++i)
	    h += S[i] * S[i] / nk[i];
	h = 12 * h / (N * (N + 1)) - 3 * (N + 1);

	Chi2 chi2 = new Chi2();
	chi2.setDegreesofFreedom(freq - 1);
	m_dist = chi2;
	m_type = TestType.Upper;
	m_val = h;
    }
}
