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

import demetra.timeseries.StaticTsDataSupplier;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDataSupplier;
import demetra.timeseries.calendars.Calendar;
import demetra.timeseries.calendars.CalendarDefinition;
import demetra.timeseries.calendars.CalendarManager;
import demetra.timeseries.regression.ModellingContext;
import demetra.timeseries.regression.TsDataSuppliers;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class ModellingContextProto {

    public final String R = "r", RPREFIX = "r@";

    public ToolkitProtos.ModellingContext convert(ModellingContext context) {
        ToolkitProtos.ModellingContext.Builder builder = ToolkitProtos.ModellingContext.newBuilder();

        CalendarManager cmgr = context.getCalendars();
        for (String key : cmgr.getNames()) {
            CalendarDefinition cd = cmgr.get(key);
            if (cd instanceof Calendar) {
                builder.putCalendars(key, CalendarProtosUtility.convert(cd));
            }
        }

        String[] vars = context.getTsVariableManagers().getNames();
        for (int i = 0; i < vars.length; ++i) {
            TsDataSuppliers cur = context.getTsVariables(vars[i]);
            String[] names = cur.getNames();
            for (String name : names) {
                TsDataSupplier v = cur.get(name);
                TsData d = v.get();
                if (d != null) {
                    if (vars[i].equals(R)) {
                        builder.putVariables(name, ToolkitProtosUtility.convert(d));
                    } else {
                        StringBuilder lname = new StringBuilder();
                        lname.append(vars[i]).append('@').append(name);
                        builder.putVariables(lname.toString(), ToolkitProtosUtility.convert(d));
                    }
                }
            }
        }
        return builder.build();
    }

    public ModellingContext convert(ToolkitProtos.ModellingContext context) {
        ModellingContext rslt = new ModellingContext();
        Map<String, ToolkitProtos.CalendarDefinition> cmgr = context.getCalendarsMap();
        for (Map.Entry<String, ToolkitProtos.CalendarDefinition> entry : cmgr.entrySet()) {
            rslt.getCalendars().set(entry.getKey(), CalendarProtosUtility.convert(entry.getValue()));
        }

        Map<String, ToolkitProtos.TsData> smap = context.getVariablesMap();
        TsDataSuppliers vars = new TsDataSuppliers();
        smap.forEach((n, s) -> vars.set(n, new StaticTsDataSupplier(ToolkitProtosUtility.convert(s))));
        rslt.getTsVariableManagers().set(R, vars);
        return rslt;
    }
}
