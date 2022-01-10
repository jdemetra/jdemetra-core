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
package demetra.workspace;

import demetra.timeseries.DynamicTsDataSupplier;
import demetra.timeseries.TsDataSupplier;
import demetra.timeseries.calendars.CalendarDefinition;
import demetra.timeseries.calendars.CalendarManager;
import demetra.timeseries.regression.ModellingContext;
import demetra.timeseries.regression.TsDataSuppliers;
import demetra.util.NameManager;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class WorkspaceUtility {

    public List<String> ids(WorkspaceDescriptor descriptor, WorkspaceFamily family) {
        try {
            return descriptor.getItems().stream()
                    .filter(desc -> desc.getKey().getFamily().equals(family))
                    .map(desc -> desc.getKey().getId())
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            return Collections.emptyList();
        }
    }

    public List<WorkspaceItemDescriptor> select(WorkspaceDescriptor descriptor, WorkspaceFamily family) {
        try {
            return descriptor.getItems().stream()
                    .filter(desc -> desc.getKey().getFamily().equals(family))
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            return Collections.emptyList();
        }
    }

    public ModellingContext context(WorkspaceDescriptor ws, boolean refresh) throws IOException {
        ModellingContext context = new ModellingContext();
        loadAllCalendars(ws, context);
        loadAllVariables(ws, refresh, context);
        return context;
    }

    private void loadAllCalendars(WorkspaceDescriptor ws, ModellingContext context) throws IOException {
        CalendarManager target = context.getCalendars();
        for (WorkspaceItemDescriptor item : ws.getItems()) {
            WorkspaceFamily family = item.getKey().getFamily();
            if (family.equals(demetra.workspace.WorkspaceFamily.UTIL_CAL)) {
                CalendarManager calendar = (CalendarManager) ws.load(item.getKey());
                for (String s : calendar.getNames()) {
                    if (!target.contains(s)) {
                        CalendarDefinition cal = calendar.get(s);
                        target.set(s, cal);
                    }
                }
                target.resetDirty();
            }
        }
    }

    private void loadAllVariables(WorkspaceDescriptor ws, boolean refresh, ModellingContext context) throws IOException {
        NameManager<TsDataSuppliers> manager = context.getTsVariableManagers();
        for (WorkspaceItemDescriptor item : ws.getItems()) {
            WorkspaceFamily family = item.getKey().getFamily();
            if (family.equals(demetra.workspace.WorkspaceFamily.UTIL_VAR)) {
                TsDataSuppliers vars = (TsDataSuppliers) ws.load(item.getKey());
                if (refresh && refreshVariables(vars)){
                    try{
                    ws.store(item, vars);
                    }catch (IOException err){
                    }
                }
                vars.resetDirty();
                manager.set(item.getAttributes().getLabel(), vars);
            }
        }
        manager.resetDirty();
    }

    private boolean refreshVariables(TsDataSuppliers vars) {
        boolean dirty = false;
        Collection<TsDataSupplier> variables = vars.variables();
        for (TsDataSupplier var : variables) {
            if (var instanceof DynamicTsDataSupplier) {
                DynamicTsDataSupplier dvar = (DynamicTsDataSupplier) var;
                dvar.refresh();
                dirty = true;
            }
        }
        return dirty;
//            if (dirty) {
//                try {
//                    ws.store(item, v);
//                } catch (IOException ex) {
//                }
//            }
    }

}
