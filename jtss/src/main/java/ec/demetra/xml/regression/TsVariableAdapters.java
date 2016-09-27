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
package ec.demetra.xml.regression;

import ec.tstoolkit.design.GlobalServiceProvider;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import java.lang.ref.Reference;
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
public class TsVariableAdapters {

    private static final AtomicReference<TsVariableAdapters> defadapters = new AtomicReference<>();

    public static final TsVariableAdapters getDefault() {
        defadapters.compareAndSet(null, new TsVariableAdapters());
        return defadapters.get();
    }

    public static final void setDefault(TsVariableAdapters adapters) {
        defadapters.set(adapters);
    }

    private final List<TsVariableAdapter> adapters = new ArrayList<>();

    public TsVariableAdapters() {
        load();
    }

    public TsVariableAdapters(TsVariableAdapter... adapters) {
        for (int i = 0; i < adapters.length; ++i) {
            this.adapters.add(adapters[i]);
        }
    }

    private void load() {
        Lookup.Result<TsVariableAdapter> all = Lookup.getDefault().lookupResult(TsVariableAdapter.class);
        adapters.addAll(all.allInstances());
    }

    public List<Class> getXmlClasses() {
        return adapters.stream().map(adapter -> adapter.getXmlType()).collect(Collectors.toList());
    }

    public ITsVariable unmarshal(XmlRegressionVariable xvar) {
        for (TsVariableAdapter adapter : adapters) {
            if (adapter.getXmlType().isInstance(xvar)) {
                try {
                    return (ITsVariable) adapter.unmarshal(xvar);
                } catch (Exception ex) {
                    return null;
                }
            }
        }
        return null;
    }

    public XmlRegressionVariable marshal(ITsVariable ivar) {
        for (TsVariableAdapter adapter : adapters) {
            if (adapter.getImplementationType().isInstance(ivar)) {
                try {
                    return (XmlRegressionVariable) adapter.marshal(ivar);
                } catch (Exception ex) {
                    return null;
                }
            }
        }
        return null;
    }
}
