/*
 * Copyright 2016 National Bank of Belgium
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
package ec.demetra.xml.calendars;

import com.google.common.collect.Iterables;
import ec.tstoolkit.design.GlobalServiceProvider;
import ec.tstoolkit.timeseries.calendars.GregorianCalendarManager;
import ec.tstoolkit.timeseries.calendars.IGregorianCalendarProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 *
 * @author Jean Palate
 */
@GlobalServiceProvider
public class CalendarAdapters {

    private static final AtomicReference<CalendarAdapters> defadapters = new AtomicReference<>();

    public static final CalendarAdapters getDefault() {
        defadapters.compareAndSet(null, make());
        return defadapters.get();
    }

    public CalendarAdapters() {
        load();
    }

    public static final void setDefault(CalendarAdapters adapters) {
        defadapters.set(adapters);
    }

    private static CalendarAdapters make() {
        CalendarAdapters adapters = new CalendarAdapters();
        return adapters;
    }

    private final List<CalendarAdapter> adapters = new ArrayList<>();

    private void load() {
        Iterable<CalendarAdapter> all = ServiceLoader.load(CalendarAdapter.class);
        Iterables.addAll(adapters, all);
    }

    public List<Class> getXmlClasses() {
        return adapters.stream().map(adapter -> adapter.getXmlType()).collect(Collectors.toList());
    }

    public IGregorianCalendarProvider unmarshal(XmlCalendar xvar) {
        for (CalendarAdapter adapter : adapters) {
            if (adapter.getXmlType().isInstance(xvar)) {
                try {
                    return (IGregorianCalendarProvider) adapter.unmarshal(xvar);
                } catch (Exception ex) {
                    return null;
                }
            }
        }
        return null;
    }

    public IGregorianCalendarProvider unmarshal(XmlCalendar xvar, GregorianCalendarManager mgr) {
        for (CalendarAdapter adapter : adapters) {
            if (adapter.getXmlType().isInstance(xvar)) {
                try {
                    return (IGregorianCalendarProvider) adapter.unmarshal(xvar, mgr);
                } catch (Exception ex) {
                    return null;
                }
            }
        }
        return null;
    }

    public XmlCalendar marshal(IGregorianCalendarProvider ivar) {
        for (CalendarAdapter adapter : adapters) {
            if (adapter.getValueType().isInstance(ivar)) {
                try {
                    return (XmlCalendar) adapter.marshal(ivar);
                } catch (Exception ex) {
                    return null;
                }
            }
        }
        return null;
    }
}
