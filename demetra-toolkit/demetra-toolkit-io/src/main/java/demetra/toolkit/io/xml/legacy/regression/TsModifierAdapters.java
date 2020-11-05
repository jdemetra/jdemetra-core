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

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import demetra.timeseries.regression.ModifiedTsVariable;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class TsModifierAdapters {

    private final AtomicReference<TsModifierAdapterLoader> ADAPTERS = new AtomicReference<>(new TsModifierAdapterLoader());

    private List<TsModifierAdapter> adapters(){
        return ADAPTERS.get().get();
    }

    public void reload() {
        ADAPTERS.set(new TsModifierAdapterLoader());
    }


    public List<Class> getXmlClasses() {
        return adapters().stream().map(adapter -> adapter.getXmlType()).collect(Collectors.toList());
    }

    public ModifiedTsVariable.Modifier unmarshal(XmlRegressionVariableModifier xvar) {
        for (TsModifierAdapter adapter : adapters()) {
            if (adapter.getXmlType().isInstance(xvar)) {
                try {
                    return (ModifiedTsVariable.Modifier ) adapter.unmarshal(xvar);
                } catch (Exception ex) {
                    return null;
                }
            }
        }
        return null;
    }

    public XmlRegressionVariableModifier marshal(ModifiedTsVariable.Modifier  ivar) {
        for (TsModifierAdapter adapter : adapters()) {
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
    
    public List<ModifiedTsVariable.Modifier> unmarshal(List<XmlRegressionVariableModifier>  ms){
        if (ms.isEmpty())
            return Collections.EMPTY_LIST;
        List<ModifiedTsVariable.Modifier> mod=new ArrayList<>();
        for (XmlRegressionVariableModifier m : ms){
            mod.add(unmarshal(m));
        }
        return mod;
    }
   
    public List<XmlRegressionVariableModifier> marshal(List<ModifiedTsVariable.Modifier>  ms){
        if (ms.isEmpty())
            return Collections.EMPTY_LIST;
        List<XmlRegressionVariableModifier> mod=new ArrayList<>();
        for (ModifiedTsVariable.Modifier m : ms){
            mod.add(marshal(m));
        }
        return mod;
    }
}
