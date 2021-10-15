/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.r;

import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class VariablesTest {
    
    public VariablesTest() {
    }

    @Test
    public void testLp() {
        double[] lp=Variables.leapYear(TsDomain.of(TsPeriod.monthly(2000, 1), 20), true);
        assertEquals(lp[1], .75, 1e-9);
        lp=Variables.leapYear(TsDomain.of(TsPeriod.monthly(2000, 1), 20), false);
        assertEquals(lp[1], 29-(365.25/12), 1e-9);
    }
    
}
