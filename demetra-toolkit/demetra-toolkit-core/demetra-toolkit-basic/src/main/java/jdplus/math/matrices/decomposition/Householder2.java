/*
 * Copyright 2016 National Bank copyOf Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions copyOf the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy copyOf the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.math.matrices.decomposition;

import demetra.math.Constants;
import demetra.design.Development;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.MatrixWindow;
import jdplus.math.matrices.lapack.DataPointer;
import jdplus.math.matrices.lapack.LapackUtility;

/**
 * QR decomposition of A. The matrix Q is represented as a product of
 * elementary reflectors Q = H(1) H(2) . . . H(k), where k = min(m,n).
 *
 * Each H(i) has the form
 *
 * H(i) = I - tau * v * v**T
 *
 * where tau is a real scalar, and v is a real vector with v(0:i-1) = 0 and
 * v(i) = 1; v(i+1:m) is stored on exit in A(i+1:m,i), and tau in TAU(i).
 *
 * @param A On entry, the m by n matrix A. On exit, the elements on and
 * above the diagonal of the Matrix contain the min(m,n) by n upper
 * trapezoidal matrix R (R is upper triangular if m >= n); the elements
 * below the diagonal, with the array tAU, represent the orthogonal matrix Q
 * as a product of elementary reflectors.
 *
 * @param tau The scalar factors of the elementary reflectors (out
 * parameter). The size of tau must be equal to the number of columns of A
 * (n)
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class Householder2 {

    private double[] qr, tau;
    private int m, n; // m=nrows, n=ncols

    public QRDecomposition decompose(final Matrix A, final double eps) {
        init(A);
        householder();
        return new QRDecomposition(Matrix.builder(qr).nrows(m).ncolumns(n).build(),
                tau, null);
    }

    private void init(final Matrix A) {
        qr = A.toArray();
        m = A.getRowsCount();
        n = A.getColumnsCount();
        tau = new double[n];
    }

    private void householder() {
        int k = Math.min(m, n);
        Reflector hous = new Reflector(qr);
        Matrix M = Matrix.builder(qr).nrows(m).ncolumns(n).build();
        MatrixWindow wnd = M.all();
        for (int i = 0, j = 0; i < k; ++i, j += m + 1) {
            hous.set(j, m - i);
            hous.larfg();
            tau[i] = hous.tau;
            int nc = n - i - 1;
            if (nc > 0) {
                hous.lapply(wnd.bhshrink());
            }
            qr[j] = hous.beta;
            wnd.bvshrink();
        }
    }

    @lombok.Data
    public static class Reflector {

        double tau, beta;
        private final double[] px;
        private int xstart; // start of the vector 
        int n; // length of the vector 

        double x0() {
            return px[xstart];
        }

        void x0(double value) {
            px[xstart] = value;
        }

        DataPointer x() {
            return DataPointer.of(px, xstart);
        }

        DataPointer v() {
            return DataPointer.of(px, xstart + 1);
        }

        public Reflector(double[] x) {
            this.px = x;
        }

        /**
         * Initialize the reflector
         *
         * @param start Starting position of the vector
         * @param n Number of elements in the vector
         */
        public void set(int start, int n) {
            this.xstart = start;
            this.n = n;
            this.tau = 0;
        }

        public void larfg() {
            tau = 0;
            int m = n - 1;
            if (m <= 0) {
                return;
            }
            double x0 = x0();
            DataPointer v = v();
            double xnorm = v.norm2(m);
            if (xnorm == 0) {
                return;
            }

            beta = -Math.copySign(LapackUtility.lapy2(x0, xnorm), x0);
            double eps = Constants.getEpsilon();
            double safemin = Constants.getSafeMin() / eps;
            int k = 0;
            if (Math.abs(beta) < safemin) {
                double rsafemin = 1 / safemin;
                do {
                    v.mul(m, rsafemin);
                    x0 *= rsafemin;
                    beta *= rsafemin;
                } while (Math.abs(beta) < safemin && ++k < 4);
                xnorm = v.norm2(m);
                beta = -Math.copySign(LapackUtility.lapy2(x0, xnorm), x0);
            }
            tau = (beta - x0) / beta;
            v.mul(m, 1 / (x0 - beta));
            for (int j = 0; j < k; ++j) {
                beta *= safemin;
            }
            x0(1);
        }

        public void lapply(Matrix X) {
            int m = X.getRowsCount(), n = X.getColumnsCount(), lda = X.getColumnIncrement();
            DataPointer col = DataPointer.of(X.getStorage(), X.getStartPosition());
            DataPointer h = x();
            for (int i = 0; i < n; ++i, col.move(lda)) {
                double xh = h.dot(m, col);
                col.addAX(m, -tau * xh, h);
            }
        }

    }

}
