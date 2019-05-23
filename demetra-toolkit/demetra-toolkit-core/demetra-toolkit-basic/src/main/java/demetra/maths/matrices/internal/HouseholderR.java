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
package demetra.maths.matrices.internal;

import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import demetra.maths.Constants;
import jdplus.maths.matrices.MatrixException;
import jdplus.maths.matrices.CanonicalMatrix;
import demetra.data.DoubleSeq;

/**
 * Householder transformation with partial pivoting. R-like
 *
 * @author Jean Palate
 */
public class HouseholderR {

    private double[] qrauxilary;
    private CanonicalMatrix matrix;
    private int[] pivot;
    private int nrows, ncols;
    private int rank;
    private double eps = Constants.getEpsilon();

    public HouseholderR() {
    }

    // / <summary>
    // / The method decomposes the matrix passed to it into its Q and R
    // components.
    // / The method is typicallly called when a Householder object is declared
    // / with the default constructor.
    // / </summary>
    // / <param name="m">A matrix interface pointer to a m x n matrix</param>
    /**
     *
     * @param m
     */
    public void decompose(CanonicalMatrix m) {
        init(m.deepClone());
        householder();
    }

    public int[] getPivot() {
        return pivot;
    }

    public int getRank() {
        return rank;
    }

    private void init(CanonicalMatrix m) {
        matrix = m;
        nrows = m.getRowsCount();
        ncols = m.getColumnsCount();
        pivot = new int[ncols];
        for (int i = 0; i < ncols; ++i) {
            pivot[i] = i;
        }
        qrauxilary = new double[ncols];
    }

    private void householder() {
        double[] tmp1 = new double[ncols];
        double[] tmp2 = new double[ncols];
        double[] x = matrix.getStorage();

//     compute the norms of the columns of x.
        DataBlockIterator columns = matrix.columnsIterator();
        for (int i = 0; i < ncols; ++i) {
            double nrm = columns.next().norm2();
            qrauxilary[i] = nrm;
            tmp1[i] = nrm;
            if (nrm == 0) {
                tmp2[i] = 1;
            } else {
                tmp2[i] = nrm;
            }
        }
//     perform the householder reduction of x.

        int lup = Math.min(nrows, ncols);
        // k is the end (= 1 + last valid index) of the non negligible columns
        rank = ncols;
        double eps = getPrecision();
        for (int l = 0, lq = 0; l < lup; ++l, lq += nrows + 1) {
            /*     cycle the columns from l to p left-to-right until one
             with non-negligible norm is located.  a column is considered
             to have become negligible if its norm has fallen below
             tol times its original norm.  the check for l .le. k
             avoids infinite cycling.
             */

            while (l < rank && qrauxilary[l] < tmp2[l] * eps) {
                int z = ncols - 1;
                // move all the columns after l to the left. 
                System.arraycopy(x, (l + 1) * nrows, x, l * nrows, (z - l) * nrows);
                // pivot the auxiliaries
                int ip = pivot[l];
                double aux = qrauxilary[l], t1 = tmp1[l], t2 = tmp2[l];
                for (int k = l; k < z; ++k) {
                    qrauxilary[k] = qrauxilary[k + 1];
                    tmp1[k] = tmp1[k + 1];
                    tmp2[k] = tmp2[k + 1];
                    pivot[k] = pivot[k + 1];
                }
                qrauxilary[z] = aux;
                tmp1[z] = t1;
                tmp2[z] = t2;
                pivot[z] = ip;
                --rank;
            }
            if (l == nrows - 1) {
                continue;
            }
//           compute the householder transformation for column l.
            int nl = nrows - l;
            double nrmxl = DataBlock.of(x, lq, lq + nl).norm2();
            if (nrmxl == 0) {
                continue;
            }
            double xq = x[lq];
            if (xq < 0) {
                nrmxl = -nrmxl;
            }
            // rescale the column
            for (int j = lq; j < lq + nl; ++j) {
                x[j] /= nrmxl;
            }
            x[lq] += 1;

//              apply the transformation to the remaining columns, updating the norms.
            for (int j = l + 1, jc = lq + nrows; j < ncols; ++j, jc += nrows) {
                double t = 0;
                for (int k = 0, iq = lq, jq = jc; k < nl; ++k, ++iq, ++jq) {
                    t -= x[iq] * x[jq];
                }
                t /= x[lq];
                for (int k = 0, iq = lq, jq = jc; k < nl; ++k, ++iq, ++jq) {
                    x[jq] += t * x[iq];
                }
                if (qrauxilary[j] != 0) {
                    double z = x[jc] / qrauxilary[j];
                    double tt = Math.max(0, 1 - z * z);
                    if (tt < 1e-6) {
                        qrauxilary[j] = DataBlock.of(x, jc + 1, jc + nl).norm2();
                        tmp1[j] = qrauxilary[j];
                    } else {
                        qrauxilary[j] *= Math.sqrt(tt);
                    }
                }
            }
//          save the transformation.
            qrauxilary[l] = x[lq];
            x[lq] = -nrmxl;
        }
        rank = Math.min(rank, nrows);
    }

    public double getPrecision() {
        return eps;
    }

    public void setPrecision(double eps) {
        this.eps = eps;
    }

    private void pivot(double[] external, double[] internal) {
        for (int i = 0; i < external.length; ++i) {
            internal[i] = external[pivot[i]];
        }
    }

    private void restore(double[] external, double[] internal) {
        for (int i = 0; i < external.length; ++i) {
            external[pivot[i]] = internal[i];
        }
    }

    public void applyQt(double[] b) {
        applyQt(b, rank);
    }

    public void applyQt(double[] b, int k) {
        double[] x = matrix.getStorage();

        int kmax = Math.min(k, nrows - 1);
        for (int j = 0, jj = 0; j < kmax; ++j, jj += nrows + 1) {
            if (qrauxilary[j] != 0) {
                double a = qrauxilary[j];
                double t = -a * b[j];
                for (int i = j + 1, l = jj + 1; i < nrows; ++i, ++l) {
                    t -= x[l] * b[i];
                }
                t /= a;
                b[j] += t * a;
                for (int i = j + 1, l = jj + 1; i < nrows; ++i, ++l) {
                    b[i] += t * x[l];
                }
            }
        }
    }

    public void applyQ(double[] b) {
        applyQ(b, rank);
    }

    public void applyQ(double[] xb, int k) {
        double[] b = xb.clone();
        pivot(xb, b);
        double[] x = matrix.getStorage();

        int kmax = Math.min(k, nrows - 1);
        for (int j = kmax - 1, jj = j * (nrows + 1); j >= 0; jj -= nrows + 1, --j) {
            if (qrauxilary[j] != 0) {
                double a = qrauxilary[j];
                double t = -a * b[j];
                for (int i = j + 1, l = jj + 1; i < nrows; ++i, ++l) {
                    t -= x[l] * b[i];
                }
                t /= a;
                b[j] += t * a;
                for (int i = j + 1, l = jj + 1; i < nrows; ++i, ++l) {
                    b[i] += t * x[l];
                }
            }
        }
        restore(xb, b);
    }

    /**
     *
     * @param x
     * @return
     */
    public double[] solve(double[] x) {
        if (rank != Math.min(nrows, ncols)) {
            throw new MatrixException(MatrixException.SINGULAR);
        }
        double[] b = new double[rank];
        leastSquares(DataBlock.of(x), DataBlock.of(b), null);
        return b;
    }

    public void partialLeastSquares(DoubleSeq x, DataBlock b, DataBlock res) throws MatrixException {
        double[] data = matrix.getStorage();
        double[] y = x.toArray();
        int rc = b.length();
        applyQt(y, rc);
        if (res != null) {
            res.copyFrom(y, rc);
        }
        // Solve R*X = Y;
        for (int k = rc - 1, kk = k * (nrows + 1); k >= 0; --k, kk -= nrows + 1) {
            y[k] /= data[kk];
            for (int i = 0; i < k; ++i) {
                y[i] -= y[k] * data[i + k * nrows];
            }
        }
        b.copyFrom(y, 0);
    }

    public int rank(final int nvars) {
        int rank = getRank();
        if (nvars < 0 || nvars >= pivot.length) {
            return rank;
        } else {
            int n = 0;
            for (int i = 0; i < nvars; ++i) {
                if (pivot[i] < rank) {
                    ++n;
                }
            }
            return n;
        }
    }

    public CanonicalMatrix r() {
       double[] x = matrix.getStorage();
        int rank = getRank();
        CanonicalMatrix r = CanonicalMatrix.square(rank);
        double[] data = r.getStorage();
        for (int i = 0, k = 0, l = 0; i < rank; ++i, k += rank, l += nrows) {
            for (int j = 0; j <= i; ++j) {
                data[k + j] = x[l + j];
            }
        }
        return r;
    }
    
    public int getRowsCount(){
        return nrows;
    }

    public int getColumnsCount(){
        return ncols;
    }

    public DoubleSeq rdiagonal() {
            return matrix.diagonal();
    }

    public int rank() {
        return rank;
    }

    public int[] unused() {
        int[] n = new int[ncols - rank];
        for (int i = rank, j = 0; i < ncols; ++i, ++j) {
            n[j] = pivot[i];
        }
        return n;
    }

    public void leastSquares(DoubleSeq x, DataBlock b, DataBlock res) throws MatrixException {
        double[] data = matrix.getStorage();
        double[] y = new double[x.length()];
        x.copyTo(y, 0);
        applyQt(y, rank);
        if (res != null) {
            res.copyFrom(y, rank);
        }
        double epsc = eps * 1000;
        // Solve R*X = Y;
        for (int k = rank - 1, kk = k * (nrows + 1); k >= 0; --k, kk -= nrows + 1) {
            double xkk = data[kk];
            if (Math.abs(xkk) > epsc) {
                y[k] /= xkk;
                for (int i = 0; i < k; ++i) {
                    y[i] -= y[k] * data[i + k * nrows];
                }
            } else {
                for (int i = 0; i < k; ++i) {
                    double xcur = data[i + k * nrows];
                    if (Math.abs(xcur) > epsc) {
                        throw new MatrixException(MatrixException.RANK);
                    }
                }
            }
        }
        b.copyFrom(y, 0);
    }

}
