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
package jdplus.data.analysis;

import demetra.data.DoubleSeq;
import nbbrd.design.Development;
import demetra.stats.AutoCovariances;
import jdplus.stats.DescriptiveStatistics;
import java.util.function.IntToDoubleFunction;

/**
 * This class implements the Durbin's algorithm, which fits recursively
 * auto-regressive models
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public class DurbinAlgorithm {

    private double[] cxx;//, cn_;
    private double sd, aic;
    private double[] x;
    private double[] a;
    private static final double SMALL = 1e-9;
    private boolean mean = true;
    private Taper taper;

    public double getAutocovariance(int lag) {
        return cxx[lag];
    }

    public int getMaxLag() {
        return cxx == null ? 0 : cxx.length - 1;
    }

    public void setTaper(Taper taper) {
        this.taper = taper;
    }

    public Taper getTaper() {
        return taper;
    }

    public boolean solve(DoubleSeq x, int l) {
        this.x = new double[x.length()];
        x.copyTo(this.x, 0);
        return calc(l);
    }

    public double[] getCoefficients() {
        return a;
    }

    public double getAIC() {
        return aic;
    }

    public double getInnovationVariance() {
        return sd;
    }

    public boolean isMeanCorrection() {
        return mean;
    }

    public void setMeanCorrection(boolean mean) {
        this.mean = mean;
    }

    private boolean calc(int l) {
        if (!checkMean()) {
            return false;
        }
        if (taper != null) {
            taper.process(x);
        }
        if (!calcCov(l)) {
            return false;
        }
        iterate(l);
        //a_ = Toeplitz.solveDurbinSystem(cxx_);
        return true;
    }

    private boolean checkMean() {
        double sum = 0;
        int nm = 0;
        for (int i = 0; i < x.length; ++i) {
            if (!Double.isFinite(x[i])) {
                ++nm;
            } else if (mean) {
                sum += x[i];
            }
        }
        if (nm == x.length) {
            return false;
        }
        if (sum != 0) {
            sum /= (x.length - nm);
            for (int i = 0; i < x.length; ++i) {
                if (Double.isFinite(x[i])) {
                    x[i] -= sum;
                }
            }
        }
        return true;
    }

    private boolean calcCov(int l) {
        IntToDoubleFunction acf = AutoCovariances.autoCovarianceFunction(DoubleSeq.of(x), 0);
        double v = acf.applyAsDouble(0);
        if (DescriptiveStatistics.isSmall(v)) {
            return false;
        }
        cxx = new double[l + 1];
        cxx[0] = v;
        for (int i = 1; i <= l; ++i) {
            cxx[i] = acf.applyAsDouble(i);
        }
        return true;
    }

    private void iterate(int l) {
        a = new double[l];
        int n = x.length;
        double csd = cxx[0];
        double caic = n * Math.log(csd);
        aic = caic;
        sd = csd;
        double se = cxx[1];
        double[] b = new double[l];
        for (int m = 0; m < l; m++) {
            double sdr = csd / cxx[0];
            if (sdr < SMALL) {
                break;
            }
            double d = se / csd;
            a[m] = d;
            csd = (1 - d * d) * csd;
            caic = n * Math.log(csd) + 2 * (m + 1);
            if (m != 0) {
                for (int i = 0; i <= m - 1; i++) {
                    a[i] -= d * b[i];
                }
            }
            for (int i = 0; i <= m; i++) {
                b[i] = a[m - i];
            }
            if (aic > caic) {
                aic = caic;
                sd = csd;
            }
            if (m != l - 1) {
                se = cxx[m + 2];
                for (int i = 0; i <= m; i++) {
                    se -= b[i] * cxx[i + 1];
                }
            }
        }

        for (int i = 0; i < l; i++) {
            a[i] = -a[i];
        }
    }
}
