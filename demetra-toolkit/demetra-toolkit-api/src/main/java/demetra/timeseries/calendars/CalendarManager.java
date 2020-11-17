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
package demetra.timeseries.calendars;

import demetra.util.NameManager;

/**
 *
 * @author Jean Palate
 */
public class CalendarManager extends NameManager<CalendarDefinition> {

    public static final String DEF = "Default";
    public static final String CAL = "Calendar_";
    
    public CalendarManager() {
        super(CalendarDefinition.class, CAL, null);
        set(DEF, Calendar.DEFAULT);
        resetDirty();
        lock(DEF);
    }
    
    public static CalendarDefinition getDefault(String name){
        if (name.equals(DEF))
            return Calendar.DEFAULT;
        else
            return null;
    }

    public boolean isEmpty() {
        return getCount() <= 1;
    }

}
