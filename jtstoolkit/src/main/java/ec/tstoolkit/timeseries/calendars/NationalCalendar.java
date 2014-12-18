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

package ec.tstoolkit.timeseries.calendars;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.utilities.Comparator;
import ec.tstoolkit.utilities.Jdk6;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class NationalCalendar implements Cloneable {

    private List<SpecialDayEvent> m_sd = new ArrayList<>();

    public NationalCalendar() {
    }

    public void add(ISpecialDay fday) {
        m_sd.add(new SpecialDayEvent(fday));
    }

    public int getCount() {
        return m_sd.size();
    }

    public void add(SpecialDayEvent sd) {
        for (SpecialDayEvent ev : m_sd) {
            if (ev.equals(sd)) {
                return;
            }
        }
        m_sd.add(sd);
    }

    public void addAll(Collection<SpecialDayEvent> c) {
        for (SpecialDayEvent nev : c) {
            boolean used = false;
            for (SpecialDayEvent ev : m_sd) {
                if (ev.equals(nev)) {
                    used = true;
                    break;
                }
            }
            if (!used) {
                m_sd.add(nev);
            }
        }
    }

    public SpecialDayEvent get(int idx) {
        return m_sd.get(idx);
    }

    public void clear() {
        m_sd.clear();
    }

    public SpecialDayEvent[] toArray() {
        return Jdk6.Collections.toArray(m_sd, SpecialDayEvent.class);
    }

    @Override
    public NationalCalendar clone() {
        try {
            NationalCalendar cal = (NationalCalendar) super.clone();
            cal.m_sd = Jdk6.newArrayList(m_sd);
            return cal;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    public Collection<SpecialDayEvent> elements() {
        return Collections.unmodifiableList(m_sd);
    }
    
    public boolean contentEquals(NationalCalendar other) {
        return Comparator.equals(m_sd, other.m_sd);        
    }
}
