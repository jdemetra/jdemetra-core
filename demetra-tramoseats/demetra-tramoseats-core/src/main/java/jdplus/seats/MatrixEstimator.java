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

import demetra.design.Development;
import demetra.modelling.ComponentInformation;
import demetra.sa.ComponentType;
import demetra.sa.DecompositionMode;
import demetra.sa.SeriesDecomposition;
import jdplus.ucarima.UcarimaModel;
import jdplus.ucarima.estimation.McElroyEstimates;
import demetra.data.DoubleSeq;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class MatrixEstimator implements IComponentsEstimator {

    private final int nfcasts, nbcasts;

    public MatrixEstimator(int nbcasts, int nfcasts) {
        this.nfcasts = nfcasts;
        this.nbcasts = nbcasts;
    }

    /**
     *
     * @param s
     * @return
     */
    @Override
    public SeriesDecomposition decompose(SeatsModel model) {
        DoubleSeq s = model.getTransformedSeries();
        SeriesDecomposition.Builder builder = SeriesDecomposition.builder(DecompositionMode.Additive);
        ComponentType[] cmps = model.componentsType();
        UcarimaModel ucm = model.compactUcarimaModel();
        McElroyEstimates mc = new McElroyEstimates();
        mc.setForecastsCount(model.extrapolationCount(nfcasts));
        // TODO backcasts
        mc.setUcarimaModel(ucm);
        mc.setData(s);
        double ser = Math.sqrt(model.getInnovationVariance());
        for (int i = 0; i < ucm.getComponentsCount(); ++i) {
            ComponentType type = cmps[i];
            double[] tmp = mc.getComponent(i);
            builder.add(DoubleSeq.of(tmp), type);
            tmp = mc.stdevEstimates(i);
            for (int j = 0; j < tmp.length; ++j) {
                tmp[j] *= ser;
            }
            builder.add(DoubleSeq.of(tmp), type, ComponentInformation.Stdev);
            tmp = mc.getForecasts(i);
            builder.add(DoubleSeq.of(tmp), type, ComponentInformation.Forecast);
            tmp = mc.stdevForecasts(i);
            for (int j = 0; j < tmp.length; ++j) {
                tmp[j] *= ser;
            }
            builder.add(DoubleSeq.of(tmp), type, ComponentInformation.StdevForecast);
        }
        builder.add(s, ComponentType.Series);
        return builder.build();
    }
}
