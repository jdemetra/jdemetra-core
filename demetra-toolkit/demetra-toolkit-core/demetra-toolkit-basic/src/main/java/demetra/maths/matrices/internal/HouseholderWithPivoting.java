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
package demetra.maths.matrices.internal;

import demetra.data.DataBlock;
import demetra.data.DataWindow;
import demetra.design.Development;
import demetra.maths.Constants;
import demetra.maths.matrices.FastMatrix;
import demetra.maths.matrices.MatrixException;
import demetra.maths.matrices.decomposition.IQRDecomposition;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class HouseholderWithPivoting implements IQRDecomposition {

    private double[] qr, rdiag, norm, wa;
    private int[] unused;
    private int norig, n, m; // m=nrows, n=ncols
    private int[] col;
    private double eps = Constants.getEpsilon();

    @Override
    public double getPrecision(){
        return eps;
    }

    @Override
    public void setPrecision(double eps){
        this.eps=eps;
    }
    /**
     *
     * @param m
     */
    @Override
    public void decompose(FastMatrix m) {
        init(m);
        householder();
    }

    private int pos(int var) {
        for (int i = 0; i < norig; ++i) {
            if (col[i] == var) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public FastMatrix r(boolean compact) {
        if (compact) {
            FastMatrix r = FastMatrix.square(n);
            return r;

        } else {
            FastMatrix r = FastMatrix.square(norig);
            return r;
        }
    }

    @Override
    public DoubleSeq rdiagonal(boolean compact) {
        double[] diag;
        if (compact) {
            diag = new double[n];
            int[] u = used();
            for (int i = 0; i < n; ++i) {
                diag[pos(u[i])] = rdiag[i];
            }
        } else {
            diag = new double[norig];
            for (int i = 0; i < n; ++i) {
                diag[col[i]] = rdiag[i];
            }
        }
        return DataBlock.of(diag);
    }

    private static final int[] EMPTY = new int[0];

    @Override
    public int[] used() {
        int[] u = new int[n];
        for (int i = 0, j = 0; i < norig; ++i) {
            if (isUsed(i)) {
                u[j++] = i;
            }
        }
        return u;
    }

    public boolean isUsed(int k) {
        if (unused == null) {
            return true;
        }
        for (int i = 0; i < unused.length; ++i) {
            if (k == unused[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int[] unused() {
        return unused != null ? unused : EMPTY;
    }

    @Override
    public int[] dimensions() {
        return new int[]{m, norig};
    }

    private void householder() {
        // Main loop.

        for (int k = 0; k < n; ++k) {

            // select the column with the greatest norm on active components
            int lmax = k;
            for (int j = k + 1; j < n; ++j) {
                if (rdiag[col[j]] > rdiag[col[lmax]]) {
                    lmax = j;
                }
            }
            if (lmax != k) {
                int tmp = col[k];
                col[k] = col[lmax];
                col[lmax] = tmp;
            }
            int ck = col[k];

            // Compute norm2 copyOf the current column
            int k0 = ck * m, k1 = k0 + m;
            k0 += k;
            DataBlock cur = DataBlock.of(qr, k0, k1);
            double nrm = cur.norm2();

            if (nrm > eps) {
                // Form k-th Householder vector. v(k)=x(k)+/-norm(x)
                // in this implementation:
                // if a(k,k) < 0 then a(k,k) = -(a(k,k) - nrm) / nrm, else
                // a(k,k)=( a(k,k) + nrm) / nrm
                if (qr[k0] < -eps) {
                    nrm = -nrm;
                }
                for (int i = k0; i < k1; ++i) {
                    qr[i] /= nrm;
                }
                qr[k0] += 1.0;
                double kk = qr[k0];
                // rdiag contains the main diagonal copyOf the R matrix
                rdiag[ck] = -nrm;

                for (int j = k + 1; j < n; ++j) {
                    int cj = col[j];
                    // Apply transformation to remaining columns.
                    int j0 = cj * m + k;
                    double s = 0.0;
                    // i+km in [j+km, m+km], 
                    for (int ik = k0, ij = j0; ik < k1; ++ik, ++ij) {
                        s += qr[ik] * qr[ij];
                    }
                    s /= -kk;
                    for (int ik = k0, ij = j0; ik < k1; ++ik, ++ij) {
                        qr[ij] += s * qr[ik];
                    }
                    // Update the norms
                    if (Math.abs(rdiag[cj]) > eps) {
                        double tmp = qr[j0] / rdiag[cj];
                        rdiag[cj] *= Math.sqrt(Math.max(1 - tmp * tmp, 0));
                        tmp = rdiag[cj] / wa[cj];
                        if (.05 * tmp * tmp <= eps) {
                            rdiag[cj] = DataBlock.of(qr, j0, j0 + m - k).fastNorm2();
                            wa[cj] = rdiag[cj];
                        }
                    }
                }
            } else {
                // we stop
                for (int i = k; i < n; ++i) {
                    rdiag[col[i]] = 0;
                }
                n = k;
                unused = new int[norig - n];
                for (int i = 0; i < unused.length; ++i, ++k) {
                    unused[i] = col[k];
                }
                break;
            }
        }

    }

    private void init(FastMatrix M) {
        m = M.getRowsCount();
        norig = n = M.getColumnsCount();
        qr = M.toArray();
        rdiag = new double[n];
        norm = new double[n];
        wa = new double[n];
        col = new int[n];
        // initializations
        DataWindow wnd = DataWindow.startOf(qr);
        for (int k = 0; k < n; ++k) {
            col[k] = k;
            double nrm = wnd.next(m).fastNorm2();
            rdiag[k] = nrm;
            norm[k] = nrm;
            wa[k] = nrm;
        }
    }

    @Override
    public int rank() {
        return n;
    }

    @Override
    public boolean isFullRank() {
        return n == norig;
    }

    @Override
    public void leastSquares(DoubleSeq x, DataBlock b, DataBlock res) {
        double[] y = new double[x.length()];
        x.copyTo(y, 0);
        Qt(y);
        if (res != null) {
            res.copyFrom(y, n);
        }
        // Solve R*X = Y; don't forget that the order ofInternal the column is given in pivot
        for (int j = n - 1; j >= 0; --j) {
            int cj = col[j];
            double t = y[j];
            if (Math.abs(t) > eps) {
                double d = rdiag[cj];
                if (Math.abs(d) < eps) {
                    throw new MatrixException(MatrixException.RANK);
                }
                t /= d;
                for (int i = 0; i < j; ++i) {
                    y[i] -= t * qr[i + cj * m];
                }
                y[j] = t;

            } else {
                y[j] = 0;
            }
        }
        restore(b, y);
    }

    /**
     *
     */
    private void Qt(double[] b) {
        for (int k = 0; k < n; k++) {
            int k0 = col[k] * m + k;
            double s = 0.0;
            for (int i = k, j = k0; i < m; ++i, ++j) {
                s -= qr[j] * b[i];
            }
            if (s != 0) {
                s /= qr[k0];
                for (int i = k, j = k0; i < m; ++i, ++j) {
                    b[i] += s * qr[j];
                }
            }
        }
    }

    private void restore(DataBlock external, double[] internal) {
        for (int i = 0; i < n; ++i) {
            external.set(col[i], internal[i]);
        }
    }

    @Override
    public void applyQt(DataBlock x) {
        double[] b=x.getStorage();
        int xinc=x.getIncrement();
        int xbeg=x.getStartPosition(), xend=x.getEndPosition();
        
        for (int k = 0, l=xbeg; k < n; k++, l+=xinc) {
            int k0 = col[k] * m + k;
            double s = 0.0;
            for (int i = l, j = k0; i != xend; i+=xinc, ++j) {
                s += qr[j] * b[i];
            }
            if (s != 0) {
                s = -s / qr[k0];
                for (int i = l, j = k0; i != xend; i+=xinc, ++j) {
                    b[i] += s * qr[j];
                }
            }
        }
    }

    @Override
    public void applyQ(DataBlock x) {
        double[] b=x.getStorage();
        int xinc=x.getIncrement();
        int xbeg=x.getStartPosition();
        for (int k = n - 1, l=xbeg+(n-1)*xinc; k >= 0; --k, l-=xinc) {
            int k0 = col[k] * m ;
            double s = 0.0;
            for (int i = k, j=l; i < m; ++i, j+=xinc) {
                s += qr[k0 + i] * b[j];
            }
            s = -s / qr[k0 + k];
            for (int i = k, j=l; i < m; ++i, j+=xinc) {
                b[j] += s * qr[k0 + i];
            }
        }
    }

}
