/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.dstats;

import demetra.stats.ProbabilityType;
import java.util.Random;
import static jdplus.dstats.NormalTest.TOLERANCE;
import jdplus.random.RandomNumberGenerator;
import org.assertj.core.api.Assertions;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class LogNormalTest {
    
    public LogNormalTest() {
    }

    @Test
    public void testGetProbabilityInverse() {
        // Ne semble fonctionner que pour un Normal(0,1);
        LogNormal n = new LogNormal(1, 1);
        for (double i = 0.001; i < 1; i += 0.001) {
            double y = n.getProbabilityInverse(i, ProbabilityType.Lower);
            double z = n.getProbability(y, ProbabilityType.Lower);
            assertThat(i).isCloseTo(z, Assertions.within(i * TOLERANCE));
        }
    }

    @Test
    public void testExpectation() {
        int iterations = 10000;
        LogNormal n = new LogNormal(1, .5);
        RandomNumberGenerator rng = getRandomNumberGenerator();

        double sum = 0, avg;
        for (int i = 0; i < iterations; i++) {
            sum += n.random(rng);
        }
        avg = sum / iterations;
        assertThat(n.getExpectation()).isCloseTo(avg, Assertions.within(0.05));
    }

    @Test
    public void testVariance() {
        int iterations = 10000;
        LogNormal n = new LogNormal(1, .5 );
        RandomNumberGenerator rng = getRandomNumberGenerator();
        double[] values = new double[iterations];
        double sum = 0, avg;

        for (int i = 0; i < iterations; i++) {
            values[i] = n.random(rng);
            sum += values[i];
        }
        avg = sum / iterations;

        double variance = 0;
        for (int i = 0; i < iterations; i++) {
            variance += Math.pow((values[i] - avg), 2);
        }
        variance = variance / iterations;
        assertThat(n.getVariance()).isCloseTo(variance, Assertions.within(n.getVariance() * TOLERANCE));
    }
    
    private RandomNumberGenerator getRandomNumberGenerator() {
        
        Random rnd=new Random(0);
        
        return new RandomNumberGenerator() {
            @Override
            public double nextDouble() {
                return rnd.nextDouble();
            }

            @Override
            public int nextInt() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public long nextLong() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public boolean nextBoolean() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public float nextFloat() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public int nextInt(int n) throws IllegalArgumentException {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public RandomNumberGenerator synchronize() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
    }
}
