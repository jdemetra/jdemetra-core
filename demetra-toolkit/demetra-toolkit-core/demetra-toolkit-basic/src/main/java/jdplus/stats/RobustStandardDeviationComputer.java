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
package jdplus.stats;

import jdplus.dstats.Normal;
import demetra.stats.ProbabilityType;
import java.util.Arrays;
import demetra.data.DoubleSeq;
import demetra.design.Immutable;

/**
 *
 * @author Jean Palate
 */
@FunctionalInterface
public interface RobustStandardDeviationComputer {

    /**
     *
     * @param data
     * @return
     */
    double compute(DoubleSeq data);

    public static RobustStandardDeviationComputer mad() {
        return Mad2.DEFAULT;
    }
    /**
     *
     * @param mediancorrected
     * @return
     */
    public static RobustStandardDeviationComputer mad(boolean mediancorrected) {
        return mediancorrected ? Mad2.DEFAULT : Mad.DEFAULT;
    }

    /**
     *
     * @param centile
     * @param mediancorrected
     * @return
     */
    public static RobustStandardDeviationComputer mad(double centile, boolean mediancorrected) {
        return mediancorrected ? new Mad2(centile) : new Mad(centile);
    }

    @Immutable
    static final class Mad implements RobustStandardDeviationComputer {
        
        public static final Mad DEFAULT=new Mad(50);

        private final double centile;

        Mad(double centile) {
            this.centile = centile;
        }

        @Override
        public double compute(DoubleSeq e) {
            double[] a = e.toArray();
            int n = a.length;
            for (int i = 0; i < n; ++i) {
                a[i] = Math.abs(a[i]);
            }
            Arrays.sort(a);
            double m;
            double dnm = (n+1) * centile / 100.0;
            int nm = (int) dnm;
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

    @Immutable
    static final class Mad2 implements RobustStandardDeviationComputer {

        public static final Mad2 DEFAULT=new Mad2(50);

        private final double centile;

        Mad2(double centile) {
            this.centile = centile;
        }

        @Override
        public double compute(DoubleSeq data) {
            double[] e = data.toArray();
            int n = e.length;
            Arrays.sort(e);
            double median;
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
            double dnm = (n+1) * centile / 100.0;
            int nm = (int) dnm;
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

}
