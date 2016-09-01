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
import ec.tstoolkit.design.Immutable;
import java.util.Arrays;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
@Immutable
public class Days implements IDomain {
    /*
     * internal Days(int[] days) { m_days = (int[])days.Clone(); }
     */

    private final int[] m_days;

    /**
         *
         */
    public Days() {
        m_days=null;
    }

    /**
     * 
     * @param days
     */
    public Days(final Day[] days) {
	m_days = new int[days.length];
	for (int i = 0; i < days.length; ++i)
	    m_days[i] = days[i].getId();
    }

    /**
     * 
     * @param days
     */
    public Days(final Days days) {
	m_days = days.m_days.clone();
    }

    Days(final int[] days, final int start, final int n) {
	m_days = new int[n];
	for (int i = 0; i < n; i++)
	    m_days[i] = days[start + i];
    }

    @Override
    public IPeriod get(final int idx) {
	return new Day(m_days[idx]);
    }

    @Override
    public int getLength() {
	return m_days == null ? 0 : m_days.length;
    }

    @Override
    public int search(final Day day) {
	if (m_days == null)
	    return -1;
	return Arrays.binarySearch(m_days, day.getId());
    }

}
