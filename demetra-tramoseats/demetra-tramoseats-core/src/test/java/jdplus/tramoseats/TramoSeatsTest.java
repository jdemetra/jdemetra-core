/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats;

import demetra.data.Data;
import demetra.tramoseats.TramoSeatsSpec;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class TramoSeatsTest {
    
    public TramoSeatsTest() {
    }

    @Test
    public void testProd() {
        TramoSeats ts = TramoSeats.of(TramoSeatsSpec.RSAfull, null);
        TramoSeatsResults rslt = ts.compute(Data.TS_PROD);
        System.out.println(rslt.getSeriesDecomposition());
    }
    
}
