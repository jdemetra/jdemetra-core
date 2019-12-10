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
import jdplus.data.DataBlock;
import demetra.design.Development;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.MatrixException;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class Householder implements QRDecomposer {
    
    public static class Processor implements QRDecomposer.Processor{
        
        @Override
        public QRDecomposer decompose(Matrix A, double eps) throws MatrixException {
            return new Householder(A, true, eps);
        }
        
    }

    private double[] qr, rdiag;
    private int[] unused;
    private int norig, n, m; // m=nrows, n=ncols
    
    public Householder(final Matrix A){
        init(A);
        householder(true, Constants.getEpsilon());
    }

    public Householder(final Matrix A, final boolean fast, final double eps){
        init(A);
        householder(fast, eps);
    }

    @Override
    public Matrix r(boolean compact) {
        Matrix r = Matrix.square(n);
        double[] data = r.getStorage();
        for (int i = 0, k = 0, l = 0; i < n; ++i, k += n, l += m) {
            for (int j = 0; j < i; ++j) {
                data[k + j] = qr[l + j];
            }
            data[k + i] = rdiag[i];
        }
        return r;
    }

    @Override
    public DoubleSeq rdiagonal(boolean compact) {
        return DataBlock.of(rdiag);
    }

    /**
     *
     * @return
     */
    @Override
    public int[] unused() {
        return unused;
    }

    @Override
    public int rank() {
        return n;
    }

    @Override
    public int[] dimensions() {
        return new int[]{m,norig};
    }

    @Override
    public int[] used() {
        int[] u=new int[n];
        for (int i=0; i<n; ++i)
            u[i]=i;
        return u;
    }

    private void householder(boolean fast, double eps) {
        int[] tmpunused = new int[norig];
        int nunused = 0, nrdiag = 0;
        // Main loop.
        int len = qr.length;
        for (int k = 0, k0 = 0, k1 = m; k < n; ++k) {
            // Compute 2-norm of k-th column .
            DataBlock col = DataBlock.of(qr, k0, k1, 1);
            double nrm = fast ? col.fastNorm2() : col.norm2();

            if (nrm > eps) {
                // Form k-th Householder vector. v(k)=x(k)+/-norm(x)
                if (qr[k0] < -eps) {
                    nrm = -nrm;
                }
                for (int i = k0; i < k1; ++i) {
                    qr[i] /= nrm;
                }
                qr[k0] += 1.0;
                // rdiag contains the main diagonal of the R matrix
                rdiag[nrdiag++] = -nrm;
                // in this implementation:
                // if a(k,k) < 0 then a(k,k) = -(a(k,k) - nrm) / nrm, else
                // a(k,k)=( a(k,k) + nrm) / nrm

                // Apply transformation to remaining columns.
                for (int jm = k0 + m; jm < len; jm += m) {
                    double s = 0.0;
                    // i+km in [j+km, m+km], 
                    for (int ik = k0, ij = jm; ik < k1; ++ik, ++ij) {
                        s += qr[ik] * qr[ij];
                    }
                    s /= -qr[k0];
                    for (int ik = k0, ij = jm; ik < k1; ++ik, ++ij) {
                        qr[ij] += s * qr[ik];
                    }
                }
                k0 += m + 1;
                k1 += m;
            } else {
                tmpunused[nunused++] = k;
                // move all the toArray to the left
                System.arraycopy(qr, k1, qr, k1 - m, len - k1);
                len -= m;
            }
        }

        if (nunused > 0) {
            this.unused = new int[nunused];
            System.arraycopy(tmpunused, 0, this.unused, 0, nunused);
            n -= nunused;
        } else {
            this.unused = null;
        }
    }

    private void init(Matrix m) {
        this.m = m.getRowsCount();
        norig = n = m.getColumnsCount();
        qr = m.toArray();
        rdiag = new double[n];
    }

    // / <summary>
    // / The read-only property checks whether the matrix to which the
    // Householder is
    // / applied is copyOf full rank. I.e. whether no column is a linear combination
    // copyOf the
    // / other columns.
    // / </summary>
    @Override
    public boolean isFullRank() {
        return n == norig;
    }

    // / <summary>
    // / The method solves a set copyOf simultaneous linear equations A.b = x using
    // / the QR decomposition copyOf the matrix A.
    // / </summary>
    // / <param name="x">In parameter.An array copyOf double. The right hand side copyOf
    // the equation</param>
    // / <returns>The solution b as an array copyOf double</returns>
    // /
    @Override
    public void leastSquares(DoubleSeq x, DataBlock b, DataBlock res) {
        double[] y = new double[x.length()];
        x.copyTo(y, 0);
        Qt(y);
        if (res != null) {
            res.copyFrom(y, n);
        }
        double eps=Constants.getEpsilon();
        // Solve R*X = Y;
        for (int j = n - 1, jm = j * m; j >= 0; --j, jm -= m) {
            double t = y[j];
            if (Math.abs(t) > eps) {
                double dk = rdiag[j];
                if (Math.abs(dk) < eps) {
                    throw new MatrixException(MatrixException.RANK);
                }
                t /= dk;
                for (int i = 0; i < j; ++i) {
                    y[i] -= t * qr[i + jm];
                }
                y[j] = t;

            } else {
                y[j] = 0;
            }
            // Solve R*X = Y;
        }
        b.copyFrom(y, 0);
    }
    
    /**
     *
     */
//    @Override
    private void Qt(double[] b) {
        for (int k = 0, km = 0; k < n; k++, km += m) {
            double s = 0.0;
            for (int i = k, j = km + k; i < m; ++i, ++j) {
                s += qr[j] * b[i];
            }
            if (s != 0) {
                s = -s / qr[km + k];
                for (int i = k, j = km + k; i < m; ++i, ++j) {
                    b[i] += s * qr[j];
                }
            }
        }
    }

    @Override
    public void applyQt(DataBlock x) {
        double[] b=x.getStorage();
        int xinc=x.getIncrement();
        int xbeg=x.getStartPosition(), xend=x.getEndPosition();
        
        for (int k = 0, l=xbeg, km = 0; k < n; k++, km += m, l+=xinc) {
            double s = 0.0;
            for (int i = l, j = km + k; i != xend; i+=xinc, ++j) {
                s += qr[j] * b[i];
            }
            if (s != 0) {
                s = -s / qr[km + k];
                for (int i = l, j = km + k; i != xend; i+=xinc, ++j) {
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
            double s = 0.0;
            for (int i = k, j=l; i < m; ++i, j+=xinc) {
                s += qr[k * m + i] * b[j];
            }
            s = -s / qr[k * m + k];
            for (int i = k, j=l; i < m; ++i, j+=xinc) {
                b[j] += s * qr[k * m + i];
            }
        }
    }
 
}
