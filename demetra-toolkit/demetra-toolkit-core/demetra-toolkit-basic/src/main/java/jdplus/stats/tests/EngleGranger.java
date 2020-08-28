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
package jdplus.stats.tests;

import demetra.data.DoubleSeq;
import demetra.math.Constants;
import demetra.stats.TestResult;
import jdplus.linearmodel.LeastSquaresResults;
import jdplus.linearmodel.LinearModel;
import jdplus.linearmodel.Ols;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class EngleGranger {

    /**
     * Cointegration test
     * Computes Dickey-Fuller on e(t) where y(t)=a+b*x(t)+e(t)
     *
     * @param x
     * @param y
     * @return
     */
    public AugmentedDickeyFuller df(DoubleSeq x, DoubleSeq y) {
        if (x.allMatch(z -> Math.abs(z) < Constants.getEpsilon())
                || y.allMatch(z -> Math.abs(z) < Constants.getEpsilon())) {
            return null;
        }
        try {
            LinearModel lm = LinearModel.builder()
                    .y(y)
                    .addX(x)
                    .meanCorrection(true)
                    .build();
            LeastSquaresResults lsr = Ols.compute(lm);
            DoubleSeq e = lsr.residuals();
            return AugmentedDickeyFuller.builder().data(e).build();
        } catch (Exception err) {
            return null;
        }
    }
}
