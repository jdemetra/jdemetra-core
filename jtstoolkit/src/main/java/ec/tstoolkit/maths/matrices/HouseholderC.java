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
@Development(status = Development.Status.Exploratory)
public class HouseholderC extends AbstractLinearSystemSolver implements
	IQrDecomposition {

    private double[] m_qr, m_rdiag;

    private int[] m_unused;

    private int m_norig, m_n, m_m; // m_m=nrows, m_n=ncols

    private boolean m_bclone;

    /**
     * 
     * @param clone
     */
    public HouseholderC(boolean clone)
    {
	m_bclone = clone;
    }

    @Override
    public void decompose(Matrix m) {
	if (m_bclone)
	    init(m.clone());
	else
	    init(m);
	householder();
    }

    @Override
    public void decompose(SubMatrix m) {
	init(new Matrix(m));
	householder();
    }

    @Override
    public int getEquationsCount() {
	return m_m;
    }

    @Override
    public Matrix getR() {
	Matrix r = new Matrix(m_n, m_n);
	double[] data = r.data_;
	for (int i = 0, k = 0, l = 0; i < m_n; ++i, k += m_n, l += m_m)
	    for (int j = 0; j <= i; ++j)
		data[k + j] = m_qr[l + j];
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
	double[] work = new double[m_m];
	m_rdiag = new double[m_n];
	int info = ec.tstoolkit.maths.matrices.lapack.Dgeqr2.fn(m_m, m_n, m_qr,
		0, m_m, m_rdiag, work);

    }

    private void init(Matrix m) {
	m_m = m.getRowsCount();
	m_norig = m_n = m.getColumnsCount();
	// if (m_m < m_n)
	// throw new MatrixException(MatrixException.IncompatibleDimensions);
	m_qr = m.data_;
	m_rdiag = new double[m_n];
    }

    @Override
    public boolean isFullRank() {
	return m_n == m_norig;
    }

    @Override
    public void leastSquares(IReadDataBlock x, IDataBlock b, IDataBlock res)
	    throws MatrixException {
	double[] y = new double[x.getLength()];
	x.copyTo(y, 0);
	QtB(y);
	if (res != null)
	    res.copyFrom(y, m_n);
	// Solve R*X = Y;
	for (int k = m_n - 1; k >= 0; --k) {
	    y[k] /= m_qr[k * m_m + k];
	    for (int i = 0; i < k; ++i)
		y[i] -= y[k] * m_qr[i + k * m_m];
	}
	b.copyFrom(y, 0);
    }

    // / <summary>
    // / The method multiplyes an array of double by Q.
    // / </summary>
    // / <param name="b">An array of double. It contains the product at the
    // return of the method</param>
    /**
     * 
     * @param b
     */
    public void QB(double[] b) {
	for (int k = m_n - 1; k >= 0; --k) {
	    double s = 0.0;
	    for (int i = k; i < m_m; ++i)
		s += m_qr[k * m_m + i] * b[i];
	    s = -s / m_qr[k * m_m + k];
	    for (int i = k; i < m_m; ++i)
		b[i] += s * m_qr[k * m_m + i];
	}
    }

    /**
     * 
     * @param b
     */
    public void QtB(double[] b)
    {
	// for (int k = 0; k < m_n; ++k)
	// {
	// Dlarf.fn(SIDE.Right, 1, m_m, m_qr, k*(1+m_m), 1, m_rdiag[k], b, 0, 1,
	// new double[m_n]);
	// }
	for (int k = 0, km = 0; k < m_n; k++, km += m_m) {
	    // H = I - tau v v'
	    // H b = b - tau v * v' * b
	    double s = b[k];
	    for (int i = k + 1; i < m_m; ++i)
		s += m_qr[km + i] * b[i];
	    s = -s * m_rdiag[k];
	    b[k] += s;
	    for (int i = k + 1; i < m_m; ++i)
		b[i] += s * m_qr[km + i];
	}
    }

    /**
     *
     * @param xin
     * @param xout
     * @throws MatrixException
     */
    @Override
    public void solve(DataBlock xin, DataBlock xout) throws MatrixException {
	leastSquares(xin, xout, null);
    }

    /**
     * 
     * @param x
     * @return
     */
    public double[] solve(double[] x)
    {
	if (m_norig != m_n)
	    throw new MatrixException(MatrixException.Singular);
	double[] b = new double[m_n];
	leastSquares(new DataBlock(x), new DataBlock(b), null);
	return b;
    }
}
