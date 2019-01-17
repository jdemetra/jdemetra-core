/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.calendars;

import demetra.modelling.regression.IMovingHolidayVariable;
import demetra.timeseries.calendars.MovingHoliday;

/**
 *
 * @author palatej
 */
@lombok.Value
public class MovingHolidayVariable implements IMovingHolidayVariable {

    private MovingHoliday definition;

    @Override
    public int dim() {
        return 1;
    }
}
