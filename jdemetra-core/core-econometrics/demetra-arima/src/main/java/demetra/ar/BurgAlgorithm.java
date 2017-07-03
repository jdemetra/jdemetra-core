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
package demetra.ar;

/**
 *
 * @author Jean Palate
 */
public class BurgAlgorithm {

    private double[] x;
    private double[] a;

    public boolean solve(IReadDataBlock x, int m) {
        this.x = new double[x.getLength()];
        x.copyTo(this.x, 0);
        return calc(m);
    }

    public IReadDataBlock getCoefficients() {
        return new ReadDataBlock(a);
    }

    public double[] residuals() {
        int n = x.length;
        double[] res = new double[n];
        for (int i = 0; i < n; ++i) {
            double e = x[i];
            int jmax = a.length > i ? i : a.length;
            for (int j = 1; j <= jmax; ++j) {
                e += a[j - 1] * x[i - j];
            }
            res[i] = e;
        }
        return res;
    }

    private boolean calc(int m) {
        int n = x.length - 1;
        double[] ak = new double[m + 1];
        ak[0] = 1.0;
        double[] f = x.clone(), b = x.clone();
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
        System.arraycopy(ak, 1, a, 0, m);
        return true;
    }
}
