/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demetra.timeseries.regression;

import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.calendars.Calendar;
import demetra.timeseries.calendars.CalendarDefinition;
import demetra.timeseries.calendars.Holiday;
import demetra.timeseries.calendars.HolidaysOption;
import nbbrd.design.Development;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@Development(status = Development.Status.Release)
public class HolidaysVariable implements ICalendarVariable {

    private static final int[] NON_WORKING = new int[]{6, 7};

    public static HolidaysVariable of(String name, HolidaysOption holidaysOption, boolean single, ModellingContext context) {
        CalendarDefinition cdef = context.getCalendars().get(name);
        if (!(cdef instanceof Calendar)) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        Calendar c = (Calendar) cdef;
        return new HolidaysVariable(c.getHolidays(), holidaysOption, single, NON_WORKING);
    }

    private Holiday[] holidays;
    private HolidaysOption holidaysOption;
    private boolean single;
    private int[] nonworking;

    @Override
    public int dim() {
        return single ? 1 : holidays.length;
    }

    @Override
    public <D extends TimeSeriesDomain<?>> String description(D context) {
        return "holidays";
    }

    @Override
    public <D extends TimeSeriesDomain<?>> String description(int idx, D context) {
        return single ? "holidays" : holidays[idx].display();
    }

}
