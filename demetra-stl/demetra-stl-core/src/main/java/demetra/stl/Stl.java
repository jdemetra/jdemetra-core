/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this fit except in compliance with the Licence.
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
package demetra.stl;

import java.util.Arrays;
import java.util.function.IntToDoubleFunction;
import java.util.function.DoubleUnaryOperator;
import demetra.data.DoubleSeq;

/**
 * Java implementation of the original FORTRAN routine
 *
 * Source; R.B. Cleveland, W.S.Cleveland, J.E. McRae, and I. Terpenning, STL: A
 * Seasonal-Trend Decomposition Procedure Based on Loess, Statistics Research
 * Report, AT&T Bell Laboratories.
 *
 * @author Jean Palate
 */
public class Stl {

    protected static final DoubleUnaryOperator W = x -> {
        double t = 1 - x * x * x;
        return t * t * t;
    };

    protected final StlSpecification spec;
    protected double[] y;
    protected boolean[] missing;
    protected double[] season;
    protected double[] trend;
    protected double[] irr;
    protected double[] weights;
    protected double[] fit;

    private static final int MAXSTEP = 100;

    private int n() {
        return y.length;
    }

    public Stl(StlSpecification spec) {
        this.spec = spec;
    }

    public boolean process(DoubleSeq data) {

        if (!initializeProcessing(data)) {
            return false;
        }
        int istep = 0;
        do {
            stlstp();
            if (++istep > spec.no) {
                return finishProcessing();
            }
            if (weights == null) {
                weights = new double[n()];
                fit = new double[n()];
            }
            for (int i = 0; i < n(); ++i) {
                fit[i] = spec.isMultiplicative() ? trend[i] * season[i] : trend[i] + season[i];
            }
            stlrwt(fit, weights);
        } while (true);
    }

    private boolean finishProcessing() {
        for (int i = 0; i < n(); ++i) {
            if (spec.isMultiplicative()) {
                fit[i] = trend[i] * season[i];
                if (missing[i]) {
                    irr[i] = 1;
                } else {
                    irr[i] = y[i] / fit[i];
                }
            } else {
                fit[i] = trend[i] + season[i];
                if (missing[i]) {
                    irr[i] = 0;
                } else {
                    irr[i] = y[i] - fit[i];
                }
            }
        }
        return true;
    }

    private boolean initializeProcessing(DoubleSeq data) {
        int n = data.length();
        y = new double[n];
        data.copyTo(y, 0);
        missing = new boolean[n];
        for (int i = 0; i < n(); ++i) {
            missing[i] = !Double.isFinite(y[i]);
        }
        fit = new double[n];
        season = new double[n];
        trend = new double[n];
        if (spec.isMultiplicative()) {
            Arrays.setAll(trend, i -> 1);
        }
        irr = new double[n];
        return true;
    }

    private static double mad(double[] r) {
        double[] sr = r.clone();
        Arrays.sort(sr);
        int n = r.length;
        int n2 = n >> 1;
        if (n % 2 != 0) {
            return 6 * sr[n2];
        } else {
            return 3 * (sr[n2 - 1] + sr[n2]);
        }
    }

    private void stlrwt(double[] fit, double[] w) {

        int n = n();
        for (int i = 0; i < n; ++i) {
            if (missing[i]) {
                w[i] = spec.isMultiplicative() ? 1 : 0;
            } else {
                w[i] = Math.abs(spec.isMultiplicative() ? y[i] / fit[i] - 1 : y[i] - fit[i]);
            }
        }

        double mad = mad(w);

        double c1 = spec.wthreshold * mad;
        double c9 = (1 - spec.wthreshold) * mad;

        for (int i = 0; i < n; ++i) {
            double r = w[i];
            if (r <= c1) {
                w[i] = 1;
            } else if (r <= c9) {
                w[i] = spec.wfn.applyAsDouble(r / mad);
            } else {
                w[i] = 0;
            }
        }

    }

    /**
     * Moving Average (aka "running mean") ave(i) := mean(x{j}, j =
     * max(1,i-k),..., min(n, i+k)) for i = 1,2,..,n
     *
     * @param len
     * @param n
     * @param x
     * @param ave
     */
    protected static void stlma(int len, int n, double[] x, double[] ave) {
        int newn = n - len + 1;
        double v = 0, flen = len;
        for (int i = 0; i < len; ++i) {
            v += x[i];
        }
        ave[0] = v / flen;
        if (newn > 1) {
            for (int i = 1, k = len, m = 0; i < newn; ++i, ++k, ++m) {
                v = v - x[m] + x[k];
                ave[i] = v / flen;
            }
        }
    }

    /**
     *
     * @param np
     * @param n
     * @param x
     * @param t
     */
    protected static void stlfts(int np, double[] x, double[] t) {
        int n = x.length;
        double[] w1 = new double[n];
        double[] w2 = new double[n];
        stlma(np, n, x, w1);
        stlma(np, n - np + 1, w1, w2);
        stlma(3, n - 2 * np + 2, w2, t);
    }

    protected double stlest(IntToDoubleFunction y, int n, int len, int degree, double xs, int nleft, int nright, IntToDoubleFunction userWeights) {
        int nj = nright - nleft + 1;
        double[] w = new double[nj];
        double range = n - 1;
        double h = Math.max(xs - nleft, nright - xs);
        if (len > n) {
            h += (len - n) * .5;
        }
        double h9 = 0.999 * h;
        double h1 = 0.001 * h;
        double a = 0;
        for (int j = nleft, jw = 0; j <= nright; ++j, ++jw) {
            boolean available = Double.isFinite(y.applyAsDouble(j));
            if (available) {
                double r = Math.abs(j - xs);
                if (r < h9) {
                    if (r < h1) {
                        w[jw] = 1;
                    } else {
                        w[jw] = spec.loessfn.applyAsDouble(r / h);
                    }

                    if (userWeights != null) {
                        w[jw] *= userWeights.applyAsDouble(j);
                    }
                    a += w[jw];
                }
            }
        }

        if (a <= 0) {
            return Double.NaN;
        } else {
            for (int j = 0; j < nj; ++j) {
                w[j] /= a;
            }
            if (h > 0 && degree > 0) {
                a = 0;
                for (int j = 0; j < nj; ++j) {
                    a += w[j] * j;
                }
                double b = xs - nleft - a;
                double c = 0;
                for (int j = 0; j < nj; ++j) {
                    double ja = j - a;
                    c += w[j] * ja * ja;
                }
                if (Math.sqrt(c) > .001 * range) {
                    b /= c;

                    for (int j = 0; j < nj; ++j) {
                        w[j] *= b * (j - a) + 1;
                    }
                }
            }
            double ys = 0;
            for (int j = nleft, jw = 0; j <= nright; ++j, ++jw) {
                double yj = y.applyAsDouble(j);
                if (Double.isFinite(yj)) {
                    ys += w[jw] * yj;
                }
            }
            return ys;
        }
    }

    protected void stless(IntToDoubleFunction y, int n, int len, int degree, int njump, IntToDoubleFunction userWeights, double[] ys) {

        if (n < 2) {
            ys[0] = y.applyAsDouble(0);
            return;
        }
        int newnj = Math.min(njump, n - 1);
        int nleft = 0, nright = 0;
        if (len >= n) {
            nleft = 0;
            nright = n - 1;
            for (int i = 0; i < n; i += newnj) {
                double yscur = stlest(y, n, len, degree, i, nleft, nright, userWeights);
                if (Double.isFinite(yscur)) {
                    ys[i] = yscur;
                } else {
                    ys[i] = y.applyAsDouble(i);
                }
            }
        } else if (newnj == 1) {
            int nsh = (len - 1) >> 1;
            nleft = 0;
            nright = len - 1;
            for (int i = 0; i < n; ++i) {
                if (i > nsh && nright != n - 1) {
                    ++nleft;
                    ++nright;
                }
                double yscur = stlest(y, n, len, degree, i, nleft, nright, userWeights);
                if (Double.isFinite(yscur)) {
                    ys[i] = yscur;
                } else {
                    ys[i] = y.applyAsDouble(i);
                }
            }
        } else {
            int nsh = (len - 1) >> 1;
            for (int i = 0; i < n; i += newnj) {
                if (i < nsh) {
                    nleft = 0;
                    nright = len - 1;
                } else if (i >= n - nsh) {
                    nleft = n - len;
                    nright = n - 1;
                } else {
                    nleft = i - nsh;
                    nright = i + nsh;
                }

                double yscur = stlest(y, n, len, degree, i, nleft, nright, userWeights);
                if (Double.isFinite(yscur)) {
                    ys[i] = yscur;
                } else {
                    ys[i] = y.applyAsDouble(i);
                }
            }
        }
        if (newnj != 1) {

            int i = 0;
            for (; i < n - newnj; i += newnj) {
                double delta = (ys[i + newnj] - ys[i]) / newnj;
                for (int j = i + 1; j < i + newnj; ++j) {
                    ys[j] = ys[i] + delta * (j - i);
                }
            }

            if (i != n - 1) {
                double yscur = stlest(y, n, len, degree, n - 1, nleft, nright, userWeights);
                if (Double.isFinite(yscur)) {
                    ys[n - 1] = yscur;
                } else {
                    ys[n - 1] = y.applyAsDouble(n - 1);
                }
                double delta = (ys[n - 1] - ys[i]) / (n - i - 1);
                for (int j = i + 1; j < n - 1; ++j) {
                    ys[j] = ys[i] + delta * (j - i);
                }
            }
        }

    }

    protected void stlss(IntToDoubleFunction fn, double[] season) {
        if (spec.np < 1) {
            return;
        }
        int n = n();
        double[] s = new double[(n - 1) / spec.np + 1];
        for (int j = 0; j < spec.np; ++j) {
            // last index fo period j (excluded)
            int k = (n - 1 - j) / spec.np + 1;
            final int start = j;
            IntToDoubleFunction yp = idx -> fn.applyAsDouble(idx * spec.np + start);
            IntToDoubleFunction wp = weights == null ? null : idx -> weights[idx * spec.np + start];
            stless(yp, k, spec.ns, spec.sdeg, spec.nsjump, wp, s);
            // backcast
            double sb = stlest(yp, k, spec.ns, spec.sdeg, -1, 0, Math.min(spec.ns - 1, k - 1), wp);
            if (Double.isFinite(sb)) {
                season[j] = sb;
            } else {
                season[j] = yp.applyAsDouble(0);
            }
            // copy s
            int l = spec.np + j;
            for (int i = 0; i < k; ++i, l += spec.np) {
                season[l] = s[i];
            }
            // forecast (pos =np*(b+1) )
            double sf = stlest(yp, k, spec.ns, spec.sdeg, k, Math.max(0, k - spec.ns), k - 1, wp);
            if (Double.isFinite(sf)) {
                season[l] = sf;
            } else {
                season[l] = yp.applyAsDouble(k - 1);
            }
        }
    }

    protected void stlstp() {
        int n = n();
        double[] si = new double[n];
        double[] l = new double[n];
        double[] w = new double[n];
        double[] c = new double[n + 2 * spec.np];
        // Step 1: SI=Y-T

        for (int j = 0; j < spec.ni; ++j) {

            for (int i = 0; i < n; ++i) {
                if (missing[i]) {
                    si[i] = Double.NaN;
                } else if (spec.isMultiplicative()) {
                    si[i] = y[i] / trend[i];
                } else {
                    si[i] = y[i] - trend[i];
                }
            }
            // Step 2: C=smooth(SI) (extended series)
            stlss(k -> si[k], c);
            // Step 3: L = f(C), low-pass filter 
            stlfts(spec.np, c, w);
            stless(k -> w[k], n, spec.nl, spec.ldeg, spec.nljump, null, l);
            // Step 4: S = C - L
            for (int i = 0; i < n; ++i) {
                if (spec.isMultiplicative()) {
                    season[i] = c[spec.np + i] / l[i];
                } else {
                    season[i] = c[spec.np + i] - l[i];
                }
            }
            // Step 5: seasonal adjustment
            for (int i = 0; i < n; ++i) {
                if (!missing[i]) {
                    if (spec.isMultiplicative()) {
                        w[i] = y[i] / season[i];
                    } else {
                        w[i] = y[i] - season[i];
                    }
                } else {
                    w[i] = Double.NaN;
                }
            }
            // Step 6: T=smooth(sa)
            stless(k -> w[k], n, spec.nt, spec.tdeg, spec.ntjump, weights == null ? null : k -> weights[k], trend);
        }
    }

    /**
     * @return the spec
     */
    public StlSpecification getSpec() {
        return spec;
    }

    /**
     * @return the y
     */
    public double[] getY() {
        return y;
    }

    /**
     * @param y the y to set
     */
    public void setY(double[] y) {
        this.y = y;
    }

    /**
     * @return the season
     */
    public double[] getSeason() {
        return season;
    }

    /**
     * @return the trend
     */
    public double[] getTrend() {
        return trend;
    }

    /**
     * @return the irr
     */
    public double[] getIrr() {
        return irr;
    }

    /**
     * @return the weights
     */
    public double[] getWeights() {
        return weights;
    }

    /**
     * @return the fit
     */
    public double[] getFit() {
        return fit;
    }
}
