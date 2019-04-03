/*
 * Copyright 2016 National Bank of Belgium
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
package demetra.ar.internal;

import demetra.design.AlgorithmImplementation;
import static demetra.design.AlgorithmImplementation.Feature.Balanced;
import org.openide.util.lookup.ServiceProvider;
import demetra.ar.AutoRegressiveEstimation;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = AutoRegressiveEstimation.class)
@AlgorithmImplementation(algorithm =AutoRegressiveEstimation.class, feature=Balanced)
public class BurgAlgorithm implements AutoRegressiveEstimation {

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
        return DoubleSeq.of(y);
    }

    private boolean calc(int m) {
        int n = y.length - 1;
        double[] ak = new double[m + 1];
        ak[0] = 1.0;
        double[] f = y.clone(), b = y.clone();
        double dk = 0.0;
        for (int j = 0; j <= n; j++) {
            dk += 2 * f[j] * f[j];
        }
        dk -= f[0] * f[0] + b[n] * b[n];
        for (int k = 0; k < m; k++) {
            double mu = 0.0;
            for (int l = 0; l <= n - k - 1; l++) {
                mu += f[l + k + 1] * b[l];
            }
            mu *= -2.0 / dk;
            for (int l = 0; l <= (k + 1) / 2; l++) {
                double t1 = ak[l] + mu * ak[k + 1 - l];
                double t2 = ak[k + 1 - l] + mu * ak[l];
                ak[l] = t1;
                ak[k + 1 - l] = t2;
            }
            for (int l = 0; l <= n - k - 1; l++) {
                double t1 = f[l + k + 1] + mu * b[l];
                double t2 = b[l] + mu * f[l + k + 1];
                f[l + k + 1] = t1;
                b[l] = t2;
            }
            dk = (1.0 - mu * mu) * dk - f[k + 1] * f[k + 1] - b[n - k - 1] * b[n - k - 1];
        }
        a = new double[m];
        for (int i=0; i<m; ++i)
            a[i]=-ak[i+1];
        return true;
    }
}
