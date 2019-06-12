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
package jdplus.ar;

import demetra.design.Algorithm;
import demetra.data.DoubleSeq;
import internal.jdplus.ar.BurgAlgorithm;
import internal.jdplus.ar.LevinsonAlgorithm;
import internal.jdplus.ar.OlsAlgorithm;

/**
 * Approximate a given series by an auto-regressive model y(t) = a1 * y(t-1) +
 * ... + an * y(t-n)+ e
 *
 * @author Jean Palate
 */
@Algorithm
public interface AutoRegressiveEstimation {

    public static AutoRegressiveEstimation levinson() {
        return new LevinsonAlgorithm();
    }

    public static AutoRegressiveEstimation ols() {
        return new OlsAlgorithm();
    }

    public static AutoRegressiveEstimation burg() {
        return new BurgAlgorithm();
    }

    /**
     * Estimates y(t) = a1 * y(t-1) + ... + an * y(t-n)+ e
     *
     * @param y The data
     * @param n The number of lags
     * @return True if the computation was successful
     */
    boolean estimate(DoubleSeq y, int n);

    /**
     * The original data
     *
     * @return
     */
    DoubleSeq data();

    /**
     * The coefficients of the models
     *
     * @return The coefficients correspond to the ai of the equation
     */
    DoubleSeq coefficients();

    /**
     * The residuals are given by y(t) - a1 * y(t-1) - ... - an * y(t-n)
     * <br> Missing initial figures are replaced b zeroes.
     *
     * @return
     */
    default DoubleSeq residuals() {
        double[] e = data().toArray();
        double[] a = coefficients().toArray();
        for (int i = e.length - 1; i >= 0; --i) {
            int jmax = a.length > i ? i : a.length;
            for (int j = 1; j <= jmax; ++j) {
                e[i] -= a[j - 1] * e[i - j];
            }
        }
        return DoubleSeq.of(e);
    }

}
