/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.calendars;

import demetra.modelling.regression.RegressionVariableFactory;
import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.TsPeriod;
import java.time.LocalDate;
import demetra.maths.matrices.Matrix;

/**
 *
 * @author palatej
 */
public class MovingHolidayFactory implements RegressionVariableFactory<MovingHolidayVariable>{

    @Override
    public boolean fill(MovingHolidayVariable var, TsPeriod start, Matrix buffer) {
//        mh(var.getDefinition().getEvent(),...);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <D extends TimeSeriesDomain> boolean fill(MovingHolidayVariable var, D domain, Matrix buffer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private LocalDate[] mh(String event, LocalDate start, LocalDate end){
        switch (event){
            case "easter":
                return GregorianMovingHolidays.easter().holidays(start, end);
            case "chinese.newyear":
                return ChineseMovingHolidays.newYear().holidays(start, end);
            default:
                return null;
        }
    }
}
