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
package jdplus.arima;

import nbbrd.design.Development;
import nbbrd.design.Immutable;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable
public final class BartlettApproximation {

    private static final int TRUNCATION_LIMIT = 1000;

    public static class AutoCorrelation {

        private final ILinearProcess model;
        private final double[] ac;

        public AutoCorrelation(ILinearProcess m) {
            this.model = m;
            this.ac = ac(m);
        }

        /**
         *
         * @param lag
         * @return
         * @throws ArimaException
         */
        public double get(final int lag) throws ArimaException {
            return lag >= ac.length ? 0 : ac[lag];
        }

        /**
         *
         * @param lag
         * @param samplesize
         * @return
         * @throws ArimaException
         */
        public double standardDeviation(final int lag, final int samplesize)
                throws ArimaException {
            int k = lag < 0 ? -lag : lag;

            double rk = get(k);
            double v = 1 - rk * rk;
            int m = ac.length - 1;
            for (int j = 1; j <= m; ++j) {
                double rj = get(j), rjpk = get(j + k), rjmk = (j - k) < 0 ? get(k
                        - j)
                        : get(j - k);
                v += 2 * (rj * rj * (1 + 2 * rk * rk) + rjpk * rjmk - 2 * rj * rk
                        * (rjpk + rjmk));
            }
            return Math.sqrt(v / samplesize);
        }

    }

    public static class CrossCorrelation {

        private final ILinearProcess xmodel, ymodel;
        private final double[] xc, yc, xycp, xycn;

        public CrossCorrelation(ILinearProcess X, ILinearProcess Y) {
            xmodel = X;
            ymodel = Y;
            xc = ac(xmodel);
            yc = ac(ymodel);
            CrossCovarianceFunction ccgf = new CrossCovarianceFunction(X, Y);
            int mn = ccgf.hasLBound() ? -ccgf.getLBound() : TRUNCATION_LIMIT;
            int mp = ccgf.hasUBound() ? ccgf.getUBound() : TRUNCATION_LIMIT;
            xycn = new double[mn + 1];
            xycp = new double[mp + 1];

            ccgf.prepare(-mn - 1, mp + 1);
            double denom = Math.sqrt(X.getAutoCovarianceFunction().get(0) * Y.getAutoCovarianceFunction().get(0));
            for (int i = 0; i >= -mn; --i) {
                xycn[-i] = ccgf.get(i) / denom;
            }
            for (int i = 0; i <= mp; ++i) {
                xycp[i] = ccgf.get(i) / denom;
            }
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

        /**
         *
         * @param lag
         * @return
         * @throws ArimaException
         */
        public double get(final int lag) throws ArimaException {
            if (lag >= 0) {
                return xycp(lag);
            } else {
                return xycn(-lag);
            }
        }

        /**
         *
         * @param samplesize
         * @param lag
         * @return
         * @throws ArimaException
         */
        public double standardDeviation(final int lag, final int samplesize)
                throws ArimaException {
            double v = 0;

            int mn = xycn.length - 1, mp = xycp.length - 1;
            for (int i = -mn; i <= mp; ++i) {
                int I = Math.abs(i), ipk = i + lag, imk = i - lag, IPK = Math.abs(ipk);
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

                if (lag <= mp) {
                    if (lag >= 0) {
                        rxyk = xycp[lag];
                    } else if (-lag <= mn) {
                        rxyk = xycn[-lag];
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
            return Math.sqrt(v / (samplesize - lag));
        }

        /**
         *
         * @return
         */
        public ILinearProcess getX() {
            return xmodel;
        }

        /**
         *
         * @return
         */
        public ILinearProcess getY() {
            return ymodel;
        }
    }

    /**
     *
     * @param X
     * @param Y
     */
    private BartlettApproximation() {
    }

    public static double standardDeviationOfVariance(final ILinearProcess model, final int samplesize) throws ArimaException {
        AutoCovarianceFunction acf = model.getAutoCovarianceFunction();
        int m = calcTruncationPoint(acf);
        acf.prepare(m+1);
        double var=acf.get(0);
        double v = 2*var*var;
        for (int i = 1; i <= m; ++i) {
            double c = acf.get(i);
            v += 4 * c * c;
        }
        v /= samplesize;
         return Math.sqrt(v);
    }

    private static int calcTruncationPoint(final AutoCovarianceFunction acgf) {
        int b = acgf.hasBound() ? acgf.getBound() : TRUNCATION_LIMIT;
        if (b > TRUNCATION_LIMIT) {
            b = TRUNCATION_LIMIT;
        }
        return b;
    }

    private static double[] ac(final ILinearProcess model) throws ArimaException {
        AutoCovarianceFunction acgf = model.getAutoCovarianceFunction();
        int m = calcTruncationPoint(acgf);
        acgf.prepare(m + 1);
        double[] c = new double[m + 1];
        double var = acgf.get(0);
        c[0] = 1;
        for (int i = 1; i <= m; ++i) {
            c[i] = acgf.get(i) / var;
        }
        return c;
    }

}
