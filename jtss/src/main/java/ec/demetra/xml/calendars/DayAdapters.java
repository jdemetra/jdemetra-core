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

import ec.demetra.xml.regression.*;
import ec.tstoolkit.design.GlobalServiceProvider;
import ec.tstoolkit.timeseries.calendars.ISpecialDay;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.openide.util.Lookup;

/**
 *
 * @author Jean Palate
 */
@GlobalServiceProvider
public class DayAdapters {

    private static final AtomicReference<DayAdapters> defadapters= new AtomicReference<>();


    public static final DayAdapters getDefault() {
        defadapters.compareAndSet(null, make());
        return defadapters.get();
    }

    public static final void setDefault(DayAdapters adapters) {
        defadapters.set(adapters);
    }
    
    private static DayAdapters make(){
        DayAdapters adapters=new DayAdapters();
        adapters.load();
        return adapters;
    }

    private final List<DayAdapter> adapters = new ArrayList<>();

    public void load() {
        Lookup.Result<DayAdapter> all = Lookup.getDefault().lookupResult(DayAdapter.class);
        adapters.addAll(all.allInstances());
    }

    public List<Class> getXmlClasses() {
        return adapters.stream().map(adapter -> adapter.getXmlType()).collect(Collectors.toList());
    }

    public ISpecialDay decode(XmlDay xvar) {
        for (DayAdapter adapter : adapters) {
            if (adapter.getXmlType().isInstance(xvar)) {
                try {
                    return (ISpecialDay) adapter.unmarshal(xvar);
                } catch (Exception ex) {
                    return null;
                }
            }
        }
        return null;
    }

    public XmlDay encode(ISpecialDay ivar) {
        for (DayAdapter adapter : adapters) {
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
