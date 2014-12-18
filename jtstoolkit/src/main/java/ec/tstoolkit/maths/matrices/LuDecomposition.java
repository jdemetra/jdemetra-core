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
import ec.tstoolkit.design.Development;

/**
 * Represents a L-U decomposition of a matrix
 * M = L * U where L is a lower triangular matrix with 1 on the main diagonal
 * and U is an upper triangular matrix.
 * Once a matrix has been decomposed in L * U, 
 * it can be easily used for solving 
 * M x = L U x = b
 * (solve L y = b and U x = y)
 * or for computing the determinant (product of the diagonal of U.
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public abstract class LuDecomposition extends AbstractLinearSystemSolver implements ILuDecomposition {

    protected double[] lu_ = null;
    protected int n_, pivsign_;
    protected int[] piv_;

    protected double get(int r, int c) {
        return lu_[c * n_ + r];
    }

    protected void set(int r, int c, double value) {
        lu_[c * n_ + r] = value;
    }

    /// <summary>
    /// Default constructor
    /// </summary>
    public LuDecomposition() {
    }

    /// <summary>
    /// This private method prepares all data strutures for decomposition and initializes several data.
    /// </summary>
    /// <param name="A"></param>
    void init(Matrix m, boolean clone) {
        if (m.getRowsCount() != m.getColumnsCount()) {
            throw new MatrixException("LU Decomposition: not squared matrix");
        }
        if (clone) {
            lu_ = m.data_.clone();
        } else {
            lu_ = m.data_;
        }
        n_ = m.getRowsCount();
        piv_ = new int[n_];
        for (int i = 0; i < n_; i++) {
            piv_[i] = i;
        }
        pivsign_ = 1;
    }

    /// <summary>
    /// The read-only property indicates whether the matrix is NonSingular.
    /// </summary>
    @Override
    public boolean isFullRank() {
        for (int j = 0; j < n_; j++) {
            if (Math.abs(get(j, j)) < getEpsilon()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getEquationsCount() {
        return n_;
    }

    @Override
    public int getUnknownsCount() {
        return n_;
    }

    /// <summary>
    /// The read-only property returns the L-component of the decomposition. The elements on the
    /// main diagonal are set to 1.0.
    /// </summary>
    @Override
    public Matrix getL() {
        Matrix l = new Matrix(n_, n_);
        for (int c = 0, idx = 0; c < n_; c++) {
            l.data_[idx] = 1.0;
            idx += c + 1;
            for (int r = c + 1; r < n_; r++, ++idx) {
                l.data_[idx] = lu_[idx];
            }
        }
        return l;
    }

    /// <summary>
    /// The read-only property returns the U-component of the decomposition. 
    /// </summary>
    @Override
    public Matrix getU() {
        Matrix u = new Matrix(n_, n_);
        for (int c = 0, idx = 0; c < n_; c++) {
            for (int r = 0; r <= c; r++, ++idx) {
                u.data_[idx] = lu_[idx];
            }
            idx += n_ - c - 1;
        }

        return u;
    }

    /**
     * The read-only property returns the pivoting vector. 
     * @return The indexes of the rows of the initial matrix, as they are used in
     * the LU decomposition.
     * getPivot[j] == i means : the j-th row of the LU decomposition corresponds to 
     * the i-th row of the initial matrix.
     */
    public int[] getPivot() {
        return piv_.clone();
    }

    /**
     * 
     * @return 
     * getReversePivot[j] == i means : the i-th row of the LU decomposition corresponds to 
     * the j-th row of the initial matrix.
     */
    public int[] getReversePivot() {
        int[] rpiv = new int[n_];
        for (int i = 0; i < n_; ++i) {
            rpiv[piv_[i]] = i;
        }
        return rpiv;

    }

    /// <summary>
    /// The method calculates the Determinant of the matrix
    /// </summary>
    /// <returns>A double value representing the determinant</returns>
    public double getDeterminant() {
        double d = (double) pivsign_;
        for (int j = 0; j < n_; j++) {
            d *= get(j, j);
        }
        return d;
    }

    /// <summary>
    /// The method solves the equation A°x=B for x. A and B must have the same number of rows. Otherwise an
    /// IncompatibleDimensionsException will be thrown.
    /// </summary>
    /// <param name="B">The right hand side of the equation</param>
    /// <returns>A vector of double representing the solution x to the equation</returns>
    @Override
    public void solve(DataBlock xin, DataBlock xout) {
        if (xin.getLength() != n_) {
            throw new MatrixException("Incompatible dimensions");
        }
        if (!isFullRank()) {
            throw new MatrixException("LU decomposition: singular matrix");
        }

        // Copy right hand side with pivoting

        double[] y = new double[n_];
        double[] x = new double[n_];
        for (int i = 0; i < n_; i++) {
            y[i] = xin.get(piv_[i]);
        }

        // forward substitution
        x[0] = y[0];
        for (int i = 1; i < n_; i++) {
            x[i] = y[i];
            for (int j = 0; j < i; j++) {
                x[i] -= lu_[j * n_ + i] * x[j];
            }
        }

        int n = n_;
        for (int i = n - 1; i >= 0 && x[i] == 0.0; i--) {
            n--;
        }
        if (n == 0)
            return;

        // backward substitution
        y[n - 1] = x[n - 1] / (lu_[(n - 1) * n_ + (n - 1)]);	// divided by last element
        for (int i = n - 2; i >= 0; i--) {
            double sum = x[i];
            for (int j = i + 1; j < n; j++) {
                sum -= lu_[j * n_ + i] * y[j];
            }
            y[i] = sum / lu_[i * n_ + i];
        }
        xout.copyFrom(y, 0);
    }
    
}
