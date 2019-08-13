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
package internal.jdplus.ar;

import demetra.design.AlgorithmImplementation;
import static demetra.design.AlgorithmImplementation.Feature.Fast;
import nbbrd.service.ServiceProvider;
import jdplus.ar.AutoRegressiveEstimation;
import demetra.data.DoubleSeq;

/**
 * This class implements the Durbin's algorithm, which fits recursively
 * auto-regressive models
 *
 * @author Jean Palate
 */
@ServiceProvider(AutoRegressiveEstimation.class)
@AlgorithmImplementation(algorithm=AutoRegressiveEstimation.class, feature=Fast)
public class LevinsonAlgorithm implements AutoRegressiveEstimation {

    private static final double SMALL = 1e-9;

    private double[] y;
    private double[] a;

    @Override
    public boolean estimate(DoubleSeq y, int m) {
        this.y = new double[y.length()];
        y.copyTo(this.y, 0);
        return calc(m);
    }

    @Override
    public DoubleSeq coefficients() {
        return DoubleSeq.of(a);
    }

    @Override
    public DoubleSeq data() {
        return DoubleSeq.copyOf(y);
    }

    private boolean calc(int m) {
        int n = y.length - 1;
        double[] r = new double[m + 1];
        for (int i = 0; i <= m; i++) {
            for (int j = 0; j <= n - i; j++) {
                r[i] += y[j] * y[j + i];
            }
        }
        double[] ak = new double[m + 1];
        ak[0] = 1.0;
        double ek = r[0];
        for (int k = 0; k < m; k++) {
            double lambda = 0.0;
            for (int j = 0; j <= k; j++) {
                lambda -= ak[j] * r[k + 1 - j];
            }
            lambda /= ek;

            for (int l = 0; l <= (k + 1) / 2; l++) {
                double tmp = ak[k + 1 - l] + lambda * ak[l];
                ak[l] += lambda * ak[k + 1 - l];
                ak[k + 1 - l] = tmp;
            }
            ek *= 1.0 - lambda * lambda;
        }

        a = new double[m];
        for (int i=0; i<m; ++i)
            a[i]=-ak[i+1];
        return true;
    }
}
