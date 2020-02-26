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
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.implementations.CompositeSsf;
import jdplus.ssf.univariate.DefaultSmoothingResults;
import jdplus.ssf.univariate.ExtendedSsfData;
import jdplus.ssf.univariate.ISsfData;
import jdplus.ssf.univariate.SsfData;
import jdplus.ucarima.UcarimaModel;
import jdplus.ucarima.ssf.SsfUcarima;
import demetra.data.DoubleSeq;
import jdplus.data.DataBlock;
import jdplus.math.matrices.QuadraticForm;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class KalmanEstimator implements IComponentsEstimator {
    
    private final int nfcasts, nbcasts;
    
    public KalmanEstimator(int nbcasts, int nfcasts){
        this.nfcasts=nfcasts;
        this.nbcasts=nbcasts;
    }
    

    /**
     *
     * @param model
     * @return
     */
    @Override
    public SeriesDecomposition decompose(SeatsModel model) {
        SeriesDecomposition.Builder builder = SeriesDecomposition.builder(DecompositionMode.Additive);
        DoubleSeq s = model.getTransformedSeries();
        int n = s.length(), nf = model.extrapolationCount(nfcasts),
                nb = model.extrapolationCount(nbcasts);

        ComponentType[] cmps = model.componentsType();
        UcarimaModel ucm = model.compactUcarimaModel();

        CompositeSsf ssf = SsfUcarima.of(ucm);
        // compute KS
        ISsfData data = new ExtendedSsfData(new SsfData(s), nb, nf);
        double mvar = model.getInnovationVariance();
        DefaultSmoothingResults srslts;
        if (mvar != 0) {
            // for using the same standard error (unbiased stdandard error, not ml)
            srslts = DkToolkit.sqrtSmooth(ssf, data, true, false);
            srslts.rescaleVariances(mvar);
        } else {
            srslts = DkToolkit.sqrtSmooth(ssf, data, true, true);
        }

        int[] pos = ssf.componentsPosition();
        DoubleSeq cmp;
        int scmp = -1;
        for (int i = 0; i < pos.length; ++i) {
            ComponentType type = cmps[i];
            if (type == ComponentType.Seasonal) {
                scmp = i;
            }
            cmp = srslts.getComponent(pos[i]);
            if (nb > 0) {
                builder.add(cmp.range(0, nb), type, ComponentInformation.Backcast);
            }
            if (nf > 0) {
                builder.add(cmp.extract(nb + n, nf), type, ComponentInformation.Forecast);
            }
            builder.add(cmp.extract(nb, n), type);
            cmp = srslts.getComponentVariance(pos[i]).fn(x -> x <= 0 ? 0 : Math.sqrt(x));
            if (nb > 0) {
                builder.add(cmp.range(0, nb), type, ComponentInformation.StdevBackcast);
            }
            if (nf > 0) {
                builder.add(cmp.extract(nb + n, nf), type, ComponentInformation.StdevForecast);
            }
            builder.add(cmp.extract(nb, n), type, ComponentInformation.Stdev);
            if (type == ComponentType.Seasonal) {
                // No missing values !
                builder.add(cmp.extract(nb, n), ComponentType.SeasonallyAdjusted, ComponentInformation.Stdev);
            }
        }

        DataBlock z = DataBlock.make(ssf.getStateDim());
        ssf.measurement().loading().Z(0, z);
        if (nb > 0) {
            double[] a = new double[nb];
            for (int i = 0; i < a.length; ++i) {
                a[i] = QuadraticForm.apply(srslts.P(i), z);
            }
            cmp = DoubleSeq.of(a).fn(x -> x <= 0 ? 0 : Math.sqrt(x));
            builder.add(cmp, ComponentType.Series, ComponentInformation.StdevBackcast);
            if (scmp < 0) {
                builder.add(cmp, ComponentType.SeasonallyAdjusted, ComponentInformation.StdevBackcast);
            }
        }
        if (nf > 0) {
            double[] a = new double[nb];
            for (int i = 0, j = n + nb; i < a.length; ++i, ++j) {
                a[i] = QuadraticForm.apply(srslts.P(j), z);
            }
            cmp = DoubleSeq.of(a).fn(x -> x <= 0 ? 0 : Math.sqrt(x));
            builder.add(cmp, ComponentType.Series, ComponentInformation.StdevForecast);
            if (scmp < 0) {
                builder.add(cmp, ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast);
            }
        }
        // idem for SA

        if (scmp >= 0) {
            z.range(pos[scmp], scmp == pos.length - 1 ? ssf.getStateDim() : pos[scmp + 1]).set(0);
            if (nb > 0) {
                double[] a = new double[nb];
                for (int i = 0; i < a.length; ++i) {
                    a[i] = QuadraticForm.apply(srslts.P(i), z);
                }
                cmp = DoubleSeq.of(a).fn(x -> x <= 0 ? 0 : Math.sqrt(x));
                builder.add(cmp, ComponentType.SeasonallyAdjusted, ComponentInformation.StdevBackcast);
            }
            if (nf > 0) {
                double[] a = new double[nb];
                for (int i = 0, j = n + nb; i < a.length; ++i, ++j) {
                    a[i] = QuadraticForm.apply(srslts.P(j), z);
                }
                cmp = DoubleSeq.of(a).fn(x -> x <= 0 ? 0 : Math.sqrt(x));
                builder.add(cmp, ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast);
            }
        }
        return builder.build();
    }

}
