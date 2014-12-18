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
            TsData y = model.getSeries(ComponentType.Series, ComponentInformation.Value);
            if (y == null) {
                return null;
            }
            y=y.exp();
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
                s.getValues().div(sbias);
                decomp.add(s, ComponentType.Seasonal);
            }
            TsData i = model.getSeries(ComponentType.Irregular, ComponentInformation.Value);
            if (i != null) {
                i = i.exp();
                ibias = bias(i,i.getLength());
                i.getValues().div(ibias);
            }
            // correct T = Y /S * I) (-> *sbias*ibias)
            TsData t = model.getSeries(ComponentType.Trend, ComponentInformation.Value);
            if (t != null) {
                t = t.exp();
                t.getValues().mul(sbias * ibias);
                decomp.add(t, ComponentType.Trend);
            }

            // correct SA =Y / S (-> *sbias)
            TsData sa = TsData.divide(y, s);
            decomp.add(sa, ComponentType.SeasonallyAdjusted);

            i = TsData.divide(sa, t);
            decomp.add(i, ComponentType.Irregular);

            // idem forecasts
            TsData fy = model.getSeries(ComponentType.Series, ComponentInformation.Forecast);
            if (fy != null) {
                fy=fy.exp();
                decomp.add(fy, ComponentType.Series, ComponentInformation.Forecast);
            }
            TsData fs = model.getSeries(ComponentType.Seasonal, ComponentInformation.Forecast);
            if (fs != null) {
                fs = fs.exp();
                fs.getValues().div(sbias);
                decomp.add(fs, ComponentType.Seasonal, ComponentInformation.Forecast);
            }

            // correct T = Y /S * I) (-> *sbias*ibias)
            TsData ft = model.getSeries(ComponentType.Trend, ComponentInformation.Forecast);
            if (ft != null) {
                ft = ft.exp();
                ft.getValues().mul(sbias * ibias);
                decomp.add(ft, ComponentType.Trend, ComponentInformation.Forecast);
            }

            // correct SA =Y / S (-> *sbias)
            TsData fsa = TsData.divide(fy, fs);
            decomp.add(fsa, ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast);

            TsData fi = TsData.divide(fsa, ft);
            decomp.add(fi, ComponentType.Irregular, ComponentInformation.Forecast);
            return decomp;
        } else {
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
            }
            TsData i = model.getSeries(ComponentType.Irregular, ComponentInformation.Value);
             TsData t = model.getSeries(ComponentType.Trend, ComponentInformation.Value);
            if (t != null) {
                 decomp.add(t, ComponentType.Trend);
            }

            // correct SA =Y / S (-> *sbias)
            TsData sa = TsData.subtract(y, s);
            decomp.add(sa, ComponentType.SeasonallyAdjusted);

            i = TsData.subtract(sa, t);
            decomp.add(i, ComponentType.Irregular);

            // idem forecasts
            TsData fy = model.getSeries(ComponentType.Series, ComponentInformation.Forecast);
            if (fy != null) {
                 decomp.add(fy, ComponentType.Series, ComponentInformation.Forecast);
            }
            TsData fs = model.getSeries(ComponentType.Seasonal, ComponentInformation.Forecast);
            if (fs != null) {
                 decomp.add(fs, ComponentType.Seasonal, ComponentInformation.Forecast);
            }

            // correct T = Y /S * I) (-> *sbias*ibias)
            TsData ft = model.getSeries(ComponentType.Trend, ComponentInformation.Forecast);
            if (ft != null) {
                decomp.add(ft, ComponentType.Trend, ComponentInformation.Forecast);
            }

            // correct SA =Y / S (-> *sbias)
            TsData fsa = TsData.subtract(fy, fs);
            decomp.add(fsa, ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast);

            TsData fi = TsData.subtract(fsa, ft);
            decomp.add(fi, ComponentType.Irregular, ComponentInformation.Forecast);
            return decomp;
        }
    }

    private double bias(TsData s, int n) {
        DataBlock d = new DataBlock(s.getValues().internalStorage(), 0, n, 1);
        return d.sum() / n;
    }
}
