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

import ec.tstoolkit.utilities.Arrays2;
import ec.tstoolkit.eco.GlsForecast;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SsfGlsForecast extends GlsForecast {
    /**
     *
     * @param <F>
     * @param y
     * @param evar
     * @param ssf
     * @param nf
     */
    public <F extends ISsf> void calcForecast(final double[] y,
	    final double evar, final F ssf, final int nf) {
	m_nf = nf;
	if (m_nf <= 0)
	    return;
	m_ef = new double[m_nf];
	m_f = new double[m_nf];

	int n = y.length;
	Filter<F> filter = new Filter<>();
	filter.setSsf(ssf);

	DiffuseFilteringResults frslts = new DiffuseFilteringResults(true);
	frslts.getFilteredData().setSavingA(true);
	// filter y, extended with nf missing values
	double[] yc = new double[n + m_nf];
	Arrays2.copy(y, yc, n);
	for (int i = n; i < yc.length; ++i)
	    yc[i] = Double.NaN;
	SsfData ssfy = new SsfData(yc, null);
	filter.process(ssfy, frslts);

	DiffuseVarianceFilter vfilter = frslts.getVarianceFilter();
	FilteredData fdata = frslts.getFilteredData();

	for (int i = 0; i < m_nf; ++i) {
	    m_f[i] = fdata.A(n + i).get(0);
	    m_ef[i] = vfilter.F(n + i) * evar;
	}
    }

    /**
     * 
     * @param <F>
     * @param y
     * @param x
     * @param evar
     * @param c
     * @param cvar
     * @param ssf
     * @param fx
     */
    public <F extends ISsf> void calcForecast(final double[] y, final Matrix x,
	    final double evar, final double[] c, final Matrix cvar,
	    final F ssf, final Matrix fx) {
	int nx = c.length;
	m_nf = fx.getRowsCount();
	if (m_nf <= 0)
	    return;
	m_ef = new double[m_nf];
	m_f = new double[m_nf];

	int n = y.length;
	Filter<F> filter = new Filter<>();
	filter.setSsf(ssf);

	DiffuseFilteringResults frslts = new DiffuseFilteringResults(true);
	frslts.getFilteredData().setSavingA(true);
	// filter y, extended with nf missing values
	double[] yc = new double[n + m_nf];
	Arrays2.copy(y, yc, n);
	for (int i = n; i < yc.length; ++i)
	    yc[i] = Double.NaN;
	SsfData ssfy = new SsfData(yc, null);
	filter.process(ssfy, frslts);

	DiffuseVarianceFilter vfilter = frslts.getVarianceFilter();
	FilteredData fdata = frslts.getFilteredData();

	for (int i = 0; i < m_nf; ++i) {
	    m_f[i] = fdata.A(n + i).get(0);
	    m_ef[i] = vfilter.F(n + i) * evar;
	}

	// compute X-LX
	int pos = ssf.getNonStationaryDim();

	double[] xcur = new double[n + m_nf];

	Matrix xe = new Matrix(m_nf, nx);

	for (int i = 0; i < nx; ++i) {
	    x.column(i).copyTo(xcur, 0);
	    fx.column(i).copyTo(xcur, n);

	    vfilter.process(fdata, pos, xcur, null);
	    for (int j = 0; j < m_nf; ++j)
		xe.set(j, i, fdata.E(n + j));
	}

	DataBlock C = new DataBlock(c);
	DataBlockIterator xrows = xe.rows();
	DataBlock xrow = xrows.getData();
	do {
	    m_f[xrows.getPosition()] += xrow.dot(C);
	    m_ef[xrows.getPosition()] += SymmetricMatrix.quadraticForm(cvar,
		    xrow);
	} while (xrows.next());

	for (int i = 0; i < m_ef.length; ++i)
	    m_ef[i] = Math.sqrt(m_ef[i]);
    }
}
