/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.dstats;

import demetra.random.IRandomNumberGenerator;
import java.util.Random;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 *
 * @author Laurent Jadoul
 */
public class Chi2Test {

    public static final double TOLERANCE = 0.05;

    public Chi2Test() {
    }

    @Test
    public void testGetProbabilityChi2_4() {
        Chi2 c = new Chi2(4);
        assertThatThrownBy(() -> c.getProbability(-1, ProbabilityType.Lower))
                .isInstanceOf(DStatException.class);

        for (double i = 0; i < 100; i += .01) {
            assertThat(c.getProbability(i, ProbabilityType.Lower))
                    .as("Value i = %s", i)
                    .isNotNaN()
                    .isGreaterThanOrEqualTo(0d)
                    .isLessThanOrEqualTo(1d);
            assertThat(c.getProbability(i, ProbabilityType.Point))
                    .as("ProbabilityType.Point")
                    .isZero();
        }
    }

    @Test
    public void testGetProbabilityChi2_8() {
        Chi2 c = new Chi2(8);
        assertThatThrownBy(() -> c.getProbability(-1, ProbabilityType.Lower))
                .isInstanceOf(DStatException.class);

        for (double i = 0; i < 100; i += .01) {
            assertThat(c.getProbability(i, ProbabilityType.Lower))
                    .as("Value i = %s", i)
                    .isNotNaN()
                    .isGreaterThanOrEqualTo(0d)
                    .isLessThanOrEqualTo(1d);
            assertThat(c.getProbability(i, ProbabilityType.Point))
                    .as("ProbabilityType.Point")
                    .isZero();
        }
    }

    @Test
    public void testGetProbabilityChi2_20() {
        Chi2 c = new Chi2(20);
        assertThatThrownBy(() -> c.getProbability(-1, ProbabilityType.Lower))
                .isInstanceOf(DStatException.class);

        for (double i = 0; i < 100; i += .01) {
            assertThat(c.getProbability(i, ProbabilityType.Lower))
                    .as("Value i = %s", i)
                    .isNotNaN()
                    .isGreaterThanOrEqualTo(0d)
                    .isLessThanOrEqualTo(1d);
            assertThat(c.getProbability(i, ProbabilityType.Point))
                    .as("ProbabilityType.Point")
                    .isZero();
        }
    }

    @Test
    public void testGetProbabilityInverseChi2_4() {
        Chi2 c = new Chi2(4);
        for (double i = 0.001; i < 1; i += 0.001) {
            double y = c.getProbabilityInverse(i, ProbabilityType.Lower);
            double z = c.getProbability(y, ProbabilityType.Lower);
            assertThat(i).isCloseTo(z, Assertions.within(i * TOLERANCE));
        }

        assertThatThrownBy(() -> c.getProbabilityInverse(IDistribution.EPS_P - 1, ProbabilityType.Lower))
                .as("p < EPS_P")
                .isInstanceOf(DStatException.class);

        assertThatThrownBy(() -> c.getProbabilityInverse((1 - IDistribution.EPS_P) - 1, ProbabilityType.Lower))
                .as("1 - p < EPS_P")
                .isInstanceOf(DStatException.class);
    }

    @Test
    public void testGetProbabilityInverseChi2_8() {
        Chi2 c = new Chi2(8);
        for (double i = 0.001; i < 1; i += 0.001) {
            double y = c.getProbabilityInverse(i, ProbabilityType.Lower);
            double z = c.getProbability(y, ProbabilityType.Lower);
            assertThat(i).isCloseTo(z, Assertions.within(i * TOLERANCE));
        }

        assertThatThrownBy(() -> c.getProbabilityInverse(IDistribution.EPS_P - 1, ProbabilityType.Lower))
                .as("p < EPS_P")
                .isInstanceOf(DStatException.class);

        assertThatThrownBy(() -> c.getProbabilityInverse((1 - IDistribution.EPS_P) - 1, ProbabilityType.Lower))
                .as("1 - p < EPS_P")
                .isInstanceOf(DStatException.class);
    }

    @Test
    public void testGetProbabilityInverseChi2_20() {
        Chi2 c = new Chi2(20);
        for (double i = 0.001; i < 1; i += 0.001) {
            double y = c.getProbabilityInverse(i, ProbabilityType.Lower);
            double z = c.getProbability(y, ProbabilityType.Lower);
            assertThat(i).isCloseTo(z, Assertions.within(i * TOLERANCE));
        }

        assertThatThrownBy(() -> c.getProbabilityInverse(IDistribution.EPS_P - 1, ProbabilityType.Lower))
                .as("p < EPS_P")
                .isInstanceOf(DStatException.class);

        assertThatThrownBy(() -> c.getProbabilityInverse((1 - IDistribution.EPS_P) - 1, ProbabilityType.Lower))
                .as("1 - p < EPS_P")
                .isInstanceOf(DStatException.class);
    }

    @Test
    public void testRandomExpectationChi2_4() {
        Chi2 c = new Chi2(4);
        int iterations = 10000;
        double sum = 0;
        double avg;
        IRandomNumberGenerator rng = getRandomNumberGenerator();

        for (int i = 0; i < iterations; i++) {
            sum += c.random(rng);
        }
        avg = sum / iterations;
        assertThat(c.getExpectation()).isCloseTo(avg, Assertions.within(c.getExpectation() * TOLERANCE));
    }

    @Test
    public void testRandomExpectationChi2_8() {
        Chi2 c = new Chi2(8);
        int iterations = 10000;
        double sum = 0;
        double avg;
        IRandomNumberGenerator rng = getRandomNumberGenerator();

        for (int i = 0; i < iterations; i++) {
            sum += c.random(rng);
        }
        avg = sum / iterations;
        assertThat(c.getExpectation()).isCloseTo(avg, Assertions.within(c.getExpectation() * TOLERANCE));
    }

    @Test
    public void testRandomExpectationChi2_20() {
        Chi2 c = new Chi2(20);
        int iterations = 10000;
        double sum = 0;
        double avg;
        IRandomNumberGenerator rng = getRandomNumberGenerator();

        for (int i = 0; i < iterations; i++) {
            sum += c.random(rng);
        }
        avg = sum / iterations;
        assertThat(c.getExpectation()).isCloseTo(avg, Assertions.within(c.getExpectation() * TOLERANCE));
    }

    @Test
    public void testRandomVarianceChi2_4() {
        int iterations = 10000;
        Chi2 c = new Chi2(4);
        IRandomNumberGenerator rng = getRandomNumberGenerator();
        double[] values = new double[iterations];
        double sum = 0, avg;

        for (int i = 0; i < iterations; i++) {
            values[i] = c.random(rng);
            sum += values[i];
        }
        avg = sum / iterations;

        double variance = 0;
        for (int i = 0; i < iterations; i++) {
            variance += Math.pow((values[i] - avg), 2);
        }
        variance = variance / iterations;
        assertThat(c.getVariance()).isCloseTo(variance, Assertions.within(c.getVariance() * TOLERANCE));
    }

    @Test
    public void testRandomVarianceChi2_8() {
        int iterations = 10000;
        Chi2 c = new Chi2(8);
        IRandomNumberGenerator rng = getRandomNumberGenerator();
        double[] values = new double[iterations];
        double sum = 0, avg;

        for (int i = 0; i < iterations; i++) {
            values[i] = c.random(rng);
            sum += values[i];
        }
        avg = sum / iterations;

        double variance = 0;
        for (int i = 0; i < iterations; i++) {
            variance += Math.pow((values[i] - avg), 2);
        }
        variance = variance / iterations;
        assertThat(c.getVariance()).isCloseTo(variance, Assertions.within(c.getVariance() * TOLERANCE));
    }

    @Test
    public void testRandomVarianceChi2_20() {
        int iterations = 10000;
        Chi2 c = new Chi2(20);
        IRandomNumberGenerator rng = getRandomNumberGenerator();
        double[] values = new double[iterations];
        double sum = 0, avg;

        for (int i = 0; i < iterations; i++) {
            values[i] = c.random(rng);
            sum += values[i];
        }
        avg = sum / iterations;

        double variance = 0;
        for (int i = 0; i < iterations; i++) {
            variance += Math.pow((values[i] - avg), 2);
        }
        variance = variance / iterations;
        assertThat(c.getVariance()).isCloseTo(variance, Assertions.within(c.getVariance() * TOLERANCE));
    }

    private IRandomNumberGenerator getRandomNumberGenerator() {
        return new IRandomNumberGenerator() {
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
            public IRandomNumberGenerator synchronize() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
    }
}
