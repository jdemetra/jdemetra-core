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
package demetra.regarima.ami;

import jdplus.data.DataBlock;
import demetra.design.BuilderPattern;
import demetra.regarima.outlier.RobustStandardDeviationComputer;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
public class FastDifferencingModule implements IGenericDifferencingModule {

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(FastDifferencingModule.class)
    public static class Builder {

        public static final int MAXD = 2, MAXBD = 1;

        private int[] maxd = new int[]{2, 1};
        private double k = 1.2;
        private double tstat = 1.96;
        private boolean mad = true;
        private double centile=90;

        private Builder() {
        }

        public Builder maxDifferencing(int[] maxd) {
            this.maxd = maxd.clone();
            return this;
        }

         public Builder mad(boolean mad) {
            this.mad = mad;
            return this;
        }

         public Builder centile(double centile) {
            this.centile=centile;
            return this;
        }

        public Builder k(double k) {
            this.k = k;
            return this;
        }

        public Builder tstat(double tstat) {
            this.tstat = tstat;
            return this;
        }

        public FastDifferencingModule build() {
            return new FastDifferencingModule(maxd, mad, centile, k, tstat);
        }
    }

    private int[] d;
    private double tmean;
    private final int[] maxd;
    private final boolean mad;
    private final double centile;
    private final double k;
    private final double tstat;

    /**
     *
     */
    private FastDifferencingModule(final int[] maxd, final boolean mad,
            final double centile, final double k, final double tstat) {
        this.maxd = maxd;
        this.mad = mad;
        this.k = k;
        this.tstat = tstat;
        this.centile=centile;
    }

    @Override
    public boolean isMeanCorrection() {
        return Math.abs(tmean) > tstat;
    }

    private double std(DoubleSeq z) {
        if (!mad) {
            return Math.sqrt(z.ssqc(z.average()) / z.length());
        } else {
            return RobustStandardDeviationComputer.mad(centile, mad).compute(z);
        }
    }

    /**
     *
     * @param x
     * @param z
     * @param periods
     * @param start
     * @return
     */
    @Override
    public int[] process(DoubleSeq x, int[] periods, int[] start) {
        DataBlock z = DataBlock.of(x);
        if (start != null) {
            for (int j = 0; j < periods.length; ++j) {
                for (int i = 0; i < start[0]; ++i) {
                    z.autoApply(-periods[j], (a, b) -> a - b);
                    z = z.drop(periods[j], 0);
                }
            }
            d = start.clone();
        } else {
            d = new int[periods.length];
        }
        double refe = std(z);
        boolean ok;
        do {
            ok = false;
            DataBlock tmp;
            double e;
            for (int j = 0; j < periods.length; ++j) {
                if (d[j] < maxd[j]) {
                    tmp = z.deepClone();
                    tmp.autoApply(-periods[j], (a, b) -> a - b);
                    tmp = tmp.drop(periods[j], 0);
                    e = std(tmp);
                    if (e < refe * k) {
                        z = tmp;
                        refe = e;
                        if (++d[j] < maxd[j]) {
                            ok = true;
                        }
                    }
                }
            }
        } while (ok);
        testMean(z);
        return d;
    }

    private void testMean(DoubleSeq z) {
        double s = z.sum(), s2 = z.ssq();
        int n = z.length();
        tmean = s / Math.sqrt((s2 * n - s * s) / n);
    }

    /**
     * @return the tmean
     */
    public double getTmean() {
        return tmean;
    }

    /**
     * @return the k
     */
    public double getK() {
        return k;
    }

    /**
     * @return the tstat
     */
    public double getTstat() {
        return tstat;
    }

    /**
     * @return the mad
     */
    public boolean isMad() {
        return mad;
    }

}
