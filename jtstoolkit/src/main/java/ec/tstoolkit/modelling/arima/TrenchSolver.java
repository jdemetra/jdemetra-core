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

package ec.tstoolkit.modelling.arima;

import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.RegModel;
import ec.tstoolkit.maths.matrices.LowerTriangularMatrix;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.Toeplitz;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class TrenchSolver implements Cloneable
{

    /**
     *
     */
    /**
     *
     */
    public DataBlock w, z;

    /**
     *
     */
    /**
     *
     */
    /**
     *
     */
    public Matrix A, B, lA;

    /**
     *
     */
    public Matrix S;

    private double[] l;

    /**
     * 
     * @param <T>
     * @param regArimaModel
     */
    public <T extends IArimaModel> TrenchSolver(RegArimaModel<T> regArimaModel)
    {
	RegModel dmodel = regArimaModel.getDModel();
	DataBlock dy = dmodel.getY();
	int nobs = dy.getLength();
	double[] r = regArimaModel.getArma().getAutoCovarianceFunction().values(nobs);
	Toeplitz toeplitz = new Toeplitz(r);
	S = toeplitz.inverse();
	z = new DataBlock(nobs);
	z.product(dy, S.columns());

	int nx = 0;
	Matrix X = dmodel.variables();
	for (int i = 0; i < dmodel.getVarsCount(); ++i)
	    if (!X.column(i).isZero())
		++nx;

	if (nx == 0) {
	    A = null;
	    lA = null;
	    B = null;
	    w = null;
	    l = null;
	} else {
	    l = new double[nx];
	    w = new DataBlock(nx);
	    B = new Matrix(nx, nobs);
	    A = new Matrix(nx, nx);
	    for (int j = 0, i = 0; j < dmodel.getVarsCount(); ++j) {
		DataBlock xcol = X.column(j);
		if (!xcol.isZero()) {

		    B.row(i).product(xcol, S.columns());
		    w.set(i, xcol.dot(new DataBlock(z)));
		    for (int k = 0; k <= i; ++k)
			A.set(i, k, xcol.dot(B.row(k)));
		    ++i;
		}
	    }
	    SymmetricMatrix.fromLower(A);
	    lA = A.clone();
	    SymmetricMatrix.lcholesky(lA, 1e-15);
	}
    }

    @Override
    public TrenchSolver clone() {
	try {
	    TrenchSolver solver = (TrenchSolver) super.clone();
	    solver.z = z.clone();

	    if (B != null) {
		solver.B = B.clone();
		solver.l = new double[B.getRowsCount()];
	    }
	    solver.S = S.clone();
	    return solver;
	} catch (CloneNotSupportedException err) {
	    throw new AssertionError();
	}

    }

    /**
     * 
     */
    public void cumul()
    {
	z.reverse().difference();
	if (B != null) {
	    DataBlockIterator brows = B.rows();
	    DataBlock brow = brows.getData();
	    do
		brow.reverse().difference();
	    while (brows.next());
	}

	DataBlockIterator scols = S.columns();
	DataBlock scol = scols.getData();
	do
	    scol.reverse().difference();
	while (scols.next());
	DataBlockIterator srows = S.rows();
	DataBlock srow = srows.getData();
	do
	    srow.reverse().difference();
	while (srows.next());
    }

    /**
     * 
     * @param delta
     */
    public void cumul(double delta)
    {
	z.reverse().difference(delta);
	if (B != null) {
	    DataBlockIterator brows = B.rows();
	    DataBlock brow = brows.getData();
	    do
		brow.reverse().difference(delta);
	    while (brows.next());
	}

	DataBlockIterator scols = S.columns();
	DataBlock scol = scols.getData();
	do
	    scol.reverse().difference(delta);
	while (scols.next());
	DataBlockIterator srows = S.rows();
	DataBlock srow = srows.getData();
	do
	    srow.reverse().difference(delta);
	while (srows.next());
    }

    /**
     * 
     * @param delta
     * @param lag
     */
    public void cumul(double delta, int lag)
    {
	z.reverse().difference(delta, lag);
	if (B != null) {
	    DataBlockIterator brows = B.rows();
	    DataBlock brow = brows.getData();
	    do
		brow.reverse().difference(delta, lag);
	    while (brows.next());
	}

	DataBlockIterator scols = S.columns();
	DataBlock scol = scols.getData();
	do
	    scol.reverse().difference(delta, lag);
	while (scols.next());
	DataBlockIterator srows = S.rows();
	DataBlock srow = srows.getData();
	do
	    srow.reverse().difference(delta, lag);
	while (srows.next());
    }

    /**
     * 
     * @param lag
     */
    public void cumul(int lag)
    {
	z.reverse().difference(lag);
	if (B != null) {
	    DataBlockIterator brows = B.rows();
	    DataBlock brow = brows.getData();
	    do
		brow.reverse().difference(lag);
	    while (brows.next());
	}

	DataBlockIterator scols = S.columns();
	DataBlock scol = scols.getData();
	do
	    scol.reverse().difference(lag);
	while (scols.next());
	DataBlockIterator srows = S.rows();
	DataBlock srow = srows.getData();
	do
	    srow.reverse().difference(lag);
	while (srows.next());
    }

    /**
     * 
     */
    public void difference()
    {
	z.reverse().cumul();
	if (B != null) {
	    DataBlockIterator brows = B.rows();
	    DataBlock brow = brows.getData();
	    do
		brow.reverse().cumul();
	    while (brows.next());
	}

	SymmetricMatrix.rcumul(S);
    }

    /**
     * 
     * @param delta
     */
    public void difference(double delta)
    {
	z.reverse().cumul(delta);
	if (B != null) {
	    DataBlockIterator brows = B.rows();
	    DataBlock brow = brows.getData();
	    do
		brow.reverse().cumul(delta);
	    while (brows.next());
	}

	SymmetricMatrix.rcumul(S, delta);
    }

    /**
     * 
     * @param delta
     * @param lag
     */
    public void difference(double delta, int lag)
    {
	z.reverse().cumul(delta, lag);
	if (B != null) {
	    DataBlockIterator brows = B.rows();
	    DataBlock brow = brows.getData();
	    do
		brow.reverse().cumul(delta, lag);
	    while (brows.next());
	}

	SymmetricMatrix.rcumul(S, delta, lag);
    }

    /**
     * 
     * @param lag
     */
    public void difference(int lag)
    {
	z.reverse().cumul(lag);
	if (B != null) {
	    DataBlockIterator brows = B.rows();
	    DataBlock brow = brows.getData();
	    do
		brow.reverse().cumul(lag);
	    while (brows.next());
	}

	SymmetricMatrix.rcumul(S, lag);

    }

    /**
     * 
     * @param pos
     * @return
     */
    public double TStat(int pos)
    {
	// calc l=Bo, xx=o'So, xy=o'Z

	if (B != null)
	    B.column(pos).copyTo(l, 0);
	double xy = z.get(pos);
	double xx = S.get(pos, pos);

	if (B != null) {
	    // K=A^-1*L
	    // lA * lA' * K = L
	    LowerTriangularMatrix.rsolve(lA, l);
	    // q = l'A^-1l

	    double q = 0;
	    for (int i = 0; i < l.length; ++i)
		q += l[i] * l[i];
	    //
	    double c = xx - q;
	    if (c <= 0)
		return Double.NaN;
	    double[] n = new double[w.getLength()];
	    w.copyTo(n, 0);
	    LowerTriangularMatrix.rsolve(lA, n);
	    double nm = 0;
	    for (int i = 0; i < l.length; ++i)
		nm += l[i] * n[i];
	    return (xy - nm) / Math.sqrt(c);
	} else if (xx <= 0)
	    return Double.NaN;
	else
	    return (xy / Math.sqrt(xx));
    }

    /**
     * 
     * @param ostart
     * @param pos
     * @param o
     * @param idx
     * @return
     */
    public double TStat(int ostart, int pos, double[] o, int[] idx)
    {
	DataBlock L = null;
	if (B != null) {
	    L = new DataBlock(l);
	    L.set(0);
	}
	// calc l=Bo, xx=o'So, xy=o'Z
	//
	double xx = 0, xy = 0;

	int n = z.getLength();
	// values of the outlier are o[ostart + idx[i]]
	for (int i = 0; i < idx.length; ++i) {
	    int iw = idx[i] + pos - ostart;
	    if (iw >= 0 && iw < n) {
		double io = o[idx[i]];
		if (B != null)
		    L.addAY(io, B.column(iw));
		xy += z.get(iw) * io;
		double s = S.get(iw, iw);
		xx += io * io * s;
		for (int j = 0; j < i; ++j) {
		    int jw = idx[j] + pos - ostart;
		    if (jw >= 0) {
			double jo = o[idx[j]];
			xx += 2 * S.get(iw, jw) * io * jo;
		    }
		}
	    }
	}

	if (B != null) {
	    // K=A^-1*L
	    // lA * lA' * K = L
	    // l'AA^-1l = |l' * lA'^-1|
	    LowerTriangularMatrix.rsolve(lA, l);
	    // q = l'A^-1l
	    double q = L.dot(L);
	    //
	    double c = xx - q;
	    if (c <= 0)
		return Double.NaN;

	    double[] nw = new double[w.getLength()];
	    w.copyTo(nw, 0);
	    LowerTriangularMatrix.lsolve(lA, l);
	    double nm = 0;
	    for (int i = 0; i < l.length; ++i)
		nm += l[i] * nw[i];
	    return (xy - nm) / Math.sqrt(c);
	} else if (xx <= 0)
	    return Double.NaN;
	else
	    return (xy / Math.sqrt(xx));
    }
}
