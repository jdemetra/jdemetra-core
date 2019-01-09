/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.r;

import demetra.timeseries.calendars.Calendar;
import demetra.timeseries.calendars.Holiday;
import demetra.timeseries.calendars.IHoliday;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PALATEJ
 */
public class Holidays {

    private final List<Holiday> holidays = new ArrayList<>();

    public boolean add(IHoliday fday) {
        Holiday ev = new Holiday(fday);
        if (!holidays.contains(ev)) {
            holidays.add(ev);
            return true;
        } else {
            return false;
        }
    }

    public boolean add(Holiday... hol) {
        for (Holiday h : hol) {
            if (!holidays.contains(h)) {
                holidays.add(h);
            }else
                return false;
        }
        return true;
    }
    
    public Holiday[] elements(){
        return holidays.toArray(new Holiday[holidays.size()]);
    }
}
