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
package jdplus.seats;

import demetra.data.DoubleSeq;
import demetra.data.DoublesMath;
import demetra.design.Development;
import demetra.modelling.ComponentInformation;
import demetra.sa.ComponentType;
import demetra.sa.DecompositionMode;
import demetra.sa.SeriesDecomposition;
import jdplus.dstats.LogNormal;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Temporary)
public class DefaultBiasCorrector implements IBiasCorrector {

    private final boolean bias;

    /**
     *
     */
    public DefaultBiasCorrector(boolean bias) {
        this.bias = bias;
    }

    /**
     *
     * @param model
     * @return
     */
    @Override
    public void correctBias(SeatsModel model) {
        if (model.isLogTransformation()) {
            correctLogs(model);
        } else {
            correctLevels(model);
        }
    }

    private DoubleSeq correctStdevForLog(DoubleSeq e, DoubleSeq s) {
        return e.fastOp(s, (stde, m) -> LogNormal.stdev2(m, stde));
    }

    private void correctLevels(SeatsModel model) {
        DoubleSeq y = model.getOriginalSeries();
        SeriesDecomposition.Builder decomp = SeriesDecomposition.builder(DecompositionMode.Additive)
                .add(y, ComponentType.Series);

        SeriesDecomposition idecomp = model.getInitialComponents();
        DoubleSeq s = idecomp.getSeries(ComponentType.Seasonal, ComponentInformation.Value);
        if (s != null) {
            decomp.add(s, ComponentType.Seasonal);
            DoubleSeq se = idecomp.getSeries(ComponentType.Seasonal, ComponentInformation.Stdev);
            if (se != null) {
                decomp.add(se, ComponentType.Seasonal, ComponentInformation.Stdev);
            }
        }
        DoubleSeq t = idecomp.getSeries(ComponentType.Trend, ComponentInformation.Value);
        if (t != null) {
            decomp.add(t, ComponentType.Trend);
            DoubleSeq te = idecomp.getSeries(ComponentType.Trend, ComponentInformation.Stdev);
            if (te != null) {
                decomp.add(te, ComponentType.Trend, ComponentInformation.Stdev);
            }
        }

        // correct SA =Y / S (-> *sbias)
        DoubleSeq sa = DoublesMath.subtract(y, s);
        decomp.add(sa, ComponentType.SeasonallyAdjusted);
        DoubleSeq sae = idecomp.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Stdev);
        if (sae != null) {
            decomp.add(sae, ComponentType.SeasonallyAdjusted, ComponentInformation.Stdev);
        }

        DoubleSeq i = DoublesMath.subtract(sa, t);
        decomp.add(i, ComponentType.Irregular);
        DoubleSeq ie = idecomp.getSeries(ComponentType.Irregular, ComponentInformation.Stdev);
        if (ie != null) {
            decomp.add(ie, ComponentType.Irregular, ComponentInformation.Stdev);
        }
        if (model.getForecastsCount() > 0) {
            fillForecasts(idecomp, decomp);
        }
        if (model.getBackcastsCount() > 0) {
            fillBackcasts(idecomp, decomp);
        }

        model.setFinalComponents(decomp.build());
    }

    private void fillForecasts(SeriesDecomposition idecomp, SeriesDecomposition.Builder decomp) {
        // idem forecasts
        DoubleSeq fy = idecomp.getSeries(ComponentType.Series, ComponentInformation.Forecast);
        DoubleSeq fs = idecomp.getSeries(ComponentType.Seasonal, ComponentInformation.Forecast);
        DoubleSeq ft = idecomp.getSeries(ComponentType.Trend, ComponentInformation.Forecast);
        DoubleSeq fi = idecomp.getSeries(ComponentType.Irregular, ComponentInformation.Forecast);
        DoubleSeq fsa;
        if (fy == null) {
            fy = DoublesMath.add(fs, ft, fi);
            fsa = DoublesMath.add(ft, fi);
        } else {
            fsa = DoublesMath.subtract(fy, fs);
        }

        if (fy != null) {
            decomp.add(fy, ComponentType.Series, ComponentInformation.Forecast);
            DoubleSeq fye = idecomp.getSeries(ComponentType.Series, ComponentInformation.StdevForecast);
            if (fye != null) {
                decomp.add(fye, ComponentType.Series, ComponentInformation.StdevForecast);
            }
        }
        if (fs != null) {
            decomp.add(fs, ComponentType.Seasonal, ComponentInformation.Forecast);
            DoubleSeq fse = idecomp.getSeries(ComponentType.Seasonal, ComponentInformation.StdevForecast);
            if (fse != null) {
                decomp.add(fse, ComponentType.Seasonal, ComponentInformation.StdevForecast);
            }
        }

        if (ft != null) {
            decomp.add(ft, ComponentType.Trend, ComponentInformation.Forecast);
            DoubleSeq fte = idecomp.getSeries(ComponentType.Trend, ComponentInformation.StdevForecast);
            if (fte != null) {
                decomp.add(fte, ComponentType.Trend, ComponentInformation.StdevForecast);
            }
        }

        if (fsa != null) {
            decomp.add(fsa, ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast);
            DoubleSeq fsae = idecomp.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast);
            if (fsae != null) {
                decomp.add(fsae, ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast);
            }
        }

        if (fi != null) {
            decomp.add(fi, ComponentType.Irregular, ComponentInformation.Forecast);
            DoubleSeq fie = idecomp.getSeries(ComponentType.Irregular, ComponentInformation.StdevForecast);
            if (fie != null) {
                decomp.add(fie, ComponentType.Irregular, ComponentInformation.StdevForecast);
            }
        }

    }

    private void fillBackcasts(SeriesDecomposition idecomp, SeriesDecomposition.Builder decomp) {
        // idem forecasts
        DoubleSeq fy = idecomp.getSeries(ComponentType.Series, ComponentInformation.Backcast);
        DoubleSeq fs = idecomp.getSeries(ComponentType.Seasonal, ComponentInformation.Backcast);
        DoubleSeq ft = idecomp.getSeries(ComponentType.Trend, ComponentInformation.Backcast);
        DoubleSeq fi = idecomp.getSeries(ComponentType.Irregular, ComponentInformation.Backcast);
        DoubleSeq fsa;
        if (fy == null) {
            fy = DoublesMath.add(fs, ft, fi);
            fsa = DoublesMath.add(ft, fi);
        } else {
            fsa = DoublesMath.subtract(fy, fs);
        }

        if (fy != null) {
            decomp.add(fy, ComponentType.Series, ComponentInformation.Backcast);
            DoubleSeq fye = idecomp.getSeries(ComponentType.Series, ComponentInformation.StdevBackcast);
            if (fye != null) {
                decomp.add(fye, ComponentType.Series, ComponentInformation.StdevBackcast);
            }
        }
        if (fs != null) {
            decomp.add(fs, ComponentType.Seasonal, ComponentInformation.Backcast);
            DoubleSeq fse = idecomp.getSeries(ComponentType.Seasonal, ComponentInformation.StdevBackcast);
            if (fse != null) {
                decomp.add(fse, ComponentType.Seasonal, ComponentInformation.StdevBackcast);
            }
        }

        if (ft != null) {
            decomp.add(ft, ComponentType.Trend, ComponentInformation.Backcast);
            DoubleSeq fte = idecomp.getSeries(ComponentType.Trend, ComponentInformation.StdevBackcast);
            if (fte != null) {
                decomp.add(fte, ComponentType.Trend, ComponentInformation.StdevBackcast);
            }
        }

        if (fsa != null) {
            decomp.add(fsa, ComponentType.SeasonallyAdjusted, ComponentInformation.Backcast);
            DoubleSeq fsae = idecomp.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevBackcast);
            if (fsae != null) {
                decomp.add(fsae, ComponentType.SeasonallyAdjusted, ComponentInformation.StdevBackcast);
            }
        }

        if (fi != null) {
            decomp.add(fi, ComponentType.Irregular, ComponentInformation.Backcast);
            DoubleSeq fie = idecomp.getSeries(ComponentType.Irregular, ComponentInformation.StdevBackcast);
            if (fie != null) {
                decomp.add(fie, ComponentType.Irregular, ComponentInformation.StdevBackcast);
            }
        }
    }

    private void correctLogs(SeatsModel model) {
        DoubleSeq y = model.getOriginalSeries();
        int period = model.getPeriod();
        SeriesDecomposition.Builder decomp = SeriesDecomposition
                .builder(DecompositionMode.Multiplicative)
                .add(y, ComponentType.Series);

        int n = y.length();
        int ny = n - n % period;

        SeriesDecomposition ldecomp = model.getInitialComponents();

        double ibias = 1, sbias = 1;
        DoubleSeq s = ldecomp.getSeries(ComponentType.Seasonal, ComponentInformation.Value);
        if (s != null) {
            s = s.exp();
            if (bias) {
                sbias = s.range(0, ny).average();
                s = s.times(1 / sbias);
            }

            decomp.add(s, ComponentType.Seasonal);
            DoubleSeq se = ldecomp.getSeries(ComponentType.Seasonal, ComponentInformation.Stdev);
            if (se != null) {
                decomp.add(correctStdevForLog(se, s), ComponentType.Seasonal, ComponentInformation.Stdev);
            }
        }
        DoubleSeq i = ldecomp.getSeries(ComponentType.Irregular, ComponentInformation.Value);
        if (i != null) {
            i = i.exp();
            if (bias) {
                ibias = i.average();
                i = i.times(1 / ibias);
            }
            decomp.add(i, ComponentType.Irregular);
            DoubleSeq ie = ldecomp.getSeries(ComponentType.Irregular, ComponentInformation.Stdev);
            if (ie != null) {
                decomp.add(correctStdevForLog(ie, i), ComponentType.Irregular, ComponentInformation.Stdev);
            }
        }
        // correct T = Y /S * I) (-> *sbias*ibias)
        DoubleSeq t = ldecomp.getSeries(ComponentType.Trend, ComponentInformation.Value);
        if (t != null) {
            t = t.exp();
            if (bias) {
                double tbias = sbias * ibias;
                t = t.times(tbias);
            }
            decomp.add(t, ComponentType.Trend);
            DoubleSeq te = ldecomp.getSeries(ComponentType.Trend, ComponentInformation.Stdev);
            if (te != null) {
                decomp.add(correctStdevForLog(te, t), ComponentType.Trend, ComponentInformation.Stdev);
            }
        }

        // correct SA =Y / S (-> *sbias)
        DoubleSeq sa = DoublesMath.divide(y, s);
        decomp.add(sa, ComponentType.SeasonallyAdjusted);
        DoubleSeq sae = ldecomp.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Stdev);
        if (sae != null) {
            decomp.add(correctStdevForLog(sae, sa), ComponentType.SeasonallyAdjusted, ComponentInformation.Stdev);
        }

        i = DoublesMath.divide(sa, t);
        decomp.add(i, ComponentType.Irregular);
        if (model.getForecastsCount() > 0) {
            fillForecasts(ldecomp, decomp, sbias, ibias);
        }
        if (model.getBackcastsCount() > 0) {
            fillBackcasts(ldecomp, decomp, sbias, ibias);
        }

        model.setFinalComponents(decomp.build());
    }

    private void fillForecasts(SeriesDecomposition ldecomp, SeriesDecomposition.Builder decomp, double sbias, double ibias) {
        // idem forecasts
        DoubleSeq fy = ldecomp.getSeries(ComponentType.Series, ComponentInformation.Forecast);
        DoubleSeq fs = ldecomp.getSeries(ComponentType.Seasonal, ComponentInformation.Forecast);
        DoubleSeq ft = ldecomp.getSeries(ComponentType.Trend, ComponentInformation.Forecast);
        DoubleSeq fi = ldecomp.getSeries(ComponentType.Irregular, ComponentInformation.Forecast);
        if (fy == null) {
            fy = DoublesMath.add(fs, ft, fi);
        }
        if (fy != null) {
            fy = fy.exp();
            decomp.add(fy, ComponentType.Series, ComponentInformation.Forecast);
            DoubleSeq fye = ldecomp.getSeries(ComponentType.Series, ComponentInformation.StdevForecast);
            if (fye != null) {
                decomp.add(correctStdevForLog(fye, fy), ComponentType.Series, ComponentInformation.StdevForecast);
            }
        }
        if (fs != null) {
            if (bias) {
                fs = fs.fn(x -> Math.exp(x) / sbias);
            } else {
                fs = fs.exp();
            }
            decomp.add(fs, ComponentType.Seasonal, ComponentInformation.Forecast);
            DoubleSeq fse = ldecomp.getSeries(ComponentType.Seasonal, ComponentInformation.StdevForecast);
            if (fse != null) {
                decomp.add(correctStdevForLog(fse, fs), ComponentType.Seasonal, ComponentInformation.StdevForecast);
            }
        }

        // correct T = Y /S * I) (-> *sbias*ibias)
        if (ft != null) {
            if (bias) {
                double tbias = sbias * ibias;
                ft = ft.fn(x -> Math.exp(x) * tbias);
            } else {
                ft = ft.exp();
            }
            decomp.add(ft, ComponentType.Trend, ComponentInformation.Forecast);
            DoubleSeq fte = ldecomp.getSeries(ComponentType.Trend, ComponentInformation.StdevForecast);
            if (fte != null) {
                decomp.add(correctStdevForLog(fte, ft), ComponentType.Trend, ComponentInformation.StdevForecast);
            }
        }

        // correct SA =Y / S (-> *sbias)
        DoubleSeq fsa = DoublesMath.divide(fy, fs);
        if (fsa != null) {
            decomp.add(fsa, ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast);
            DoubleSeq fsae = ldecomp.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast);
            if (fsae != null) {
                decomp.add(correctStdevForLog(fsae, fsa), ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast);
            }
        }

        fi = DoublesMath.divide(fsa, ft);
        if (fi != null) {
            decomp.add(fi, ComponentType.Irregular, ComponentInformation.Forecast);
            DoubleSeq fie = ldecomp.getSeries(ComponentType.Irregular, ComponentInformation.StdevForecast);
            if (fie != null) {
                decomp.add(correctStdevForLog(fie, fi), ComponentType.Irregular, ComponentInformation.StdevForecast);
            }
        }

    }

    private void fillBackcasts(SeriesDecomposition ldecomp, SeriesDecomposition.Builder decomp, double sbias, double ibias) {
        // idem forecasts
        DoubleSeq fy = ldecomp.getSeries(ComponentType.Series, ComponentInformation.Backcast);
        DoubleSeq fs = ldecomp.getSeries(ComponentType.Seasonal, ComponentInformation.Backcast);
        DoubleSeq ft = ldecomp.getSeries(ComponentType.Trend, ComponentInformation.Backcast);
        DoubleSeq fi = ldecomp.getSeries(ComponentType.Irregular, ComponentInformation.Backcast);
        if (fy == null) {
            fy = DoublesMath.add(fs, ft, fi);
        }
        if (fy != null) {
            fy = fy.exp();
            decomp.add(fy, ComponentType.Series, ComponentInformation.Backcast);
            DoubleSeq fye = ldecomp.getSeries(ComponentType.Series, ComponentInformation.StdevBackcast);
            if (fye != null) {
                decomp.add(correctStdevForLog(fye, fy), ComponentType.Series, ComponentInformation.StdevBackcast);
            }
        }
        if (fs != null) {
            if (bias) {
                fs = fs.fn(x -> Math.exp(x) / sbias);
            } else {
                fs = fs.exp();
            }
            decomp.add(fs, ComponentType.Seasonal, ComponentInformation.Backcast);
            DoubleSeq fse = ldecomp.getSeries(ComponentType.Seasonal, ComponentInformation.StdevBackcast);
            if (fse != null) {
                decomp.add(correctStdevForLog(fse, fs), ComponentType.Seasonal, ComponentInformation.StdevBackcast);
            }
        }

        // correct T = Y /S * I) (-> *sbias*ibias)
        if (ft != null) {
            if (bias) {
                double tbias = sbias * ibias;
                ft = ft.fn(x -> Math.exp(x) * tbias);
            } else {
                ft = ft.exp();
            }
            decomp.add(ft, ComponentType.Trend, ComponentInformation.Backcast);
            DoubleSeq fte = ldecomp.getSeries(ComponentType.Trend, ComponentInformation.StdevBackcast);
            if (fte != null) {
                decomp.add(correctStdevForLog(fte, ft), ComponentType.Trend, ComponentInformation.StdevBackcast);
            }
        }

        // correct SA =Y / S (-> *sbias)
        DoubleSeq fsa = DoublesMath.divide(fy, fs);
        if (fsa != null) {
            decomp.add(fsa, ComponentType.SeasonallyAdjusted, ComponentInformation.Backcast);
            DoubleSeq fsae = ldecomp.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevBackcast);
            if (fsae != null) {
                decomp.add(correctStdevForLog(fsae, fsa), ComponentType.SeasonallyAdjusted, ComponentInformation.StdevBackcast);
            }
        }

        fi = DoublesMath.divide(fsa, ft);
        if (fi != null) {
            decomp.add(fi, ComponentType.Irregular, ComponentInformation.Backcast);
            DoubleSeq fie = ldecomp.getSeries(ComponentType.Irregular, ComponentInformation.StdevBackcast);
            if (fie != null) {
                decomp.add(correctStdevForLog(fie, fi), ComponentType.Irregular, ComponentInformation.StdevBackcast);
            }
        }

    }
}
