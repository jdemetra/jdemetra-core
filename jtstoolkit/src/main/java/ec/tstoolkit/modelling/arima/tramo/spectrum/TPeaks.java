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

import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.Arrays;

/**
 *
 * @author gianluca
 */
public class TPeaks {

    private double[] Spect;

    public double[] getSPeaks() {
        return SPeaks;
    }

    public double[] getSpect() {
        return Spect;
    }

    public double getTDPeaks() {
        return TDPeaks;
    }

    public int getnSPeaks() {
        return nSPeaks;
    }
    private double TDPeaks;
    private double[] SPeaks;
    private int nSPeaks;
    private TsData serie;

    public TPeaks(TsData targetS) {
        serie = targetS;
        ComputeTPeaks();
    }

    private static double parzen(int idx, int size) {
        double tmp = idx / (double) size;
        if (idx <= size / 2) {
            return 1.0 - 6.0 * Math.pow(tmp, 2.0) + 6 * Math.pow(tmp, 3.0);
        } else {
            return 2 * Math.pow(1.0 - tmp, 3);
        }
    }

    public static double[] getWindow(WinType type, int winSize) {
        double[] window = new double[winSize];
        switch (type) {
            case Welch:
                for (int i = 0; i < winSize; i++) {
                    window[i] = 1.0 - (i / (double) winSize) * (i / (double) winSize);
                }
                break;
            case Tukey:
                for (int i = 0; i < winSize; i++) {
                    window[i] = 0.5 * (1 + Math.cos(Math.PI * i / (double) winSize));
                }
                break;
            case Bartlett:
                for (int i = 0; i < winSize; i++) {
                    window[i] = 1.0 - i / (double) winSize;
                }
                break;
            case Hamming:
                for (int i = 0; i < winSize; i++) {
                    window[i] = 0.54 + 0.46 * Math.cos(Math.PI * i / (double) winSize);
                }
                break;
            case Parzen:
                for (int i = 0; i < winSize; i++) {
                    window[i] = parzen(i, winSize);
                }
                break;
            case Square:
                for (int i = 0; i < winSize; i++) {
                    window[i] = 1.0;
                }
                break;
        }
        return window;
    }

    public static void covWind(TsData ser, double[] spect, double[] window) {
        int Win_Size = window.length;
        double[] Cxx = new double[Win_Size + 1];
        Peaks.crosco(ser, ser, Cxx);
        Arrays.fill(spect, 0.0);
        for (int i = 0; i < Win_Size; i++) {
            spect[0] += Cxx[i] * window[i];
        }
        for (int i = 1; i <= Win_Size / 2 + 1; i++) {
            spect[i] = Cxx[0] * window[0];
            for (int j = 1; j < Win_Size; j++) {
                spect[i] += 2.0 * Cxx[j] * window[j] * Math.cos((Math.PI * 2.0 * j * i / Win_Size));
            }

        }
    }

    private double[] dfPeaks(int Win_Size) {
        int Nz = serie.getLength();
        double[] retVal = new double[4];
        retVal[0] = 0.0;
        retVal[1] = 0.0;
        retVal[2] = 0.0;
        retVal[3] = 0.0;
        if (Win_Size == 112) {
            double[][] df = {{0.54630, 2.93030, 2.2042}, {1.1329, 7.6924, 10.8795}, {-.3492, 1.533, 2.7696}, {0.9829, 3.8217, 6.9345}};
            double n100 = Nz / 100.0;
            double n_100 = 100.0 / Nz;
            retVal[0] = df[0][0] + df[0][1] * n100 + df[0][2] * n_100;
            retVal[1] = df[1][0] + df[1][1] * n100 + df[1][2] * n_100;
            retVal[2] = df[2][0] + df[2][1] * n100 + df[2][2] * n_100;
            retVal[3] = df[3][0] + df[3][1] * n100 + df[3][2] * n_100;
        } else if (Win_Size == 44) {
            double n100 = Nz / 100.0;
            double n_100 = 100.0 / Nz;
            double[][] df = {{1.3779, 7.2620, 0.3725}, {3.1495, 18.0654, 3.5564}, {0.2504, 3.6616, 0.7929}, {0.504, 9.7201, 3.0605}};
            retVal[0] = df[0][0] + df[0][1] * n100 + df[0][2] * n_100;
            retVal[1] = df[1][0] + df[1][1] * n100 + df[1][2] * n_100;
            retVal[2] = df[2][0] + df[2][1] * n100 + df[2][2] * n_100;
            retVal[3] = df[3][0] + df[3][1] * n100 + df[3][2] * n_100;
        } else if (Win_Size == 79) {
            retVal[0] = 6.35251;
            retVal[1] = 19.6308;
            retVal[2] = 2.29316;
            retVal[3] = 6.55412;
        }
        return retVal;
    }

    static double BetaCfra(double a, double b, double x) {
        double d = 1.0 - (a + b) * x / (a + 1);
        if (Math.abs(d) < Double.MIN_VALUE) {
            d = Double.MAX_VALUE;
        }
        d = 1.0 / d;
        double c = 1.0;
        double f = d;
        for (int m = 1; m <= 1000; m++) {
            // even step            
            double m2 = 2.0 * m;
            double aa = m * (b - m) * x / ((a - 1.0 + m2) * (a + m2));
            d = 1.0 + aa * d;
            if (Math.abs(d) < Double.MIN_VALUE) {
                d = Double.MAX_VALUE;
            }
            d = 1.0 / d;
            c = 1.0 + aa / c;
            if (Math.abs(c) < Double.MIN_VALUE) {
                c = Double.MIN_VALUE;
            }
            f = f * d * c;
            // odd step
            aa = -(a + m) * (a + b + m) * x / ((a + m2) * (a + 1.0 + m2));
            d = 1.0 + aa * d;
            if (Math.abs(d) < Double.MIN_VALUE) {
                d = Double.MIN_VALUE;
            }
            d = 1.0 / d;
            c = 1.0 + aa / c;
            if (Math.abs(c) < Double.MIN_VALUE) {
                c = Double.MIN_VALUE;
            }
            double del = d * c;
            f = f * d * c;
            if (Math.abs(del - 1.0) < 1.0e-7) {
                return f;
            }
        }
        return f;
    }

    static double LogGamma(double a) {
        double[] c = {76.18009172947146e0, -86.50532032941677e0,
            24.01409824083091e0, -1.231739572450155e0,
            .1208650973866179e-2, -.5395239384953e-5};
        double y = a;
        double tmp = a + 5.5;
        tmp = (a + 0.5) * Math.log(tmp) - tmp;
        double sum = 1.000000000190015;
        for (int i = 0; i < 6; i++) {
            y += 1.0;
            sum += c[i] / y;
        }
        return tmp + Math.log(2.5066282746310005 * sum / a);

    }

    static double BetaInc(double x, double a, double b) {
        if (x <= 0.0) {
            return 0.0;
        }
        if (x >= 1.0) {
            return 1.0;
        }
        double bt = Math.exp(LogGamma(a + b) - LogGamma(a) - LogGamma(b) + a * Math.log(x) + b * Math.log(1.0 - x));
        if (x < (a + 1.0) / (a + b + 2.0)) {
            return bt * BetaCfra(a, b, x) / a;
        } else {
            return 1.0 - bt * BetaCfra(b, a, 1.0 - x) / b;
        }

    }

    public static double Fcdf(double F, double x, double y) {
        return 1.0 - BetaInc(y / (y + x * F), y / 2, x / 2);
    }

    private void Tpeaks2(int Win_Size) {
        double[] retVal;
        int[] indSmid;
        int indTD = 0;
        int indPI = 0;
        retVal = dfPeaks(Win_Size);
        indSmid = new int[5];
        int indSmidLen = 0;
        Arrays.fill(indSmid, 0);
        switch (Win_Size) {
            case 112:
                indSmid[0] = 10;
                indSmid[1] = 20;
                indSmid[2] = 29;
                indSmid[3] = 38;
                indSmid[4] = 48;
                indSmidLen = 5;
                indTD = 40;
                indPI = 57;
                break;
            case 79:
                indSmid[0] = 8;
                indSmid[1] = 14;
                indSmid[2] = 21;
                indSmid[3] = 27;
                indSmid[4] = 34;
                indTD = 29;
                indPI = 40;
                indSmidLen = 5;
                break;
            default:
                indTD = -1;
                indPI = 22;
                switch (serie.getFrequency()) {
                    case HalfYearly:
                        indSmid[0] = 0;
                        indSmid[1] = 15;
                        indSmidLen = 2;
                        break;
                    case Quarterly:
                        indTD = 14;
                        indSmid[0] = 12;
                        indSmidLen = 1;
                        break;
                    case QuadriMonthly:
                        indPI = -1;
                        indSmid[0] = 15;
                        indSmidLen = 1;
                        break;
                    case Yearly:
                        indPI = -1;
                        break;
                }
                break;
        }
        if (indTD > 0) {
            double incH = 2.0 * Spect[indTD - 1] / (Spect[indTD] + Spect[indTD - 2]);
            TDPeaks = Fcdf(incH, retVal[0], retVal[1]);
        }
        nSPeaks = indSmidLen;
        for (int i = 0; i < indSmidLen; i++) {
            double incH = 2.0 * Spect[indSmid[i] - 1];
            incH = incH / (Spect[indSmid[i]] + Spect[indSmid[i] - 2]);
            SPeaks[i] = Fcdf(incH, retVal[0], retVal[1]);
        }
        if (indPI > 0) {
            double incH = Spect[indPI - 1] / Spect[indPI - 2];
            SPeaks[serie.getFrequency().intValue() / 2 - 1] = Fcdf(incH, retVal[2], retVal[3]);
        }
    }

    private void ComputeTPeaks() {
        int Win_Size = 0;
        if ((serie.getFrequency() != TsFrequency.Monthly) && (serie.getLength() >= 60)) {
            Win_Size = 44;
        } else if ((serie.getFrequency() == TsFrequency.Monthly) && (serie.getLength() >= 120)) {
            Win_Size = 112;
        } else if ((serie.getFrequency() == TsFrequency.Monthly) && (serie.getLength() >= 80)) {
            Win_Size = 79;
        } else {
            Win_Size = -1;
            Spect = null;
            SPeaks = null;
            nSPeaks = 0;
            TDPeaks = 0.0;
            return;
        }
        Spect = new double[60];
        double[] window = getWindow(WinType.Tukey, Win_Size);
        covWind(serie, Spect, window);
        TDPeaks = 0.0;
        SPeaks = new double[6];
        Arrays.fill(SPeaks, 0.0);
        Tpeaks2(Win_Size);
    }
}
