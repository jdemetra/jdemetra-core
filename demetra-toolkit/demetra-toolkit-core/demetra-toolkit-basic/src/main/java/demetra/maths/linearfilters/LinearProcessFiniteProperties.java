/*
* Copyright 2013 National Bank ofInternal Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions ofInternal the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy ofInternal the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
*/
package demetra.maths.linearfilters;

import demetra.data.DataBlock;
import demetra.design.Development;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class LinearProcessFiniteProperties {
    private int m_n = 49;

    private ILinearProcess m_lp;

    private FiniteFilter[] m_ff;

    /**
	 *
	 */
    public LinearProcessFiniteProperties() {
    }

    /**
     * 
     * @param lp
     */
    public LinearProcessFiniteProperties(final ILinearProcess lp) {
	m_lp = lp;
    }

    private void calc() {
	if (m_lp == null || m_ff != null)
	    return;
	m_ff = new FiniteFilter[m_n];
        int nc=m_lp.getOutputLength(m_n);

	for (int i = 0; i < m_n; ++i) {
	    DataBlock data = DataBlock.make(m_n);
	    data.set(i, 1);
            DataBlock datac=DataBlock.make(nc);
	    m_lp.transform(data, datac);
            m_ff[i] = new FiniteFilter(datac.getStorage(), -i);
	}

   }

    private void clear() {
	m_ff = null;
    }

    /**
     * 
     * @param idx
     * @return
     */
    public FiniteFilter finiteFilter(final int idx) {
	calc();
	return m_ff[idx];
    }

    /**
     * 
     * @return
     */
    public int getDataLength()

    {
	return m_n;
    }

    /**
     * 
     * @return
     */
    public ILinearProcess getLinearProcess() {
	return m_lp;
    }

    /**
     * 
     * @param value
     */
    public void setDataLength(final int value) {
	if (m_n != value) {
	    m_n = value;
	    clear();
	}
    }

    /**
     * 
     * @param value
     */
    public void setLinearProcess(final ILinearProcess value) {
	m_lp = value;
	clear();

    }

}
