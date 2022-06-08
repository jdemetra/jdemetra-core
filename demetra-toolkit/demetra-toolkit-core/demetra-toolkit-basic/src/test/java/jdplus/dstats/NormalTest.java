/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.dstats;

import demetra.stats.ProbabilityType;
import java.util.Random;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import demetra.dstats.RandomNumberGenerator;

/**
 *
 * @author Laurent Jadoul
 */
public class NormalTest {

    public static final double TOLERANCE = 0.05;

    public NormalTest() {
    }

    @Test
    public void testGetProbability() {
        Normal n = new Normal(0, 100);
        for (double i = -100; i < 100; i += .0001) {
            assertThat(n.getProbability(i, ProbabilityType.Lower))
                    .as("Value i = %s", i)
                    .isNotNaN()
                    .isGreaterThan(0d)
                    .isLessThan(1d);
        }
    }

    @Test
    public void testGetProbabilityInverse() {
        // Ne semble fonctionner que pour un Normal(0,1);
        Normal n = new Normal(1, 3);
        for (double i = 0.001; i < 1; i += 0.001) {
            double y = n.getProbabilityInverse(i, ProbabilityType.Lower);
            double z = n.getProbability(y, ProbabilityType.Lower);
            assertThat(i).isCloseTo(z, Assertions.within(i * TOLERANCE));
        }
    }

    @Test
    public void testExpectation() {
        int iterations = 10000;
        Normal n = new Normal(0, 1);
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
        Normal n = new Normal();
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
        return new RandomNumberGenerator() {
            @Override
            public double nextDouble() {
                return new Random().nextDouble();
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
