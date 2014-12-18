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

package ec.tstoolkit.modelling.arima.tramo.spectrum;

import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.utilities.Arrays2;
import java.util.Arrays;

/**
 *
 * @author gianluca
 */
public class Peaks {
//

    private int nfrq = 61;
    private double[] Spect;
    private double[] Frq;
    private double[] SPeaks;
    private int[] SPeaksIdx;
    private int nSPeaks;
    private double[] TDPeaks;
    private int[] TDPeaksIdx;
    private int nTDPeaks;

    public double[] getSPeaks() {
        return SPeaks;
    }

    public int[] getSPeaksIdx() {
        return SPeaksIdx;
    }

    public double[] getSpect() {
        return Spect;
    }

    public double[] getTDPeaks() {
        return TDPeaks;
    }

    public int[] getTDPeaksIdx() {
        return TDPeaksIdx;
    }

    public int getnSPeaks() {
        return nSPeaks;
    }

    public int getnTDPeaks() {
        return nTDPeaks;
    }
    private TsData serie;
    private int spLen;
    private boolean delta;

    public Peaks(TsData targetS, int spectrumLen, boolean diff) {
        serie = targetS;
        spLen = spectrumLen;
        delta = diff;
        computePeaks();
    }

    private void taper(TsData x, double R) {
        for (int i = 0; i < x.getLength(); i++) {
            double xpi = 0.0;
            double tap = 0.0;
            double xtap = (i + 0.5) / x.getLength();
            if ((xtap >= R / 2.0) && (xtap <= (1.0 - R / 2.0))) {
                tap = 1.0;
            } else {
                if (xtap < R / 2.0) {
                    xpi = (2.0 * Math.PI * xtap) / R;
                }
                if (xtap > (1.0 - R / 2.0)) {
                    xpi = (2.0 * Math.PI * (1.0 - xtap)) / R;
                }
                tap = (1 - Math.cos(xpi)) / 2.0;
            }
            x.set(i, x.get(i) * tap);
        }
    }

    static public void crosco(TsData x, TsData y, double[] Cxx) {
        DescriptiveStatistics bs = new DescriptiveStatistics(x.getValues());
        double an = x.getLength() - bs.getMissingValuesCount();
        double bn = 1.0 / an;
        double ct0 = 0.0;
        for (int i = 0; i < Cxx.length; i++) {
            double t = ct0;
            for (int j = 0; j < y.getLength() - i; j++) {
                if (!x.getValues().isMissing(i + j) && !y.getValues().isMissing(j)) {
                    t += x.get(i + j) * y.get(j);
                }
            }
            Cxx[i] = t * bn;
        }
    }

    private void cornorm(double[] Cxx, double[] Cn, double cx0, double cy0) {
        double ds = 1.0 / Math.sqrt(cx0 * cy0);
        for (int i = 0; i < Cxx.length; i++) {
            Cn[i] = Cxx[i] * ds;
        }
    }

    private boolean satutco(TsData x, double[] Cxx, double R) {
        boolean good = true;
        DescriptiveStatistics bs = new DescriptiveStatistics(x.getValues());
        x = x.minus(bs.getAverage());
        if (R > 0.0) {
            taper(x, R);
        }
        crosco(x, x, Cxx);
        double cx0 = Cxx[0];
        if (cx0 == 0.0) {
            good = false;
        } else {
            double[] Cn = new double[Cxx.length];
            cornorm(Cxx, Cn, cx0, cx0);
        }
        return good;
    }

    private int sicp2(double[] Cxx, int len, double[] Coef, double[] stat) {
        double sd = 0.0;
        double aic = 0.0;
        int nAr = 0;
        int n = len;
        double cst1 = 1.0;
        double cst2 = 2.0;
        double cst01 = 0.00001;
        int L = Coef.length - 1;
        sd = Cxx[0];
        aic = n * Math.log(sd);
        double laic = aic;
        double lsd = sd;
        nAr = 0;
        double se = Cxx[1];
        double[] a = new double[L];
        double[] b = new double[L];
        java.util.Arrays.fill(a, 0.0);
        java.util.Arrays.fill(b, 0.0);
        for (int m = 0; m < L; m++) {
            double sdr = sd / Cxx[0];
            if (sdr >= cst01) {
                double d = se / sd;
                a[m] = d;
                sd = (cst1 - d * d) * sd;
                aic = n * Math.log(sd) + cst2 * (m + 1);
                if (m != 0) {
                    for (int i = 0; i <= m - 1; i++) {
                        a[i] -= d * b[i];
                    }
                }
                for (int i = 0; i <= m; i++) {
                    b[i] = a[m - i];
                }
                if (laic > aic) {
                    laic = aic;
                    lsd = sd;
                    nAr = m;

                }
                if (m != L) {
                    se = Cxx[m + 2];
                    for (int i = 0; i <= m; i++) {
                        se -= b[i] * Cxx[i + 1];
                    }
                }
            }
        }
        stat[0] = sd;
        stat[1] = aic;
        nAr = L;
        for (int i = 0; i < L; i++) {
            Coef[i] = -a[i];
        }
        return nAr;
    }

    private void fouger(double[] Ar, double[] fcos, double[] fsin) {
        int lAr = Ar.length - 1;
        double cst0 = 0.0;
        if (lAr > 0) {
            Arrays2.reverse(Ar);
        }
        for (int k = 0; k < fcos.length; k++) {
            double tk = Math.PI * 2.0 * Frq[k];
            double ck = Math.cos(tk);
            double sk = Math.sin(tk);
            double um1 = cst0;
            double um2 = cst0;
            for (int i = 0; i < lAr; i++) {
                double um0 = 2 * ck * um1 - um2 + Ar[i];
                um2 = um1;
                um1 = um0;
            }
            fcos[k] = ck * um1 - um2 + Ar[lAr];
            fsin[k] = sk * um1;
        }
    }

    private void snrasp(double[] Ar, double[] Ma, double sd, int nAr, int nMa, int h1) {
        double[] g;
        double cst1 = 1.0;
        if (nAr > 0) {
            for (int i = 0; i <= nAr; i++) {
                Ar[i] = -Ar[i];
            }
            g = new double[nAr + 1];
            g[0] = cst1;
            for (int i = 0; i < nAr; i++) {
                g[i + 1] = -Ar[i];
            }
        } else {
            g = new double[1];
            g[0] = cst1;
        }
        double[] fcos = new double[h1];
        double[] fsin = new double[h1];
        fouger(g, fcos, fsin);
        g = null;
        if (nMa > 0) {
            g = new double[nMa + 1];
            g[0] = cst1;
            System.arraycopy(Ma, 0, g, 1, nMa);
        } else {
            g = new double[1];
            g[0] = cst1;
        }
        double[] fcos1 = new double[h1];
        double[] fsin1 = new double[h1];
        fouger(g, fcos1, fsin1);
        for (int i = 0; i < h1; i++) {
            double t = (fcos1[i] * fcos1[i] + fsin1[i] * fsin1[i]) / (fcos[i] * fcos[i] + fsin[i] * fsin[i]) * sd;
            Spect[i] = Math.log10(Math.abs(t)) * 10.0;

        }
    }

    private void spgrh(TsData diff, double Thtapr, boolean good) {
        double sd = 0.0;
        int nAr = 0;
        double aic = 0.0;
        int n = diff.getLength();
        int h = nfrq - 1;
        int h1 = h + 1;
        TsData x = diff.clone();
        int lagh1 = Math.min(n - 1, nfrq - 1) + 1;
        double[] Cxx = new double[lagh1];
        good = satutco(x, Cxx, Thtapr);
        if (!good) {
            return;
        }
        int ifpl1 = Math.min(30, n - 1) + 1;
        double[] Ar = new double[ifpl1];
        java.util.Arrays.fill(Ar, 0.0);
        double[] stat = new double[2];
        nAr = sicp2(Cxx, n, Ar, stat);
        int nMa = 0;
        double[] Ma = new double[ifpl1];
        java.util.Arrays.fill(Ma, 0.0);
        snrasp(Ar, Ma, stat[0], nAr, nMa, h1);
    }

    private int indexGE(double val, double[] ValArray) {
        int i = 0;
        while (i < ValArray.length) {
            if (ValArray[i] <= val) {
                i++;
            } else {
                return i;
            }
        }
        return i;
    }

    private int pARpeak(double[] Spect, int pkIdx, double Rango, double limit, double[] Ppeaks, int[] Peaks) {
        int[] pkvec = {43, 53, 36, 42, 11, 21, 31, 41, 51, 61, 31, 61, 21, 41, 61, 61, 41};
        int[] pkptr = {1, 3, 5, 11, 13, 16, 17, 18};
       double[] silHf = {
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
        double[] silHm = {
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
        Arrays.fill(Ppeaks, 0.0);
        int cont = 0;
        for (int i = pkptr[pkIdx]; i <= pkptr[pkIdx + 1] - 1; i++) {
            int j = i - pkptr[pkIdx]; //removed +1
            int freq = pkvec[i - 1] - 1; // added -1
            Ppeaks[j] = 0.0;
            if (Spect[freq] > limit) {
                double incH = (Spect[freq] - Spect[freq - 1]) / Rango;
                if (freq != 60) //in Fortran 61
                {
                    double incH2 = (Spect[freq] - Spect[freq + 1]) / Rango;
                    if (incH > incH2) {
                        incH = incH2;
                    }
                    if (incH > 0.0) {
                        Ppeaks[j] = 0.8 + 0.002 * indexGE(incH, silHm);
                    }
                } else {
                    if (incH > 0.0) {
                        Ppeaks[j] = 0.8 + 0.002 * indexGE(incH, silHf);
                    }

                }
            }
            if (Ppeaks[j] > 0.95) {
                Peaks[cont] = freq;
                cont++;
            } else {
                Ppeaks[j] = 0.0;
            }
        }
        return cont;
    }

    private void computePeaks1(TsData diff) {
        Frq = new double[nfrq];
        int frqidx = 0;
        for (int i = 0; i < nfrq; i++) {
            Frq[i] = i / 120.0;
        }
        if (serie.getFrequency() == TsFrequency.Monthly) {
            Frq[41] = 0.3482 - Frq[1];
            Frq[42] = 0.3482;
            Frq[43] = 0.3482 + Frq[1];
            Frq[51] = 0.432 - Frq[1];
            Frq[52] = 0.432;
            Frq[53] = 0.432 + Frq[1];
            frqidx = 0;
        } else {
            Frq[34] = 0.29465 - Frq[1];
            Frq[35] = 0.29465;
            Frq[36] = 0.29465 + Frq[1];
            Frq[40] = 0.3393 - Frq[1];
            Frq[41] = 0.3393;
            Frq[42] = 0.3393 + Frq[1];
            frqidx = 1;
        }
        Spect = new double[nfrq];
        spgrh(diff, 0.0, true);
        double[] tmpSpect = new double[Spect.length];
        System.arraycopy(Spect, 0, tmpSpect, 0, Spect.length);
        Arrays.sort(tmpSpect);
        double Rango = tmpSpect[tmpSpect.length - 1] - tmpSpect[0];
        SPeaks = new double[6];
        SPeaksIdx = new int[6];
        TDPeaks = new double[6];
        TDPeaksIdx = new int[6];
        if (diff.getFrequency() == TsFrequency.Monthly || diff.getFrequency() == TsFrequency.Quarterly) {
            nTDPeaks = pARpeak(Spect, frqidx, Rango, tmpSpect[30], TDPeaks, TDPeaksIdx);
            nSPeaks = pARpeak(Spect, frqidx + 2, Rango, tmpSpect[30], SPeaks, SPeaksIdx);
        } else {
            if (diff.getFrequency() == TsFrequency.HalfYearly) {
                nSPeaks = pARpeak(Spect, 5, Rango, tmpSpect[30], SPeaks, SPeaksIdx);
            } else {
                if (diff.getFrequency() == TsFrequency.BiMonthly) {
                    nSPeaks = pARpeak(Spect, 6, Rango, tmpSpect[30], SPeaks, SPeaksIdx);
                } else {
                    if (diff.getFrequency() == TsFrequency.QuadriMonthly) {
                        nSPeaks = pARpeak(Spect, 7, Rango, tmpSpect[30], SPeaks, SPeaksIdx);
                    } else {
                        nSPeaks = 0;
                    }
                }
            }
        }
    }

    private void computePeaks() {
        int n1 = 0;
        if (serie.getLength() > spLen) {
            n1 = serie.getLength() - spLen;
        }
        TsData diff = serie.drop(n1, 0);
        if (delta) {
            diff = serie.delta(1);
        }
        DescriptiveStatistics bs = new DescriptiveStatistics(diff.getValues());
        double va = bs.getVar();
        computePeaks1(diff);
        for (int i = 0; i < Spect.length; i++) {
            Spect[i] = Math.exp(Spect[i] * Math.log(10.0) / 10.0);
            Frq[i] *= Math.PI * 2.0;
        }

    }
}
