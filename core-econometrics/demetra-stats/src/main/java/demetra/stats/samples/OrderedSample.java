/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.stats.samples;

import demetra.data.DoubleSequence;
import java.util.function.IntToDoubleFunction;
import java.util.stream.DoubleStream;
import demetra.data.DoubleSequence;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface OrderedSample extends Sample {

    /**
     * Gets the data
     * @return 
     */
    DoubleSequence data();

    /**
     * Computes the auto-covariance at the given lag 
     * @param lag
     * @return 
     */
    double autoCovariance(int lag);

    /**
     * Gets the auto-correlation function. The function is valid from 1 to m,
     * where m is less than the underlying sample
     * @return 
     */
    default IntToDoubleFunction autoCorrelationFunction() {
        final double v = variance();
        return i -> autoCovariance(i) / v;
    }

    default IntToDoubleFunction autoCovarianceFunction() {
        return i -> autoCovariance(i);
    }

    /**
     * Retrieves the  auto-correlations, up to lag n (included)
     *
     * @param n The max lag
     * @return An array of n elements, containing the autocorrelations at lags 1...n
     */
    default double[] autoCorrelations(int n) {
        double[] ac = new double[n];
        final double v = variance();
        for (int i = 0; i < n; ++i) {
            ac[i] = autoCovariance(i + 1) / v;
        }
        return ac;
    }

    /**
     * Retrieves the partial auto-correlations, up to lag n (included)
     *
     * @param n The max lag
     * @return An array of n elements, containing the partial autocorrelations at lags 1...n
     */
    default double[] partialAutoCorrelations(int n) {
        return partialAutoCorrelations(n, null);
    }

    /**
     * Retrieves the partial auto-corrrelations, up to lag n (included)
     *
     * @param n
     * @param coeff Output buffer. It will contain the coefficients of the auto-regressive model,
     * computed by means of the Levinson algorithm
     * @return
     */
    default double[] partialAutoCorrelations(int n, double[] coeff) {
        double[] ac = autoCorrelations(n);
        double[] pac = new double[n];
        double[] tmp = new double[n];

        if (coeff == null) {
            coeff = new double[n];
        }

        pac[0] = coeff[0] = ac[0]; // K = 1
        for (int K = 2; K <= n; ++K) {
            double m = 0, d = 0;
            for (int k = 1; k <= K - 1; ++k) {
                double x = coeff[k - 1];
                m += ac[K - k - 1] * x;
                d += ac[k - 1] * x;
            }
            pac[K - 1] = coeff[K - 1] = (ac[K - 1] - m) / (1 - d);

            for (int i = 0; i < K; ++i) {
                tmp[i] = coeff[i];
            }
            for (int j = 1; j <= K - 1; ++j) {
                coeff[j - 1] = tmp[j - 1] - tmp[K - 1] * tmp[K - j - 1];
            }
        }
        return pac;
    }

    @Override
    default double variance() {
        return autoCovariance(0);
    }

    @Override
    default DoubleStream all() {
        return data().stream();
    }

}
