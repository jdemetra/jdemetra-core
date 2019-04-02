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
package demetra.seats;

import demetra.data.DoubleSequence;
import demetra.design.Development;
import demetra.modelling.ComponentInformation;
import demetra.sa.ComponentType;
import demetra.sa.DecompositionMode;
import demetra.sa.SeriesDecomposition;
import demetra.ucarima.UcarimaModel;
import demetra.ucarima.estimation.BurmanEstimates;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class WienerKolmogorovEstimator implements IComponentsEstimator {

    /**
     *
     * @param model
     * @return
     */
    @Override
    public SeriesDecomposition decompose(SeatsModel model) {
        SeriesDecomposition.Builder builder = SeriesDecomposition.builder(DecompositionMode.Additive);
        BurmanEstimates burman = new BurmanEstimates();

        DoubleSequence s = model.getSeries();
        burman.setForecastsCount(model.getForecastsCount());

        UcarimaModel ucm = model.getUcarimaModel();
        // check the ucarima model. 
        // ucm=checkModel(ucm);
        if (model.isMeanCorrection()) {
            burman.setUcarimaModelWithMean(ucm);
        } else {
            burman.setUcarimaModel(ucm);
        }
        burman.setData(s);
        burman.setSer(Math.sqrt(model.getInnovationVariance()));
        int ncmps = ucm.getComponentsCount();

        for (int i = 0; i < ncmps; ++i) {
            ComponentType type = model.getTypes()[i];
            process(builder, burman, i, true, type);
            if (type == ComponentType.Seasonal) {
                process(builder, burman, i, false, ComponentType.SeasonallyAdjusted);
            }
        }
        return builder.build();
    }

    private void process(SeriesDecomposition.Builder builder, BurmanEstimates burman, int i, boolean b, ComponentType type) {
        double[] tmp = burman.estimates(i, true);
        builder.add(DoubleSequence.ofInternal(tmp), type);
        tmp = burman.stdevEstimates(i);
        if (tmp != null) {
            builder.add(DoubleSequence.ofInternal(tmp), type, ComponentInformation.Stdev);
        }
        tmp = burman.forecasts(i, true);
        if (tmp != null) {
            builder.add(DoubleSequence.ofInternal(tmp), type, ComponentInformation.Forecast);
        }
        tmp = burman.stdevForecasts(i, true);
        if (tmp != null) {
            builder.add(DoubleSequence.ofInternal(tmp), type, ComponentInformation.StdevForecast);
        }
    }

}
