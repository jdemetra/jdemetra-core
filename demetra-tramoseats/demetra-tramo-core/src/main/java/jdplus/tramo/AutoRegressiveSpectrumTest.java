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

package jdplus.tramo;

import jdplus.data.analysis.AutoRegressiveSpectrum;
import jdplus.data.analysis.Periodogram;
import demetra.stats.DescriptiveStatistics;
import demetra.data.DoubleSeq;


/**
 *
 * @author Jean Palate
 */
public class AutoRegressiveSpectrumTest {

    private static final int NFREQ = 60;
    private final double[] f = new double[NFREQ + 1], spectrum = new double[NFREQ + 1];
    private int arcount;
    private double sensitivity = .95;
    // results
    private AutoRegressiveSpectrum ar;
    private double median, srange;
    private int[] spos, tdpos;
    private static final double[] SILHF = {
        0.0696, 0.0705, 0.0715, 0.0726, 0.0735,
        0.0746, 0.0756, 0.0768, 0.0778, 0.0788,
        0.0801, 0.0812, 0.0822, 0.0834, 0.0845,
        0.0857, 0.0869, 0.0881, 0.0895, 0.0909,
        0.0923, 0.0935, 0.0948, 0.0961, 0.0975,
        0.0988, 0.1003, 0.1018, 0.1034, 0.1047,
        0.1062, 0.1076, 0.1090, 0.1106, 0.1125,
        0.1143, 0.1161, 0.1175, 0.1192, 0.1208,
        0.1224, 0.1245, 0.1263, 0.1282, 0.1301,
        0.1324, 0.1342, 0.1360, 0.1381, 0.1404,
        0.1428, 0.1452, 0.1477, 0.1500, 0.1523,
        0.1548, 0.1573, 0.1598, 0.1620, 0.1645,
        0.1674, 0.1702, 0.1731, 0.1756, 0.1790,
        0.1820, 0.1852, 0.1883, 0.1922, 0.1958,
        0.1991, 0.2030, 0.2069, 0.2108, 0.2154,
        0.2198, 0.2243, 0.2296, 0.2347, 0.2396,
        0.2447, 0.2510, 0.2572, 0.2640, 0.2717,
        0.2787, 0.2863, 0.2952, 0.3043, 0.3147,
        0.3259, 0.3385, 0.3522, 0.3658, 0.3849,
        0.4040, 0.4314, 0.4590, 0.4958, 0.5485};
    private static final double[] SILHM = {
        0.0023, 0.0029, 0.0036, 0.0042, 0.0048,
        0.0055, 0.0061, 0.0068, 0.0074, 0.0080,
        0.0089, 0.0095, 0.0102, 0.0109, 0.0116,
        0.0123, 0.0131, 0.0139, 0.0147, 0.0154,
        0.0162, 0.0169, 0.0178, 0.0186, 0.0195,
        0.0203, 0.0212, 0.0221, 0.0230, 0.0240,
        0.0249, 0.0259, 0.0270, 0.0280, 0.0290,
        0.0300, 0.0309, 0.0319, 0.0330, 0.0342,
        0.0353, 0.0364, 0.0376, 0.0388, 0.0400,
        0.0411, 0.0424, 0.0435, 0.0448, 0.0459,
        0.0474, 0.0488, 0.0502, 0.0515, 0.0529,
        0.0544, 0.0559, 0.0572, 0.0589, 0.0606,
        0.0624, 0.0640, 0.0658, 0.0676, 0.0695,
        0.0714, 0.0732, 0.0751, 0.0768, 0.0790,
        0.0814, 0.0839, 0.0864, 0.0889, 0.0912,
        0.0939, 0.0968, 0.0995, 0.1027, 0.1057,
        0.1093, 0.1127, 0.1163, 0.1202, 0.1239,
        0.1280, 0.1332, 0.1379, 0.1430, 0.1490,
        0.1551, 0.1620, 0.1705, 0.1794, 0.1901,
        0.2025, 0.2173, 0.2380, 0.2661, 0.3120};

    private static int indexGE(double val, boolean sym) {
        double[] valArray = sym ? SILHM : SILHF;
        int i = 0;
        while (i < valArray.length) {
            if (valArray[i] <= val) {
                i++;
            } else {
                return i;
            }
        }
        return i;
    }

    private static double prob(double sinc, boolean sym) {
        return .8 + .0025 * indexGE(sinc, sym);
    }

    public AutoRegressiveSpectrumTest() {
    }

    // settings
    public boolean test(DoubleSeq data, int period) {
        if (!computeARSpectrum(data, period)) {
            return false;
        }
        computeFrequencies(period);
        computeRange();
        return true;
    }

    public boolean isPeak(double freq, double p) {
        int pos = findPos(freq);
        return peak(pos, p);
    }

    private boolean peak(int pos, double p) {
        return peakProb(pos) > p;
    }

    private double peakProb(int pos) {
        double s = spectrum[pos];
        if (s <= median) {
            return 0;
        }
        if (pos < NFREQ && s > 0) {
            double sm = s - spectrum[pos - 1], sp = s - spectrum[pos + 1];
            double sinc = Math.min(sm, sp);
            if (sinc < 0) {
                return 0;
            }
            sinc /= srange;
            return prob(sinc, true);
        } else if (pos == NFREQ) {
            double sinc = s - spectrum[pos - 1];
            if (sinc < 0) {
                return 0;
            }
            sinc /= srange;
            return prob(sinc, false);
        } else {
            double sinc = s - spectrum[pos + 1];
            if (sinc < 0) {
                return 0;
            }
            sinc /= srange;
            return prob(sinc, false);
        }
    }

    public int tdPeaksCount() {
        return peaks(tdpos);
    }

    public int seasonalPeaksCount() {
        return peaks(spos);
    }

    public AutoRegressiveSpectrum getAutoRegressiveSpectrum() {
        return ar;
    }

    /**
     * Checks that there is a significant peak (taking into account the given
     * probability
     *
     * @param ifreq index of the seasonal peak. Should be in ]0, freq/2]
     * @param prob the probability (usually .95 (='a') or .99 (='A')
     * @return
     */
    public boolean hasSeasonalPeak(int ifreq, double prob) {
        if (ifreq <= 0 || ifreq > spos.length) {
            return false;
        }
        return peak(spos[ifreq - 1], prob);
    }

    /**
     * Returns the presence of seasonal peaks
     * 0 = no peak, 1 = significative peak, 2= very significative peak
     * @param plow low sensitivity -> 1
     * @param phigh high sensitivity -> 2
     * @return
     */
    public int[] seasonalPeaks(double plow, double phigh) {
        int[] rslt = new int[spos.length];
        for (int i=0; i<spos.length; ++i){
            double p=peakProb(spos[i]);
            if (p>phigh)
                rslt[i]=2;
            else if (p>plow)
                rslt[i]=1;
        }
        return rslt;
    }

    private int peaks(int[] pos) {
        if (pos == null) {
            return 0;
        }
        int n = 0;
        for (int i = 0; i < pos.length; ++i) {
            if (peak(pos[i], getSensitivity())) {
                ++n;
            }
        }
        return n;
    }

    private int arcount(int ndata, int freq) {
        if (arcount == 0) {
            int n = 30 * freq / 12;
            if (n > ndata - 1) {
                return ndata - 1;
            } else {
                return n;
            }
        } else {
            return arcount;
        }
    }

    private boolean computeARSpectrum(DoubleSeq data, int period) {
        int n = data.length();
        int nar = arcount(n, period);
        if (nar >= n) {
            return false;
        }
        ar = new AutoRegressiveSpectrum(AutoRegressiveSpectrum.Method.Ols);
        return ar.process(data, nar);
    }

    private void computeFrequencies(int freq) {
        // f...
        for (int i = 0; i <= NFREQ; ++i) {
            f[i] = step() * i;
        }
        spos = new int[freq / 2];
        int step = 10 * 12 / freq;
        for (int i = 0; i < spos.length; ++i) {
            spos[i] = step * (i + 1);
        }
        // trading day frequencies
        double[] tdf = Periodogram.getTradingDaysFrequencies(freq);
        if (tdf != null) {
            tdpos = new int[tdf.length];
            for (int i = 0; i < tdf.length; ++i) {
                int pos = findPos(tdf[i]);
                tdpos[i] = pos;
                f[pos - 1] = tdf[i] - step();
                f[pos] = tdf[i];
                f[pos + 1] = tdf[i] + step();
            }
        }
        for (int i = 0; i <= NFREQ; ++i) {
            spectrum[i] = ar.value(f[i]);
        }
    }

    private double step() {
        return Math.PI / NFREQ;
    }

    private int findPos(double f) {
        int pos = (int) (f / step() + .5);
        if (pos > NFREQ) {
            pos = NFREQ;
        }
        return pos;
    }

    private void computeRange() {
        DescriptiveStatistics stats = DescriptiveStatistics.ofInternal(spectrum);
        median = stats.getMedian();
        srange = stats.getMax() - stats.getMin();

    }

    /**
     * @return the arcount
     */
    public int getArCount() {
        return arcount;
    }

    /**
     * @param arcount the arcount to set
     */
    public void setArCount(int arcount) {
        this.arcount = arcount;
    }

    /**
     * @return the sensitivity
     */
    public double getSensitivity() {
        return sensitivity;
    }

    /**
     * @param sensitivity the sensitivity to set
     */
    public void setSensitivity(double sensitivity) {
        this.sensitivity = sensitivity;
    }
}
