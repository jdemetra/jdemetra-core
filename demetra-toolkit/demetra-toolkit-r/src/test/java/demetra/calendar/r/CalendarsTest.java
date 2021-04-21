/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.calendar.r;

import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.regression.EasterVariable;
import jdplus.data.DataBlock;
import jdplus.modelling.regression.Regression;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class CalendarsTest {
    
    public CalendarsTest() {
    }

    @Test
    public void testEaster() {
        int dur=10;
        EasterVariable easter=EasterVariable.builder()
                .duration(dur)
                .meanCorrection(EasterVariable.Correction.None)
                .build();
        
        DataBlock x = Regression.x(TsDomain.of(TsPeriod.monthly(1980, 1), 480), easter);
        assertEquals(x.sum(), 40, 1e-9);
    }
    
    public static void main(String[] arg){
        String[] easters = Calendars.easter(1900, 2050, false);
        for (int i=0; i<easters.length; ++i)
            System.out.println(easters[i]);
    }
    
}
