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
package demetra.timeseries;

import demetra.design.Development;
import demetra.timeseries.calendars.ISpecialDay;
import demetra.timeseries.calendars.SpecialDayEvent;
import demetra.utilities.Comparator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class NationalCalendar {

    private final List<SpecialDayEvent> specialDays = new ArrayList<>();
    private final ISpecialDay.Context context;

    public NationalCalendar() {
        this(true, false);
    }

    public NationalCalendar(boolean mean, boolean julianeaster) {
        context = new ISpecialDay.Context(mean, julianeaster);
    }

    public ISpecialDay.Context getContext() {
        return context;
    }

    public boolean add(ISpecialDay fday) {
        if (fday.match(context)) {
            SpecialDayEvent ev = new SpecialDayEvent(fday);
            if (specialDays.contains(ev)) {
                return false;
            } else {
                specialDays.add(ev);
                return true;
            }
        } else {
            return false;
        }
    }

    public int getCount() {
        return specialDays.size();
    }

    public boolean add(SpecialDayEvent sd) {
        if (!sd.day.match(context)) {
            return false;
        }
        for (SpecialDayEvent ev : specialDays) {
            if (ev.equals(sd)) {
                return false;
            }
        }
        specialDays.add(sd);
        return true;
    }

    public void addAll(Collection<SpecialDayEvent> c) {
        for (SpecialDayEvent nev : c) {
            if (!nev.day.match(context)) {
                continue;
            }
            boolean used = false;
            for (SpecialDayEvent ev : specialDays) {
                if (ev.equals(nev)) {
                    used = true;
                    break;
                }
            }
            if (!used) {
                specialDays.add(nev);
            }
        }
    }

    public SpecialDayEvent get(int idx) {
        return specialDays.get(idx);
    }

    public void clear() {
        specialDays.clear();
    }

    public SpecialDayEvent[] toArray() {
        return specialDays.toArray(new SpecialDayEvent[specialDays.size()]);
    }

    public Collection<SpecialDayEvent> elements() {
        return Collections.unmodifiableList(specialDays);
    }

    public boolean contentEquals(NationalCalendar other) {
        return Objects.deepEquals(context, other.context) && Comparator.equals(specialDays, other.specialDays);
    }
}
