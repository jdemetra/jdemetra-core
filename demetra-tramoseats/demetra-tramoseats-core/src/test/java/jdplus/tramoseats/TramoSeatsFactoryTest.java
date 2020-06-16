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
 * @author PALATEJ
 */
public class TramoSeatsFactoryTest {

    public TramoSeatsFactoryTest() {
    }

    @Test
    public void testUpdateSpec() {
        TramoSeatsKernel ts = TramoSeatsKernel.of(TramoSeatsSpec.RSAfull, null);
        ProcessingLog log = new ProcessingLog();
        TramoSeatsResults rslt = ts.process(Data.TS_PROD, log);
        assertTrue(rslt.getFinals() != null);
        TramoSeatsSpec nspec = TramoSeatsFactory.INSTANCE.of(TramoSeatsSpec.RSAfull, rslt);
        log = new ProcessingLog();
        System.out.println(nspec);
        ts = TramoSeatsKernel.of(nspec, null);
        TramoSeatsResults rslt2 = ts.process(Data.TS_PROD, log);
        assertTrue(rslt2.getFinals() != null);
        TramoSeatsSpec nspec2 = TramoSeatsFactory.INSTANCE.of(nspec, rslt2);
        System.out.println(nspec2);
        assertEquals(rslt.getPreprocessing().getConcentratedLikelihood().logLikelihood(),
                rslt2.getPreprocessing().getConcentratedLikelihood().logLikelihood(), 1e-4);
    }

}
