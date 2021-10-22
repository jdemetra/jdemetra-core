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
import demetra.highfreq.FractionalAirlineDecomposition;
import demetra.highfreq.FractionalAirlineEstimation;
import demetra.highfreq.FractionalAirlineSpec;
import jdplus.fractionalairline.FractionalAirlineKernel;
import jdplus.ssf.extractors.SsfUcarimaEstimation;
import demetra.math.matrices.Matrix;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class FractionalAirlineProcessor{

    public FractionalAirlineDecomposition decompose(double[] s, double period, boolean sn, boolean cov, int nb, int nf) {
        int iperiod = (int) period;
        if (Math.abs(period - iperiod) < 1e-9) {
            period = iperiod;
        }
        return FractionalAirlineKernel.decompose(DoubleSeq.of(s), period, sn, cov, nb, nf);
    }

    public FractionalAirlineDecomposition decompose(double[] s, double[] periods, int ndiff, boolean cov, int nb, int nf) {
        return FractionalAirlineKernel.decompose(DoubleSeq.of(s), periods, ndiff, cov, nb, nf);
    }
    
    public FractionalAirlineEstimation estimate(double[] y, Matrix x, boolean mean, double[] periods, int ndiff, String[] outliers, double cv, double precision, boolean approximateHessian){
        FractionalAirlineSpec spec = FractionalAirlineSpec.builder()
                .y(y)
                .X(x)
                .meanCorrection(mean)
                .periodicities(periods)
                .outliers(outliers)
                .criticalValue(cv)
                .adjustToInt(false)
                .precision(precision)
                .differencingOrder(ndiff)
                .approximateHessian(approximateHessian)
                .build();
        return FractionalAirlineKernel.process(spec);
    }
    
    public SsfUcarimaEstimation ssfDetails(FractionalAirlineDecomposition fad){
        return new SsfUcarimaEstimation(fad.getUcarima(), fad.getY());
    }
}

