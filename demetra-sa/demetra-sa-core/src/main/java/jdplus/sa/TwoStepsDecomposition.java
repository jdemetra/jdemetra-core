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
package jdplus.sa;

import demetra.data.DoubleSeq;
import demetra.data.DoublesMath;
import demetra.modelling.ComponentInformation;
import demetra.sa.ComponentType;
import demetra.sa.DecompositionMode;
import demetra.sa.SeriesDecomposition;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import jdplus.regsarima.regular.ModelEstimation;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class TwoStepsDecomposition {

    private DoubleSeq op(boolean mul, DoubleSeq l, DoubleSeq... r) {
        if (mul) {
            return DoublesMath.multiply(l, r);
        } else {
            return DoublesMath.add(l, r);
        }
    }

    private DoubleSeq inv_op(boolean mul, DoubleSeq l, DoubleSeq r) {
        if (mul) {
            return DoublesMath.divide(l, r);
        } else {
            return DoublesMath.subtract(l, r);
        }
    }

    public SeriesDecomposition merge(RegArimaDecomposer decomposer, SeriesDecomposition sadecomp) {
        ModelEstimation model = decomposer.getModel();
        boolean mul = model.isLogTransformation();
        SeriesDecomposition.Builder builder = SeriesDecomposition.builder(mul ? DecompositionMode.Multiplicative : DecompositionMode.Additive);

        TsData orig = model.getOriginalSeries();
        TsDomain domain = orig.getDomain();

        DoubleSeq f = sadecomp.getSeries(ComponentType.Series, ComponentInformation.Forecast);
        DoubleSeq b = sadecomp.getSeries(ComponentType.Series, ComponentInformation.Backcast);

        int nf = f == null ? 0 : f.length();
        int nb = b == null ? 0 : b.length();
        int n0 = 0, n1 = nb, n2 = nb + domain.getLength(), n3 = n2 + nf;

        TsDomain cdomain = domain.extend(nb, nf);

        DoubleSeq detT = decomposer.deterministicEffect(cdomain, ComponentType.Trend, false).getValues();
        DoubleSeq detS = decomposer.deterministicEffect(cdomain, ComponentType.Seasonal, false).getValues();
        DoubleSeq detC = decomposer.deterministicEffect(cdomain, ComponentType.CalendarEffect, false).getValues();
        detS = op(mul, detS, detC);
        DoubleSeq detI = decomposer.deterministicEffect(cdomain, ComponentType.Irregular, false).getValues();
        DoubleSeq detY = decomposer.deterministicEffect(cdomain, ComponentType.Series, false).getValues();
        DoubleSeq detSA = decomposer.deterministicEffect(cdomain, ComponentType.SeasonallyAdjusted, false).getValues();

        DoubleSeq y = inv_op(mul, orig.getValues(), detY.range(n1, n2));
        builder.add(y, ComponentType.Series);

        // core
        DoubleSeq t = op(mul, detT.range(n1, n2), sadecomp.getSeries(ComponentType.Trend, ComponentInformation.Value));
        if (t != null) {
            builder.add(t, ComponentType.Trend);
        }
        DoubleSeq s = op(mul, detS.range(n1, n2), sadecomp.getSeries(ComponentType.Seasonal, ComponentInformation.Value));
        if (s != null) {
            builder.add(s, ComponentType.Seasonal);
        }
        DoubleSeq i = op(mul, detI.range(n1, n2), sadecomp.getSeries(ComponentType.Irregular, ComponentInformation.Value));
        if (i != null) {
            builder.add(i, ComponentType.Irregular);
        }
        DoubleSeq sa = op(mul, detSA.range(n1, n2), sadecomp.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
        if (sa != null) {
            builder.add(sa, ComponentType.SeasonallyAdjusted);
        }

        // backcast
        if (nb > 0) {
            DoubleSeq all = op(mul, detT.range(n0, n1), detS.range(n0, n1), detI.range(n0, n1), b);
            builder.add(all, ComponentType.Series, ComponentInformation.Backcast);
            DoubleSeq bt = op(mul, detT.range(n0, n1), sadecomp.getSeries(ComponentType.Trend, ComponentInformation.Backcast));
            if (bt != null) {
                builder.add(bt, ComponentType.Trend, ComponentInformation.Backcast);
            }
            DoubleSeq bs = op(mul, detS.range(n0, n1), sadecomp.getSeries(ComponentType.Seasonal, ComponentInformation.Backcast));
            if (bs != null) {
                builder.add(bs, ComponentType.Seasonal, ComponentInformation.Backcast);
            }
            DoubleSeq bi = op(mul, detI.range(n0, n1), sadecomp.getSeries(ComponentType.Irregular, ComponentInformation.Backcast));
            if (bi != null) {
                builder.add(bi, ComponentType.Irregular, ComponentInformation.Backcast);
            }
            DoubleSeq bsa = op(mul, detSA.range(n0, n1), sadecomp.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Backcast));
            if (bsa != null) {
                builder.add(bsa, ComponentType.SeasonallyAdjusted, ComponentInformation.Backcast);
            }

        }

        // forecast
        if (nf > 0) {

            DoubleSeq all = op(mul, detT.range(n2, n3), detS.range(n2, n3), detI.range(n2, n3), f);
            builder.add(all, ComponentType.Series, ComponentInformation.Forecast);
            DoubleSeq ft = op(mul, detT.range(n2, n3), sadecomp.getSeries(ComponentType.Trend, ComponentInformation.Forecast));
            if (ft != null) {
                builder.add(ft, ComponentType.Trend, ComponentInformation.Forecast);
            }
            DoubleSeq fs = op(mul, detS.range(n2, n3), sadecomp.getSeries(ComponentType.Seasonal, ComponentInformation.Forecast));
            if (fs != null) {
                builder.add(fs, ComponentType.Seasonal, ComponentInformation.Forecast);
            }
            DoubleSeq fi = op(mul, detI.range(n2, n3), sadecomp.getSeries(ComponentType.Irregular, ComponentInformation.Forecast));
            if (fi != null) {
                builder.add(fi, ComponentType.Irregular, ComponentInformation.Forecast);
            }
            DoubleSeq fsa = op(mul, detSA.range(n2, n3), sadecomp.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast));
            if (fsa != null) {
                builder.add(fsa, ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast);
            }

        }

        return builder.build();
    }
}
