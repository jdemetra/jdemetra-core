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

package ec.tstoolkit.algorithm;

import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.calendars.GregorianCalendarManager;
import ec.tstoolkit.timeseries.calendars.IGregorianCalendarProvider;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.utilities.DefaultNameValidator;
import ec.tstoolkit.utilities.IModifiable;
import ec.tstoolkit.utilities.INameValidator;
import ec.tstoolkit.utilities.NameManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class ProcessingContext implements IModifiable {
    
    public static final String LEGACY="legacy";

    private final HashMap<Class, NameManager> map_ = new HashMap<>();
    private final static ProcessingContext[] def_ = new ProcessingContext[]{new ProcessingContext()};

    public ProcessingContext() {
        map_.put(TsVariables.class, new NameManager(TsVariables.class, "Variables_", new DefaultNameValidator(".")));
        map_.put(IGregorianCalendarProvider.class, new GregorianCalendarManager());
    }

    public GregorianCalendarManager getGregorianCalendars() {
        return (GregorianCalendarManager) map_.get(IGregorianCalendarProvider.class);
    }

    public NameManager<TsVariables> getTsVariableManagers() {
        return map_.get(TsVariables.class);
    }

    public TsVariables getTsVariables(String family) {
        Object obj = map_.get(TsVariables.class);
        if (obj == null) {
            return null;
        }
        NameManager<TsVariables> mgr = (NameManager<TsVariables>) obj;
        return mgr.get(family);
    }

    public ITsVariable getTsVariable(String family, String var) {
        Object obj = map_.get(TsVariables.class);
        if (obj == null) {
            return null;
        }
        NameManager<TsVariables> mgr = (NameManager<TsVariables>) obj;
        TsVariables vars = mgr.get(family);
        if (vars == null) {
            return null;
        }
        return vars.get(var);
    }

    public ITsVariable getTsVariable(String name) {
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
        NameManager<TsVariables> mgrs = getTsVariableManagers();
        String[] groups = mgrs.getNames();
        for (int i = 0; i < groups.length; ++i) {
            TsVariables tv = mgrs.get(groups[i]);
            String[] vars = tv.getNames();
            for (int j = 0; j < vars.length; ++j) {
                all.add(InformationSet.item(groups[i], vars[j]));
            }
        }
        return all;
    }

    public <T> boolean add(Class<T> tclass, String prefix, INameValidator validator) {
        if (map_.containsKey(tclass)) {
            return false;
        }
        map_.put(tclass, new NameManager<>(tclass, prefix, validator));
        return true;
    }

    public Collection<Class> getTypes() {
        return map_.keySet();
    }

    public <T> NameManager<T> getInformation(Class<T> tclass) {
        return map_.get(tclass);
    }

    public static ProcessingContext getActiveContext() {
        synchronized (def_) {
            return def_[0];
        }
    }

    public static void setActiveContext(ProcessingContext context) {
        synchronized (def_) {
            def_[0] = context;
        }
    }

    @Override
    public boolean isDirty() {
        for (NameManager<?> mgr : map_.values()) {
            if (mgr.isDirty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void resetDirty() {
        for (NameManager<?> mgr : map_.values()) {
            mgr.resetDirty();
        }
    }

    public void clear() {
        for (NameManager<?> mgr : map_.values()) {
            mgr.clear();
        }
    }

    public void resetDefault() {
        map_.clear();
        map_.put(TsVariables.class, new NameManager(TsVariables.class, "Variables_", new DefaultNameValidator(".")));
        map_.put(IGregorianCalendarProvider.class, new GregorianCalendarManager());
    }
}
