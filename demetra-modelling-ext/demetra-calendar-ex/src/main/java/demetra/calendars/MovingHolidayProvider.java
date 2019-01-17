/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.calendars;

import demetra.timeseries.TsDomain;
import java.time.LocalDate;

/**
 *
 * @author palatej
 */
public interface MovingHolidayProvider {
    /**
     * holidays contained in the given time range
     * @param start Included
     * @param end Excluded
     * @return 
     */
    LocalDate[] holidays(LocalDate start, LocalDate end);
}
