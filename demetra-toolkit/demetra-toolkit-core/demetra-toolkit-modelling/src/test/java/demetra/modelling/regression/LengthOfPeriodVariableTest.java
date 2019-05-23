/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.regression;

import jdplus.data.DataBlock;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.calendars.LengthOfPeriodType;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class LengthOfPeriodVariableTest {
    
    public LengthOfPeriodVariableTest() {
    }

    @Test
    public void testLeapYear() {
        LengthOfPeriod var=new LengthOfPeriod(LengthOfPeriodType.LeapYear);
        TsPeriod start=TsPeriod.monthly(1980, 5);
        TsDomain domain=TsDomain.of(start, 28*12);
        DataBlock x=Regression.x(domain, var);
//        System.out.println(x);
        assertEquals(x.sum(), 0, 1e-9);
    }
    
    @Test
    public void testLengthOfPeriod() {
        LengthOfPeriod var=new LengthOfPeriod(LengthOfPeriodType.LengthOfPeriod);
        TsPeriod start=TsPeriod.monthly(1980, 5);
        TsDomain domain=TsDomain.of(start, 28*12);
        DataBlock x=Regression.x(domain, var);
//        System.out.println(x);
        assertEquals(x.sum(), 0, 1e-9);
    }
}
