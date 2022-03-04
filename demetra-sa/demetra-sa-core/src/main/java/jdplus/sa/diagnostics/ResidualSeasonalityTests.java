/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.sa.diagnostics;

import demetra.data.DoubleSeq;
import jdplus.modelling.DifferencingResult;
import demetra.stats.StatisticalTest;
import demetra.timeseries.TsData;
import jdplus.sa.tests.AutoRegressiveSpectrumTest;
import jdplus.sa.tests.FTest;
import jdplus.sa.tests.Friedman;
import jdplus.sa.tests.KruskalWallis;
import jdplus.sa.tests.PeriodogramTest;
import jdplus.sa.tests.Qs;
import jdplus.sa.tests.SpectralPeaks;
import jdplus.sa.tests.TukeySpectrumPeaksTest;

/**
 *
 * @author palatej
 */
@lombok.Getter
public class ResidualSeasonalityTests {

    private final TsData series;
    private final DifferencingResult differencing;

    private final ResidualSeasonalityTestsOptions options;

    @lombok.Builder(builderClassName = "Builder")
    private ResidualSeasonalityTests(TsData series, int ndiff, boolean mean, ResidualSeasonalityTestsOptions options) {
        this.series = series;
        this.differencing = DifferencingResult.of(series.getValues(), series.getAnnualFrequency(), ndiff, mean);
        this.options = options;
    }

    @lombok.Getter(lombok.AccessLevel.PRIVATE)
    private volatile StatisticalTest fTest, qsTest, friedmanTest, kruskalWallisTest, periodogramTest;

    @lombok.Getter(lombok.AccessLevel.PRIVATE)
    @SuppressWarnings("VolatileArrayField")
    private volatile SpectralPeaks[] spectralPeaks;

    public int annualFrequency() {
        return series.getAnnualFrequency();
    }

    public StatisticalTest qsTest() {
        StatisticalTest test = qsTest;
        if (test == null) {
            synchronized (this) {
                test = qsTest;
                if (test == null) {
                    int ifreq = series.getAnnualFrequency();
                    DoubleSeq ds = differencing.getDifferenced();
                    if (options.getQslast() > 0) {
                        ds = ds.drop(Math.max(0, ds.length() - ifreq * options.getQslast()), 0);
                    }
                    test = new Qs(ds, ifreq)
                            .autoCorrelationsCount(options.getQsLags())
                            .build();
                    qsTest = test;
                }
            }
        }
        return test;
    }

    public StatisticalTest friedmanTest() {
        StatisticalTest test = friedmanTest;
        if (test == null) {
            synchronized (this) {
                test = friedmanTest;
                if (test == null) {
                    DoubleSeq ds = differencing.getDifferenced();
                    test = new Friedman(ds, annualFrequency())
                            .build();
                    friedmanTest = test;
                }
            }
        }
        return test;
    }

    public StatisticalTest kruskallWallisTest() {
        StatisticalTest test = kruskalWallisTest;
        if (test == null) {
            synchronized (this) {
                test = kruskalWallisTest;
                if (test == null) {
                    DoubleSeq ds = differencing.getDifferenced();
                    test = new KruskalWallis(ds, annualFrequency())
                            .build();
                    kruskalWallisTest = test;
                }
            }
        }
        return test;
    }

    public StatisticalTest periodogramTest() {
        StatisticalTest test = periodogramTest;
        if (test == null) {
            synchronized (this) {
                test = periodogramTest;
                if (test == null) {
                    DoubleSeq ds = differencing.getDifferenced();
                    test = new PeriodogramTest(ds, annualFrequency()).buildF();
                    periodogramTest = test;
                }
            }
        }
        return test;
    }

    public StatisticalTest fTest() {
        StatisticalTest test = fTest;
        if (test == null) {
            synchronized (this) {
                test = fTest;
                if (test == null) {
                    test = new FTest(series.getValues(), annualFrequency())
                            .model(options.getModelForFTest())
                            .ncycles(options.getFlast())
                            .build();
                    fTest = test;
                }
            }
        }
        return test;
    }

    public SpectralPeaks[] spectralPeaks() {
        SpectralPeaks[] sp = spectralPeaks;
        if (sp == null) {
            synchronized (this) {
                sp = spectralPeaks;
                if (sp == null) {
                    int ifreq = annualFrequency();
                    DoubleSeq ds = differencing.getDifferenced();
                    AutoRegressiveSpectrumTest arPeaks = new AutoRegressiveSpectrumTest();
                    TukeySpectrumPeaksTest tPeaks = new TukeySpectrumPeaksTest();
                    if (!arPeaks.test(ds, ifreq) || !tPeaks.test(ds, ifreq)) {
                        return null;
                    }
                    int[] a = arPeaks.seasonalPeaks(.90, .99);
                    int[] t = tPeaks.seasonalPeaks(.90, .99);
                    sp = new SpectralPeaks[ifreq / 2];
                    for (int i = 0; i < sp.length; ++i) {
                        SpectralPeaks.AR ar = SpectralPeaks.AR.none;
                        SpectralPeaks.Tukey tu = SpectralPeaks.Tukey.none;
                        if (a != null && a.length > i ) {
                            ar = SpectralPeaks.AR.fromInt(a[i]);
                        }
                        if (t != null && t.length > i) {
                            tu = SpectralPeaks.Tukey.fromInt(t[i]);
                        }
                        sp[i] = new SpectralPeaks(ar, tu);
                    }
                    spectralPeaks = sp;
                }
            }
        }
        return sp;
    }
}
