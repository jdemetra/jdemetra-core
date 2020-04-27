/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.revisions.parametric;

import demetra.data.DoubleSeq;
import demetra.revisions.parametric.Bias;
import java.util.Random;
import jdplus.dstats.Normal;
import jdplus.dstats.T;
import jdplus.random.MersenneTwister;
import jdplus.stats.samples.Population;
import jdplus.stats.samples.Sample;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class BiasComputerTest {

    public BiasComputerTest() {
    }

    @Test
    public void testSomeMethod() {
    }

    private static final MersenneTwister RND = MersenneTwister.fromSystemNanoTime();
    private static int K0 = 100;

    private static DoubleSeq generate(int size, double rho) {
        Normal N = new Normal();
        // x(t)=rho*x(t-1)+e
        // burning length==K = 100
        double x, xprev = 0;
        for (int i = 0; i < K0; ++i) {
            x = rho * xprev + N.random(RND);
            xprev = x;
        }
        double[] s = new double[size];
        for (int i = 0; i < size; ++i) {
            x = rho * xprev + N.random(RND);
            xprev = x;
            s[i] = x;
        }
        return DoubleSeq.of(s);
    }

    public static void main(String[] args) {
        int K = 1000000, N = 50;

        double rho = .1;
        do {
            double[] r0 = new double[K];
            double[] r1 = new double[K];
            for (int k = 0; k < K; ++k) {
                DoubleSeq sample = generate(N, rho);
                Bias bias = BiasComputer.of(sample);
                r0[k] = bias.getTPvalue();
                r1[k] = bias.getAdjustedTPvalue();
            }

            DoubleSeq R0 = DoubleSeq.of(r0);
            DoubleSeq R1 = DoubleSeq.of(r1);
            System.out.print(Sample.build(R0, true, Population.UNKNOWN).mean());
            System.out.print('\t');
            System.out.println(Sample.build(R1, true, Population.UNKNOWN).mean());
            rho += .05;
        } while (rho < .99);
        
        // should be 0.5
    }

}
