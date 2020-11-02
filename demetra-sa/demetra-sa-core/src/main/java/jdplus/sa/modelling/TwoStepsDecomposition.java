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
package jdplus.sa.modelling;

import demetra.modelling.ComponentInformation;
import demetra.sa.ComponentType;
import demetra.sa.DecompositionMode;
import demetra.sa.SeriesDecomposition;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import jdplus.regsarima.regular.ModelEstimation;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class TwoStepsDecomposition {

    private TsData op(boolean mul, TsData l, TsData... r) {
        if (mul) {
            return TsData.multiply(l, r);
        } else {
            return TsData.add(l, r);
        }
    }

    private TsData inv_op(boolean mul, TsData l, TsData r) {
        if (mul) {
            return TsData.divide(l, r);
        } else {
            return TsData.subtract(l, r);
        }
    }

    public SeriesDecomposition merge(ModelEstimation model, SeriesDecomposition sadecomp) {
        boolean mul = model.isLogTransformation();
        SeriesDecomposition.Builder builder = SeriesDecomposition.builder(mul ? DecompositionMode.Multiplicative : DecompositionMode.Additive);

        TsData orig = model.getOriginalSeries();
        TsDomain domain = orig.getDomain();

        TsData f = sadecomp.getSeries(ComponentType.Series, ComponentInformation.Forecast);
        TsData b = sadecomp.getSeries(ComponentType.Series, ComponentInformation.Backcast);

        int nf = f == null ? 0 : f.length();
        int nb = b == null ? 0 : b.length();
        int n0 = 0, n1 = nb, n2 = nb + domain.getLength(), n3 = n2 + nf;

        TsDomain cdomain = domain.extend(nb, nf);
        TsPeriod start = domain.getStartPeriod(), bstart = cdomain.getStartPeriod(), fstart = domain.getEndPeriod();

        TsData detT = RegArimaDecomposer.deterministicEffect(model, cdomain, ComponentType.Trend, false);
        TsData detS = RegArimaDecomposer.deterministicEffect(model, cdomain, ComponentType.Seasonal, false);
        TsData detC = RegArimaDecomposer.deterministicEffect(model, cdomain, ComponentType.CalendarEffect, false);
        detS = op(mul, detS, detC);
        TsData detI = RegArimaDecomposer.deterministicEffect(model, cdomain, ComponentType.Irregular, false);
        TsData detY = RegArimaDecomposer.deterministicEffect(model, cdomain, ComponentType.Series, false);
        TsData detSA = RegArimaDecomposer.deterministicEffect(model, cdomain, ComponentType.SeasonallyAdjusted, false);

        TsData y = inv_op(mul, orig, detY.range(n1, n2));
        builder.add(y, ComponentType.Series);

        // core
        TsData t = op(mul, detT.range(n1, n2), sadecomp.getSeries(ComponentType.Trend, ComponentInformation.Value));
        if (t != null) {
            builder.add(t, ComponentType.Trend);
        }
        TsData s = op(mul, detS.range(n1, n2), sadecomp.getSeries(ComponentType.Seasonal, ComponentInformation.Value));
        if (s != null) {
            builder.add(s, ComponentType.Seasonal);
        }
        TsData i = op(mul, detI.range(n1, n2), sadecomp.getSeries(ComponentType.Irregular, ComponentInformation.Value));
        if (i != null) {
            builder.add(i, ComponentType.Irregular);
        }
        TsData sa = op(mul, detSA.range(n1, n2), sadecomp.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
        if (sa != null) {
            builder.add(sa, ComponentType.SeasonallyAdjusted);
        }

        // backcast
        if (nb > 0) {
            TsData all = op(mul, detT.range(n0, n1), detS.range(n0, n1), detI.range(n0, n1), b);
            builder.add(all, ComponentType.Series, ComponentInformation.Backcast);
            TsData bt = op(mul, detT.range(n0, n1), sadecomp.getSeries(ComponentType.Trend, ComponentInformation.Backcast));
            if (bt != null) {
                builder.add(bt, ComponentType.Trend, ComponentInformation.Backcast);
            }
            TsData bs = op(mul, detS.range(n0, n1), sadecomp.getSeries(ComponentType.Seasonal, ComponentInformation.Backcast));
            if (bs != null) {
                builder.add(bs, ComponentType.Seasonal, ComponentInformation.Backcast);
            }
            TsData bi = op(mul, detI.range(n0, n1), sadecomp.getSeries(ComponentType.Irregular, ComponentInformation.Backcast));
            if (bi != null) {
                builder.add(bi, ComponentType.Irregular, ComponentInformation.Backcast);
            }
            TsData bsa = op(mul, detSA.range(n0, n1), sadecomp.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Backcast));
            if (bsa != null) {
                builder.add(bsa, ComponentType.SeasonallyAdjusted, ComponentInformation.Backcast);
            }
        }

        // forecast
        if (nf > 0) {

            TsData all = op(mul, detT.range(n2, n3), detS.range(n2, n3), detI.range(n2, n3), f);
            builder.add(all, ComponentType.Series, ComponentInformation.Forecast);
            TsData ft = op(mul, detT.range(n2, n3), sadecomp.getSeries(ComponentType.Trend, ComponentInformation.Forecast));
            if (ft != null) {
                builder.add(ft, ComponentType.Trend, ComponentInformation.Forecast);
            }
            TsData fs = op(mul, detS.range(n2, n3), sadecomp.getSeries(ComponentType.Seasonal, ComponentInformation.Forecast));
            if (fs != null) {
                builder.add(fs, ComponentType.Seasonal, ComponentInformation.Forecast);
            }
            TsData fi = op(mul, detI.range(n2, n3), sadecomp.getSeries(ComponentType.Irregular, ComponentInformation.Forecast));
            if (fi != null) {
                builder.add(fi, ComponentType.Irregular, ComponentInformation.Forecast);
            }
            TsData fsa = op(mul, detSA.range(n2, n3), sadecomp.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast));
            if (fsa != null) {
                builder.add(fsa, ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast);
            }
        }

        return builder.build();
    }
}
