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
package ec.satoolkit.diagnostics;

import ec.tstoolkit.data.BlackmanTukeySpectrum;
import ec.tstoolkit.data.ITaper;
import ec.tstoolkit.dstats.SpecialFunctions;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author Jean Palate
 */
public class TukeySpectrumPeaksTest {

    private final BlackmanTukeySpectrum tukey = new BlackmanTukeySpectrum();
    private double[] sinc;
    private double[] dfpeaks;
    private double tdinc, spi;

    public TukeySpectrumPeaksTest() {
    }

    public void setTaper(ITaper taper) {
        tukey.setTaper(taper);
    }

    double getTdInc() {
        return tdinc;
    }

    public boolean isTdSignificant(double eps) {
        return SpecialFunctions.FProbability(tdinc, dfpeaks[0], dfpeaks[1]) < eps;
    }

    public double getTdProb() {
        return SpecialFunctions.FProbability(tdinc, dfpeaks[0], dfpeaks[1]);
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
        if (ifreq <= 0) {
            return false;
        }
        if (ifreq <= sinc.length) {
            return SpecialFunctions.FProbability(sinc[ifreq - 1], dfpeaks[0], dfpeaks[1]) < 1 - prob;
        }
        if (ifreq == sinc.length + 1 && ! Double.isNaN(spi) ) {
            return SpecialFunctions.FProbability(spi, dfpeaks[2], dfpeaks[3]) < 1 - prob;
        }
        return false;
    }

    public int getSignificantSeasonalPeaks(double eps) {
        int n = 0;
        for (int i = 0; i < sinc.length; ++i) {
            if (sinc[i] > 0 && SpecialFunctions.FProbability(sinc[i], dfpeaks[0], dfpeaks[1]) < eps) {
                ++n;
            }
        }
        if (! Double.isNaN(spi) && SpecialFunctions.FProbability(spi, dfpeaks[2], dfpeaks[3]) < eps) {
            ++n;
        }
        return n;
    }

    public double[] getSeasonalProbs() {
        double[] p = new double[sinc.length + (Double.isNaN(spi) ? 0 : 1)];
        for (int i = 0; i < sinc.length; ++i) {
            if (sinc[i] > 0) {
                p[i] = SpecialFunctions.FProbability(sinc[i], dfpeaks[0], dfpeaks[1]);
            }
        }
        if (! Double.isNaN(spi)) {
            p[sinc.length] = SpecialFunctions.FProbability(spi, dfpeaks[2], dfpeaks[3]);
        }

        return p;
    }

    /**
     * Returns the presence of seasonal peaks 0 = no peak, 1 = significative
     * peak, 2= very significative peak
     *
     * @param plow low sensitivity -> 1
     * @param phigh high sensitivity -> 2
     * @return
     */
    public int[] seasonalPeaks(double plow, double phigh) {
        int[] rslt = new int[sinc.length + (Double.isNaN(spi) ? 0 : 1)];
        for (int i = 0; i < sinc.length; ++i) {
            if (sinc[i] > 0) {
                double p = 1 - SpecialFunctions.FProbability(sinc[i], dfpeaks[0], dfpeaks[1]);
                if (p > phigh) {
                    rslt[i] = 2;
                } else if (p > plow) {
                    rslt[i] = 1;
                }
            }
        }
        if (! Double.isNaN(spi)) {
            double p = 1 - SpecialFunctions.FProbability(spi, dfpeaks[2], dfpeaks[3]);
            if (p > phigh) {
                rslt[sinc.length] = 2;
            } else if (p > plow) {
                rslt[sinc.length] = 1;
            }
        }
        return rslt;
    }

    private void calcPeaks(BlackmanTukeySpectrum spectrum, int freq) {
        spi=Double.NaN;
        sinc=new double[0];
        int winlen = spectrum.getWindowLength();
        int[] indSmid;
        int indTD = -1;
        int indPI = -1;
        double[] s = spectrum.getSpectrum();

        switch (winlen) {
            case 112:
                indSmid = new int[]{9, 19, 28, 37, 47};
                indTD = 39;
                indPI = 56;
                break;
            case 79:
                indSmid = new int[]{7, 13, 20, 26, 33};
                indTD = 28;
                indPI = 39;
                break;
            default:
                switch (freq) {
                    case 6:
                        indSmid = new int[]{7, 14};
                        indPI = 21;
                        break;
                    case 4:
                        indTD = 13;
                        indSmid = new int[]{11};
                        indPI = 21;
                        break;
                    case 3:
                        indSmid = new int[]{14};
                        break;
                    default:
                        indSmid = new int[0];
                        break;
                }
                break;
        }
        if (indTD > 0) {
            tdinc = 2.0 * s[indTD - 1] / (s[indTD] + s[indTD - 2]);
        }
        sinc = new double[indSmid.length];
        for (int i = 0; i < indSmid.length; i++) {
            double incH = 2.0 * s[indSmid[i]];
            sinc[i] = incH / (s[indSmid[i] + 1] + s[indSmid[i] - 1]);
        }
        if (indPI > 0) {
            double denom=s[indPI - 1];
            spi = denom != 0 ? s[indPI] / s[indPI - 1] : s[indPI] ;
        }
    }

    private double[] calcDfPeaks(BlackmanTukeySpectrum spectrum) {
        int ndata = spectrum.getData().length, winlen = spectrum.getWindowLength();
        double[] retVal = new double[4];
        retVal[0] = 0.0;
        retVal[1] = 0.0;
        retVal[2] = 0.0;
        retVal[3] = 0.0;
        if (winlen == 112) {
            double[][] df = {{0.54630, 2.93030, 2.2042}, {1.1329, 7.6924, 10.8795}, {-.3492, 1.533, 2.7696}, {0.9829, 3.8217, 6.9345}};
            double n100 = ndata / 100.0;
            double n_100 = 100.0 / ndata;
            retVal[0] = df[0][0] + df[0][1] * n100 + df[0][2] * n_100;
            retVal[1] = df[1][0] + df[1][1] * n100 + df[1][2] * n_100;
            retVal[2] = df[2][0] + df[2][1] * n100 + df[2][2] * n_100;
            retVal[3] = df[3][0] + df[3][1] * n100 + df[3][2] * n_100;
        } else if (winlen == 44) {
            double n100 = ndata / 100.0;
            double n_100 = 100.0 / ndata;
            double[][] df = {{1.3779, 7.2620, 0.3725}, {3.1495, 18.0654, 3.5564}, {0.2504, 3.6616, 0.7929}, {0.504, 9.7201, 3.0605}};
            retVal[0] = df[0][0] + df[0][1] * n100 + df[0][2] * n_100;
            retVal[1] = df[1][0] + df[1][1] * n100 + df[1][2] * n_100;
            retVal[2] = df[2][0] + df[2][1] * n100 + df[2][2] * n_100;
            retVal[3] = df[3][0] + df[3][1] * n100 + df[3][2] * n_100;
        } else if (winlen == 79) {
            retVal[0] = 6.35251;
            retVal[1] = 19.6308;
            retVal[2] = 2.29316;
            retVal[3] = 6.55412;
        }
        return retVal;
    }

    public boolean test(TsData s) {
        clear();
        tukey.setData(s.internalStorage());
        int freq = s.getFrequency().intValue();
        tukey.setWindowLength(windowLength(s));
        if (!tukey.isValid()) {
            return false;
        }
        // compute statistics;
        computeStatistics(freq);
        return true;
    }

    private void computeStatistics(int freq) {
        dfpeaks = calcDfPeaks(tukey);
        calcPeaks(tukey, freq);
    }

    private int windowLength(TsData s) {
        int ndata = s.getLength(), freq = s.getFrequency().intValue();
        if (freq != 12 && ndata >= 45) {
            return 44;
        } else if (freq == 12 && ndata >= 120) {
            return 112;
        } else if (freq == 12 && ndata >= 80) {
            return 79;
        } else {
            return -1;
        }
    }

    private void clear() {
        this.sinc = null;
        this.dfpeaks = null;
        this.spi = Double.NaN;
        this.tdinc = 0;
    }
}
