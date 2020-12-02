/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramoseats.r;

import demetra.data.Data;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class TramoSeatsTest {
    
    public TramoSeatsTest() {
    }

    @Test
    public void testProd() {
        TramoSeats.Results rslt = TramoSeats.process(Data.TS_PROD , "rsafull");
        assertTrue(rslt != null);
    }
    
}
