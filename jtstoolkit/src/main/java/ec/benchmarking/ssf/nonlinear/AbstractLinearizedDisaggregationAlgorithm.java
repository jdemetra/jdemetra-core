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


package ec.benchmarking.ssf.nonlinear;

import ec.benchmarking.BaseDisaggregation;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockStorage;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.ssf.ISsf;
import ec.tstoolkit.ssf.extended.INonLinearSsf;

/**
 * 
 * @param <S>
 * @param <T>
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public abstract class AbstractLinearizedDisaggregationAlgorithm<S extends INonLinearSsf, T extends ISsf>
	extends BaseDisaggregation implements Cloneable {
    /**
     *
     */
    protected DataBlockStorage m_states;

    /**
     *
     */
    protected T m_lssf;
    /**
     *
     */
    /**
     *
     */
    protected double[] m_y, m_yc;
    private S m_nlssf;

    private double m_eps = 1e-6;

    private boolean m_converged;

    private int m_niter, m_maxiter = 100;

    /**
     *
     * @param y
     * @param conv
     * @param nlssf
     */
    protected AbstractLinearizedDisaggregationAlgorithm(DataBlock y, int conv,
	    S nlssf) {
	super(conv);
	m_nlssf = nlssf;
	m_y = new double[y.getLength()];
	y.copyTo(m_y, 0);
    }

    /**
     * 
     * @return
     */
    protected boolean calc()
    {
	clear();
	if (!calcInitialApproximation())
	    return false;

	m_niter = 0;
	DataBlockStorage next;
	do {
	    // modified observations

	    next = iterate();
	    if (next == null)
		return false;
	    if (mustStop(next))
		break;
	    m_states = next;
	    if (!calcNextApproximation())
		return false;
	} while (++m_niter < m_maxiter);

	m_converged = m_niter != m_maxiter;
	return true;
    }

    /**
     *
     * @return
     */
    protected abstract boolean calcInitialApproximation();

    /**
     *
     * @return
     */
    protected abstract boolean calcNextApproximation();

    /**
     * 
     */
    protected void clear()
    {
	m_states = null;
	m_yc = null;
	m_niter = 0;
	m_lssf = null;
    }

    /**
     * 
     * @return
     */
    public T getLinearizedModel()
    {
	if (m_lssf == null && !calc())
	    return null;
	return m_lssf;
    }

    /**
     * 
     * @return
     */
    public int getMaxIter()
    {
	return m_maxiter;
    }

    /**
     * 
     * @return
     */
    public DataBlock getModifiedObservations()
    {
	return new DataBlock(m_yc);
    }

    /**
     * 
     * @return
     */
    public int getNIter()
    {
	return m_niter;
    }

    /**
     * 
     * @return
     */
    public S getNonLinearSsf()
    {
	return m_nlssf;
    }

    /**
     * 
     * @return
     */
    public DataBlock getObservations()
    {
	return new DataBlock(m_y);
    }

    /**
     * 
     * @return
     */
    public double getPrecision()
    {
	return m_eps;
    }

    /**
     * 
     * @return
     */
    public DataBlockStorage getStates()
    {
	if (m_states == null && !calc())
	    return null;
	return m_states;
    }

    /**
     * 
     * @return
     */
    public boolean hasConverged()
    {
	return m_converged;
    }

    /**
     *
     * @return
     */
    protected abstract DataBlockStorage iterate();

    /**
     * 
     * @param next
     * @return
     */
    protected boolean mustStop(DataBlockStorage next)
    {
	if (m_states == null)
	    return false;
	DataBlock p0 = m_states.storage(0, m_y.length);
	DataBlock p1 = next.storage(0, m_y.length);
	double delta = p0.distance(p1) / p0.nrm2();
	return delta < m_eps;
    }

    /**
     * 
     * @param value
     */
    public void setMaxIter(int value)
    {
	m_maxiter = value;
    }

    /**
     * 
     * @param value
     */
    public void setNIter(int value)
    {
	m_niter = value;
    }

    /**
     * 
     * @param value
     */
    public void setPrecision(double value)
    {
	m_eps = value;
	clear();
    }
}
