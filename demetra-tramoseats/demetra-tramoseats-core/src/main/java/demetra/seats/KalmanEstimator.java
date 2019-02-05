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
import demetra.ssf.dk.DkToolkit;
import demetra.ssf.implementations.CompositeSsf;
import demetra.ssf.univariate.DefaultSmoothingResults;
import demetra.ssf.univariate.ExtendedSsfData;
import demetra.ssf.univariate.ISsfData;
import demetra.ssf.univariate.SsfData;
import demetra.ucarima.UcarimaModel;
import demetra.ucarima.ssf.SsfUcarima;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class KalmanEstimator implements IComponentsEstimator {

    /**
     *
     * @param model
     * @return
     */
    @Override
    public SeriesDecomposition decompose(SeatsModel model) {
        SeriesDecomposition decomposition = new SeriesDecomposition(
                DecompositionMode.Additive);
        DoubleSequence s = model.getSeries();
        int n = s.length(), nf = model.getForecastsCount(), nb = model.getBackcastsCount();

        CompositeSsf ssf = SsfUcarima.of(model.getUcarimaModel());
        // compute KS
        ISsfData data = new ExtendedSsfData(new SsfData(s), nb, nf);
        DefaultSmoothingResults srslts = DkToolkit.sqrtSmooth(ssf, data, true, true);
        // for using the same standard error (unbiased stdandard error, not ml)
        srslts.rescaleVariances(model.getInnovationVariance());

        UcarimaModel ucm = model.getUcarimaModel();
        int[] pos = ssf.componentsPosition();
        for (int i = 0; i < ucm.getComponentsCount(); ++i) {
            ComponentType type = model.getTypes()[i];
            DoubleSequence cmp = DoubleSequence.of(srslts.getComponent(pos[i]));
            if (nb > 0) {
                decomposition.add(cmp.range(0, nb), type, ComponentInformation.Backcast);
            }
            if (nf > 0) {
                decomposition.add(cmp.extract(nb + n, nf), type, ComponentInformation.Forecast);
            }
            decomposition.add(cmp.extract(nb, n), type);
            cmp = DoubleSequence.of(srslts.getComponentVariance(pos[i]));
            if (nb > 0) {
                decomposition.add(cmp.range(0, nb), type, ComponentInformation.StdevBackcast);
            }
            if (nf > 0) {
                decomposition.add(cmp.extract(nb + n, nf), type, ComponentInformation.StdevForecast);
            }
            decomposition.add(cmp.extract(nb, n), type, ComponentInformation.Stdev);
        }
        return decomposition;
    }
}
