/*
 * Copyright 2013 National Bank of Belgium
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
package demetra.arima;

import demetra.design.Development;
import demetra.design.Immutable;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable
public class BartlettApproximation {

    private final ILinearModel xmodel, ymodel;
    private double[] xc, yc, xycp, xycn;
    private static final int TRUNCATION_LIMIT = 1000;

    /**
     *
     * @param X
     * @param Y
     */
    public BartlettApproximation(ILinearModel X, ILinearModel Y) {
        xmodel = X;
        ymodel = Y;
        calc();
    }

    /**
     *
     * @param k
     * @return
     * @throws ArimaException
     */
    public double autoCovariance(final int k) throws ArimaException {
        calc();
        return xc[k] * xmodel.getAutoCovarianceFunction().get(0);
    }

    private double xycp(int i) {
        return i >= xycp.length ? 0 : xycp[i];
    }

    private double xycn(int i) {
        return i >= xycn.length ? 0 : xycn[i];
    }

    private double xc(int i) {
        return i >= xc.length ? 0 : xc[i];
    }

    private double yc(int i) {
        return i >= yc.length ? 0 : yc[i];
    }

    private void calc() throws ArimaException {
        xc = prepare(xmodel);
        yc = prepare(ymodel);
        prepare(xmodel, ymodel);
    }

    private int calctruncationpoint(final AutoCovarianceFunction acgf) {
        int b = acgf.hasBound() ? acgf.getBound() : TRUNCATION_LIMIT;
        if (b > TRUNCATION_LIMIT) {
            b = TRUNCATION_LIMIT;
        }
        return b;
    }

    /**
     *
     * @param k
     * @return
     * @throws ArimaException
     */
    public double crossCorrelation(int k) throws ArimaException {
        if (k >= 0) {
            return xycp(k);
        } else {
            return xycn(-k);
        }
    }

    /**
     *
     * @return
     */
    public ILinearModel getX() {
        return xmodel;
    }

    /**
     *
     * @return
     */
    public ILinearModel getY() {
        return ymodel;
    }

    private double[] prepare(final ILinearModel model) throws ArimaException {
        AutoCovarianceFunction acgf = model.getAutoCovarianceFunction();
        int m = calctruncationpoint(acgf);
        acgf.prepare(m + 1);
        double[] c = new double[m + 1];
        double var = acgf.get(0);
        c[0] = 1;
        for (int i = 1; i <= m; ++i) {
            c[i] = acgf.get(i) / var;
        }
        return c;
    }

    private void prepare(final ILinearModel x, final ILinearModel y)
            throws ArimaException {
        CrossCovarianceFunction ccgf = new CrossCovarianceFunction(x, y);
        int mn = ccgf.hasLBound() ? -ccgf.getLBound() : TRUNCATION_LIMIT;
        int mp = ccgf.hasUBound() ? ccgf.getUBound() : TRUNCATION_LIMIT;
        xycn = new double[mn + 1];
        xycp = new double[mp + 1];

        ccgf.prepare(-mn - 1, mp + 1);
        double denom = Math.sqrt(x.getAutoCovarianceFunction().get(0) * y.getAutoCovarianceFunction().get(0));
        for (int i = 0; i >= -mn; --i) {
            xycn[-i] = ccgf.get(i) / denom;
        }
        for (int i = 0; i <= mp; ++i) {
            xycp[i] = ccgf.get(i) / denom;
        }
    }

    /**
     *
     * @param samplesize
     * @param k
     * @return
     * @throws ArimaException
     */
    public double SDAutoCorrelation(final int samplesize, int k)
            throws ArimaException {
        // V(ace(k))= 1/T*(sum(-m, m)[
        // ac(j)*ac(j)+ac(j+k)*ac(j-k)+2*ac(j)*ac(j)*ac(k)*ac(k)-4*ac(k)*ac(j)*ac(j-k))
        if (k < 0) {
            k = -k;
        }
        double rk = xc(k);
        double v = 1 - rk * rk;
        int m = xc.length - 1;
        for (int j = 1; j <= m; ++j) {
            double rj = xc(j), rjpk = xc(j + k), rjmk = (j - k) < 0 ? xc(k
                    - j)
                    : xc(j - k);
            v += 2 * (rj * rj * (1 + 2 * rk * rk) + rjpk * rjmk - 2 * rj * rk
                    * (rjpk + rjmk));
        }
        return Math.sqrt(v / samplesize);
    }

    /**
     *
     * @param samplesize
     * @param k
     * @return
     * @throws ArimaException
     */
    public double SDCrossCorrelation(final int samplesize, int k)
            throws ArimaException {
        double v = 0;

        int mn = xycn.length - 1, mp = xycp.length - 1;
        for (int i = -mn; i <= mp; ++i) {
            int I = Math.abs(i), ipk = i + k, imk = i - k, IPK = Math.abs(ipk);
            double rxi = xc(I);
            double ryi = yc(I);
            double ryipk = yc(IPK);
            double rxyipk = 0, rxyimk = 0, rxyk = 0, rxymi = 0, rxyi = 0;
            if (ipk <= mp) {
                if (ipk >= 0) {
                    rxyipk = xycp[ipk];
                } else if (-ipk <= mn) {
                    rxyipk = xycn[-ipk];
                }
            }
            if (imk <= mp) {
                if (imk >= 0) {
                    rxyimk = xycp[imk];
                } else if (-imk <= mn) {
                    rxyimk = xycn[-imk];
                }
            }

            if (k <= mp) {
                if (k >= 0) {
                    rxyk = xycp[k];
                } else if (-k <= mn) {
                    rxyk = xycn[-k];
                }
            }

            if (-i <= mp) {
                if (-i >= 0) {
                    rxymi = xycp[-i];
                } else if (i <= mn) {
                    rxymi = xycn[i];
                }
            }
            if (i <= mp) {
                if (i >= 0) {
                    rxyi = xycp[i];
                } else if (-i <= mn) {
                    rxyi = xycn[-i];
                }
            }

            v += rxi * ryi + rxyimk * rxyipk - 2 * rxyk
                    * (rxi * rxyipk + rxymi * ryipk) + rxyk * rxyk
                    * (rxyi * rxyi + .5 * rxi * rxi + .5 * ryi * ryi);
        }
        return Math.sqrt(v / (samplesize - k));
    }

    /**
     *
     * @param samplesize
     * @return
     * @throws ArimaException
     */
    public double SDVar(final int samplesize) throws ArimaException {
        double v = 2;
        int m = xc.length - 1;
        for (int i = 1; i <= m; ++i) {
            v += 4 * xc[i] * xc[i];
        }
        v /= samplesize;
        return xmodel.getAutoCovarianceFunction().get(0) * Math.sqrt(v);
    }

}
