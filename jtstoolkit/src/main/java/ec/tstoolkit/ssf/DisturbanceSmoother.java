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

import ec.tstoolkit.data.SubArrayOfInt;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.MatrixStorage;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockStorage;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class DisturbanceSmoother extends BaseDiffuseSmoother {

    private int m_nres;

    private int[] m_R;

    private DataBlock m_u;

    private Matrix m_uVar, m_Q;

    private Matrix m_W, m_WQ;

    private boolean m_qinit;

    // Results
    private MatrixStorage m_P;

    private DataBlockStorage m_sres;

    private double[] m_smoothations, m_vsmoothations;

    /**
     * 
     */
    public DisturbanceSmoother()
    {
    }

    /**
     * 
     * @return
     */
    public SmoothingResults calcSmoothedStates()
    {
	SmoothingResults sm = new SmoothingResults(true, false);
	sm.prepare(m_data.getCount(), m_r);

	DataBlock a = new DataBlock(m_r);

	// stationary initialization
	Matrix Pf0 = new Matrix(m_r, m_r);
	m_ssf.Pf0(Pf0.subMatrix());
	a.product(Pf0.rows(), m_Rf);
	if (m_data.getInitialState() != null)
	    a.add(new DataBlock(m_data.getInitialState()));

	// non stationary initialisation
	if (m_ssf.isDiffuse()) {
	    Matrix Pi0 = new Matrix(m_r, m_r);
	    m_ssf.Pi0(Pi0.subMatrix());
	    DataBlock ai = new DataBlock(m_r);
	    ai.product(Pi0.rows(), m_Ri);
	    a.add(ai);
	}

	int pos = 0;
	sm.save(pos, a, null, m_smoothations[pos], m_vsmoothations[pos]);
	while (++pos < m_data.getCount()) {
	    // Ta
	    m_ssf.TX(pos - 1, a);
	    if (loadResInfo(pos - 1, false)) {
		// Ta + RW*u
		DataBlock sres = m_sres.block(pos);
		if (!sres.isZero())
		    if (m_W != null)
			if (m_R != null)
			    for (int i = 0; i < m_nres; ++i) {
				double u = sres.get(i);
				for (int k = 0; k < m_R.length; ++k)
				    a.add(m_R[k], m_W.get(k, i) * u);
			    }
			else
			    for (int i = 0; i < m_nres; ++i) {
				double u = sres.get(i);
				for (int k = 0; k < m_r; ++k)
				    a.add(k, m_W.get(k, i) * u);
			    }
		    else if (m_R != null)
			for (int i = 0; i < m_R.length; ++i)
			    a.add(m_R[i], sres.get(i));
		    else
			for (int i = 0; i < m_r; ++i)
			    a.add(i, sres.get(i));
	    }
	    sm.save(pos, a, null, m_smoothations[pos], m_vsmoothations[pos]);
	}
	return sm;
    }

    /**
     *
     */
    @Override
    protected void clear() {
	super.clear();
	m_qinit = false;
	m_Q = null;
	m_R = null;
	m_W = null;
	m_WQ = null;
	m_uVar = null;
	clearResults();
    }

    /**
     * 
     */
    protected void clearResults()
    {
	m_sres = null;
	m_P = null;
    }

    /**
     * 
     * @return
     */
    public double[] getSmoothations()
    {
	return m_smoothations;
    }

    /**
     * 
     * @return
     */
    public double[] getSmoothationsVariance()
    {
	return m_vsmoothations;
    }

    /**
     *
     */
    @Override
    protected void initSmoother() {
	super.initSmoother();
	m_nres = m_ssf.getTransitionResDim();
	m_u = new DataBlock(m_nres);

	// WQ
	int rescount = m_ssf.getTransitionResCount(), resdim = m_ssf
		.getTransitionResDim();
	if (m_ssf.hasR())
	    m_R = new int[rescount];
	if (m_ssf.hasW())
	    m_W = new Matrix(rescount, resdim);
	m_Q = new Matrix(resdim, resdim);
	prepareResults();
    }

    /**
     * 
     */
    protected void iterateSmoother()
    {
	if (m_pos >= m_enddiffuse) {
	    iterateR();
	    if (m_bCalcVar)
		iterateN();
	} else {
	    iterateInitialR();
	    if (m_bCalcVar)
		iterateInitialN();
	}
	// u = r * RW * Q = ( r * R ) * (W * Q)
	// if (m_ssf.HasTransitionRes(m_pos) && m_WQ != null)
	if (m_pos > 0 && loadResInfo(m_pos - 1, true)) {
	    iterateU();
	    if (m_bCalcVar)
		iterateUVar();
	} else {
	    m_u.set(0);
	    if (m_bCalcVar)
		m_uVar.subMatrix().set(0);
	}
    }

    /**
     * 
     */
    protected void iterateU()
    {
	if (m_R != null)
	    for (int i = 0; i < m_u.getLength(); ++i) {
		double u = 0;
		for (int j = 0; j < m_R.length; ++j)
		    u += m_Rf.get(m_R[j]) * m_WQ.get(j, i);
		m_u.set(i, u);
	    }
	else
	    for (int i = 0; i < m_u.getLength(); ++i) {
		double u = 0;
		for (int j = 0; j < m_r; ++j)
		    u += m_Rf.get(j) * m_WQ.get(j, i);
		m_u.set(i, u);
	    }
    }

    /**
     * 
     */
    protected void iterateUVar()
    {
	if (m_R != null) {
	    Matrix N = new Matrix(m_R.length, m_R.length);
	    for (int i = 0; i < m_R.length; ++i)
		for (int j = 0; j < m_R.length; ++j)
		    N.set(i, j, m_Nf.get(m_R[i], m_R[j]));
	    m_uVar = SymmetricMatrix.quadraticForm(N, m_WQ);
	} else
	    m_uVar = SymmetricMatrix.quadraticForm(m_Nf, m_WQ);
	m_uVar.chs();
	m_uVar.add(m_Q);
    }

    private boolean loadResInfo(int pos, boolean wq) {
	if (!m_ssf.hasTransitionRes(pos))
	    return false;
	if (m_qinit)
	    return true;
	if (m_ssf.isTransitionResidualTimeInvariant())
	    m_qinit = true;
	if (m_R != null){
            SubArrayOfInt R=SubArrayOfInt.create(m_R);
            R.set(0);
	    m_ssf.R(pos, R);
        }
	if (m_W != null){
            m_W.set(0);
	    m_ssf.W(pos, m_W.subMatrix());
        }
	if (wq) {
            m_Q.set(0);
	    m_ssf.Q(m_pos - 1, m_Q.subMatrix());
	    if (m_W != null)
		m_WQ = m_W.times(m_Q);
	    else
		m_WQ = m_Q.clone();
	}
	return true;
    }

    /**
     * 
     */
    protected void prepareResults()
    {
	int n = m_data.getCount();
	clearResults();
	if (m_nres > 0)
	    m_sres = new DataBlockStorage(m_nres, n);
	if (m_bCalcVar && m_nres > 0)
	    m_P = new MatrixStorage(m_nres, n);
	m_smoothations = new double[n];
	m_vsmoothations = new double[n];
    }

    /**
     * 
     * @param data
     * @return
     */
    public boolean process(ISsfData data)
    {
	return process(data, null);
    }

    /**
     * 
     * @param data
     * @param frslts
     * @return
     */
    public boolean process(ISsfData data, DiffuseFilteringResults frslts)
    {
	clear();
	if (m_ssf == null)
	    return false;
	m_frslts = null;
	if (frslts == null) {
	    frslts = new DiffuseFilteringResults(true);
	    Filter<ISsf> filter = new Filter<>();
	    filter.setSsf(m_ssf);
	    if (!filter.process(data, frslts))
		return false;
	}
	m_frslts = frslts;

	m_data = data;

	initSmoother();
	if (m_ssf.isTimeInvariant())
	    loadModelInfo();
	while (m_pos >= 0) {
	    if (!m_ssf.isTimeInvariant() || m_pos == m_enddiffuse - 1)
		loadModelInfo();
	    loadInfo();
	    iterateSmoother();
	    saveResults();
	    --m_pos;
	}

	return true;
    }

    /**
     * 
     */
    protected void saveResults()
    {
	if (m_uVar != null)
	    m_P.save(m_pos, m_uVar);
	m_sres.save(m_pos, m_u);
	m_smoothations[m_pos] = m_c;
	m_vsmoothations[m_pos] = m_cvar;
    }

    /**
     * 
     * @param t
     * @return
     */
    public DataBlock sdisturbances(int t)
    {
	return m_sres.block(t);
    }

    /**
     * 
     * @param idx
     * @param bstudentized
     * @return
     */
    public double[] smoothedSDisturbance(int idx, boolean bstudentized)
    {
	if (m_sres == null || (m_P == null && bstudentized))
	    return null;
	double[] c = new double[m_data.getCount()];
	if (!bstudentized)
	    for (int i = 0; i < c.length; ++i)
		c[i] = m_sres.block(i).get(idx);
	else
	    for (int i = 0; i < c.length; ++i)
		c[i] = m_sres.block(i).get(idx)
			/ Math.sqrt(m_P.matrix(i).get(idx, idx));

	return c;
    }

    /**
     * 
     * @param t
     * @return
     */
    public SubMatrix svar(int t)
    {
	return m_P.matrix(t);
    }
}
