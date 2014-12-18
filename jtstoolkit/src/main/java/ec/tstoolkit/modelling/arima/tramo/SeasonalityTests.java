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
package ec.tstoolkit.modelling.arima.tramo;

import ec.satoolkit.diagnostics.AutoRegressiveSpectrumTest;
import ec.satoolkit.diagnostics.FriedmanTest;
import ec.satoolkit.diagnostics.PeriodogramTest;
import ec.satoolkit.diagnostics.QSTest;
import ec.satoolkit.diagnostics.TukeySpectrumPeaksTest;
import ec.tstoolkit.data.BlackmanTukeySpectrum;
import ec.tstoolkit.data.WindowType;
import ec.tstoolkit.modelling.DifferencingResults;
import ec.tstoolkit.stats.StatisticalTest;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;

/**
 *
 * @author Jean Palate
 */
public class SeasonalityTests {

    public static final int MSHORT = 80, SHORT = 60;

    /**
     * This test corresponds to the OverResidSeasTest function of TRAMO
     *
     * @param res The residuals of the model
     * @param freq The frequency of the series
     * @return
     */
    public static SeasonalityTests residualSeasonalityTest(double[] res, TsFrequency freq) {
        SeasonalityTests tests = new SeasonalityTests();
        tests.testResiduals(res, freq);
        // compute the score
        StatisticalTest qs = tests.getQs();
        if (qs != null && qs.isSignificant()) // 9.21 at the 0.01 level
        {
            tests.score++;
        }
        FriedmanTest np = tests.getNonParametricTest();
        if (np != null && np.isSignificant()) // 24.725 at the 0.01 level (freq=12)
        {
            tests.score++;
        }
        int n = tests.getDifferencing().differenced.getLength();
        if (n >= MSHORT || (freq != TsFrequency.Monthly && n >= SHORT)) {
            if (SpectralPeaks.hasSeasonalPeaks(tests.getSpectralPeaks())) {
                tests.score++;
            }
        }
        return tests;
    }

    /**
     * @param s The original series
     * @param diff The differencing order (-1 if it is automatically detected)
     * @param mean Mean correction of the differenced series
     * @param all Executed all the tests or stop when one of them is significant
     * @return
     */
    public static SeasonalityTests seasonalityTest(TsData s, int diff, boolean mean, boolean all) {
        if (s.getFrequency() == TsFrequency.Yearly) {
            return null;
        }
        SeasonalityTests tests = new SeasonalityTests();
        tests.test(s, diff, mean);
        // compute the score
        StatisticalTest qs = tests.getQs();
        if (qs != null && qs.isSignificant()) // 9.21 at the 0.01 level
        {
            tests.score++;
            if (!all) {
                return tests;
            }
        }
        FriedmanTest np = tests.getNonParametricTest();
        if (np != null && np.isSignificant()) // 24.725 at the 0.01 level (freq=12)
        {
            tests.score++;
            if (!all) {
                return tests;
            }
        }
        int n = tests.getDifferencing().differenced.getLength();
        if (n >= MSHORT || (s.getFrequency() != TsFrequency.Monthly && n >= SHORT)) {
            if (SpectralPeaks.hasSeasonalPeaks(tests.getSpectralPeaks())) {
                tests.score++;
                if (!all) {
                    return tests;
                }
            }
        }
        return tests;
    }

    /**
     *
     * @param input Original series
     * @param ndiff Differencing order
     * @param mean Mean correction (after differencing)
     * @return
     */
    void test(TsData input, int ndiff, boolean mean) {
        delta = DifferencingResults.create(input, ndiff, mean);
        clear();
    }

    /**
     * The differencing order and the mean correction are chosen by the
     * algorithm.
     *
     * @param input Original series
     * @return
     */
    private void test(TsData input) {
        delta = DifferencingResults.create(input, -1, true);
        clear();
    }

    private void testResiduals(double[] res, TsFrequency freq) {
        // we create a "temporary" time series. Dates don't matter
        TsData tmp = new TsData(freq, 2000, 0, res, false);
        delta = DifferencingResults.create(tmp, 0, false);
        clear();
    }
    private DifferencingResults delta;
    private FriedmanTest nptest;
    private BlackmanTukeySpectrum btSpectrum;
    private TukeySpectrumPeaksTest tpeaks;
    private AutoRegressiveSpectrumTest arpeaks;
    private SpectralPeaks[] peaks;
    private StatisticalTest qs, periodogram;
    private int score;

    private int nqs_ = 2;

    public void setQSCount(int nqs) {
        if (nqs != nqs_) {
            nqs_ = nqs;
            qs = null;
        }
    }

    public int getQSCount() {
        return nqs_;
    }

    public DifferencingResults getDifferencing() {
        return delta;
    }

    // Lazy evaulation
    public FriedmanTest getNonParametricTest() {
        if (nptest == null) {
            nptest = new FriedmanTest(delta.differenced);
        }
        return nptest;
    }

    public TukeySpectrumPeaksTest getTukeyPeaks() {
        if (tpeaks == null) {
            tpeaks = new TukeySpectrumPeaksTest();
            if (!tpeaks.test(delta.differenced)) {
                tpeaks = null;
            }
        }
        return tpeaks;
    }

    public BlackmanTukeySpectrum getBlackmanTukeySpectrum() {
        if (btSpectrum == null) {
            btSpectrum = new BlackmanTukeySpectrum();
            btSpectrum.setWindowType(WindowType.Tukey);
            btSpectrum.setData(delta.differenced.getValues().internalStorage());
            int ifreq = delta.differenced.getFrequency().intValue();
            int n = delta.differenced.getLength();
            int wlen = 3 * n / 4 / ifreq;
            if (wlen > 11) {
                wlen = 11;
            }
            btSpectrum.setWindowLength(wlen * ifreq);
        }
        return btSpectrum;
    }

    public AutoRegressiveSpectrumTest getArPeaks() {
        if (arpeaks == null) {
            arpeaks = new AutoRegressiveSpectrumTest();
            if (!arpeaks.test(delta.differenced)) {
                arpeaks = null;
            }
        }
        return arpeaks;
    }

    public SpectralPeaks[] getSpectralPeaks() {
        if (peaks == null) {
            int ifreq = delta.original.getFrequency().intValue();
            AutoRegressiveSpectrumTest arPeaks = getArPeaks();
            TukeySpectrumPeaksTest tPeaks = getTukeyPeaks();
            if (arPeaks == null || tPeaks == null) {
                return null;
            }
            int[] a = arPeaks != null ? arPeaks.seasonalPeaks(.90, .99) : null;
            int[] t = tPeaks != null ? tPeaks.seasonalPeaks(.90, .99) : null;
            peaks = new SpectralPeaks[ifreq / 2];
            for (int i = 0; i < peaks.length; ++i) {
                SpectralPeaks.AR ar = SpectralPeaks.AR.none;
                SpectralPeaks.Tukey tu = SpectralPeaks.Tukey.none;
                if (a != null) {
                    ar = SpectralPeaks.AR.fromInt(a[i]);
                }
                if (t != null) {
                    tu = SpectralPeaks.Tukey.fromInt(t[i]);
                }
                peaks[i] = new SpectralPeaks(ar, tu);
            }
        }
        return peaks;

    }

    public StatisticalTest getQs() {
        if (qs == null) {
            qs = QSTest.compute(delta.differenced.getValues().internalStorage(), delta.differenced.getFrequency().intValue(), nqs_);
        }
        return qs;
    }

    public StatisticalTest getPeriodogramTest() {
        if (periodogram == null) {
            periodogram = PeriodogramTest.computeSum(delta.differenced.getValues(), delta.differenced.getFrequency().intValue());
        }
        return periodogram;
    }

    public int getScore() {
        return score;
    }

    private void clear() {
        nptest = null;
        arpeaks = null;
        tpeaks = null;
        qs = null;
        periodogram = null;
        score = 0;
    }
}
