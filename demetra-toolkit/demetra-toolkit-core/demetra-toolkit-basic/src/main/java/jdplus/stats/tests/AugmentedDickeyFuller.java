/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.stats.tests;

import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import demetra.design.BuilderPattern;
import jdplus.math.matrices.Matrix;
import demetra.data.DoubleSeq;
import demetra.data.DoublesMath;
import jdplus.leastsquares.QRSolution;
import jdplus.leastsquares.QRSolver;

/**
 * (Augmented) Dickey-Fuller test
 * The estimated model is dy(t)=d*y(t-1)[+a][+b*t+][+e1*dy(t-1)+...+ek*dy(t-k)]+eps
 *
 * @author Jean Palate
 */
public class AugmentedDickeyFuller {

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(AugmentedDickeyFuller.class)
    public static class Builder {

        private int k = 0; // number of lags. 0 for simple Dickey-Fuller
        private boolean cnt=false, trend=false;
        private DoubleSeq y;

        public Builder data(DoubleSeq y) {
            this.y = y;
            return this;
        }

        public Builder numberOfLags(int nlags) {
            if (k < 0) {
                throw new java.lang.IllegalArgumentException("k should be greater or equal to 0");
            }
            this.k = nlags;
            return this;
        }

        public Builder constant(boolean cnt) {
            this.cnt = cnt;
            return this;
        }

        public Builder linearTrend(boolean trend) {
            this.trend = trend;
            return this;
        }

        public AugmentedDickeyFuller build() {
            //
            DoubleSeq del=DoublesMath.delta(y, 1);
            int ndata = del.length()-k;
            int ncols = k+1;
            if (cnt) {
                ++ncols;
            }
            if (trend) {
                ++ncols;
            }
            Matrix x = Matrix.make(ndata, ncols);

            DataBlockIterator columns = x.columnsIterator();
            if (cnt) {
                columns.next().set(1);
            }
            if (trend) {
                columns.next().set(idx -> idx);
            }
            for (int i = 1; i <= k; ++i) {
                columns.next().copy(del.extract(k - i, ndata));
            }
            columns.next().copy(y.extract(k, ndata));
            return new AugmentedDickeyFuller(del.extract(k, ndata), x, cnt, trend);
        }

    }

    private static final double[] TNC_01 = new double[]{-2.56574, -2.2358, -3.627};
    private static final double[] TNC_05 = new double[]{-1.94100, -0.2686, -3.365, 31.223};
    private static final double[] TNC_10 = new double[]{-1.61682, 0.2656, -2.714, 25.364};
    private static final double[] TC_01 = new double[]{-3.43035, -6.5393, -16.786, -79.433};
    private static final double[] TC_05 = new double[]{-2.86154, -2.8903, -4.234, -40.040};
    private static final double[] TC_10 = new double[]{-2.56677, -1.5384, -2.809};
    private static final double[] LT_01 = new double[]{-3.95877, -9.0531, -28.428, -134.155};
    private static final double[] LT_05 = new double[]{-3.41049, -4.3904, -9.036, -45.374};
    private static final double[] LT_10 = new double[]{-3.12705, -2.5856, -3.925, -22.380};

    private final boolean cnt, trend;
    private final Matrix x;
    private final DoubleSeq y;
    private final DataBlock b, e;
    private final double stderr, t;

    private AugmentedDickeyFuller(DoubleSeq y, Matrix x, boolean cnt, boolean trend) {
        this.x = x;
        this.y = y;
        this.cnt = cnt;
        this.trend = trend;
        QRSolution ls = QRSolver.fastLeastSquares(y, x);
        int l=x.getColumnsCount()-1;
        b=DataBlock.of(ls.getB());
        e=DataBlock.of(ls.getE());
        double ssq = ls.getSsqErr();
        double val = b.get(l);
        stderr = Math.abs(Math.sqrt(ssq / e.length()) / ls.rawRDiagonal().get(l));
        t = val / stderr;
    }
    
    public boolean isSignificant(double eps) {
        if (!cnt && !trend) {
            return sign00(eps);
        } else if (!trend) {
            return sign10(eps);
        } else {
            return sign11(eps);
        }
    }

    public double getRho(){
        return b.get(0)+1;
    }

    public double getStdErr(){
        return stderr;
    }

    /**
     * @return the t
     */
    public double getT() {
        return t;
    }

    /**
     * @return the cnt
     */
    public boolean isConstant() {
        return cnt;
    }

    /**
     * @return the trend
     */
    public boolean isTrend() {
        return trend;
    }

    static double thresholdnc(double eps, int n) {
        double[] w = null;
        if (eps == 0.01) {
            w = TNC_01;
        } else if (eps == 0.05) {
            w = TNC_05;
        } else if (eps == 0.1) {
            w = TNC_10;
        }
        if (w == null) {
            return Double.NaN;
        }
        double q = n;
        double s = w[0] + w[1] / q;
        for (int i = 2; i < w.length; ++i) {
            q *= n;
            s += w[i] / q;
        }
        return s;
    }

    static double thresholdc(double eps, int n) {
        double[] w = null;
        if (eps == 0.01) {
            w = TC_01;
        } else if (eps == 0.05) {
            w = TC_05;
        } else if (eps == 0.1) {
            w = TC_10;
        }
        if (w == null) {
            return Double.NaN;
        }
        double q = n;
        double s = w[0] + w[1] / q;
        for (int i = 2; i < w.length; ++i) {
            q *= n;
            s += w[i] / q;
        }
        return s;
    }

    static double thresholdt(double eps, int n) {
        double[] w = null;
        if (eps == 0.01) {
            w = LT_01;
        } else if (eps == 0.05) {
            w = LT_05;
        } else if (eps == 0.1) {
            w = LT_10;
        }
        if (w == null) {
            return Double.NaN;
        }
        double q = n;
        double s = w[0] + w[1] / q;
        for (int i = 2; i < w.length; ++i) {
            q *= n;
            s += w[i] / q;
        }
        return s;
    }

    private boolean sign10(double eps) {
        double th = thresholdc(eps, e.length());
        return t <= th;
    }

    private boolean sign00(double eps) {
        double th = thresholdnc(eps, e.length());
        return t <= th;
    }

    private boolean sign11(double eps) {
        double th = thresholdt(eps, e.length());
        return t <= th;
    }
}
