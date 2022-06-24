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

import nbbrd.design.Development;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.MatrixWindow;

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
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class Householder2 {

    private double[] qr, beta;
    private int m, n; // m=nrows, n=ncols

    public QRDecomposition decompose(final FastMatrix A) {
        init(A);
        householder();
        return new QRDecomposition(FastMatrix.builder(qr).nrows(m).ncolumns(n).build(),
                beta, null);
    }

    private void init(final FastMatrix A) {
        qr = A.toArray();
        m = A.getRowsCount();
        n = A.getColumnsCount();
        beta = new double[n];
    }

    private void householder() {
        int k = Math.min(m, n);
        Reflector hous = new Reflector(qr);
        FastMatrix M = FastMatrix.builder(qr).nrows(m).ncolumns(n).build();
        MatrixWindow wnd = M.all();
        for (int i = 0, j = 0; i < k; ++i, j += m + 1) {
            hous.set(j, m - i);
            hous.larfg();
            int nc = n - i - 1;
            if (nc > 0) {
                FastMatrix fm = wnd.bhshrink();
                if (hous.beta != 0) {
                    hous.lapply(fm);
                }
            }
            beta[i] = hous.beta;
            qr[j] = hous.alpha;
            wnd.bvshrink();
        }
    }
}
