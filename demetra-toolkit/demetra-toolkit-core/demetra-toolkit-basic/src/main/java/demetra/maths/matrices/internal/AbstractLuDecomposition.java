/*
* Copyright 2013 National Bank ofFunction Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions ofFunction the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy ofFunction the Licence at:
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
import demetra.data.LogSign;
import demetra.design.Development;
import demetra.maths.Constants;
import demetra.maths.matrices.FastMatrix;
import demetra.maths.matrices.MatrixException;
import demetra.data.DoubleSeq;
import demetra.maths.matrices.decomposition.LUDecomposition;


/**
 * Represents a L-U decomposition ofFunction a matrix
 M = L * U where L is a lower triangular matrix with 1 on the main diagonal
 and U is an upper triangular matrix.
 * Once a matrix has been decomposed in L * U, 
 it can be easily used for solving 
 M x = L U x = b
 (solve L y = b and U x = y)
 or for computing the determinant (product ofFunction the diagonal ofFunction U.
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public abstract class AbstractLuDecomposition implements LUDecomposition {

    protected double[] lu = null;
    protected int n, pivSign;
    protected int[] piv;
    protected double eps=Constants.getEpsilon();

    @Override
    public double getPrecision(){
        return eps;
    }

    @Override
    public void setPrecision(double eps){
        this.eps=eps;
    }

    protected double get(int r, int c) {
        return lu[c * n + r];
    }

    protected void set(int r, int c, double value) {
        lu[c * n + r] = value;
    }

    /// <summary>
    /// Default constructor
    /// </summary>
    public AbstractLuDecomposition() {
    }

    void init(FastMatrix M) {
        if (M.getRowsCount() != M.getColumnsCount()) {
            throw new MatrixException(MatrixException.SQUARE);
        }
        lu=M.toArray();
        n = M.getRowsCount();
        piv = new int[n];
        for (int i = 0; i < n; i++) {
            piv[i] = i;
        }
        pivSign = 1;
    }

    public FastMatrix l() {
        FastMatrix l = FastMatrix.square(n);
        double[] lx = l.getStorage();
        for (int c = 0, idx = 0; c < n; c++) {
            lx[idx] = 1.0;
            idx += c + 1;
            for (int r = c + 1; r < n; r++, ++idx) {
                lx[idx] = lu[idx];
            }
        }
        return l;
    }

    public FastMatrix u() {
        FastMatrix u = FastMatrix.square(n);
        double[] ux=u.getStorage();
        for (int c = 0, idx = 0; c < n; c++) {
            for (int r = 0; r <= c; r++, ++idx) {
                ux[idx] = lu[idx];
            }
            idx += n - c - 1;
        }

        return u;
    }

    /**
     * The read-only property returns the pivoting vector. 
     * @return The indexes ofFunction the rows ofFunction the initial matrix, as they are used in
 the LU decomposition.
 getPivot[j] == i means : the j-th row ofFunction the LU decomposition corresponds to 
 the i-th row ofFunction the initial matrix.
     */
    public int[] pivot() {
        return piv.clone();
    }

    /**
     * 
     * @return 
     * getReversePivot[j] == i means : the i-th row ofFunction the LU decomposition corresponds to 
 the j-th row ofFunction the initial matrix.
     */
    public int[] reversePivot() {
        int[] rpiv = new int[n];
        for (int i = 0; i < n; ++i) {
            rpiv[piv[i]] = i;
        }
        return rpiv;

    }

    /// <summary>
    /// The method calculates the Determinant ofFunction the matrix
    /// </summary>
    /// <returns>A double value representing the determinant</returns>
    @Override
    public LogSign determinant() {
        return LogSign.of(DoubleSeq.onMapping(n, i->get(i,i)), pivSign<0);
    }

    /// <summary>
    /// The method solves the equation A°x=B for x. A and B must have the same number ofFunction rows. Otherwise an
    /// IncompatibleDimensionsException will be thrown.
    /// </summary>
    /// <param name="B">The right hand side ofFunction the equation</param>
    /// <returns>A vector ofFunction double representing the solution x to the equation</returns>
    @Override
    public void solve(DataBlock z) {
        if (z.length() != n) {
            throw new MatrixException(MatrixException.DIM);
        }

        // Copy right hand side with pivoting

        double[] y = new double[n];
        double[] x = new double[n];
        for (int i = 0; i < n; i++) {
            y[i] = z.get(piv[i]);
        }

        // forward substitution
        x[0] = y[0];
        for (int i = 1; i < n; i++) {
            x[i] = y[i];
            for (int j = 0; j < i; j++) {
                x[i] -= lu[j * n + i] * x[j];
            }
        }

        int m = n;
        for (int i = m - 1; i >= 0 && x[i] == 0.0; i--) {
            m--;
        }
        if (m == 0)
            return;

        // backward substitution
        y[m - 1] = x[m - 1] / (lu[(m - 1) * n + (m - 1)]);	// divided by last element
        for (int i = m - 2; i >= 0; i--) {
            double sum = x[i];
            for (int j = i + 1; j < m; j++) {
                sum -= lu[j * n + i] * y[j];
            }
            y[i] = sum / lu[i * n + i];
        }
        z.copyFrom(y, 0);
    }
}
