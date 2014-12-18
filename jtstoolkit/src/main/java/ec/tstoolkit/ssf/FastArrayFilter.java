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

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;

/**
 * 
 * @param <F>
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class FastArrayFilter<F extends ISsf> {

    /*
     * private void checksteady() { m_steady = false; for (int i = 0; i < m_dim;
     * ++i) if (Math.abs(m_matrix.L[i]) > m_epsilon) return; m_steady = true; }
     */
    private int m_pos, m_end, m_dim, m_ndiffuse;

    private boolean m_steady;

    private final UMatrix m_matrix = new UMatrix();

    private FastArrayState m_state;

    private IFastArrayInitializer<F> m_initializer;

    private F m_ssf;

    private ISsfData m_data;

    /**
     * 
     */
    public FastArrayFilter()
    {
    }

    /**
     * 
     * @return
     */
    public IFastArrayInitializer<F> getInitializer()
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
    public FastArrayState getState()
    {
	return m_state;
    }

    @SuppressWarnings("unchecked")
    private boolean initialize(final IFastArrayFilteringResults rslts) {
	m_state = new FastArrayState(m_dim, m_data.hasData());
	if (m_initializer != null)
	    m_ndiffuse = m_initializer
		    .initialize(m_ssf, m_data, m_state, rslts);
	else {
	    IFastArrayInitializer<ISsf> initializer = new FastInitializer();
	    m_ndiffuse = initializer.initialize(m_ssf, m_data, m_state, rslts);
	}
	if (m_ndiffuse < 0)
	    return false;

	m_matrix.K = m_state.K.getData();
	m_matrix.L = m_state.L.getData();
	m_pos = m_ndiffuse;
	m_matrix.R = m_state.r;

	return true;
    }

    private void preArray() {
	// if (m_pos != m_ndiffuse)
	// {
	DataBlock l = new DataBlock(m_matrix.L);
	m_matrix.Z = m_ssf.ZX(m_pos, l);
	m_ssf.TX(m_pos, l);
	// }
    }

    /**
     *
     * @param data
     * @param rslts
     * @return
     */
    public boolean process(final ISsfData data,
	    final IFastArrayFilteringResults rslts) {
	if (m_ssf == null)
	    return false;
	m_data = data;
	m_dim = m_ssf.getStateDim();
	m_pos = 0;
	m_end = m_data.getCount();
	if (!initialize(rslts))
	    return false;
	if (rslts != null)
	    rslts.prepare(m_ssf, data);
	if (m_pos < m_end)
	    do {
		updateE();
		if (rslts != null)
		    rslts.save(m_pos, m_state);
		updateA();
		//
		if (!m_steady) {
		    preArray();
		    if (!m_matrix.triangularize())
			return false;
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
    public void setInitializer(final IFastArrayInitializer<F> value)
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
	double es = m_state.e / m_state.r;
	m_state.A.addAY(es, m_state.K);
    }

    private void updateE() {
	double y = m_data.get(m_pos);
	m_state.e = y - m_ssf.ZX(m_pos, m_state.A);
	m_state.r = m_matrix.R;
    }
}
