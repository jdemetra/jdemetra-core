/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.dstats;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class FTest {

    public FTest() {
    }

    @Test
    public void testChi2Compare() {
        int K = 5;
        Chi2 chi2 = new Chi2();
        chi2.setDegreesofFreedom(K);
        double x = chi2.getProbabilityInverse(.99, ProbabilityType.Lower);

        for (int i = 100000; i < 100090; ++i) {
            F f = new F();
            f.setDFNum(K);
            f.setDFDenom(i - K);
            double n = i;
            double c = n * (n - K);
            c /= K * (n - 1) * (n + 1);
            double xf = f.getProbabilityInverse(.99, ProbabilityType.Lower);
            assertTrue(Math.abs(x-xf / c)<.01);

        }
    }

    @Test
    public void testProbinverse() {
        F dist = new F();
        dist.setDFNum(5);
        dist.setDFDenom(100);
        double step = 0.00001;
        for (double p = step; p <= 1 - step; p += step) {
            double x = dist.getProbabilityInverse(p, ProbabilityType.Lower);
            double np = dist.getProbability(x, ProbabilityType.Lower);
            double nx = dist.getProbabilityInverse(np, ProbabilityType.Lower);
            assertTrue(Math.abs(x-nx) <1e-6);
        }
    }
}
