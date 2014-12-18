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
package ec.tstoolkit.timeseries;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.design.IntValue;
import java.util.Calendar;
import java.util.EnumSet;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public enum DayOfWeek implements IntValue {

    /**
     *
     */
    Sunday(0),
    /**
     * 
     */
    Monday(1),
    /**
     *
     */
    Tuesday(2),
    /**
     * 
     */
    Wednesday(3),
    /**
     *
     */
    Thursday(4),
    /**
     * 
     */
    Friday(5),
    /**
     *
     */
    Saturday(6);

    /**
     * 
     * @param dayofweek
     * @return
     */
    public static DayOfWeek fromCalendar(final int dayofweek) {
	return valueOf((dayofweek - 1 + 6) % 7);
    }

    /**
     * 
     * @param dayofWeek
     * @return
     */
    public static int toCalendar(final DayOfWeek dayofWeek) {
        switch (dayofWeek) {
            case Sunday:
                return Calendar.SUNDAY;
            case Monday:
                return Calendar.MONDAY;
            case Tuesday:
                return Calendar.TUESDAY;
            case Wednesday:
                return Calendar.WEDNESDAY;
            case Thursday:
                return Calendar.THURSDAY;
            case Friday:
                return Calendar.FRIDAY;
            case Saturday:
                return Calendar.SATURDAY;
        }
        throw new RuntimeException();
    }

    /**
     * 
     * @param value
     * @return
     */
    public static DayOfWeek valueOf(final int value) {
	for (DayOfWeek dayofweek : EnumSet.allOf(DayOfWeek.class))
	    if (dayofweek.intValue() == value)
		return dayofweek;
	return null;
    }

    private final int value;

    // Calendar: sunday 1->7, DayOfWeek: monday 0->6

    DayOfWeek(final int value) {
	this.value = value;
    }

    /**
     * Returns the value of this DayOfWeek as an int.
     * @return
     */
    @Override
    public int intValue() {
	return value;
    }

}
