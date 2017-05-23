/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package internal.tsprovider.util;

import demetra.timeseries.simplets.TsFrequency;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Utility class that groups calendar-related methods.
 *
 * @author Philippe Charles
 */
final class CalendarUtil {

    /**
     * Calendar.getInstance() creates a new instance of GregorianCalendar and
     * its constructor triggers a lot of internal synchronized code. => We use
     * ThreadLocal to avoid this overhead
     */
    public static CalendarUtil getInstance() {
        return THREAD_LOCAL.get();
    }

    private static final ThreadLocal<CalendarUtil> THREAD_LOCAL = ThreadLocal.withInitial(CalendarUtil::new);

    private final GregorianCalendar cal = new GregorianCalendar();

    private void set(int year, int month, int day) {
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    public int calcTsPeriodId(TsFrequency freq, long timeInMillis) {
        int ifreq = freq.getAsInt();
        cal.setTimeInMillis(timeInMillis);
        return calcId(ifreq, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) / (12 / ifreq));
    }

    static int calcId(final int freq, final int year, final int placeinyear) {
        return (year - 1970) * freq + placeinyear;
    }
}
