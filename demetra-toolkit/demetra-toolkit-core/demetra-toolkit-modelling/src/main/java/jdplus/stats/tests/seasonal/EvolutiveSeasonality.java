/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.stats.tests.seasonal;

import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import jdplus.stats.tests.AnovaTest;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class EvolutiveSeasonality {
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
    
}
