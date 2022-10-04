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
package demetra.toolkit.io.protobuf;

import demetra.timeseries.DynamicTsDataSupplier;
import demetra.timeseries.StaticTsDataSupplier;
import demetra.timeseries.TsDataSupplier;
import demetra.timeseries.TsMoniker;
import demetra.timeseries.calendars.CalendarDefinition;
import demetra.timeseries.calendars.CalendarManager;
import demetra.timeseries.regression.ModellingContext;
import demetra.timeseries.regression.TsDataSuppliers;
import demetra.util.NameManager;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class ModellingContextProto {

    public final String R = "r", RPREFIX = "r@";

    public ToolkitProtos.TsMoniker convert(TsMoniker moniker) {
        return ToolkitProtos.TsMoniker.newBuilder()
                .setSource(moniker.getSource())
                .setId(moniker.getId())
                .build();
    }

    public TsMoniker convert(ToolkitProtos.TsMoniker moniker) {
        return TsMoniker.of(moniker.getSource(), moniker.getId());
    }

    public ToolkitProtos.TsDataSuppliers.Item convert(String name, TsDataSupplier supplier) {
        ToolkitProtos.TsDataSuppliers.Item.Builder builder = ToolkitProtos.TsDataSuppliers.Item.newBuilder();
        builder.setName(name);
        if (supplier instanceof StaticTsDataSupplier s) {
            builder.setData(ToolkitProtosUtility.convert(s.getData()));
        } else if (supplier instanceof DynamicTsDataSupplier s) {
            builder.setMoniker(convert(s.getMoniker()));
        }
        return builder.build();
    }

    public ToolkitProtos.TsDataSuppliers convert(TsDataSuppliers suppliers) {
        ToolkitProtos.TsDataSuppliers.Builder builder = ToolkitProtos.TsDataSuppliers.newBuilder();
        for (String name : suppliers.getNames()) {
            builder.addItems(convert(name, suppliers.get(name)));
        }
        return builder.build();
    }

    public TsDataSupplier convert(ToolkitProtos.TsDataSuppliers.Item supplier) {
        if (supplier.hasData()) {
            return new StaticTsDataSupplier(ToolkitProtosUtility.convert(supplier.getData()));
        } else if (supplier.hasMoniker()) {
            return new DynamicTsDataSupplier(convert(supplier.getMoniker()));
        } else {
            return null;
        }
    }

    public TsDataSuppliers convert(ToolkitProtos.TsDataSuppliers suppliers) {
        TsDataSuppliers s = new TsDataSuppliers();
        for (ToolkitProtos.TsDataSuppliers.Item item : suppliers.getItemsList()) {
            s.set(item.getName(), convert(item));
        }
        return s;
    }

    public ModellingContext convert(ToolkitProtos.ModellingContext context) {
        ModellingContext rslt = new ModellingContext();
        Map<String, ToolkitProtos.CalendarDefinition> cmgr = context.getCalendarsMap();
        for (Map.Entry<String, ToolkitProtos.CalendarDefinition> entry : cmgr.entrySet()) {
            rslt.getCalendars().set(entry.getKey(), CalendarProtosUtility.convert(entry.getValue()));
        }

        Map<String, ToolkitProtos.TsDataSuppliers> smap = context.getVariablesMap();
        for (Map.Entry<String, ToolkitProtos.TsDataSuppliers> entry : smap.entrySet()) {
            rslt.getTsVariableManagers().set(entry.getKey(), convert(entry.getValue()));
        }
        return rslt;
    }

    public ToolkitProtos.ModellingContext convert(ModellingContext context) {
        ToolkitProtos.ModellingContext.Builder builder = ToolkitProtos.ModellingContext.newBuilder();

        CalendarManager cmgr = context.getCalendars();
        for (String key : cmgr.getNames()) {
            CalendarDefinition cd = cmgr.get(key);
            ToolkitProtos.CalendarDefinition cdef = CalendarProtosUtility.convert(cd);
            if (cdef != null) {
                builder.putCalendars(key, cdef);
            }
        }
        NameManager<TsDataSuppliers> tmgr = context.getTsVariableManagers();
        for (String key : tmgr.getNames()) {
            TsDataSuppliers cur = tmgr.get(key);
            ToolkitProtos.TsDataSuppliers s = convert(cur);
            if (s != null) {
                builder.putVariables(key, s);
            }
        }
        return builder.build();
    }

}
