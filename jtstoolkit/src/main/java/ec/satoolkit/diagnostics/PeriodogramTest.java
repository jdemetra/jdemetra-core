/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.satoolkit.diagnostics;

import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.Periodogram;
import ec.tstoolkit.dstats.Chi2;
import ec.tstoolkit.dstats.ProbabilityType;
import ec.tstoolkit.dstats.TestType;
import ec.tstoolkit.stats.StatisticalTest;

/**
 *
 * @author PCUser
 */
public class PeriodogramTest {

    private static final double D = 1.01;

    public static StatisticalTest computeSum(IReadDataBlock data, int freq) {
        Periodogram periodogram = new Periodogram(data);
        double[] seasfreqs = new double[(freq - 1) / 2];
        // seas freq in radians...
        for (int i = 0; i < seasfreqs.length; ++i) {
            seasfreqs[i] = (i + 1) * 2 * Math.PI / freq;
        }

        double[] p = periodogram.getS();
        double xsum = 0;
        double dstep = periodogram.getIntervalInRadians(), estep = dstep * D;
        int nf = 0;
        for (int i = 0; i < seasfreqs.length; ++i) {
            double f = seasfreqs[i];
            int j = (int) (seasfreqs[i] / dstep);
            if (f-(j-1)*dstep<estep){
                nf+=2;
                xsum+=p[j-1];
            }
            if (f-j*dstep<estep){
                nf+=2;
                xsum+=p[j];
            }
            if ((j+1)*dstep-f<estep){
                nf+=2;
                xsum+=p[j+1];
            }
        }
        if (freq % 2 == 0) {
            ++nf;
            xsum += p[p.length - 1];
        }
        Chi2 chi2 = new Chi2();
        chi2.setDegreesofFreedom(nf);
        return new StatisticalTest(chi2, xsum, TestType.Upper, true);
    }

    public static double computeMax(IReadDataBlock data, int freq) {
        Periodogram periodogram = new Periodogram(data);
        double[] seasfreqs = new double[(freq - 1) / 2];
        // seas freq in radians...
        for (int i = 0; i < seasfreqs.length; ++i) {
            seasfreqs[i] = (i + 1) * 2 * Math.PI / freq;
        }

        double[] p = periodogram.getS();
        double xmax = 0;
        double dstep = periodogram.getIntervalInRadians(), estep=dstep*D;
        int nf=0;
        for (int i = 0; i < seasfreqs.length; ++i) {
             double f = seasfreqs[i];
            int j = (int) (seasfreqs[i] / dstep);
            if (f-(j-1)*dstep<estep){
                ++nf;
                xmax=Math.max(xmax, p[j-1]);
            }
            if (f-j*dstep<estep){
                ++nf;
                xmax=Math.max(xmax, p[j]);
            }
            if ((j+1)*dstep-f<estep){
                ++nf;
                xmax=Math.max(xmax, p[j+1]);
            }
        }
        return 1 - Math.pow(1 - Math.exp(-xmax * .5), nf);
    }
}
