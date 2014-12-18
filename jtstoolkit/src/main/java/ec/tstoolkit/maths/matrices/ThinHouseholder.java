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
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class ThinHouseholder extends AbstractLinearSystemSolver implements IQrDecomposition {


    /// <summary>
    /// default constructor
    /// </summary>
    public ThinHouseholder() {
    }

    /// <summary>
    /// The method decomposes the matrix passed to it into its Q and R components.
    /// The method is typicallly called when a Householder object is declared 
    /// with the default constructor. The parameter is set to null before returning.
    /// </summary>
    /// <param name="m">A reference to an Matrix</param>
    @Override
    public void decompose(Matrix m) {
        init(m, true);
        householder();
    }

    @Override
    public void decompose(SubMatrix m) {
        init(new Matrix(m), false);
        householder();
    }

    /// <summary>
    /// The read-only property checks whether the matrix to which the Householder is 
    /// applied is of full rank. I.e. whether no column is a linear combination of the 
    /// other columns.
    /// </summary>
    @Override
    public boolean isFullRank() {
        if (m_beta == null) {
            return false;
        }
        for (int j = 0; j < m_n; j++) {
            if (Math.abs(m_beta[j]) <= getEpsilon()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getEquationsCount() {
        return m_m;
    }

    @Override
    public int getUnknownsCount() {
        return m_n;
    }

    /// <summary>
    /// The read-only property returns the component R - an uppertriangular
    /// matrix of dimensions m x n. R is the result of the successive Householder
    /// transformations on the original matrix.
    /// </summary>
    @Override
    public Matrix getR() {
        Matrix r = new Matrix(m_n, m_n);
        double[] data = r.internalStorage();
        for (int i = 0, k = 0, l = 0; i < m_n; ++i, l += m_m, k += m_n) {
            for (int j = 0; j <= i; ++j) {
                data[k + j] = m_qr[l + j];
            }
        }
        return r;
    }

    @Override
    public DataBlock getRDiagonal() {
        int inc = m_m + 1;
        return new DataBlock(m_qr, 0, inc * m_n, inc);
    }

    /// <summary>
    /// The method solves a set of simultaneous linear equations A.x = b using
    /// the QR decomposition of the matrix A.  It also returns extra diagnostic 
    /// information.
    /// </summary>
    /// <param name="b">In parameter.An array of double. The right hand side of the equation</param>
    /// <param name="se2">Out parameter. The sum of squares of the elements in x - the solution</param>
    /// <param name="err">Out parameter. The error of the solution (mutliplied by Q).</param>
    /// <returns>The solution x as an array of double</returns>
    @Override
    public void leastSquares(DataBlock x, DataBlock b, DataBlock res) {
        if (x.getLength() != m_m) {
            throw new MatrixException("Incompatible dimensions");
        }
        if (!isFullRank()) {
            throw new MatrixException("Matrix is rank deficient.");
        }

        double[] y = new double[x.getLength()];
        x.copyTo(y, 0);
        QtB(y);
        if (res != null) {
            res.copyFrom(y, m_n);
        }

        // Solve R*X = Y;
        for (int k = m_n - 1, km = k * m_m; k >= 0; --k, km -= m_m) {
            y[k] /= m_qr[k + km];
            for (int i = 0; i < k; ++i) {
                y[i] -= y[k] * m_qr[i + km];
            }
        }
        b.copyFrom(y, 0);
    }

    /// <summary>
    /// The method solves a set of simultaneous linear equations A.x = b using
    /// the QR decomposition of the matrix A.  
    /// </summary>
    /// <param name="b">In parameter.An array of double. The right hand side of the equation</param>
    /// <returns>The solution x as an array of double</returns>
    @Override
    public void solve(DataBlock xin, DataBlock xout) {
        leastSquares(xin, xout, null);
    }

    private void init(Matrix m, boolean clone) {
        m_m = m.getRowsCount();
        m_n = m.getColumnsCount();
        if (m_m < m_n) {
            throw new MatrixException("Incompatible dimensions");
        }
        if (clone) {
            m_qr = m.data_.clone();
        } else {
            m_qr = m.data_;
        }
        m_beta = new double[m_n];
    }

    private void householder() {
        for (int k = 0, km = 0; k < m_n; ++k, km += m_m) {
            // cfr Golub - Van Loan. We compute the householder vector
            double sigma = 0;
            double x1 = m_qr[k + km];
            for (int i = k + 1; i < m_m; ++i) {
                double tmp = m_qr[i + km];
                sigma += tmp * tmp;
            }

            if (sigma > getEpsilon())
            {
                double nrm = Math.sqrt(sigma + x1 * x1);
                double v1 = x1 <= 0 ? x1 - nrm : -sigma / (x1 + nrm);
                m_beta[k] = 2 * v1 * v1 / (sigma + v1 * v1);

                for (int i = k + 1; i < m_m; ++i) {
                    m_qr[i + km] /= v1;
                }
                //m_qr[k+m_m*k]*=(1-m_beta[k]*();
                m_qr[k + km] = nrm;
                // Apply transformation to remaining columns.
                for (int j = k + 1, jm = (k + 1) * m_m; j < m_n; ++j, jm += m_m) {
                    double s = m_qr[k + jm];
                    for (int i = k + 1; i < m_m; ++i) {
                        s += m_qr[i + km] * m_qr[i + jm];
                    }
                    s *= m_beta[k];
                    m_qr[k + jm] -= s;
                    for (int i = k + 1; i < m_m; ++i) {
                        m_qr[i + jm] -= s * m_qr[i + km];
                    }
                }
            }
            else if ( k == m_m - 1) // squared matrix)
            {
                double nrm = -x1;
                m_beta[k] = 2;
                m_qr[k + km] = nrm;
            }
        }
    }

    /// <summary>
    /// The method multiplyes an array of double by the transpose of Q (also equal to its inverse).
    /// </summary>
    /// <param name="b">An array of double. It contains the product at the return of the method</param>
    public void QtB(double[] b) {
        for (int k = 0, km = 0; k < m_n; k++, km += m_m) {
            // q[0] = 1 !
            double s = b[k];
            for (int i = k + 1; i < m_m; ++i) {
                s += m_qr[km + i] * b[i];
            }
            s *= m_beta[k];
            b[k] -= s;
            for (int i = k + 1; i < m_m; ++i) {
                b[i] -= s * m_qr[km + i];
            }
        }
    }

    /// <summary>
    /// The method multiplyes an array of double by Q.
    /// </summary>
    /// <param name="b">An array of double. It contains the product at the return of the method</param>
    public void QB(double[] b) {
        for (int k = m_n - 1; k >= 0; k--) {
            // q[0] = 1 !
            double s = b[k];
            for (int i = k + 1; i < m_m; ++i) {
                s += m_qr[k * m_m + i] * b[i];
            }
            s *= m_beta[k];
            b[k] -= s;
            for (int i = k + 1; i < m_m; ++i) {
                b[i] -= s * m_qr[k * m_m + i];
            }
        }
    }
    private double[] m_qr = null, m_beta = null;
    private int m_n = 0, m_m = 0;	// m_m=nrows, m_n=ncols
}
