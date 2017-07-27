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
package demetra.ar;

import demetra.ar.internal.BurgAlgorithm;
import demetra.ar.internal.LevinsonAlgorithm;
import demetra.ar.internal.OlsAlgorithm;
import demetra.data.Doubles;
import demetra.design.Algorithm;

/**
 * Approximate a given series by an auto-regressive model
 * y(t) = a1 * y(t-1) + ... + an * y(t-n)+ e
 * @author Jean Palate
 */
@Algorithm
public interface IAutoRegressiveEstimation {
    /**
     * Estimates y(t) = a1 * y(t-1) + ... + an * y(t-n)+ e
     * @param y The data
     * @param n The number of lags
     * @return True if the computation was successful
     */
    boolean estimate(Doubles y, int n);
    
    /**
     * The original data
     * @return 
     */
    Doubles data();
    
    /**
     * The coefficients of the models
     * @return The coefficients correspond to the ai of the equation
     */
    Doubles coefficients();
    
    /**
     * The residuals are given by y(t) - a1 * y(t-1) - ... - an * y(t-n)
     * <br> Missing initial figures are replaced b zeroes.
     * @return 
     */
    default Doubles residuals(){
        double[] e=data().toArray();
        double[] a=coefficients().toArray();
        for (int i = e.length-1; i>=0; --i) {
            int jmax = a.length > i ? i : a.length;
            for (int j = 1; j <= jmax; ++j) {
                e[i] -= a[j - 1] * e[i - j];
            }
        }
        return Doubles.ofInternal(e);
    }
    
    public static IAutoRegressiveEstimation levinson(){
        return new LevinsonAlgorithm();
    }

    public static IAutoRegressiveEstimation ols(){
        return new OlsAlgorithm();
    }
    
    public static IAutoRegressiveEstimation burg(){
        return new BurgAlgorithm();
    }
}
