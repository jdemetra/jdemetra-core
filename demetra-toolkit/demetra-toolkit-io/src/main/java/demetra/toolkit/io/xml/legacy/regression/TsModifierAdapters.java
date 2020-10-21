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
package demetra.toolkit.io.xml.legacy.regression;

import demetra.timeseries.regression.ITsModifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 *
 * @author Jean Palate
 */
public class TsModifierAdapters {

    private static final AtomicReference<TsModifierAdapters> defadapters= new AtomicReference<>();


    public static final TsModifierAdapters getDefault() {
        defadapters.compareAndSet(null, make());
        return defadapters.get();
    }

    public static final void setDefault(TsModifierAdapters adapters) {
        defadapters.set(adapters);
    }
    
    private static TsModifierAdapters make(){
        TsModifierAdapters adapters=new TsModifierAdapters();
        adapters.load();
        return adapters;
    }

    private final List<TsModifierAdapter> adapters = new ArrayList<>();

    public void load() {
        adapters.addAll(new TsModifierAdapterLoader().get());
    }

    public List<Class> getXmlClasses() {
        return adapters.stream().map(adapter -> adapter.getXmlType()).collect(Collectors.toList());
    }

    public ITsModifier unmarshal(XmlRegressionVariableModifier xvar) {
        for (TsModifierAdapter adapter : adapters) {
            if (adapter.getXmlType().isInstance(xvar)) {
                try {
                    return (ITsModifier) adapter.unmarshal(xvar);
                } catch (Exception ex) {
                    return null;
                }
            }
        }
        return null;
    }

    public XmlRegressionVariableModifier marshal(ITsModifier ivar) {
        for (TsModifierAdapter adapter : adapters) {
            if (adapter.getValueType().isInstance(ivar)) {
                try {
                    return (XmlRegressionVariableModifier) adapter.marshal(ivar);
                } catch (Exception ex) {
                    return null;
                }
            }
        }
        return null;
    }
}
