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

import demetra.maths.Constants;
import demetra.data.DataBlock;
import demetra.data.accumulator.DoubleAccumulator;
import demetra.design.Development;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.MatrixException;
import demetra.maths.matrices.decomposition.IQRDecomposition;
import demetra.data.accumulator.NeumaierAccumulator;
import java.util.function.Supplier;
import demetra.data.DoubleSequence;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class RobustHouseholder implements IQRDecomposition {

    private final boolean fast;
    private double[] qr, rdiag;
    private int[] unused;
    private int norig, n, m; // m=nrows, n=ncols
    private final Supplier<DoubleAccumulator> acc = () -> new NeumaierAccumulator();
    private double eps=Constants.getEpsilon();

    public RobustHouseholder() {
        fast = false;
    }

    public RobustHouseholder(final boolean fast) {
        this.fast = fast;
    }

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
    public void decompose(Matrix m) {
        init(m);
        householder();
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
    public DoubleSequence rdiagonal(boolean compact) {
        return DataBlock.ofInternal(rdiag);
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
        return new int[]{m, norig};
    }

    @Override
    public int[] used() {
        int[] u = new int[n];
        for (int i = 0; i < n; ++i) {
            u[i] = i;
        }
        return u;
    }

    private void householder() {
        int[] tmpunused = new int[norig];
        double[] del=new double[qr.length];
        int nunused = 0, nrdiag = 0;
        // Main loop.
        int len = qr.length;
        for (int c = 0, k0 = 0, k1 = m; c < n; ++c) {

            // Compute 2-norm copyOf k-th column .
           if (c > 0) {
                for (int q = m*c; q < k1; ++q) {
                    qr[q] += del[q];
                }
            }
            DataBlock col = DataBlock.ofInternal(qr, k0, k1, 1);
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
                // rdiag contains the main diagonal copyOf the R matrix
                rdiag[nrdiag++] = -nrm;
                // in this implementation:
                // if a(k,k) < 0 then a(k,k) = -(a(k,k) - nrm) / nrm, else
                // a(k,k)=( a(k,k) + nrm) / nrm

                // Apply transformation to remaining columns.
                for (int jm = k0 + m; jm < len; jm += m) {
                    double s = 0, sdel = 0;
                    // i+km in [j+km, m+km], 
                    for (int ik = k0, ij = jm; ik < k1; ++ik, ++ij) {
                        double x = qr[ik] * (qr[ij] + del[ij]);
                        double t = s + x;
                        if (Math.abs(s) >= Math.abs(x)) {
                            sdel += (s - t) + x;
                        } else {
                            sdel += (x - t) + s;
                        }
                        s = t;
                    }
                    s += sdel;
                    s /= -qr[k0];
                    for (int ik = k0, ij = jm; ik < k1; ++ik, ++ij) {
                        double x = s * qr[ik];
                        double t = qr[ij] + x;
                        if (Math.abs(qr[ij]) >= Math.abs(x)) {
                            del[ij] += (qr[ij] - t) + x;
                        } else {
                            del[ij] += (x - t) + qr[ij];
                        }
                        qr[ij] = t;
                    }
                }
                k0 += m + 1;
                k1 += m;
            } else {
                tmpunused[nunused++] = c;
                // move all the toArray to the left
                System.arraycopy(qr, k1, qr, k1 - m, len - k1);
                System.arraycopy(del, k1, del, k1 - m, len - k1);
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
        qr = m.data();
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
    public void leastSquares(DoubleSequence x, DataBlock b, DataBlock res) {
        // if (x.Length != m)
        // throw new MatrixException(MatrixException.IncompatibleDimensions);
        // if (!IsFullRank)
        // throw new MatrixException(MatrixException.RankError);

        // Compute Y = transpose(applyQ)*B
        // copy b
        double[] y = new double[x.length()];
        x.copyTo(y, 0);
        Qt(y);
        if (res != null) {
            res.copyFrom(y, n);
        }
        // Solve R*X = Y;
        DoubleAccumulator da = acc.get();
        for (int j = n - 1, jm = j + (j + 1) * m; j >= 0; --j, jm -= m + 1) { // jm= q(j, j+1) 
            da.set(y[j]);
            for (int k = j + 1, l = jm; k < n; ++k, l += m) {
                da.add(-y[k] * qr[l]);
            }
            double t = da.sum();
            if (Math.abs(t) > eps) {
                double d = rdiag[j];
                if (d == 0) {
                    throw new MatrixException(MatrixException.SINGULAR);
                }
                y[j] = t / d;
            } else {
                y[j] = 0;
            }
        }
        b.copyFrom(y, 0);
    }

    /**
     *
     */
//    @Override
    private void Qt(double[] b) {

        DoubleAccumulator da = acc.get();
        double[] bdel = new double[b.length];
        for (int k = 0, km = 0; k < n; k++, km += m) {
            da.reset();
            for (int i = k, j = km + k; i < m; ++i, ++j) {
                da.add(qr[j] * (b[i]+bdel[i]));
            }
            double s = da.sum();
            if (s != 0) {
                s = -s / qr[km + k];
                for (int i = k, j = km + k; i < m; ++i, ++j) {
                    double x = s * qr[j];
                    double t = b[i] + x;
                    if (Math.abs(b[i]) >= Math.abs(x)) {
                        bdel[i] += (b[i] - t) + x;
                    } else {
                        bdel[i] += (x - t) + b[i];
                    }
                    b[i] = t;
                }
            }
        }
        for (int i=0; i<m; ++i){
            b[i]+=bdel[i];
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
