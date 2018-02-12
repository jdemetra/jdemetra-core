/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.regarima.outlier;

import demetra.data.DoubleSequence;
import static demetra.data.Doubles.ssq;
import demetra.dstats.Normal;
import demetra.dstats.ProbabilityType;
import java.util.Arrays;

/**
 *
 * @author Jean Palate
 */
public interface IRobustStandardDeviationComputer {

    /**
     *
     * @param data
     * @return
     */
    double compute(DoubleSequence data);

    public static IRobustStandardDeviationComputer mad() {
        return new Mad2(50);
    }
    /**
     *
     * @param mediancorrected
     * @return
     */
    public static IRobustStandardDeviationComputer mad(boolean mediancorrected) {
        return mediancorrected ? new Mad2(50) : new Mad(50);
    }

    /**
     *
     * @param centile
     * @param mediancorrected
     * @return
     */
    public static IRobustStandardDeviationComputer mad(int centile, boolean mediancorrected) {
        return mediancorrected ? new Mad2(centile) : new Mad(centile);
    }

    static class Mad implements IRobustStandardDeviationComputer {

        private final int centile;

        Mad(int centile) {
            this.centile = centile;
        }

        @Override
        public double compute(DoubleSequence e) {
            double[] a = e.toArray();
            int n = a.length;
            for (int i = 0; i < n; ++i) {
                a[i] = Math.abs(a[i]);
            }
            Arrays.sort(a);
            double m;
            int nm = (n+1) * centile / 100;
            double dnm = (n+1) * centile / 100.0;
            double dx = dnm - nm;
            if (dx < 1e-9) {
                m = a[nm-1];
            } else { // compute weighted approximation
                m = a[nm-1] * (1 - dx) + a[nm] * dx;
            }
            Normal normal = new Normal();
            double l = normal.getProbabilityInverse(0.5 + .005 * centile,
                    ProbabilityType.Lower);
            return m / l;
        }

    }

    static class Mad2 implements IRobustStandardDeviationComputer {

        private final int centile;

        Mad2(int centile) {
            this.centile = centile;
        }

        @Override
        public double compute(DoubleSequence data) {
            double[] e = data.toArray();
            int n = e.length;
            Arrays.sort(e);
            double median = 0;
            int n2 = n / 2;
            if (n2 * 2 == n) // n even
            {
                median = (e[n2 - 1] + e[n2]) / 2;
            } else {
                median = e[n2];
            }
            for (int i = 0; i < n; ++i) {
                if (e[i] >= median) {
                    e[i] -= median;
                } else {
                    e[i] = median - e[i];
                }
            }

            Arrays.sort(e);
            double m;
            int nm = (n+1) * centile / 100;
            double dnm = (n+1) * centile / 100.0;
            double dx = dnm - nm;
            if (dx < 1e-9) {
                m = e[nm-1];
            } else { // compute weighted approximation
                m = e[nm-1] * (1 - dx) + e[nm] * dx;
            }
            Normal normal = new Normal();
            double l = normal.getProbabilityInverse(0.5 + .005 * centile,
                    ProbabilityType.Lower);
            return m / l;
        }

    }

//    static class Rmse implements IRobustStandardDeviationComputer {
//
//        private double ss;
//        private DoubleSequence data;
//
//        @Override
//        public double compute(DoubleSequence e) {
//            data = e;
//            ss = ssq(e);
//            int n = data.length();
//            return Math.sqrt(ss / n);
//        }
//
//         public double get(int i) {
//            int n = data.length();
//            if (i >= 0 && i < n) {
//                double e = data.get(i);
//                return Math.sqrt((ss - e * e) / (n - 1));
//            } else {
//                return Math.sqrt(ss / (n - 1));
//            }
//        }
//
//    }
}
