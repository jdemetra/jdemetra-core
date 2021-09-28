/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.timeseries.regression;

import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.calendars.HolidayPattern;

/**
 *
 * @author palatej
 */
@lombok.Value
public class MovingHolidayVariable implements IMovingHolidayVariable, ISystemVariable{

    @lombok.NonNull
    String event;
    HolidayPattern pattern;

    @Override
    public int dim() {
        return 1;
    }
    
    @Override
    public <D extends TimeSeriesDomain<?>> String description(D context){
        return "event";
    }
    
}
