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
import demetra.math.Constants;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.MatrixWindow;
import jdplus.math.matrices.DataPointer;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class HouseholderWithPivoting {

    private double[] qr, beta;
    private int m, n; // m=nrows, n=ncols
    private int nfixed;
    private int[] pivot;

    public QRDecomposition decompose(FastMatrix A, int nfixed) {
        init(A, nfixed);
        householder();
        return new QRDecomposition(FastMatrix.builder(qr).nrows(m).ncolumns(n).build(),
                beta, pivot);
    }

    private void householder() {
        int k = Math.min(m, nfixed);
        Reflector hous = new Reflector(qr);
        FastMatrix M = FastMatrix.builder(qr).nrows(m).ncolumns(n).build();
        MatrixWindow wnd = M.all();
        int j = 0;
        for (int i = 0; i < k; ++i, j += m + 1) {
            hous.set(j, m - i);
            hous.larfg();
            int nc = n - i - 1;
            if (nc > 0) {
                FastMatrix m = wnd.bhshrink();
                if (hous.beta != 0) {
                    hous.lapply(m);
                }
            }
            beta[i] = hous.beta;
            qr[j] = hous.alpha;
            wnd.bvshrink();

        }
        if (nfixed == n) {
            return;
        }

        k = Math.min(m, n);
        double[] norm = new double[k];
        double[] pnorm = new double[k];
        pivot = new int[k];
        // initializations
        for (int i = 0; i < k; ++i) {
            pivot[i] = i;
        }
        // computes the norms of the free columns
        DataPointer c = DataPointer.of(qr, j);
        for (int i = nfixed; i < k; ++i, c.move(m)) {
            double nrm = c.fastNorm2(m - nfixed);
            norm[i] = nrm;
            pnorm[i] = nrm;
        }

        double tol = Math.sqrt(Constants.getEpsilon());

        for (int i = nfixed; i < k; ++i, j += m + 1) {
            // search the column with the highest partial norm
            int icur = i;
            for (int l = i + 1; l < k; ++l) {
                if (pnorm[l] > pnorm[icur]) {
                    icur = l;
                }
            }
            // swap the considered columns if need be
            if (icur != i) {
                // swap the full columns
                swapColumns(i, icur);
                pnorm[icur] = pnorm[i];
                norm[icur] = norm[i];
                // don't care about the current norms
            }
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
            // update the norms of partial columns
            // pnorm(ci:n) = pnorm(H(i)c(i:n))-> pnorm(c(i+1:n))=sqrt(pnorm(ci:n)^2-H(i)c(i)^2)
            // =pnorm(ci:n)sqrt(1-tmp*tmp)
            for (int l = i + 1, j0 = j + m; l < k; ++l, j0 += m) {
                if (pnorm[l] != 0) {
                    double tmp = qr[j0] / pnorm[l];
                    tmp = Math.max(1 - tmp * tmp, 0);
                    double rnorm = pnorm[l] / norm[l];
                    double tmp2 = tmp * rnorm * rnorm;
                    if (tmp2 <= tol) {
                        c.pos(j0 + 1);
                        double nrm = c.fastNorm2(m - i - 1);
                        norm[l] = pnorm[l] = nrm;
                    } else {
                        pnorm[l] *= Math.sqrt(tmp);
                    }
                }
            }
        }
    }

    private void init(FastMatrix M, int nfixed) {
        this.m = M.getRowsCount();
        this.n = M.getColumnsCount();
        this.nfixed = nfixed;
        qr = M.toArray();
        beta = new double[n];
    }

    private void swapColumns(int i, int j) {
        for (int ri = i * m, rimax = ri + m, rj = j * m; ri < rimax; ++ri, ++rj) {
            double tmp = qr[ri];
            qr[ri] = qr[rj];
            qr[rj] = tmp;
        }
        // swap idx
        int tmp = pivot[j];
        pivot[j] = pivot[i];
        pivot[i] = tmp;
    }
}
