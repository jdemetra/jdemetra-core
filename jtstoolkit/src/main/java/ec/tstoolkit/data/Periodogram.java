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

/**
 *
 * @author Jean Palate
 */
package ec.tstoolkit.data;

import ec.tstoolkit.BaseException;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.utilities.IntList;

/**
 * 
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
        }
        else if (freq == 4) {
            return new double[]{f, 1.292, 1.850, 2.128};
        }
        else {
            return new double[]{f};
        }
    }
    private double m_y2;
    private int m_n;
    private int[] m_w;
    private double[] m_data, m_p, m_s;

    /**
     * 
     * @param data
     */
    public Periodogram(IReadDataBlock data) {
        calcwnd(1);
        m_data = new double[data.getLength()];
        data.copyTo(m_data, 0);
        // Start 4/5/2007
        // remove mean;
        double s = 0;
        m_n = 0;
        for (int i = 0; i < m_data.length; ++i) {
            if (!Double.isNaN(m_data[i])) {
                ++m_n;
                s += m_data[i];
            }
        }
        if (m_n > 0) {
            s /= m_n;
            m_y2 = 0;
            for (int i = 0; i < m_data.length; ++i) {
                if (!Double.isNaN(m_data[i])) {
                    m_data[i] -= s;
                    m_y2 += m_data[i] * m_data[i];
                }
            }
            m_y2 /= m_n;
        }
        // End 4/5/2007
    }

    private void calcp() {
        if (m_p != null || m_data == null) {
            return;
        }
        // p(l(j)) = a(j)*a(j) + b(j)*b(j)
        // l(j) = 2*pi*j / T, where T = m_data.Length
        // a(j) = (1/sqrt(T))
        int T = m_data.length, T2 = (T + 1) / 2;
        m_p = new double[T2];
        double l = 2 * Math.PI / T;
        double cosl = Math.cos(l), sinl = Math.sin(l);
        double cos = 1, sin = 0; // current cos and sin...

        // the mean has been removed
        m_p[0] = 0;
        double a = 0, b = 0;
        for (int i = 1; i < T / 2; ++i) {
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
            m_p[i] = (a * a + b * b) * 2 / (m_n * m_y2);
        }

        if (T + 1 == 2 * T2) // T odd
        {
            a = 0;
            for (int i = 0; i < T; ++i) {
                if (!Double.isNaN(m_data[i])) {
                    if (i % 2 == 0) {
                        a += m_data[i];
                    }
                    else {
                        a -= m_data[i];
                    }
                }
            }
            m_p[T2 - 1] = a * a / (m_n * m_y2);
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
        }
        else if (n == 1) {
            m_w = new int[]{1};
        }
        else {

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
        }
        else {
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
