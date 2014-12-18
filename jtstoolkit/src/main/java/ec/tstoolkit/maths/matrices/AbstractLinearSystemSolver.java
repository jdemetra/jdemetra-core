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

import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public abstract class AbstractLinearSystemSolver implements ILinearSystemSolver
{

    private double m_eps = Math.pow(2.0, -52.0);

    /**
     * 
     * @param m
     */
    public abstract void decompose(SubMatrix m);

    /**
     * 
     * @return
     */
    public double getEpsilon() {
	return m_eps;
    }

    /**
     * 
     * @return
     * @throws MatrixException
     */
    public Matrix inverse() throws MatrixException {
	return solve(Matrix.identity(getEquationsCount()));
    }

    /**
     * 
     * @param value
     */
    public void setEpsilon(final double value) {
	m_eps = value;
    }
    
    public double[] solve(double[] in){
        DataBlock rslt=new DataBlock(in.length);
        solve(new DataBlock(in), rslt);
        return rslt.getData();
    }

    /**
     *
     * @param xin
     * @param xout
     * @throws MatrixException
     */
    public abstract void solve(DataBlock xin, DataBlock xout)
	    throws MatrixException;

    /**
     * Solves the system A*X=B. X=Inv(A)*B, if A is square
     * 
     * @param m
     * @return
     * @throws MatrixException
     */
    @Override
    public Matrix solve(final Matrix m) throws MatrixException {
	if (m == null)
	    return null;
	int n = getEquationsCount();
	// if (n != UnknownsCount)
	// throw new MatrixException(MatrixException.SquareOnly);
	if (!isFullRank())
	    throw new MatrixException(MatrixException.RankError);
	// if (n != m.RowsCount)
	// throw new MatrixException(MatrixException.IncompatibleDimensions);
	int nc = m.getColumnsCount();

	Matrix S = new Matrix(n, nc);

	DataBlockIterator xin = m.columns();
	DataBlockIterator xout = S.columns();

	DataBlock icur = xin.getData();
	DataBlock ocur = xout.getData();
	do
	    solve(icur, ocur);
	while (xin.next() && xout.next());

	return S;
    }

}
