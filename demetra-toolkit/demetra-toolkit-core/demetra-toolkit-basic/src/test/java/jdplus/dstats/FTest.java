/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.dstats;

import demetra.dstats.DStatException;
import demetra.dstats.Distribution;
import demetra.stats.ProbabilityType;
import java.util.Random;
import jdplus.random.JdkRNG;
import org.assertj.core.api.Assertions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;
import demetra.dstats.RandomNumberGenerator;

/**
 *
 * @author laurent jadoul
 */
public class FTest {

    public static final double TOLERANCE = 0.1;

    public FTest() {
    }

    @Test
    public void testGetProbabilityF_4_10() {
        F f = new F(4, 10);
        for (double i = 0.01; i < 6375.22; i += .01) {
            assertThat(f.getProbability(i, ProbabilityType.Lower))
                    .as("Value i = %s", i)
                    .isNotNaN()
                    .isGreaterThan(0d)
                    .isLessThan(1d);
        }
    }

    @Test
    public void testGetProbabilityF_10_20() {
        F f = new F(10, 20);
        for (double i = 0.01; i < 165.78; i += .01) {
            assertThat(f.getProbability(i, ProbabilityType.Lower))
                    .as("Value i = %s", i)
                    .isNotNaN()
                    .isGreaterThan(0d)
                    .isLessThan(1d);
        }
    }

    @Test
    public void testGetProbabilityF_10_50() {
        F f = new F(10, 50);
        for (double i = 0.01; i < 27.6; i += .01) {
            assertThat(f.getProbability(i, ProbabilityType.Lower))
                    .as("Value i = %s", i)
                    .isNotNaN()
                    .isGreaterThan(0d)
                    .isLessThan(1d);
        }
    }

    @Test
    // Bounds [0.02-8.11]
    public void testGetProbabilityF_20_200() {
        F f = new F(20, 200);
        for (double i = 0.02; i < 8.11; i += .001) {
            assertThat(f.getProbability(i, ProbabilityType.Lower))
                    .as("Value i = %s", i)
                    .isNotNaN()
                    .isGreaterThan(0d)
                    .isLessThan(1d);
        }
    }

    @Test
    public void testGetProbabilityInverseF_4_10() {
        F f = new F(4, 10);
        for (double i = 0.001; i < 1; i += 0.001) {
            double y = f.getProbabilityInverse(i, ProbabilityType.Lower);
            double z = f.getProbability(y, ProbabilityType.Lower);
            assertThat(i).isCloseTo(z, Assertions.within(i * TOLERANCE));
        }

        assertThatThrownBy(() -> f.getProbabilityInverse(Distribution.EPS_P - 1, ProbabilityType.Lower))
                .as("p < EPS_P")
                .isInstanceOf(DStatException.class);

        assertThatThrownBy(() -> f.getProbabilityInverse((1 - Distribution.EPS_P) - 1, ProbabilityType.Lower))
                .as("1 - p < EPS_P")
                .isInstanceOf(DStatException.class);
    }

    @Test
    public void testGetProbabilityInverseF_10_20() {
        F f = new F(10, 20);
        for (double i = 0.001; i < 1; i += 0.001) {
            double y = f.getProbabilityInverse(i, ProbabilityType.Lower);
            double z = f.getProbability(y, ProbabilityType.Lower);
            assertThat(i).isCloseTo(z, Assertions.within(i * TOLERANCE));
        }

        assertThatThrownBy(() -> f.getProbabilityInverse(Distribution.EPS_P - 1, ProbabilityType.Lower))
                .as("p < EPS_P")
                .isInstanceOf(DStatException.class);

        assertThatThrownBy(() -> f.getProbabilityInverse((1 - Distribution.EPS_P) - 1, ProbabilityType.Lower))
                .as("1 - p < EPS_P")
                .isInstanceOf(DStatException.class);
    }

    @Test
    public void testGetProbabilityInverseF_10_50() {
        F f = new F(10, 50);
        for (double i = 0.001; i < 1; i += 0.001) {
            double y = f.getProbabilityInverse(i, ProbabilityType.Lower);
            double z = f.getProbability(y, ProbabilityType.Lower);
            assertThat(i).isCloseTo(z, Assertions.within(i * TOLERANCE));
        }

        assertThatThrownBy(() -> f.getProbabilityInverse(Distribution.EPS_P - 1, ProbabilityType.Lower))
                .as("p < EPS_P")
                .isInstanceOf(DStatException.class);

        assertThatThrownBy(() -> f.getProbabilityInverse((1 - Distribution.EPS_P) - 1, ProbabilityType.Lower))
                .as("1 - p < EPS_P")
                .isInstanceOf(DStatException.class);
    }

    @Test
    public void testGetProbabilityInverseF_20_200() {
        F f = new F(20, 200);
        for (double i = 0.001; i < 1; i += 0.001) {
            double y = f.getProbabilityInverse(i, ProbabilityType.Lower);
            double z = f.getProbability(y, ProbabilityType.Lower);
            assertThat(i).isCloseTo(z, Assertions.within(i * TOLERANCE));
        }

        assertThatThrownBy(() -> f.getProbabilityInverse(Distribution.EPS_P - 1, ProbabilityType.Lower))
                .as("p < EPS_P")
                .isInstanceOf(DStatException.class);

        assertThatThrownBy(() -> f.getProbabilityInverse((1 - Distribution.EPS_P) - 1, ProbabilityType.Lower))
                .as("1 - p < EPS_P")
                .isInstanceOf(DStatException.class);
    }

    @Test
    public void testRandomExpectationF_4_10() {
        F f = new F(4, 10);
        int iterations = 10000;
        double sum = 0;
        double avg;
        RandomNumberGenerator rng = getRandomNumberGenerator();

        for (int i = 0; i < iterations; i++) {
            sum += f.random(rng);
        }
        avg = sum / iterations;
        assertThat(f.getExpectation()).isCloseTo(avg, Assertions.within(f.getExpectation() * TOLERANCE));
    }

    @Test
    public void testRandomExpectationF_10_20() {
        F f = new F(10, 20);
        int iterations = 10000;
        double sum = 0;
        double avg;
        RandomNumberGenerator rng = getRandomNumberGenerator();

        for (int i = 0; i < iterations; i++) {
            sum += f.random(rng);
        }
        avg = sum / iterations;
        assertThat(f.getExpectation()).isCloseTo(avg, Assertions.within(f.getExpectation() * TOLERANCE));
    }

    @Test
    public void testRandomExpectationF_10_50() {
        F f = new F(10, 50);
        int iterations = 10000;
        double sum = 0;
        double avg;
        RandomNumberGenerator rng = getRandomNumberGenerator();

        for (int i = 0; i < iterations; i++) {
            sum += f.random(rng);
        }
        avg = sum / iterations;
        assertThat(f.getExpectation()).isCloseTo(avg, Assertions.within(f.getExpectation() * TOLERANCE));
    }

    @Test
    public void testRandomExpectationF_20_200() {
        F f = new F(20, 200);
        int iterations = 10000;
        double sum = 0;
        double avg;
        RandomNumberGenerator rng = getRandomNumberGenerator();

        for (int i = 0; i < iterations; i++) {
            sum += f.random(rng);
        }
        avg = sum / iterations;
        assertThat(f.getExpectation()).isCloseTo(avg, Assertions.within(f.getExpectation() * TOLERANCE));
    }

    @Test
    public void testRandomVarianceF_4_10() {
        F f = new F(4, 10);
        int iterations = 20000;
        double sum = 0;
        double avg;
        double[] values = new double[iterations];
        RandomNumberGenerator rng = getRandomNumberGenerator();

        for (int i = 0; i < iterations; i++) {
            values[i] = f.random(rng);
            sum += values[i];
        }
        avg = sum / iterations;
        double variance = 0;
        for (int i = 0; i < iterations; i++) {
            variance += Math.pow((values[i] - avg), 2);
        }
        variance = variance / iterations;
        assertThat(f.getVariance()).isCloseTo(variance, Assertions.within(f.getVariance() * TOLERANCE));
    }

    @Test
    public void testRandomVarianceF_10_20() {
        F f = new F(10, 20);
        int iterations = 10000;
        double sum = 0;
        double avg;
        double[] values = new double[iterations];
        RandomNumberGenerator rng = getRandomNumberGenerator();

        for (int i = 0; i < iterations; i++) {
            values[i] = f.random(rng);
            sum += values[i];
        }
        avg = sum / iterations;
        double variance = 0;
        for (int i = 0; i < iterations; i++) {
            variance += Math.pow((values[i] - avg), 2);
        }
        variance = variance / iterations;
        assertThat(f.getVariance()).isCloseTo(variance, Assertions.within(f.getVariance() * TOLERANCE));
    }

    @Test
    public void testRandomVarianceF_10_50() {
        F f = new F(10, 50);
        int iterations = 10000;
        double sum = 0;
        double avg;
        double[] values = new double[iterations];
        RandomNumberGenerator rng = getRandomNumberGenerator();

        for (int i = 0; i < iterations; i++) {
            values[i] = f.random(rng);
            sum += values[i];
        }
        avg = sum / iterations;
        double variance = 0;
        for (int i = 0; i < iterations; i++) {
            variance += Math.pow((values[i] - avg), 2);
        }
        variance = variance / iterations;
        assertThat(f.getVariance()).isCloseTo(variance, Assertions.within(f.getVariance() * TOLERANCE));
    }

    @Test
    public void testRandomVarianceF_20_200() {
        F f = new F(20, 200);
        int iterations = 10000;
        double sum = 0;
        double avg;
        double[] values = new double[iterations];
        RandomNumberGenerator rng = getRandomNumberGenerator();

        for (int i = 0; i < iterations; i++) {
            values[i] = f.random(rng);
            sum += values[i];
        }
        avg = sum / iterations;
        double variance = 0;
        for (int i = 0; i < iterations; i++) {
            variance += Math.pow((values[i] - avg), 2);
        }
        variance = variance / iterations;
        assertThat(f.getVariance()).isCloseTo(variance, Assertions.within(f.getVariance() * TOLERANCE));
    }

    @Test
    public void testExpectationF_4_10() {
        F f = new F(4, 10);
        double sum = 0.0;
        int cnt = 0;
        for (double i = 0.01; i < 1; i += 0.01) {
            sum += f.getProbabilityInverse(i, ProbabilityType.Lower);
            cnt++;
        }
        assertThat(f.getExpectation()).isCloseTo(sum / cnt, Assertions.within(f.getExpectation() * TOLERANCE));
    }

    @Test
    public void testExpectationF_10_20() {
        F f = new F(10, 20);
        double sum = 0.0;
        int cnt = 0;
        for (double i = 0.01; i < 1; i += 0.01) {
            sum += f.getProbabilityInverse(i, ProbabilityType.Lower);
            cnt++;
        }
        assertThat(f.getExpectation()).isCloseTo(sum / cnt, Assertions.within(f.getExpectation() * TOLERANCE));
    }

    @Test
    public void testExpectationF_10_50() {
        F f = new F(10, 50);
        double sum = 0.0;
        int cnt = 0;
        for (double i = 0.01; i < 1; i += 0.01) {
            sum += f.getProbabilityInverse(i, ProbabilityType.Lower);
            cnt++;
        }
        assertThat(f.getExpectation()).isCloseTo(sum / cnt, Assertions.within(f.getExpectation() * TOLERANCE));
    }

    @Test
    public void testExpectationF_20_200() {
        F f = new F(20, 200);
        double sum = 0.0;
        int cnt = 0;
        for (double i = 0.01; i < 1; i += 0.01) {
            sum += f.getProbabilityInverse(i, ProbabilityType.Lower);
            cnt++;
        }
        assertThat(f.getExpectation()).isCloseTo(sum / cnt, Assertions.within(f.getExpectation() * TOLERANCE));
    }

    @Test
    public void testVarianceF_4_10() {
        F f = new F(4, 10);
        double sum = 0.0;
        int cnt = 0;
        double[] values = new double[9999];
        for (int i = 0; i < values.length; i++) {
            double d = 0.0001 + 0.0001 * i;
            values[cnt] = f.getProbabilityInverse(d, ProbabilityType.Lower);
            sum += values[cnt];
            cnt++;
        }
        double avg = sum / cnt;
        double variance = 0;
        for (int i = 0; i < values.length; i++) {
            variance += Math.pow((values[i] - avg), 2);
        }
        variance = variance / values.length;
        assertThat(f.getVariance()).isCloseTo(variance, Assertions.within(f.getVariance() * TOLERANCE));
    }

    @Test
    public void testVarianceF_10_20() {
        F f = new F(10, 20);
        double sum = 0.0;
        int cnt = 0;
        double[] values = new double[9999];
        for (int i = 0; i < values.length; i++) {
            double d = 0.0001 + 0.0001 * i;
            values[cnt] = f.getProbabilityInverse(d, ProbabilityType.Lower);
            sum += values[cnt];
            cnt++;
        }
        double avg = sum / cnt;
        double variance = 0;
        for (int i = 0; i < values.length; i++) {
            variance += Math.pow((values[i] - avg), 2);
        }
        variance = variance / values.length;
        assertThat(f.getVariance()).isCloseTo(variance, Assertions.within(f.getVariance() * TOLERANCE));
    }

    @Test
    public void testVarianceF_10_50() {
        F f = new F(10, 50);
        double sum = 0.0;
        int cnt = 0;
        double[] values = new double[9999];
        for (int i = 0; i < values.length; i++) {
            double d = 0.0001 + 0.0001 * i;
            values[cnt] = f.getProbabilityInverse(d, ProbabilityType.Lower);
            sum += values[cnt];
            cnt++;
        }
        double avg = sum / cnt;
        double variance = 0;
        for (int i = 0; i < values.length; i++) {
            variance += Math.pow((values[i] - avg), 2);
        }
        variance = variance / values.length;
        assertThat(f.getVariance()).isCloseTo(variance, Assertions.within(f.getVariance() * TOLERANCE));
    }

    @Test
    public void testVarianceF_20_200() {
        F f = new F(20, 200);
        double sum = 0.0;
        int cnt = 0;
        double[] values = new double[9999];
        for (int i = 0; i < values.length; i++) {
            double d = 0.0001 + 0.0001 * i;
            values[cnt] = f.getProbabilityInverse(d, ProbabilityType.Lower);
            sum += values[cnt];
            cnt++;
        }
        double avg = sum / cnt;
        double variance = 0;
        for (int i = 0; i < values.length; i++) {
            variance += Math.pow((values[i] - avg), 2);
        }
        variance = variance / values.length;
        assertThat(f.getVariance()).isCloseTo(variance, Assertions.within(f.getVariance() * TOLERANCE));
    }

    private RandomNumberGenerator getRandomNumberGenerator() {
        return JdkRNG.newRandom(0);
    }

}
