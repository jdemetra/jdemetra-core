/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats;

import demetra.data.Data;
import demetra.processing.ProcessingLog;
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
        TramoSeatsKernel ts = TramoSeatsKernel.of(TramoSeatsSpec.RSAfull, null);
        ProcessingLog log=new ProcessingLog();
        TramoSeatsResults rslt = ts.process(Data.TS_PROD, log);
        System.out.println(rslt.getFinals());
    }
    
}
