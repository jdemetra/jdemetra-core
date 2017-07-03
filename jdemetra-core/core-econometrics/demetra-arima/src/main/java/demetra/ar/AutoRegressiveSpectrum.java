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
package demetra.ar;

import ec.tstoolkit.arima.Spectrum;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.linearfilters.SymmetricFilter;
import ec.tstoolkit.maths.matrices.Householder;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.MatrixException;
import ec.tstoolkit.maths.polynomials.Polynomial;

/**
 *
 * @author Jean Palate
 */
public class AutoRegressiveSpectrum {

    public static final int MAX_AR = 50;
    private double[] ar;
    private double sig;
    private final Method method_;

    public static enum Method {

        Ols, Durbin
    };

    public AutoRegressiveSpectrum(Method m) {
        method_ = m;
    }

    public boolean process(IReadDataBlock data, final int nar) {
        clear();
        int nlags = nar == 0 ? computeDefaultLags(data.getLength()) : nar;
        if (nlags < 0) {
            return false;
        }
        switch (method_) {
            case Durbin:
                computeDurbin(data, nlags);
                break;
            case Ols:
                computeOls(data, nlags);
        }
        return ar != null;
    }

    private void clear() {
        ar = null;
        sig = 0;
    }

    private void computeDurbin(IReadDataBlock data, int nar) {
        DurbinAlgorithm durbin = new DurbinAlgorithm();
        if (durbin.solve(data, nar)) {
            ar = durbin.getCoefficients();
        }
        sig = durbin.getInnovationVariance();
    }

    public static int computeDefaultLags(int n) {
        int npi;
        double ln = Math.log(n);
        npi = (int) (ln * ln);
        if (npi > MAX_AR) {
            npi = MAX_AR;
        }
        if (npi > n - n / 4) {
            return -1;
        }
        return npi;
    }

    private void computeOls(IReadDataBlock data, int nar) {
        try {
            clear();
            // OLS on the data
            int n = data.getLength();
            double[] all = new double[n];
            data.copyTo(all, 0);
            // remove mean
            double m = 0;
            for (int i = 0; i < all.length; ++i) {
                if (Double.isNaN(all[i])) {
                    all[i] = 0;
                } else {
                    m += all[i];
                }
            }
            m /= all.length;
            for (int i = 0; i < all.length; ++i) {
                all[i] -= m;
            }
            int nc = n - nar;
            if (nc < nar) {
                return;
            }
            Matrix M = new Matrix(nc, nar);
            DataBlock rc = new DataBlock(all, nar, n, 1);
            DataBlockIterator cols = M.columns();
            DataBlock col = cols.getData();
            do {
                rc.move(-1);
                col.copy(rc);
            } while (cols.next());

            Householder qr = new Householder(false);
            qr.decompose(M);
            ar = new double[nar];
            DataBlock c = new DataBlock(ar);
            DataBlock e = new DataBlock(nc - nar);
            qr.leastSquares(new DataBlock(all, nar, n, 1), c, e);
            c.chs();
            sig = e.ssq() / nc;
        } catch (MatrixException err) {
            clear();
        }
    }

    public double value(double f) {
        if (f < 0 || f > Math.PI) {
            throw new IllegalArgumentException();
        }
        double c2 = 1, s2 = 0;
        for (int j = 0; j < ar.length; ++j) {
            double dj = (j + 1) * f;
            c2 += ar[j] * Math.cos(dj);
            s2 += ar[j] * Math.sin(dj);
        }
        double p = sig / (c2 * c2 + s2 * s2);
        if (p <= 0) {
            return 0;
        }
        return Math.log10(p) * 10;
    }

    public double getSigma() {
        return sig; //To change body of generated methods, choose Tools | Templates.
    }

    public IReadDataBlock getCoefficients() {
        return ar != null ? new ReadDataBlock(ar) : DataBlock.EMPTY;
    }
}
