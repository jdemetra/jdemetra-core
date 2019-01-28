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
import demetra.ucarima.estimation.McElroyEstimates;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class MatrixEstimator implements IComponentsEstimator {

    /**
     *
     * @param s
     * @return
     */
    @Override
    public SeriesDecomposition decompose(SeatsModel model) {
        DoubleSequence s = model.getSeries();
        SeriesDecomposition decomposition = new SeriesDecomposition(
                DecompositionMode.Additive);
        UcarimaModel ucm = model.getUcarimaModel();
        McElroyEstimates mc = new McElroyEstimates();
        mc.setForecastsCount(model.getForecastsCount());
        mc.setUcarimaModel(ucm);
        mc.setData(s);
        double ser=Math.sqrt(model.getInnovationVariance());
        for (int i = 0; i < ucm.getComponentsCount(); ++i) {
            ComponentType type=model.getTypes()[i];
            double[] tmp = mc.getComponent(i);
            decomposition.add(DoubleSequence.ofInternal(tmp), type);
            tmp = mc.stdevEstimates(i);
            for (int j=0; j<tmp.length; ++j){
                tmp[j]*=ser;
            }
            decomposition.add(DoubleSequence.ofInternal(tmp), type, ComponentInformation.Stdev);
            tmp = mc.getForecasts(i);
            decomposition.add(DoubleSequence.ofInternal(tmp), type, ComponentInformation.Forecast);
            tmp = mc.stdevForecasts(i);
            for (int j=0; j<tmp.length; ++j){
                tmp[j]*=ser;
            }
            decomposition.add(DoubleSequence.ofInternal(tmp), type, ComponentInformation.StdevForecast);
        }
        decomposition.add(s, ComponentType.Series);
        return decomposition;
    }
}
