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

import ec.tstoolkit.design.Development;

/**
 * Chandrasekhar recursions
 * @param <F>
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class FastFilter<F extends ISsf> {

    private int m_pos, m_end, m_dim, m_ndiffuse;

    private boolean m_steady;

    private FastState m_state;

    private IFastInitializer<F> m_initializer;

    private F m_ssf;

    private ISsfData m_data;

    private IFastFilteringResults m_rslts;

    private static final double m_epsilon = 0;// 1e-9;

    /**
     * 
     */
    public FastFilter()
    {
    }

    private void checksteady() {
	m_steady = false;
	for (int i = 0; i < m_dim; ++i)
	    if (Math.abs(m_state.L.get(i)) > m_epsilon)
		return;
	m_steady = true;
    }

    /**
     * 
     * @return
     */
    public IFastInitializer<F> getInitializer()
    {
	return m_initializer;
    }

    /**
     * 
     * @return
     */
    public F getSsf()
    {
	return m_ssf;
    }

    /**
     * 
     * @return
     */
    public FastState getState()
    {
	return m_state;
    }

    @SuppressWarnings("unchecked")
    private boolean initialize(final IFastFilteringResults rslts) {
	m_state = new FastState(m_dim, m_data.hasData());
	if (m_initializer != null)
	    m_ndiffuse = m_initializer
		    .initialize(m_ssf, m_data, m_state, rslts);
	else {
	    IFastInitializer<ISsf> initializer = new FastInitializer();
	    m_ndiffuse = initializer.initialize(m_ssf, m_data, m_state, rslts);
	}
	if (m_ndiffuse < 0)
	    return false;
	m_pos = m_ndiffuse;
	return true;
    }

    private void iterate() {
	// C(i+1) = C(i) - T L(i) * (Z*L(i))/V(i)
	// L(i+1) = T L(i) - C(i) * (Z*L(i))/V(i)
	// V(i+1) = V(i) - (Z*L(i))^2/V(i)

	// ZLi, V(i+1)
	double zl = m_ssf.ZX(m_pos, m_state.L);
	double zlv = zl / m_state.f;
	m_state.f -= zl * zlv;

	// TL(i)
	m_ssf.TX(m_pos, m_state.L);

	// C, L
	double[] L = m_state.L.getData();
	double[] C = m_state.C.getData();
	for (int i = 0; i < m_dim; ++i) {
	    double tl = L[i];
	    L[i] -= C[i] * zlv;
	    C[i] -= tl * zlv;
	}
    }

    /**
     *
     * @param data
     * @param rslts
     * @return
     */
    public boolean process(final ISsfData data,
	    final IFastFilteringResults rslts) {
	if (m_ssf == null)
	    return false;
	BaseState.fnCalls.incrementAndGet();
	m_data = data;
	m_rslts = rslts;
	m_dim = m_ssf.getStateDim();
	m_pos = 0;
	m_end = m_data.getCount();
	if (!initialize(rslts))
	    return false;
	if (rslts != null)
	    rslts.prepare(m_ssf, m_data);
	if (m_pos < m_end)
	    do {
		updateE();
		if (rslts != null)
		    m_rslts.save(m_pos, m_state);
		updateA();
		//
		if (!m_steady) {
		    iterate();
		    // checksteady();
		}
	    } while (++m_pos < m_end);
	if (rslts != null)
	    rslts.close();
	return true;
    }

    /**
     * 
     * @param value
     */
    public void setInitializer(final IFastInitializer<F> value)
    {
	m_initializer = value;
    }

    /**
     * 
     * @param value
     */
    public void setSsf(final F value)
    {
	m_ssf = value;
    }

    private void updateA() {
	m_ssf.TX(m_pos, m_state.A);
	double s = m_state.e / m_state.f;
	m_state.A.addAY(s, m_state.C);
    }

    private void updateE() {
	double y = m_data.get(m_pos);
	m_state.e = y - m_ssf.ZX(m_pos, m_state.A);
    }
}
