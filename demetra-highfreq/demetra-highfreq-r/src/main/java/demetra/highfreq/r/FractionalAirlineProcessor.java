/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.highfreq.r;

import demetra.data.DoubleSeq;
import demetra.data.Parameter;
import demetra.data.ParameterType;
import jdplus.highfreq.extendedairline.decomposiiton.LightExtendedAirlineDecomposition;
import jdplus.highfreq.extendedairline.ExtendedAirlineEstimation;
import demetra.highfreq.ExtendedAirlineSpec;
import demetra.math.matrices.Matrix;
import jdplus.arima.ArimaModel;
import jdplus.arima.ArimaSeriesGenerator;
import jdplus.dstats.Normal;
import jdplus.highfreq.extendedairline.decomposiiton.ExtendedAirlineDecomposer;
import jdplus.highfreq.extendedairline.ExtendedAirlineKernel;
import jdplus.highfreq.extendedairline.ExtendedAirlineMapping;
import jdplus.math.linearfilters.BackFilter;
import jdplus.math.polynomials.Polynomial;
import jdplus.ssf.extractors.SsfUcarimaEstimation;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class FractionalAirlineProcessor {

    public LightExtendedAirlineDecomposition decompose(double[] s, double period, boolean sn, boolean cov, int nb, int nf) {
        int iperiod = (int) period;
        if (Math.abs(period - iperiod) < 1e-9) {
            period = iperiod;
        }
        return ExtendedAirlineDecomposer.decompose(DoubleSeq.of(s), period, sn, cov, nb, nf);
    }

    public LightExtendedAirlineDecomposition decompose(double[] s, double[] periods, int ndiff, boolean ar, boolean cov, int nb, int nf) {
        return ExtendedAirlineDecomposer.decompose(DoubleSeq.of(s), periods, ndiff, ar, cov, nb, nf);
    }

    public ExtendedAirlineEstimation estimate(double[] y, Matrix x, boolean mean, double[] periods, int ndiff, boolean ar, String[] outliers, double cv, double precision, boolean approximateHessian) {
        ExtendedAirlineSpec spec = ExtendedAirlineSpec.builder()
                .periodicities(periods)
                .differencingOrder(ndiff)
                .phi(ar ? Parameter.undefined() : null)
                .theta(ar ? null : Parameter.undefined())
                .adjustToInt(false)
                .build();
        return ExtendedAirlineKernel.fastProcess(DoubleSeq.of(y), x, mean, outliers, cv, spec, precision);
    }

    public double[] random(double[] periods, double theta, double[] stheta, boolean adjust, int n, double[] initial, double stdev, int warmup) {
        ExtendedAirlineSpec spec = ExtendedAirlineSpec.builder()
                .periodicities(periods)
                .theta(Parameter.undefined())
                .stheta(Parameter.make(stheta.length))
                .adjustToInt(adjust)
                .build();

        ExtendedAirlineMapping mapping = ExtendedAirlineMapping.of(spec);
        double[] p = new double[stheta.length + 1];
        p[0] = theta;
        for (int i = 0; i < stheta.length; ++i) {
            p[i + 1] = stheta[i];
        }

        ArimaModel model = mapping.map(DoubleSeq.of(p));

        if (initial == null) {
            double[] s = ArimaSeriesGenerator.builder()
                    .initialWarmUp(0)
                    .startMean(0)
                    .startStdev(100)
                    .build()
                    .generate(model, n);
            return s;
        }else{
            return ArimaSeriesGenerator.generate(model, n, initial, new Normal(0, stdev), warmup);
        }
    }

    public SsfUcarimaEstimation ssfDetails(LightExtendedAirlineDecomposition fad) {
        return new SsfUcarimaEstimation(fad.getUcarima(), fad.getY());
    }
}
