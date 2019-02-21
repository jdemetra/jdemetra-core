/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11.extremevaluecorrector;

import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import demetra.design.VisibleForTesting;
import demetra.dstats.F;
import demetra.dstats.ProbabilityType;
import demetra.sa.DecompositionMode;
import demetra.x11.X11Context;
import lombok.AccessLevel;

/**
 *
 * @author Christiane Hofer
 */
public class Cochran {

    public Cochran(DoubleSequence ds, X11Context context) {
        input = DataBlock.of(ds);
        isMulti = (context.getMode() == DecompositionMode.Multiplicative || context.getMode() == DecompositionMode.PseudoAdditive);
        period = context.getPeriod();
        standardDeviation = new double[period]; //original PSP first remains empty 0,...,Ny-1
        calcCochranTest();
    }

    private final DataBlock input;
    private final boolean isMulti;
    private final int period;

    /**
     * @return standard deviation for each period
     */
    @lombok.Getter(AccessLevel.PACKAGE)
    @VisibleForTesting
    private final double[] standardDeviation;
    @lombok.Getter
    private double testValue;

    /**
     * @return critical value used
     */
    @lombok.Getter
    private double criticalValue;

    /**
     * true if the test can not be rejected
     */
    private boolean result = true;

    /**
     * @return minimun numbers of years, this is the number of values per period taken into account when the Cochran test is calculated
     */
    @lombok.Getter
    private int minNumberOfYears = 0;

    /**
     * Critical values for monthly data
     */
    private static final double[] T12 = {0.5410, 0.3934, 0.3264, 0.2880, 0.2624, 0.2439, 0.2299, 0.2187, 0.2098, 0.2020, 0.1980, 0.194, 0.186, 0.182,
        0.178, 0.174, 0.17, 0.166, 0.162, 0.158, 0.15, 0.15, 0.15, 0.15, 0.15,
        0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.1403, 0.14,
        0.14, 0.14, 0.14};

    /**
     * Critical values for quarterly data
     */
    private static final double[] T4 = {0.9065, 0.7679, 0.6841, 0.6287, 0.5895, 0.5598, 0.5365, 0.5175,
        0.5017, 0.4884, 0.480, 0.471, 0.463, 0.454, 0.445, 0.4366,
        0.433, 0.430, 0.427, 0.424, 0.421, 0.417, 0.414, 0.411, 0.408,
        0.404, 0.401, 0.398, 0.395, 0.391, 0.388, 0.385, 0.382, 0.379,
        0.375, 0.3720, 0.369, 0.366, 0.362, 0.359};

    /**
     * Critical values for halfyearly data
     */
    private static final double[] T2 = {0.9985, 0.975, 0.9392, 0.9057, 0.8772, 0.8534, 0.8332, 0.8159, 0.801, 0.788, 0.7765,
        0.7662, 0.757, 0.7487, 0.7411, 0.7341, 0.7278, 0.7219, 0.7164, 0.7114, 0.7066, 0.7022, 0.698, 0.6941, 0.6904, 0.6869, 0.6836, 0.6805, 0.6775, 0.6747, 0.672, 0.6694, 0.6669,
        0.6646, 0.6623, 0.6601, 0.658, 0.656, 0.6541, 0.6522};

    /**
     * The critical values C are calculated for up to 41 years from C=[1+(N-1)/(F(0.05/N;n-1;(N-1)(n-1)))]^(-1) N=number of observation per year n= number of years F distribution
     */
    private double C(int numberOfYears) {
        F f = new F(numberOfYears + 1, (period - 1) * (numberOfYears + 1));
        double C = 1.0 / (1.0 + ((period - 1.0) / f.getProbabilityInverse(0.05 / period, ProbabilityType.Upper)));
        return C;
    }

    ;

    /**
     * Calculates the Cochran test for a given Timeseries, and sets result to false if the
     * Nullhypothesis of equal variances of the periods has to be rejected and
     * different standard deviations should be used for outlier detection
     */
    private void calcCochranTest() {
        int n1; //number of values in a periode eg. in January
        int nmin; // minimal number of observations of a period
        double smax; //max standarddeviation of periods
        double st;// theoretical mean

        testValue = 0;
        smax = -10.0;
        nmin = 100;

        if (isMulti) {
            st = 1;
        } else {
            st = 0;
        }

        for (int i = 0; i <= period - 1; i++) {
            DataBlock dsPeriod = input.extract(i, -1, period);
            standardDeviation[i] = 0;
            standardDeviation[i] = dsPeriod.ssqcWithMissing(st);
            n1 = dsPeriod.count(y -> !Double.isNaN(y));
            standardDeviation[i] = standardDeviation[i] / n1;

            if (nmin > n1 - 2) {
                nmin = n1 - 2;
            }

            if (smax < standardDeviation[i]) {
                smax = standardDeviation[i];
            }
            testValue += standardDeviation[i];

        }

        if (!(testValue == 0)) {
            testValue = smax / testValue;
        }
        minNumberOfYears = nmin + 1;
        if (nmin > 39) {
            nmin = 39;
        }

        switch (period) {
            case 12:
                criticalValue = T12[nmin];
                break;
            case 4:
                criticalValue = T4[nmin];
                break;
            case 2:
                criticalValue = T2[nmin];
                break;
            default:
                criticalValue = C(nmin);
                break;
        }

        if (testValue >= criticalValue) {
            result = false;
        }

    }

    /**
     * @return true if CriticalValue > TestValue; Nullhypothesis for identical variance in each period has to be rejected, and different variances should be used
     */
    public boolean getTestResult() {
        return result;
    }
}
