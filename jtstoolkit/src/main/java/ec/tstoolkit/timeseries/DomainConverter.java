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
package ec.tstoolkit.timeseries;

import ec.tstoolkit.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class DomainConverter {
    private IDomain m_ndom, m_odom;

    private int[] m_start, m_end;

    private void adjustendpos() {
	for (int i = 0; i < m_end.length; ++i)
	    if (m_end[i] >= 0)
		++m_end[i];
    }

    private void clear() {
	m_start = null;
	m_end = null;
    }

    /**
     * 
     * @param newdom
     * @param olddom
     * @return
     */
    public boolean convert(final IDomain newdom, final IDomain olddom)
    {
	init(newdom, olddom);
	if (search()) {
	    adjustendpos();
	    return true;
	} else {
	    clear();
	    return false;
	}
    }

    /**
     * 
     * @param idx
     * @return
     */
    public int endPos(final int idx) {
	return m_end[idx];
    }

    /**
     * 
     * @return
     */
    public IDomain getNewDomain() {
	return m_ndom;
    }

    /**
     * 
     * @return
     */
    public IDomain getOldDomain() {
	return m_odom;
    }

    private void init(final IDomain newdom, final IDomain olddom) {
	m_ndom = newdom;
	m_odom = olddom;
	int n = m_ndom.getLength();
	m_start = new int[n];
	m_end = new int[n];
	for (int i = 0; i < n; ++i) {
	    m_start[i] = -1;
	    m_end[i] = -1;
	}

    }

    private boolean search() {
	int nold = m_odom.getLength();

	for (int i = 0; i < nold; ++i) {
	    IPeriod oldp = m_odom.get(i);
	    Day start = oldp.firstday(), end = oldp.lastday();
	    int pos = m_ndom.search(start);
	    if (pos >= 0) {
		if (start != end) {
		    int endpos = m_ndom.search(end);
		    if (pos != endpos)
			return false;
		}
		if ((m_start[pos] == -1) || (m_start[pos] > i))
		    m_start[pos] = i;
		if ((m_end[pos] == -1) || (m_end[pos] < i))
		    m_end[pos] = i;
	    }
	}
	return true;
    }

    /**
     * 
     * @param idx
     * @return
     */
    public int startPos(final int idx) {
	return m_start[idx];
    }

}
