/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.timeseries.calendars;

import demetra.design.Development;
import demetra.timeseries.TsDomain;
import java.time.LocalDate;
import demetra.math.matrices.MatrixType;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Alpha)
@lombok.Value
public class ChainedCalendar implements CalendarDefinition {

    private String first, second;
    private LocalDate breakDate;

}
