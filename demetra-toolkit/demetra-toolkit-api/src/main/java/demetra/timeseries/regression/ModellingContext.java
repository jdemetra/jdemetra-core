/*
 * Copyright 2020 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.timeseries.regression;

import demetra.information.InformationSet;
import demetra.timeseries.TsDataSupplier;
import demetra.timeseries.calendars.CalendarManager;
import demetra.util.DefaultNameValidator;
import demetra.util.INameValidator;
import demetra.util.NameManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import demetra.timeseries.calendars.CalendarDefinition;
import demetra.timeseries.regression.TsSuppliers;

/**
 *
 * @author Jean Palate
 */
public class ModellingContext  {
    
    public static final String LEGACY="legacy";

    private final HashMap<Class, NameManager> map = new HashMap<>();
    
    private static AtomicReference<ModellingContext> DEF_CONTEXT=new AtomicReference<>(null);

    public ModellingContext() {
        map.put(TsSuppliers.class, new NameManager(TsSuppliers.class, "Variables_", new DefaultNameValidator(".")));
        map.put(CalendarDefinition.class, new CalendarManager());
    }
 
    public CalendarManager getCalendars() {
        return (CalendarManager) map.get(CalendarDefinition.class);
    }

    public NameManager<TsSuppliers> getTsVariableManagers() {
        return map.get(TsSuppliers.class);
    }

    public TsSuppliers getTsVariables(String family) {
        Object obj = map.get(TsSuppliers.class);
        if (obj == null) {
            return null;
        }
        NameManager<TsSuppliers> mgr = (NameManager<TsSuppliers>) obj;
        return mgr.get(family);
    }

    public TsDataSupplier getTsVariable(String family, String var) {
        Object obj = map.get(TsSuppliers.class);
        if (obj == null) {
            return null;
        }
        NameManager<TsSuppliers> mgr = (NameManager<TsSuppliers>) obj;
        TsSuppliers vars = mgr.get(family);
        if (vars == null) {
            return null;
        }
        return vars.get(var);
    }

    public TsDataSupplier getTsVariable(String name) {
        String[] s = InformationSet.split(name);
        if (s.length == 1){
            return getTsVariable(LEGACY, s[0]);
        }
        else if (s.length != 2) {
            return null;
        } else {
            return getTsVariable(s[0], s[1]);
        }
    }

    public List<String> getTsVariableDictionary() {
        ArrayList<String> all = new ArrayList<>();
        NameManager<TsSuppliers> mgrs = getTsVariableManagers();
        String[] groups = mgrs.getNames();
        for (int i = 0; i < groups.length; ++i) {
            TsSuppliers tv = mgrs.get(groups[i]);
            String[] vars = tv.getNames();
            for (int j = 0; j < vars.length; ++j) {
                all.add(InformationSet.item(groups[i], vars[j]));
            }
        }
        return all;
    }

    public <T> boolean add(Class<T> tclass, String prefix, INameValidator validator) {
        if (map.containsKey(tclass)) {
            return false;
        }
        map.put(tclass, new NameManager<>(tclass, prefix, validator));
        return true;
    }

    public Collection<Class> getTypes() {
        return map.keySet();
    }

    public <T> NameManager<T> getInformation(Class<T> tclass) {
        return map.get(tclass);
    }

    public static ModellingContext getActiveContext() {
        return DEF_CONTEXT.get();
    }

    public static void setActiveContext(ModellingContext context) {
        DEF_CONTEXT.set(context);
    }

    public boolean isDirty() {
        for (NameManager<?> mgr : map.values()) {
            if (mgr.isDirty()) {
                return true;
            }
        }
        return false;
    }

    public void resetDirty() {
        for (NameManager<?> mgr : map.values()) {
            mgr.resetDirty();
        }
    }

    public void clear() {
        for (NameManager<?> mgr : map.values()) {
            mgr.clear();
        }
    }

    public void resetDefault() {
        map.clear();
        map.put(TsSuppliers.class, new NameManager(TsSuppliers.class, "Variables_", new DefaultNameValidator(".")));
    }
}
