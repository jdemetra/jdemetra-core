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
package ec.tstoolkit.maths.matrices;

import ec.tstoolkit.data.DataBlock;

/**
 * Householder transformation with partial pivoting. R-like
 *
 * @author pcuser
 */
public class HouseholderR extends AbstractLinearSystemSolver implements
        IQrDecomposition {

    private double[] qraux_;
    private Matrix m_;
    private int[] pivot_;
    private int n_, p_; // n=nrows, p=ncols
    private final boolean m_bclone;
    private int rank_;

    public HouseholderR(boolean clone) {
        m_bclone = clone;
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
    @Override
    public void decompose(Matrix m) {
        if (m_bclone) {
            init(m.clone());
        } else {
            init(m);
        }
        householder();
    }

    @Override
    public void decompose(SubMatrix m) {
        init(new Matrix(m));
        householder();
    }

    public int[] getPivot() {
        return pivot_;
    }

    public int getRank() {
        return rank_;
    }

    private void init(Matrix m) {
        m_ = m;
        n_ = m.getRowsCount();
        p_ = m.getColumnsCount();
        pivot_ = new int[p_];
        for (int i = 0; i < p_; ++i) {
            pivot_[i] = i;
        }
        qraux_ = new double[p_];
    }

    private void householder() {
        double[] tmp1 = new double[p_];
        double[] tmp2 = new double[p_];
        double[] x = m_.data_;

//     compute the norms of the columns of x.
        DataBlock col = new DataBlock(x, 0, n_, 1);
        for (int i = 0; i < p_; ++i) {
            double nrm = col.nrm2();
            qraux_[i] = nrm;
            tmp1[i] = nrm;
            if (nrm == 0) {
                tmp2[i] = 1;
            } else {
                tmp2[i] = nrm;
            }
            col.slide(n_);
        }
//     perform the householder reduction of x.

        int lup = Math.min(n_, p_);
        // k is the end (= 1 + last valid index) of the non negligible columns
        rank_ = p_;
        double eps = getEpsilon();
        for (int l = 0, lq = 0; l < lup; ++l, lq += n_ + 1) {
            /*     cycle the columns from l to p left-to-right until one
             with non-negligible norm is located.  a column is considered
             to have become negligible if its norm has fallen below
             tol times its original norm.  the check for l .le. k
             avoids infinite cycling.
             */

            while (l < rank_ && qraux_[l] < tmp2[l] * eps) {
                int z = p_ - 1;
                // move all the columns after l to the left. 
                System.arraycopy(x, (l+1)*n_, x, l*n_, (z-l)*n_);
                // pivot the auxiliaries
                int ip = pivot_[l];
                double aux = qraux_[l], t1 = tmp1[l], t2 = tmp2[l];
                for (int k = l; k < z; ++k) {
                    qraux_[k] = qraux_[k + 1];
                    tmp1[k] = tmp1[k + 1];
                    tmp2[k] = tmp2[k + 1];
                    pivot_[k] = pivot_[k + 1];
                }
                qraux_[z] = aux;
                tmp1[z] = t1;
                tmp2[z] = t2;
                pivot_[z] = ip;
                --rank_;
            }
            if (l == n_ - 1) {
                continue;
            }
//           compute the householder transformation for column l.
            int nl = n_ - l;
            double nrmxl = DataBlock.create(x, lq, nl).nrm2();
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
            for (int j = l + 1, jc = lq + n_; j < p_; ++j, jc += n_) {
                double t = 0;
                for (int k = 0, iq = lq, jq = jc; k < nl; ++k, ++iq, ++jq) {
                    t -= x[iq] * x[jq];
                }
                t /= x[lq];
                for (int k = 0, iq = lq, jq = jc; k < nl; ++k, ++iq, ++jq) {
                    x[jq] += t * x[iq];
                }
                if (qraux_[j] != 0) {
                    double z = x[jc] / qraux_[j];
                    double tt = Math.max(0, 1 - z * z);
                    if (tt < 1e-6) {
                        qraux_[j] = DataBlock.create(x, jc + 1, nl - 1).nrm2();
                        tmp1[j] = qraux_[j];
                    } else {
                        qraux_[j] *= Math.sqrt(tt);
                    }
                }
            }
//          save the transformation.
            qraux_[l] = x[lq];
            x[lq] = -nrmxl;
        }
        rank_ = Math.min(rank_, n_);
    }

    private void pivot(double[] external, double[] internal){
        for (int i=0; i<external.length; ++i){
            internal[i]=external[pivot_[i]];
        }
    }
    
    private void restore(double[] external, double[] internal){
        for (int i=0; i<external.length; ++i){
            external[pivot_[i]]=internal[i];
        }
    }
    
   public void applyQt(double[] b) {
        applyQt(b, rank_);
    }
 
    public void applyQt(double[] b, int k) {
        double[] x = m_.data_;

        int kmax = Math.min(k, n_ - 1);
        for (int j = 0, jj = 0; j < kmax; ++j, jj += n_ + 1) {
            if (qraux_[j] != 0) {
                double a = qraux_[j];
                double t = -a * b[j];
                for (int i = j + 1, l = jj + 1; i < n_; ++i, ++l) {
                    t -= x[l] * b[i];
                }
                t /= a;
                b[j] += t * a;
                for (int i = j + 1, l = jj + 1; i < n_; ++i, ++l) {
                    b[i] += t * x[l];
                }
            }
        }
    }

    public void applyQ(double[] b) {
        applyQ(b, rank_);
    }

    public void applyQ(double[] xb, int k) {
        double[] b=xb.clone();
        pivot(xb, b);
        double[] x = m_.data_;

        int kmax = Math.min(k, n_ - 1);
        for (int j = kmax - 1, jj = j * (n_ + 1); j >= 0; jj -= n_ + 1, --j) {
            if (qraux_[j] != 0) {
                double a = qraux_[j];
                double t = -a * b[j];
                for (int i = j + 1, l = jj + 1; i < n_; ++i, ++l) {
                    t -= x[l] * b[i];
                }
                t /= a;
                b[j] += t * a;
                for (int i = j + 1, l = jj + 1; i < n_; ++i, ++l) {
                    b[i] += t * x[l];
                }
            }
        }
        restore(xb, b);
    }

    @Override
    public void solve(DataBlock xin, DataBlock xout) throws MatrixException {
        leastSquares(xin, xout, null);
    }

    /**
     *
     * @param x
     * @return
     */
    @Override
    public double[] solve(double[] x) {
        int rank = getRank();
        if (rank != Math.min(n_, p_)) {
            throw new MatrixException(MatrixException.Singular);
        }
        double[] b = new double[rank];
        leastSquares(new DataBlock(x), new DataBlock(b), null);
        return b;
    }

    @Override
    public int getEquationsCount() {
        return n_;
    }

    @Override
    public int getUnknownsCount() {
        return p_;
    }

    @Override
    public boolean isFullRank() {
        return rank_ == Math.min(n_, p_);
    }

    @Override
    public Matrix getR() {
        double[] x = m_.data_;
        int rank = getRank();
        Matrix r = new Matrix(rank, rank);
        double[] data = r.data_;
        for (int i = 0, k = 0, l = 0; i < rank; ++i, k += rank, l += n_) {
            for (int j = 0; j <= i; ++j) {
                data[k + j] = x[l + j];
            }
        }
        return r;
    }

    @Override
    public DataBlock getRDiagonal() {
        return DataBlock.create(m_.data_, 0, Math.min(n_, p_), n_ + 1);
    }

    @Override
    public void leastSquares(DataBlock x, DataBlock b, DataBlock res) throws MatrixException {
        double[] x_ = m_.data_;
        double[] y = new double[x.getLength()];
        x.copyTo(y, 0);
        applyQt(y, rank_);
        if (res != null) {
            res.copyFrom(y, rank_);
        }
        // Solve R*X = Y;
        for (int k = rank_ - 1, kk = k * (n_ + 1); k >= 0; --k, kk -= n_ + 1) {
            y[k] /= x_[kk];
            for (int i = 0; i < k; ++i) {
                y[i] -= y[k] * x_[i + k * n_];
            }
        }
        b.copyFrom(y, 0);
    }

    public void partialLeastSquares(DataBlock x, DataBlock b, DataBlock res) throws MatrixException {
        double[] x_ = m_.data_;
        double[] y = new double[x.getLength()];
        x.copyTo(y, 0);
        int rc = b.getLength();
        applyQt(y, rc);
        if (res != null) {
            res.copyFrom(y, rc);
        }
        // Solve R*X = Y;
        for (int k = rc - 1, kk = k * (n_ + 1); k >= 0; --k, kk -= n_ + 1) {
            y[k] /= x_[kk];
            for (int i = 0; i < k; ++i) {
                y[i] -= y[k] * x_[i + k * n_];
            }
        }
        b.copyFrom(y, 0);
    }

    public int rank(final int nvars) {
        int rank = getRank();
        if (nvars < 0 || nvars >= pivot_.length) {
            return rank;
        } else {
            int n = 0;
            for (int i = 0; i < nvars; ++i) {
                if (pivot_[i] < rank) {
                    ++n;
                }
            }
            return n;
        }
    }

    public int[] getUnused() {
        int[] n = new int[p_ - rank_];
        for (int i = rank_, j = 0; i < p_; ++i, ++j) {
            n[j] = pivot_[i];
        }
        return n;
    }
}
