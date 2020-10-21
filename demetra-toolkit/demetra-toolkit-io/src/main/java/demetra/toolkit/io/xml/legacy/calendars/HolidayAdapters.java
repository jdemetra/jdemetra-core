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
package demetra.toolkit.io.xml.legacy.calendars;

import demetra.timeseries.calendars.Holiday;
import internal.xml.calendars.DayAdapterLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 *
 * @author Jean Palate
 */
public class HolidayAdapters {

    private static final AtomicReference<HolidayAdapters> defadapters= new AtomicReference<>();


    public static final HolidayAdapters getDefault() {
        defadapters.compareAndSet(null, make());
        return defadapters.get();
    }

    public static final void setDefault(HolidayAdapters adapters) {
        defadapters.set(adapters);
    }
    
    private static HolidayAdapters make(){
        HolidayAdapters adapters=new HolidayAdapters();
        adapters.load();
        return adapters;
    }

    private final List<HolidayAdapter> adapters = new ArrayList<>();

    public void load() {
        adapters.addAll(new DayAdapterLoader().get());
    }

    public List<Class> getXmlClasses() {
        return adapters.stream().map(adapter -> adapter.getXmlType()).collect(Collectors.toList());
    }

    public Holiday unmarshal(XmlDay xvar) {
        for (HolidayAdapter adapter : adapters) {
            if (adapter.getXmlType().isInstance(xvar)) {
                try {
                    return (Holiday) adapter.unmarshal(xvar);
                } catch (Exception ex) {
                    return null;
                }
            }
        }
        return null;
    }

    public XmlDay marshal(Holiday ivar) {
        for (HolidayAdapter adapter : adapters) {
            if (adapter.getValueType().isInstance(ivar)) {
                try {
                    return (XmlDay) adapter.marshal(ivar);
                } catch (Exception ex) {
                    return null;
                }
            }
        }
        return null;
    }
}
