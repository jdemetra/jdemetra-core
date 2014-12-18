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

package ec.satoolkit;

import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.timeseries.calendars.DefaultGregorianCalendarProvider;
import ec.tstoolkit.timeseries.calendars.GregorianCalendarManager;
import ec.tstoolkit.timeseries.calendars.IGregorianCalendarProvider;
import ec.tstoolkit.utilities.Arrays2;
import ec.tstoolkit.utilities.NameManager;

/**
 *
 * @author pcuser
 */
public abstract class AbstractSaSpecification {

    protected abstract void checkContext(ProcessingContext context);

    protected static boolean checkVariables(TsVariableDescriptor[] vars, ProcessingContext context) {
        if (Arrays2.isNullOrEmpty(vars)) {
            return true;
        }
        if (context == null) {
            return false;
        }
        for (int i = 0; i < vars.length; ++i) {
            if (context.getTsVariable(vars[i].getName()) == null) {
                return false;
            }
        }
        return true;
    }

    protected static boolean checkVariables(String[] vars, ProcessingContext context) {
        if (Arrays2.isNullOrEmpty(vars)) {
            return true;
        }
        if (context == null) {
            return false;
        }
        for (int i = 0; i < vars.length; ++i) {
            if (context.getTsVariable(vars[i]) == null) {
                return false;
            }
        }
        return true;
    }

    protected static boolean checkCalendar(String calendar, ProcessingContext context) {
        if (calendar == null || calendar.isEmpty() || calendar.equals(GregorianCalendarManager.DEF)) {
            return true;
        }
        if (context == null) {
            return false;
        }
        NameManager<IGregorianCalendarProvider> mgr = context.getGregorianCalendars();
        return mgr.contains(calendar);
    }

    public IGregorianCalendarProvider getCalendar(String calendar, ProcessingContext context) {
        if (calendar == null || calendar.isEmpty() || calendar.equals(GregorianCalendarManager.DEF)) {
            return DefaultGregorianCalendarProvider.instance;
        }
        if (context == null) {
            return null;
        }
        NameManager<IGregorianCalendarProvider> mgr = context.getGregorianCalendars();
        return mgr.get(calendar);
    }
}
