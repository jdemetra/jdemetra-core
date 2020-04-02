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
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import jdplus.ucarima.UcarimaModel;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class WienerKolmogorovEstimator implements IComponentsEstimator {

    private final int nfcasts, nbcasts;

    public WienerKolmogorovEstimator(int nbcasts, int nfcasts) {
        this.nfcasts = nfcasts;
        this.nbcasts = nbcasts;
    }

    /**
     *
     * @param model
     * @return
     */
    @Override
    public SeriesDecomposition decompose(SeatsModel model) {
        SeriesDecomposition.Builder decomposition = SeriesDecomposition.builder(DecompositionMode.Additive);

        UcarimaModel ucm = model.getUcarimaModel();
        ucm = ucm.compact(2, 2);

        TsData s = model.getTransformedSeries();
        int nf = model.extrapolationCount(nfcasts);
        int nb = model.extrapolationCount(nbcasts);
        
        TsPeriod start=s.getStart(), bstart=start.plus(-nb), fstart=s.getEnd();

        BurmanEstimates burman = BurmanEstimates.builder()
                .data(s.getValues())
                .forecastsCount(nf)
                .backcastsCount(nb)
                .mean(model.isMeanCorrection())
                .ucarimaModel(ucm)
                .innovationStdev(Math.sqrt(model.getInnovationVariance()))
                .build();

        int ncmps = ucm.getComponentsCount();

        DoubleSeq[] cmps = new DoubleSeq[ncmps];
        DoubleSeq[] fcmps = new DoubleSeq[ncmps];
        DoubleSeq[] ecmps = new DoubleSeq[ncmps];
        DoubleSeq[] efcmps = new DoubleSeq[ncmps];
        DoubleSeq[] bcmps = new DoubleSeq[ncmps];
        DoubleSeq[] ebcmps = new DoubleSeq[ncmps];

        for (int i = 0; i < ncmps; ++i) {
            if (i == 0 || !ucm.getComponent(i).isNull()) {
                cmps[i] = burman.estimates(i, true);
                ecmps[i] = burman.stdevEstimates(i);
                fcmps[i] = burman.forecasts(i, true);
                efcmps[i] = burman.stdevForecasts(i, true);
                bcmps[i] = burman.backcasts(i, true);
                ebcmps[i] = burman.stdevBackcasts(i, true);
            }
        }

        DoubleSeq fs = burman.getSeriesForecasts();
        DoubleSeq efs = null, efsa = null;
        for (int i = 0; i < efcmps.length; ++i) {
            if (efcmps[i] != null) {
                DoubleSeq var = efcmps[i].fn(z -> z * z);
                efs = DoublesMath.add(efs, var);
                if (i != 1) {
                    efsa = DoublesMath.add(efsa, var);
                }

            }
        }

        DoubleSeq bs = burman.getSeriesBackcasts();
        DoubleSeq ebs = null, ebsa = null;
        for (int i = 0; i < ebcmps.length; ++i) {
            if (ebcmps[i] != null) {
                DoubleSeq var = ebcmps[i].fn(z -> z * z);
                ebs = DoublesMath.add(ebs, var);
                if (i != 1) {
                    ebsa = DoublesMath.add(ebsa, var);
                }
            }
        }
        decomposition.add(s, ComponentType.Series);
        if (fs != null) {
            decomposition.add(TsData.ofInternal(fstart, fs), ComponentType.Series, ComponentInformation.Forecast);
        }
        if (efs != null) {
            decomposition.add(TsData.ofInternal(fstart, efs.fn(z -> z <= 0 ? 0 : Math.sqrt(z))), ComponentType.Series, ComponentInformation.StdevForecast);
        }
        if (bs != null) {
            decomposition.add(TsData.ofInternal(bstart, bs), ComponentType.Series, ComponentInformation.Backcast);
        }
        if (ebs != null) {
            decomposition.add(TsData.ofInternal(bstart, ebs.fn(z -> z <= 0 ? 0 : Math.sqrt(z))), ComponentType.Series, ComponentInformation.StdevBackcast);
        }
        if (cmps[0] != null) {
            decomposition.add(TsData.ofInternal(start, cmps[0]), ComponentType.Trend);
        }
        if (cmps[1] != null) {
            decomposition.add(TsData.ofInternal(start, cmps[1]), ComponentType.Seasonal);
        }

        if (fcmps[0] != null) {
            decomposition.add(TsData.ofInternal(fstart, fcmps[0]), ComponentType.Trend, ComponentInformation.Forecast);
        }
        if (fcmps[1] != null) {
            decomposition.add(TsData.ofInternal(fstart, fcmps[1]), ComponentType.Seasonal, ComponentInformation.Forecast);
        }
        decomposition.add(TsData.ofInternal(fstart, DoublesMath.subtract(fs, fcmps[1])),
                ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast);
        if (ecmps[0] != null) {
            decomposition.add(TsData.ofInternal(start, ecmps[0]), ComponentType.Trend, ComponentInformation.Stdev);
        }
        if (efcmps[0] != null) {
            decomposition.add(TsData.ofInternal(fstart, efcmps[0]), ComponentType.Trend, ComponentInformation.StdevForecast);
        }
        decomposition.add(TsData.ofInternal(start, DoublesMath.subtract(s.getValues(), cmps[1])),
                ComponentType.SeasonallyAdjusted);
        if (ecmps[1] != null) {
            decomposition.add(TsData.ofInternal(start, ecmps[1]), ComponentType.Seasonal, ComponentInformation.Stdev);
            decomposition.add(TsData.ofInternal(start, ecmps[1]), ComponentType.SeasonallyAdjusted, ComponentInformation.Stdev);
        }
        if (efcmps[1] != null) {
            decomposition.add(TsData.ofInternal(fstart, efcmps[1]), ComponentType.Seasonal, ComponentInformation.StdevForecast);
        }

        decomposition.add(TsData.ofInternal(start, cmps[2]), ComponentType.Irregular);
        if (fcmps[2] != null) {
            decomposition.add(TsData.ofInternal(fstart, fcmps[2]), ComponentType.Irregular, ComponentInformation.Forecast);
        }
        if (ecmps[2] != null) {
            decomposition.add(TsData.ofInternal(start, ecmps[2]), ComponentType.Irregular, ComponentInformation.Stdev);
        }
        if (efcmps[2] != null) {
            decomposition.add(TsData.ofInternal(fstart, efcmps[2]), ComponentType.Irregular, ComponentInformation.StdevForecast);
        }
        if (bcmps[0] != null) {
            decomposition.add(TsData.ofInternal(bstart, bcmps[0]), ComponentType.Trend, ComponentInformation.Backcast);
        }
        if (bcmps[1] != null) {
            decomposition.add(TsData.ofInternal(bstart, bcmps[1]), ComponentType.Seasonal, ComponentInformation.Backcast);
        }
        if (efsa != null) {
            decomposition.add(TsData.ofInternal(fstart, efsa.fn(z -> z <= 0 ? 0 : Math.sqrt(z))), ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast);
        }

        decomposition.add(TsData.ofInternal(bstart, DoublesMath.subtract(bs, bcmps[1])),
                ComponentType.SeasonallyAdjusted, ComponentInformation.Backcast);
        if (ebcmps[0] != null) {
            decomposition.add(TsData.ofInternal(bstart, ebcmps[0]), ComponentType.Trend, ComponentInformation.StdevBackcast);
        }
        if (ebcmps[1] != null) {
            decomposition.add(TsData.ofInternal(bstart, ebcmps[1]), ComponentType.Seasonal, ComponentInformation.StdevBackcast);
        }
        if (bcmps[2] != null) {
            decomposition.add(TsData.ofInternal(bstart, bcmps[2]), ComponentType.Irregular, ComponentInformation.Backcast);
        }
        if (ebcmps[2] != null) {
            decomposition.add(TsData.ofInternal(bstart, ebcmps[2]), ComponentType.Irregular, ComponentInformation.StdevBackcast);
        }
        if (ebsa != null) {
            decomposition.add(TsData.ofInternal(bstart, ebsa.fn(z -> z <= 0 ? 0 : Math.sqrt(z))), ComponentType.SeasonallyAdjusted, ComponentInformation.StdevBackcast);
        }
        return decomposition.build();
    }

}
