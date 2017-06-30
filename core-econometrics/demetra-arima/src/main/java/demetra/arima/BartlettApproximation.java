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

    private ILinearModel m_xmodel, m_ymodel;
    private double[] m_xc, m_yc, m_xycp, m_xycn;
    private static final int g_max = 1000;

    /**
     *
     * @param X
     * @param Y
     */
    public BartlettApproximation(ILinearModel X, ILinearModel Y) {
        m_xmodel=X;
        m_ymodel=Y;
    }

    /**
     *
     * @param k
     * @return
     * @throws ArimaException
     */
    public double autoCovariance(final int k) throws ArimaException {
        calc();
       return m_xc[k] * m_xmodel.getAutoCovarianceFunction().get(0);
    }

    private double xycp(int i) {
        return i >= m_xycp.length ? 0 : m_xycp[i];
    }

    private double xycn(int i) {
        return i >= m_xycn.length ? 0 : m_xycn[i];
    }

    private double xc(int i) {
        return i >= m_xc.length ? 0 : m_xc[i];
    }

    private double yc(int i) {
        return i >= m_yc.length ? 0 : m_yc[i];
    }

    private void calc() throws ArimaException {
        if ((m_xmodel == null || m_xc != null)
                && (m_ymodel == null || m_yc != null)) {
            return;
        }
        // x
        if (m_xmodel != null && m_xc == null) {
            m_xc = prepare(m_xmodel);
        }
        if (m_ymodel != null && m_yc == null) {
            m_yc = prepare(m_ymodel);
        }
        if (m_ymodel != null && m_xmodel != null && m_xycp == null
                && m_xycn == null) {
            prepare(m_xmodel, m_ymodel);
        }
    }

    private int calctruncationpoint(final AutoCovarianceFunction acgf) {
        int b = acgf.hasBound() ? acgf.getBound() : g_max;
        if (b > g_max) {
            b = g_max;
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
        calc();
        if (m_xycp == null || m_xycn == null) {
            throw new ArimaException(ArimaException.UnitializedModel);
        }
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
        return m_xmodel;
    }

    /**
     *
     * @return
     */
    public ILinearModel getY() {
        return m_ymodel;
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
        int mn = ccgf.hasLBound() ? -ccgf.getLBound() : g_max;
        int mp = ccgf.hasUBound() ? ccgf.getUBound() : g_max;
        m_xycn = new double[mn + 1];
        m_xycp = new double[mp + 1];

        ccgf.prepare(-mn - 1, mp + 1);
        double denom = Math.sqrt(x.getAutoCovarianceFunction().get(0) * y.getAutoCovarianceFunction().get(0));
        for (int i = 0; i >= -mn; --i) {
            m_xycn[-i] = ccgf.get(i) / denom;
        }
        for (int i = 0; i <= mp; ++i) {
            m_xycp[i] = ccgf.get(i) / denom;
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
        calc();
        if (m_xc == null) {
            throw new ArimaException(ArimaException.UnitializedModel);
        }
        // V(ace(k))= 1/T*(sum(-m, m)[
        // ac(j)*ac(j)+ac(j+k)*ac(j-k)+2*ac(j)*ac(j)*ac(k)*ac(k)-4*ac(k)*ac(j)*ac(j-k))
        if (k < 0) {
            k = -k;
        }
        double rk = xc(k);
        double v = 1 - rk * rk;
        int m = m_xc.length - 1;
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
        calc();
        if (m_xc == null || m_yc == null) {
            throw new ArimaException(ArimaException.UnitializedModel);
        }
        double v = 0;

        int mn = m_xycn.length - 1, mp = m_xycp.length - 1;
        for (int i = -mn; i <= mp; ++i) {
            int I = Math.abs(i), ipk = i + k, imk = i - k, IPK = Math.abs(ipk);
            double rxi = xc(I);
            double ryi = yc(I);
            double ryipk = yc(IPK);
            double rxyipk = 0, rxyimk = 0, rxyk = 0, rxymi = 0, rxyi = 0;
            if (ipk <= mp) {
                if (ipk >= 0) {
                    rxyipk = m_xycp[ipk];
                } else if (-ipk <= mn) {
                    rxyipk = m_xycn[-ipk];
                }
            }
            if (imk <= mp) {
                if (imk >= 0) {
                    rxyimk = m_xycp[imk];
                } else if (-imk <= mn) {
                    rxyimk = m_xycn[-imk];
                }
            }

            if (k <= mp) {
                if (k >= 0) {
                    rxyk = m_xycp[k];
                } else if (-k <= mn) {
                    rxyk = m_xycn[-k];
                }
            }

            if (-i <= mp) {
                if (-i >= 0) {
                    rxymi = m_xycp[-i];
                } else if (i <= mn) {
                    rxymi = m_xycn[i];
                }
            }
            if (i <= mp) {
                if (i >= 0) {
                    rxyi = m_xycp[i];
                } else if (-i <= mn) {
                    rxyi = m_xycn[-i];
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
        calc();
        if (m_xc == null) {
            throw new ArimaException(ArimaException.UnitializedModel);
        }
        double v = 2;
        int m = m_xc.length - 1;
        for (int i = 1; i <= m; ++i) {
            v += 4 * m_xc[i] * m_xc[i];
        }
        v /= samplesize;
        return m_xmodel.getAutoCovarianceFunction().get(0) * Math.sqrt(v);
    }

    /**
     *
     * @param value
     */
    public void setX(final LinearModel value) {
        initxmodel(value);
    }

    /**
     *
     * @param value
     */
    public void setY(final LinearModel value) {
        initymodel(value);
    }
}
