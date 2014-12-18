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

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
@Immutable
public class Weeks implements IDomain {

    private final int m_start;
    private final int m_n;

    /**
     *
     */
    public Weeks() {
        m_start = 0;
        m_n = 0;
    }

    /**
     * 
     * @param start
     * @param nweeks
     */
    public Weeks(final Day start, final int nweeks) {
        m_start = start.getId();
        m_n = nweeks;
    }

    /**
     * 
     * @param week
     * @param nweeks
     */
    public Weeks(final Week week, final int nweeks) {
        m_start = week.getId();
        m_n = nweeks;
    }

    /**
     * 
     * @param weeks
     */
    public Weeks(final Weeks weeks) {
        m_start = weeks.m_start;
        m_n = weeks.m_n;
    }

    @Override
    public IPeriod get(final int idx) {
        return new Week(m_start + 7 * idx);
    }

    @Override
    public int getLength() {
        return m_n;
    }

    @Override
    public int search(final Day day) {
        int id = day.getId() - m_start;
        if (id < 0) {
            return -1;
        }
        int idx = id / 7;
        if (idx >= m_n) {
            return -1;
        }
        return idx;
    }
}
