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

package ec.tstoolkit.timeseries.calendars;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.design.Singleton;
import ec.tstoolkit.timeseries.DayOfWeek;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Singleton
public class DefaultGregorianCalendarProvider implements IGregorianCalendarProvider {

    /**
     *
     */
    public static final DefaultGregorianCalendarProvider instance = new DefaultGregorianCalendarProvider();

    /**
     *
     */
    protected DefaultGregorianCalendarProvider() {
    }

    @Override
    @Deprecated
    public void calendarData(TradingDaysType dtype, TsDomain domain, List<DataBlock> buffer, int start) {
        calendarData(dtype, domain, buffer.subList(start, start+count(dtype)));
    }
    /**
     *
     * @param dtype
     * @param domain
     * @param buffer
     */
    @Override
    public void calendarData(TradingDaysType dtype,
            TsDomain domain, List<DataBlock> buffer) {
        // see tramo
        int nreg = 0;
        switch (dtype) {
            case TradingDays:
                nreg = 6;
                tradingDays(domain, buffer);
                break;
            case WorkingDays:
                nreg = 1;
                workingDays(domain, buffer.get(0));
                break;
        }
    }

    public static String description(TradingDaysType dw, int idx) {
        if (idx >= 7) {
            return "";
        }
        return (DayOfWeek.fromCalendar(idx + 1)).toString();
    }

    @Override
    public List<DataBlock> holidays(TradingDaysType dtype, TsDomain domain) {
        return null;
    }

    /**
     *
     * @param domain
     * @param buffer
     * @param start
     */
    protected void tradingDays(TsDomain domain, List<DataBlock> buffer) {
        int n = domain.getLength();
        int[][] days = Utilities.tradingDays(domain);

        for (int i = 0; i < 6; ++i) {
            DataBlock cur = buffer.get(i);
            for (int j = 0; j < n; ++j) {
                cur.set(j, days[i][j] - days[6][j]);
            }
        }
    }

    /**
     *
     * @param domain
     * @param buffer
     */
    protected void workingDays(TsDomain domain, DataBlock buffer) {
        int n = domain.getLength();
        int[][] days = Utilities.tradingDays(domain);

        for (int j = 0; j < n; ++j) {
            int sum = 0;
            for (int i = 0; i < 5; ++i) {
                sum += days[i][j];
            }
            int sumf = days[5][j] + days[6][j];
            double d = sum - 2.5 * sumf;
            buffer.set(j, d);
        }
    }

    @Override
    public int count(TradingDaysType type) {
        return type.getVariablesCount();
    }

    public int count(LengthOfPeriodType type) {
        return type == LengthOfPeriodType.None ? 0 : 1;
    }

    @Override
    public String getDescription(TradingDaysType dtype, int idx) {
        if (idx >= count(dtype)) {
            return "";
        }
        if (dtype == TradingDaysType.TradingDays) {
            return DayOfWeek.valueOf(idx + 1).toString();
        } else //(dtype == TradingDaysType.WorkingDays)
        {
            return "Week days";
        }
    }

    public String getDescription(LengthOfPeriodType dtype) {
        switch (dtype) {
            case LeapYear:
                return "Leap year";
            case LengthOfPeriod:
                return "Length of period";
            default:
                return "";
        }
    }
}
