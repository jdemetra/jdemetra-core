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
package ec.tstoolkit.ssf;

import ec.tstoolkit.eco.DefaultLikelihoodEvaluation;
import ec.tstoolkit.eco.DiffuseConcentratedLikelihood;
import ec.tstoolkit.maths.matrices.Householder;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.maths.matrices.UpperTriangularMatrix;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public abstract class AbstractSsfAlgorithm {

    private boolean m_ssq = false, m_ml = true;

    /**
     * 
     */
    public AbstractSsfAlgorithm()
    {
    }

    /**
     *
     * @param dped
     * @return
     */
    protected DefaultLikelihoodEvaluation<DiffuseConcentratedLikelihood> calcLikelihood(
	    final DiffusePredictionErrorDecomposition dped) {
	DiffuseConcentratedLikelihood cll = new DiffuseConcentratedLikelihood();
	LikelihoodEvaluation.evaluate(dped, cll);
	DefaultLikelihoodEvaluation<DiffuseConcentratedLikelihood> ll = new DefaultLikelihoodEvaluation<>(
		cll);
	ll.useLogLikelihood(!m_ssq);
	ll.useML(m_ml);
	return ll;
    }

    /**
     *
     * @param instance
     * @param drslts
     * @return
     */
    protected DefaultLikelihoodEvaluation<DiffuseConcentratedLikelihood> calcLikelihood(
	    final SsfModelData instance, final DiffuseFilteringResults drslts) {
	double[] yl = drslts.getFilteredData().data(true, true);
	Matrix xl = new Matrix(yl.length, instance.getX().getColumnsCount());
	double[] buffer = new double[instance.getX().getRowsCount()];
	DataBlockIterator cols = instance.getX().columns();
	DataBlockIterator lcols = xl.columns();
	DataBlock col = cols.getData(), lcol = lcols.getData();
	do {
	    col.copyTo(buffer, 0);
	    drslts.getVarianceFilter().process(drslts.getFilteredData(), 0,
		    buffer, null);
	    lcol.copyFrom(drslts.getFilteredData().data(true, true), 0);
	} while (lcols.next() && cols.next());

	Householder qr = new Householder(false);
	qr.decompose(xl);
	DataBlock res = new DataBlock(xl.getRowsCount() - xl.getColumnsCount());
	double[] b = new double[xl.getColumnsCount()];
	qr.leastSquares(new DataBlock(yl), new DataBlock(b),
			res);
	double ssqerr = res.ssq();
	Matrix u = UpperTriangularMatrix.inverse(qr.getR());

	DiffuseConcentratedLikelihood ll = new DiffuseConcentratedLikelihood();
	// initializing the results...
	int n = instance.getData().getObsCount();
	int d = drslts.getDiffuseCount()
		+ (instance.getDiffuseX() != null ? instance.getDiffuseX().length
			: 0);
	double sig = ssqerr / (n - d);
	Matrix bvar = SymmetricMatrix.XXt(u);
	bvar.mul(sig);
	double ldet = drslts.getLogDeterminant(), lddet = drslts
		.getDiffuseLogDeterminant();
	if (instance.getDiffuseX() != null) {
	    DataBlock rdiag = qr.getRDiagonal();
	    double lregdet = 0;
	    for (int i = 0; i < instance.getDiffuseX().length; ++i)
		lregdet += Math.log(Math.abs(rdiag
			.get(instance.getDiffuseX()[i])));
	    lregdet *= 2;
	    lddet += lregdet;
	}
	ll.set(ssqerr, ldet, lddet, n, d);
	ll.setRes(res.getData());
	ll.setB(b, bvar, b.length);
	DefaultLikelihoodEvaluation<DiffuseConcentratedLikelihood> rslt = new DefaultLikelihoodEvaluation<>(
		ll);
	rslt.useLogLikelihood(!m_ssq);
	rslt.useML(m_ml);
	return rslt;
    }

    /**
     * 
     * @return
     */
    public boolean isUsingML()
    {
	return m_ml;
    }

    /**
     * 
     * @return
     */
    public boolean isUsingSsq()
    {
	return m_ssq;
    }

    /**
     * 
     * @param value
     */
    public void useML(final boolean value)
    {
	m_ml = value;
    }

    /**
     * 
     * @param value
     */
    public void useSsq(final boolean value)
    {
	m_ssq = value;
    }
}
