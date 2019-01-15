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
import ec.tstoolkit.data.IDataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class Householder extends AbstractLinearSystemSolver implements
        IQrDecomposition {

    private double[] m_qr, m_rdiag;

    private int[] m_unused;

    private int m_norig, m_n, m_m; // m_m=nrows, m_n=ncols

    private boolean m_bclone;

    // / <summary>
    // / default constructor
    // / </summary>
    /**
     *
     * @param clone
     */
    public Householder(boolean clone) {
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

    // / <summary>
    // / The method decomposes the matrix passed to it into its Q and R
    // components.
    // / The method is typicallly called when a Householder object is declared
    // / with the default constructor.
    // / </summary>
    // / <param name="m">A matrix interface pointer to a m x n matrix</param>
    @Override
    public void decompose(SubMatrix m) {
        init(new Matrix(m));
        householder();
    }

    @Override
    public int getEquationsCount() {
        return m_m;
    }

    // / <summary>
    // / The read-only property returns the component R - an uppertriangular
    // / matrix of dimensions n x n. R is the result of the successive
    // Householder
    // / transformations on the original matrix.
    // / </summary>
    @Override
    public Matrix getR() {
        Matrix r = new Matrix(m_n, m_n);
        double[] data = r.data_;
        for (int i = 0, k = 0, l = 0; i < m_n; ++i, k += m_n, l += m_m) {
            for (int j = 0; j < i; ++j) {
                data[k + j] = m_qr[l + j];
            }
            data[k + i] = m_rdiag[i];
        }
        return r;
    }

    /**
     *
     * @return
     */
    public int getRank() {
        return m_n;
    }

    @Override
    public DataBlock getRDiagonal() {
        return new DataBlock(m_rdiag);
    }

    @Override
    public int getUnknownsCount() {
        return m_norig;
    }

    /**
     *
     * @return
     */
    public int[] getUnused() {
        return m_unused;
    }

    private void householder() {
        int[] unused = new int[m_norig];
        int nunused = 0, nrdiag = 0;
        // Main loop.
        double eps = getEpsilon();
        int len = m_qr.length;
        for (int l = 0, k = 0, km = 0; k < m_n; ++k) {
            // Compute 2-norm of k-th column .
            DataBlock col = new DataBlock(m_qr, km + l, km + m_m, 1);
            double nrm = col.nrm2();

            if (nrm > eps) {
                // Form k-th Householder vector. v(k)=x(k)+/-norm(x)
                if (m_qr[l + km] < -eps) {
                    nrm = -nrm;
                }
                for (int i = l; i < m_m; ++i) {
                    m_qr[i + km] /= nrm;
                }
                m_qr[l + km] += 1.0;
                // rdiag contains the main diagonal of the R matrix
                m_rdiag[nrdiag++] = -nrm;
		// in this implementation:
                // if a(k,k) < 0 then a(k,k) = -(a(k,k) - nrm) / nrm, else
                // a(k,k)=( a(k,k) + nrm) / nrm

                // Apply transformation to remaining columns.
                for (int jm = km + m_m; jm < len; jm += m_m) {
                    double s = 0.0;
                    for (int i = l; i < m_m; ++i) {
                        s += m_qr[i + km] * m_qr[i + jm];
                    }
                    s /= -m_qr[l + km];
                    for (int i = l; i < m_m; ++i) {
                        m_qr[i + jm] += s * m_qr[i + km];
                    }
                }
                km += m_m;
                ++l;
            } else {
                unused[nunused++] = k;
                // move all the data to the left
                len -= m_m;
                System.arraycopy(m_qr, km + m_m, m_qr, km, len - km);
            }
        }

        if (nunused > 0) {
            m_unused = new int[nunused];
            System.arraycopy(unused, 0, m_unused, 0, nunused);
            // shift the columns to the left 
            for (int i = 0; i < m_unused.length; ++i) {
                int j = m_unused[i];

            }
            m_n -= nunused;
        } else {
            m_unused = null;
        }
    }

    private void init(Matrix m) {
        m_m = m.getRowsCount();
        m_norig = m_n = m.getColumnsCount();
        // if (m_m < m_n)
        // throw new MatrixException(MatrixException.IncompatibleDimensions);
        m_qr = m.data_;
        m_rdiag = new double[m_n];
    }

    // / <summary>
    // / The read-only property checks whether the matrix to which the
    // Householder is
    // / applied is of full rank. I.e. whether no column is a linear combination
    // of the
    // / other columns.
    // / </summary>
    @Override
    public boolean isFullRank() {
        return m_n == m_norig;
    }

    // / <summary>
    // / The method solves a set of simultaneous linear equations A.b = x using
    // / the QR decomposition of the matrix A.
    // / </summary>
    // / <param name="x">In parameter.An array of double. The right hand side of
    // the equation</param>
    // / <returns>The solution b as an array of double</returns>
    // /
    @Override
    public void leastSquares(IReadDataBlock x, IDataBlock b, IDataBlock res) {
	// if (x.Length != m_m)
        // throw new MatrixException(MatrixException.IncompatibleDimensions);
        // if (!IsFullRank)
        // throw new MatrixException(MatrixException.RankError);

        // Compute Y = transpose(Q)*B
        // copy b
        double[] y = new double[x.getLength()];
        x.copyTo(y, 0);
        applyQt(y);
        if (res != null) {
            res.copyFrom(y, m_n);
        }
        // Solve R*X = Y;
        double eps = getEpsilon() * 1000;
        for (int k = m_n - 1; k >= 0; --k) {
            double dk = m_rdiag[k];
            if (Math.abs(dk) > eps) {
                y[k] /= dk;
                for (int i = 0; i < k; ++i) {
                    y[i] -= y[k] * m_qr[i + k * m_m];
                }
            } else {
                for (int i = 0; i < k; ++i) {
                    double xcur = m_qr[i + k * m_m];
                    if (Math.abs(xcur) > eps) {
                        throw new MatrixException(MatrixException.RankError);
                    }
                }
            }
            // Solve R*X = Y;
        }
        b.copyFrom(y, 0);
    }

    /**
     * The method multiplies an array of double by Q.
     *
     * @param b The array of double. It contains the product at the // return of
     * the method
     */
    public void applyQ(double[] b) {
        for (int k = m_n - 1; k >= 0; --k) {
            double s = 0.0;
            for (int i = k; i < m_m; ++i) {
                s += m_qr[k * m_m + i] * b[i];
            }
            s = -s / m_qr[k * m_m + k];
            for (int i = k; i < m_m; ++i) {
                b[i] += s * m_qr[k * m_m + i];
            }
        }
    }

    // / <summary>
    // / The method multiplyes an array of double by the transpose of Q (also
    // equal to its inverse).
    // / </summary>
    // / <param name="b">An array of double. It contains the product at the
    // return of the method</param>
    /**
     *
     * @param b
     */
    public void applyQt(double[] b) {
        for (int k = 0, km = 0; k < m_n; k++, km += m_m) {
            double s = 0.0;
            for (int i = k; i < m_m; ++i) {
                s += m_qr[km + i] * b[i];
            }
            if (s != 0) {
                s = -s / m_qr[km + k];
                for (int i = k; i < m_m; ++i) {
                    b[i] += s * m_qr[km + i];
                }
            }
        }
    }

    /**
     *
     * @param xin
     * @param xout
     */
    @Override
    public void solve(DataBlock xin, DataBlock xout) {
        leastSquares(xin, xout, null);
    }

    /**
     *
     * @param x
     * @return
     */
    @Override
    public double[] solve(double[] x) {
        if (m_norig != m_n) {
            throw new MatrixException(MatrixException.Singular);
        }
        double[] b = new double[m_n];
        leastSquares(new DataBlock(x), new DataBlock(b), null);
        return b;
    }
}
