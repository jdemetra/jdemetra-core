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
import demetra.math.matrices.MatrixType;
import jdplus.fractionalairline.FractionalAirlineKernel;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class FractionalAirlineProcessor{


    public FractionalAirlineDecomposition decompose(double[] s, double period, boolean adjust, boolean sn) {
        int iperiod = (int) period;
        if (period - iperiod < 1e-9) {
            period = iperiod;
            adjust = false;
        }
        
        return FractionalAirlineKernel.decompose(DoubleSeq.of(s), period, adjust, sn);
    }
    
    public FractionalAirlineEstimation estimate(double[] y, MatrixType x, boolean mean, double[] periods, String[] outliers, double cv, double precision){
        FractionalAirlineSpec spec = FractionalAirlineSpec.builder()
                .y(y)
                .X(x)
                .meanCorrection(mean)
                .periodicities(periods)
                .outliers(outliers)
                .criticalValue(cv)
                .adjustToInt(false)
                .precision(precision)
                .build();
        return FractionalAirlineKernel.process(spec);
    }
}

