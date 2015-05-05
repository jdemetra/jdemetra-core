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
package ec.tstoolkit.data;

import ec.tstoolkit.BaseException;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.utilities.IntList;

/**
 * Periodogram of a series of real-valued data
 * The periodogram is defined at the Fourier frequencies:
 * if we have n data, the Fourier frequencies f(j) are (2*pi)*j/n, j in [0,n[.
 * p(k) is usually defined as (1/n)*|sum(x(j)*e(i*k*f(j))|^2
 * As the x(j) are real-valued, we consider only the Fourier frequencies with j 
 * in [0, n/2]
 * We have:
 * p(0) = (1/n)*|sum(x(j)|^2 = (1/n)*|sx|^2
 * p(j) = (2/n)*|sum(x(j)*e(i*k*f(j))|^2
 * if n is even, p(n/2) = (1/n)*|(-1)^k*sum(x(j)|^2
 * This implementation will rescale the periodogram with the factor
 * n/sum(x(j)^2)=n/sx2, so that we will have:
 * p(0) = |sx|^2/sx2
 * p(j) = 2*|sum(x(j)*e(i*k*f(j))|^2/sx2
 * if n is even, p(n/2) = |(-1)^k*sum(x(j)|^2/sx2
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class Periodogram {

    public static double[] getSeasonalFrequencies(int freq) {
        double[] dfreq = new double[freq / 2];
        for (int i = 1; i <= dfreq.length; ++i) {
            dfreq[i - 1] = Math.PI * 2 * i / freq;
        }
        return dfreq;
    }

    /**
     *
     * @param freq
     * @return
     */
    public static double[] getTradingDaysFrequencies(int freq) {
        double n = 365.25 / freq;
        double f = 2 * Math.PI / 7 * (n - 7 * Math.floor(n / 7));
        if (f > Math.PI) {
            f = 2 * Math.PI - f;
        }
        if (freq == 12) {
            return new double[]{f}; // , 2 * Math.PI * 0.432 };
        } else if (freq == 4) {
            return new double[]{f, 1.292, 1.850, 2.128};
        } else {
            return new double[]{f};
        }
    }
    private double m_sy, m_sy2;
    private int m_n;
    private int[] m_w;
    private double[] m_data, m_p, m_s;
    private final boolean m_mean;

    public Periodogram(IReadDataBlock data) {
        this(data, true);
    }

    /**
     *
     * @param data
     * @param mean True if the periodogram is computed on the data corrected for mean,
     * false otherwise.
     */
    public Periodogram(IReadDataBlock data, boolean mean) {
        calcwnd(1);
        m_data = new double[data.getLength()];
        data.copyTo(m_data, 0);
        m_mean = mean;
        // remove mean;
        if (m_mean) {
            m_sy = 0;
            m_n = 0;
            for (int i = 0; i < m_data.length; ++i) {
                if (!Double.isNaN(m_data[i])) {
                    ++m_n;
                    m_sy += m_data[i];
                }
            }
            if (m_n > 0) {
                m_sy /= m_n;
                m_sy2 = 0;
                for (int i = 0; i < m_data.length; ++i) {
                    if (!Double.isNaN(m_data[i])) {
                        m_data[i] -= m_sy;
                        m_sy2 += m_data[i] * m_data[i];
                    }
                }
            }
        } else {
            m_n = 0;
            m_sy2 = 0;
            for (int i = 0; i < m_data.length; ++i) {
                if (!Double.isNaN(m_data[i])) {
                    ++m_n;
                    m_sy += m_data[i];
                    m_sy2 += m_data[i] * m_data[i];
                }
            }
        }
    }

    public boolean isMeanCorrection() {
        return m_mean;
    }
    
    public double getSsq(){
        return m_sy2;
    }

    private void calcp() {
        if (m_p != null || m_data == null) {
            return;
        }
        // p(l(j)) = a(j)*a(j) + b(j)*b(j)
        // l(j) = 2*pi*j / T, where T = m_data.Length
        // a(j) = (1/sqrt(T))
        int T = m_data.length, T1 = (1 + T) / 2, T2 = 1 + T / 2;
        m_p = new double[T2];
        double l = 2 * Math.PI / T;
        double cosl = Math.cos(l), sinl = Math.sin(l);
        double cos = 1, sin = 0; // current cos and sin...

        // the mean has been removed
        if (m_mean) {
            m_p[0] = 0;
        } else {
            m_p[0] = m_sy * m_sy / m_sy2;
        }
        double a = 0, b = 0;
        for (int i = 1; i < T1; ++i) {
            // compute next cos, sin
            double ctmp = cos, stmp = sin;
            sin = cosl * stmp + sinl * ctmp;
            cos = -sinl * stmp + cosl * ctmp;

            a = 0;
            b = 0;
            double c = 1, s = 0;
            for (int j = 0; j < T; ++j) {
                // compute next c, s ...
                ctmp = c;
                stmp = s;
                s = cos * stmp + sin * ctmp;
                c = -sin * stmp + cos * ctmp;
                if (!Double.isNaN(m_data[j])) {
                    a += c * m_data[j];
                    b += s * m_data[j];
                }
            }
            m_p[i] = 2 * (a * a + b * b) / m_sy2;
        }

        if (T1 != T2) // T even
        {
            a = 0;
            for (int i = 0; i < T; ++i) {
                if (!Double.isNaN(m_data[i])) {
                    if (i % 2 == 0) {
                        a += m_data[i];
                    } else {
                        a -= m_data[i];
                    }
                }
            }
            m_p[T2 - 1] = a * a / m_sy2;
        }
    }

    private void calcs() {
        if (m_s != null || m_data == null) {
            return;
        }
        calcp();
        if (m_p.length < m_w.length) {
            return;
        }
        if (m_w.length == 1) {
            m_s = m_p;
            return;
        }
        m_s = new double[m_p.length];
        // smooth the central p
        double wt = m_w[0];
        int nw = m_w.length;
        for (int i = 1; i < nw; ++i) {
            wt += 2 * m_w[i];
        }
        for (int i = nw - 1; i < m_p.length - nw; ++i) {
            double s = 0;
            for (int j = 1 - nw; j < nw; ++j) {
                s += m_p[i + j] * m_w[Math.abs(j)];
            }
            s /= wt;
            m_s[i] = s;
        }

        // extremities
        int plast = m_p.length - 1;
        for (int i = 0; i < nw; ++i) {
            wt = 0;
            double sbeg = 0, send = 0;
            for (int j = -i; j < nw; ++j) {
                double w = m_w[Math.abs(j)];
                sbeg += m_p[i + j] * w;
                send += m_p[plast - i - j] * w;
                wt += w;
            }
            m_s[i] = sbeg / wt;
            m_s[plast - i] = send / wt;
        }
    }

    private void calcwnd(int n) {
        if (n < 1) {
            throw new BaseException("Invalid Window length");
        } else if (n == 1) {
            m_w = new int[]{1};
        } else {

            m_w = new int[n];
            m_w[n - 1] = 1;
            m_w[n - 2] = 2;
            int c = n - 2;
            while (c > 0) {
                // compute even row, stored at the same place
                for (int k = c; k < n - 1; ++k) {
                    m_w[k] += m_w[k + 1];
                }
                m_w[c - 1] = m_w[c];
                // compute odd row
                --c;
                for (int k = c; k < n - 1; ++k) {
                    m_w[k] += m_w[k + 1];
                }
            }
        }
    }

    /**
     *
     * @return
     */
    public double[] getData() {
        return m_data;
    }

    /**
     *
     * @return
     */
    public double getIntervalInRadians() {
        return m_data == null ? 0 : (2 * Math.PI) / (m_data.length - 1);
    }

    /**
     *
     * @return
     */
    public double[] getP() {
        calcp();
        return m_p;
    }

    /**
     *
     * @return
     */
    public double[] getS() {
        calcs();
        return m_s;
    }

    /**
     *
     * @return
     */
    public int getWindowLength() {
        return m_w == null ? 0 : m_w.length;
    }

    // / <summary>
    // / Searches peakes higher than a given value.
    // / </summary>
    // / <param name="dMin"></param>
    // / <param name="smoothed"></param>
    // / <returns></returns>
    /**
     *
     * @param dMin
     * @param smoothed
     * @return
     */
    public int[] searchPeaks(double dMin, boolean smoothed) {
        if (!smoothed) {
            calcp();
            IntList peaks = new IntList(m_p.length);
            for (int i = 0; i < m_p.length; ++i) {
                if (m_p[i] > dMin) {
                    peaks.add(i);
                }
            }
            return peaks.toArray();
        } else {
            calcs();
            IntList peaks = new IntList(m_s.length);
            for (int i = 0; i < m_s.length; ++i) {
                if (m_s[i] > dMin) {
                    peaks.add(i);
                }
            }
            return peaks.toArray();
        }
    }

    /**
     *
     * @param value
     */
    public void setWindowLength(int value) {
        if (m_w == null || value != m_w.length) {
            calcwnd(value);
            m_s = null;
        }
    }
}
