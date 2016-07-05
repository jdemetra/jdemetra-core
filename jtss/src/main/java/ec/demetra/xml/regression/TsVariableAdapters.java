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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.openide.util.Lookup;

/**
 *
 * @author Jean Palate
 */
@GlobalServiceProvider
public class TsVariableAdapters {
    
    private static TsVariableAdapters defadapters;
    
    public static final synchronized TsVariableAdapters getDefault(){
        if (defadapters == null){
            defadapters=new TsVariableAdapters();
            defadapters.load();
            // load all the current classes
        }
        return defadapters;
    }
    
    private final List<ITsVariableAdapter> adapters=new ArrayList<>();
    
    public void load(){
        Lookup.Result<ITsVariableAdapter> all = Lookup.getDefault().lookupResult(ITsVariableAdapter.class);
        adapters.addAll(all.allInstances());
    }
    
    public List<Class> getXmlClasses(){
        return adapters.stream().map(adapter->adapter.getXmlType()).collect(Collectors.toList());
     }
    
    public ITsVariable decode(XmlVariable xvar){
        for (ITsVariableAdapter adapter: adapters ){
            if (adapter.getXmlType().isInstance(xvar))
                try {
                    return (ITsVariable) adapter.decode(xvar);
            } catch (Exception ex) {
                return null;
            }
        }
        return null;
    }

    public XmlVariable encode(ITsVariable ivar){
        for (ITsVariableAdapter adapter: adapters ){
            if (adapter.getValueType().isInstance(ivar))
                try {
                    return (XmlVariable) adapter.encode(ivar);
            } catch (Exception ex) {
                return null;
            }
        }
        return null;
    }
}
