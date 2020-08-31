/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.stats.tests;

import jdplus.math.functions.CubicSpline;
import jdplus.stats.tests.DickeyFuller.DickeyFullerType;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class DickeyFullerTable {

    private final double[][] T_NC = new double[][]{
        new double[]{-3.59, -2.66, -2.26, -1.95, -1.61, 0.92, 1.33, 1.70, 2.16, 3.12},
        new double[]{-3.42, -2.62, -2.25, -1.95, -1.61, 0.91, 1.31, 1.66, 2.08, 2.97},
        new double[]{-3.35, -2.60, -2.24, -1.95, -1.61, 0.90, 1.29, 1.64, 2.03, 2.88},
        new double[]{-3.31, -2.58, -2.23, -1.95, -1.61, 0.89, 1.29, 1.63, 2.01, 2.85},
        new double[]{-3.29, -2.57, -2.23, -1.95, -1.61, 0.89, 1.28, 1.62, 2.01, 2.83},
        new double[]{-3.28, -2.57, -2.23, -1.94, -1.61, 0.89, 1.28, 1.62, 2.01, 2.82}};
    private final double[][] T_C = new double[][]{
        new double[]{-4.73, -3.75, -3.33, -3.00, -2.62, -0.37, 0, 0.34, 0.72, 1.57},
        new double[]{-4.37, -3.58, -3.22, -2.93, -2.60, -0.40, -0.03, 0.29, 0.66, 1.46},
        new double[]{-4.23, -3.51, -3.17, -2.89, -2.58, -0.42, -0.05, 0.26, 0.63, 1.41},
        new double[]{-4.15, -3.46, -3.14, -2.88, -2.57, -0.42, -0.06, 0.24, 0.62, 1.39},
        new double[]{-4.12, -3.44, -3.13, -2.87, -2.57, -0.43, -0.07, 0.24, 0.61, 1.38},
        new double[]{-4.10, -3.43, -3.12, -2.86, -2.57, -0.44, -0.08, 0.24, 0.61, 1.38}};
    private final double[][] T_CT = new double[][]{
        new double[]{-5.44, -4.38, -3.95, -3.6, -3.24, -1.14, -0.8, -0.5, -0.15, 0.61},
        new double[]{-4.97, -4.15, -3.8, -3.5, -3.18, -1.19, -0.87, -0.58, -0.24, 0.49},
        new double[]{-4.77, -4.04, -3.73, -3.45, -3.15, -1.22, -0.9, -0.62, -0.28, 0.43},
        new double[]{-4.67, -3.99, -3.69, -3.43, -3.13, -1.23, -0.92, -0.64, -0.31, 0.40},
        new double[]{-4.63, -3.98, -3.68, -3.42, -3.13, -1.24, -0.93, -0.65, -0.31, 0.40},
        new double[]{-4.61, -3.96, -3.67, -3.41, -3.13, -1.24, -0.94, -0.66, -0.32, 0.39}};

    private final double[][] Z_NC = new double[][]{
        new double[]{-17.1, -11.9, -9.3, -7.3, -5.3, +1.01, +1.40, +1.79, +2.28, 3.50},
        new double[]{-19.4, -12.9, -9.9, -7.7, -5.5, +0.97, +1.35, +1.70, +2.16, 3.29},
        new double[]{-20.6, -13.3, -10.2, -7.9, -5.6, +0.95, +1.31, +1.65, +2.09, 3.20},
        new double[]{-21.6, -13.6, -10.3, -8.0, -5.7, +0.93, +1.29, +1.62, +2.04, 3.13},
        new double[]{-21.9, -13.7, -10.4, -8.0, -5.7, +0.93, +1.29, +1.61, +2.04, 3.12},
        new double[]{-22.1, -13.7, -10.4, -8.0, -5.7, +0.93, +1.29, +1.61, +2.03, 3.10}};

    private final double[][] Z_C = new double[][]{
        new double[]{-22.0, -17.2, -14.6, -12.5, -10.2, -0.76, +0.01, +0.65, +1.40, 3.00},
        new double[]{-25.7, -18.9, -15.7, -13.3, -10.7, -0.81, -0.07, +0.53, +1.22, 2.70},
        new double[]{-27.6, -19.8, -16.3, -13.7, -11.0, -0.83, -0.10, +0.47, +1.14, 2.53},
        new double[]{-29.0, -20.3, -16.6, -14.0, -11.2, -0.84, -0.12, +0.43, +1.09, 2.47},
        new double[]{-29.4, -20.5, -16.8, -14.0, -11.2, -0.84, -0.13, +0.42, +1.06, 2.44},
        new double[]{-29.8, -20.6, -16.9, -14.1, -11.2, -0.85, -0.14, +0.41, +1.05, 2.42}};

    private final double[][] Z_CT = new double[][]{
        new double[]{-26.9, -22.5, -19.9, -17.9, -15.6, -3.66, -2.51, -1.53, -0.43, 1.76},
        new double[]{-32.5, -25.7, -22.4, -19.8, -16.8, -3.71, -2.60, -1.66, -0.65, 1.29},
        new double[]{-35.9, -27.4, -23.6, -20.7, -17.5, -3.74, -2.62, -1.73, -0.75, 1.13},
        new double[]{-38.2, -28.4, -24.4, -21.3, -18.0, -3.75, -2.64, -1.78, -0.82, 1.03},
        new double[]{-39.0, -28.9, -24.8, -21.5, -18.1, -3.76, -2.65, -1.78, -0.84, 0.99},
        new double[]{-39.6, -29.2, -25.0, -21.7, -18.2, -3.76, -2.66, -1.79, -0.85, 0.98}};

    private final double[] NOBS = new double[]{25, 50, 100, 250, 500, 1000};
    private final double[] PROB = new double[]{0.001, 0.01, 0.025, 0.05, 0.1, 0.9, 0.95, 0.975, 0.99, 0.999};

    private double[][] adfTable(DickeyFullerType type, boolean z) {
        if (z) {
            switch (type) {
                case NC:
                    return Z_NC;
                case C:
                    return Z_C;
                default:
                    return Z_CT;
            }
        } else {
            switch (type) {
                case NC:
                    return T_NC;
                case C:
                    return T_C;
                default:
                    return T_CT;
            }
        }
    }

    public double probability3(int n, double q, DickeyFullerType type) {

        double[][] adfTable = adfTable(type, false);

        // for each n, find the suitable p
        double[] pd = new double[NOBS.length];
        for (int i = 0; i < pd.length; ++i) {
            double pi = CubicSpline.monotonic(adfTable[i], PROB).applyAsDouble(q);
            if (pi < 0) {
                pi = 0;
            }
            if (pi > 1) {
                pi = 1;
            }
            pd[i] = pi;
        }
        double p = CubicSpline.monotonic(NOBS, pd).applyAsDouble(n);
        if (p < 0) {
            return 0;
        }
        if (p > 1) {
            return 1;
        }
        return p;
    }

    public double probability2(int n, double q, DickeyFullerType type) {

        double[][] adfTable = adfTable(type, false);

        // for each n, find the suitable p
        double[] pd = new double[NOBS.length];
        for (int i = 0; i < pd.length; ++i) {
            double pi = CubicSpline.of(adfTable[i], PROB).applyAsDouble(q);
            if (pi < 0) {
                pi = 0;
            }
            if (pi > 1) {
                pi = 1;
            }
            pd[i] = pi;
        }
        double p = CubicSpline.of(NOBS, pd).applyAsDouble(n);
        if (p < 0) {
            return 0;
        }
        if (p > 1) {
            return 1;
        }
        return p;
    }

    private final double[] PROBL;

    static {
        PROBL = PROB.clone();
        for (int i = 0; i < PROBL.length; ++i) {
            PROBL[i] = -Math.log(1 / PROBL[i] - 1);
        }
    }

    public double probability(int n, double q, DickeyFullerType type, boolean ztest) {

        double[][] adfTable = adfTable(type, ztest);

        // for each n, find the suitable p
        double[] pd = new double[NOBS.length];
        for (int i = 0; i < pd.length; ++i) {
            pd[i] = CubicSpline.of(adfTable[i], PROBL).applyAsDouble(q);
        }
        double p = CubicSpline.of(NOBS, pd).applyAsDouble(n);
        return Math.exp(p) / (1 + Math.exp(p));
    }

}
