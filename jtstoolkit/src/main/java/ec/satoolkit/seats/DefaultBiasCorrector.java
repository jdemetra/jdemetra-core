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
package ec.satoolkit.seats;

import ec.tstoolkit.modelling.ComponentInformation;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.DefaultSeriesDecomposition;
import ec.satoolkit.ISeriesDecomposition;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.arima.LogForecasts;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Temporary)
public class DefaultBiasCorrector implements IBiasCorrector {

    /**
     *
     */
    public DefaultBiasCorrector() {
    }

    /**
     *
     * @param model
     * @param info
     * @param context
     * @return
     */
    @Override
    public DefaultSeriesDecomposition correct(ISeriesDecomposition model,
            InformationSet info, SeatsContext context) {
        if (context.isLogTransformed()) {
            return correctLogs(model);
        } else {
            return correctLevels(model);
        }
    }

    private double bias(TsData s, int n) {
        DataBlock d = new DataBlock(s.internalStorage(), 0, n, 1);
        return d.sum() / n;
    }

    // TODO. To be changed in JD+ 3.0 (functions)
    private TsData correctStdevForLog(TsData e, TsData s) {
        TsData ec = new TsData(e.getDomain());
        for (int i = 0; i < ec.getLength(); ++i) {
            ec.set(i, LogForecasts.expStdev2(e.get(i), s.get(i)));
        }
        return ec;
    }

    private DefaultSeriesDecomposition correctLevels(ISeriesDecomposition model) {
        TsData y = model.getSeries(ComponentType.Series, ComponentInformation.Value);
        if (y == null) {
            return null;
        }
        DefaultSeriesDecomposition decomp = new DefaultSeriesDecomposition(
                DecompositionMode.Additive);
        decomp.add(y, ComponentType.Series);

        TsData s = model.getSeries(ComponentType.Seasonal, ComponentInformation.Value);
        if (s != null) {
            decomp.add(s, ComponentType.Seasonal);
            TsData se = model.getSeries(ComponentType.Seasonal, ComponentInformation.Stdev);
            if (se != null) {
                decomp.add(se, ComponentType.Seasonal, ComponentInformation.Stdev);
            }
        }
        TsData t = model.getSeries(ComponentType.Trend, ComponentInformation.Value);
        if (t != null) {
            decomp.add(t, ComponentType.Trend);
            TsData te = model.getSeries(ComponentType.Trend, ComponentInformation.Stdev);
            if (te != null) {
                decomp.add(te, ComponentType.Trend, ComponentInformation.Stdev);
            }
        }

        // correct SA =Y / S (-> *sbias)
        TsData sa = TsData.subtract(y, s);
        decomp.add(sa, ComponentType.SeasonallyAdjusted);
        TsData sae = model.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Stdev);
        if (sae != null) {
            decomp.add(sae, ComponentType.SeasonallyAdjusted, ComponentInformation.Stdev);
        }

        TsData i = TsData.subtract(sa, t);
        decomp.add(i, ComponentType.Irregular);
        TsData ie = model.getSeries(ComponentType.Irregular, ComponentInformation.Stdev);
        if (ie != null) {
            decomp.add(ie, ComponentType.Irregular, ComponentInformation.Stdev);
        }

        // idem forecasts
        TsData fy = model.getSeries(ComponentType.Series, ComponentInformation.Forecast);
        if (fy != null) {
            decomp.add(fy, ComponentType.Series, ComponentInformation.Forecast);
            TsData fye = model.getSeries(ComponentType.Series, ComponentInformation.StdevForecast);
            if (fye != null) {
                decomp.add(fye, ComponentType.Series, ComponentInformation.StdevForecast);
            }
        }
        TsData fs = model.getSeries(ComponentType.Seasonal, ComponentInformation.Forecast);
        if (fs != null) {
            decomp.add(fs, ComponentType.Seasonal, ComponentInformation.Forecast);
            TsData fse = model.getSeries(ComponentType.Seasonal, ComponentInformation.StdevForecast);
            if (fse != null) {
                decomp.add(fse, ComponentType.Seasonal, ComponentInformation.StdevForecast);
            }
        }

        TsData ft = model.getSeries(ComponentType.Trend, ComponentInformation.Forecast);
        if (ft != null) {
            decomp.add(ft, ComponentType.Trend, ComponentInformation.Forecast);
            TsData fte = model.getSeries(ComponentType.Trend, ComponentInformation.StdevForecast);
            if (fte != null) {
                decomp.add(fte, ComponentType.Trend, ComponentInformation.StdevForecast);
            }
        }

        TsData fsa = TsData.subtract(fy, fs);
        decomp.add(fsa, ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast);
        TsData fsae = model.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast);
        if (fsae != null) {
            decomp.add(fsae, ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast);
        }

        TsData fi = TsData.subtract(fsa, ft);
        decomp.add(fi, ComponentType.Irregular, ComponentInformation.Forecast);
        TsData fie = model.getSeries(ComponentType.Irregular, ComponentInformation.StdevForecast);
        if (fie != null) {
            decomp.add(fie, ComponentType.Irregular, ComponentInformation.StdevForecast);
        }

        return decomp;
    }

    private DefaultSeriesDecomposition correctLogs(ISeriesDecomposition model) {
        TsData y = model.getSeries(ComponentType.Series, ComponentInformation.Value);
        if (y == null) {
            return null;
        }
        y = y.exp();
        DefaultSeriesDecomposition decomp = new DefaultSeriesDecomposition(
                DecompositionMode.Multiplicative);
        decomp.add(y, ComponentType.Series);

        int n = y.getLength();
        int freq = y.getFrequency().intValue();
        int ny = n - n % freq;

        double ibias = 1, sbias = 1;
        TsData s = model.getSeries(ComponentType.Seasonal, ComponentInformation.Value);
        if (s != null) {
            s = s.exp();
            sbias = bias(s, ny);
            s.apply(x->x/sbias);
            decomp.add(s, ComponentType.Seasonal);
            TsData se = model.getSeries(ComponentType.Seasonal, ComponentInformation.Stdev);
            if (se != null) {
                decomp.add(correctStdevForLog(se, s), ComponentType.Seasonal, ComponentInformation.Stdev);
            }
        }
        TsData i = model.getSeries(ComponentType.Irregular, ComponentInformation.Value);
        if (i != null) {
            i = i.exp();
            ibias = bias(i, i.getLength());
            i.apply(x->x/ibias);
            TsData ie = model.getSeries(ComponentType.Irregular, ComponentInformation.Stdev);
            if (ie != null) {
                decomp.add(correctStdevForLog(ie, i), ComponentType.Irregular, ComponentInformation.Stdev);
            }
        }
        // correct T = Y /S * I) (-> *sbias*ibias)
        TsData t = model.getSeries(ComponentType.Trend, ComponentInformation.Value);
        if (t != null) {
            t = t.exp();
            t.apply(x->x*sbias * ibias);
            decomp.add(t, ComponentType.Trend);
            TsData te = model.getSeries(ComponentType.Trend, ComponentInformation.Stdev);
            if (te != null) {
                decomp.add(correctStdevForLog(te, t), ComponentType.Trend, ComponentInformation.Stdev);
            }
        }

        // correct SA =Y / S (-> *sbias)
        TsData sa = TsData.divide(y, s);
        decomp.add(sa, ComponentType.SeasonallyAdjusted);
        TsData sae = model.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Stdev);
        if (sae != null) {
            decomp.add(correctStdevForLog(sae, sa), ComponentType.SeasonallyAdjusted, ComponentInformation.Stdev);
        }

        i = TsData.divide(sa, t);
        decomp.add(i, ComponentType.Irregular);

        // idem forecasts
        TsData fy = model.getSeries(ComponentType.Series, ComponentInformation.Forecast);
        if (fy != null) {
            fy = fy.exp();
            decomp.add(fy, ComponentType.Series, ComponentInformation.Forecast);
        }
        TsData fs = model.getSeries(ComponentType.Seasonal, ComponentInformation.Forecast);
        if (fs != null) {
            fs = fs.exp();
            fs.apply(x->x/sbias);
            decomp.add(fs, ComponentType.Seasonal, ComponentInformation.Forecast);
            TsData fse = model.getSeries(ComponentType.Seasonal, ComponentInformation.StdevForecast);
            if (fse != null) {
                decomp.add(correctStdevForLog(fse, fs), ComponentType.Seasonal, ComponentInformation.StdevForecast);
            }
        }

        // correct T = Y /S * I) (-> *sbias*ibias)
        TsData ft = model.getSeries(ComponentType.Trend, ComponentInformation.Forecast);
        if (ft != null) {
            ft = ft.exp();
            ft.apply(x->x*sbias * ibias);
            decomp.add(ft, ComponentType.Trend, ComponentInformation.Forecast);
            TsData fte = model.getSeries(ComponentType.Trend, ComponentInformation.StdevForecast);
            if (fte != null) {
                decomp.add(correctStdevForLog(fte, ft), ComponentType.Trend, ComponentInformation.StdevForecast);
            }
        }

        // correct SA =Y / S (-> *sbias)
        TsData fsa = TsData.divide(fy, fs);
        decomp.add(fsa, ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast);
        TsData fsae = model.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast);
        if (fsae != null) {
            decomp.add(correctStdevForLog(fsae, fsa), ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast);
        }

        TsData fi = TsData.divide(fsa, ft);
        decomp.add(fi, ComponentType.Irregular, ComponentInformation.Forecast);
        TsData fie = model.getSeries(ComponentType.Irregular, ComponentInformation.StdevForecast);
        if (fie != null) {
            decomp.add(correctStdevForLog(fie, fi), ComponentType.Irregular, ComponentInformation.StdevForecast);
        }
        return decomp;
    }
}
