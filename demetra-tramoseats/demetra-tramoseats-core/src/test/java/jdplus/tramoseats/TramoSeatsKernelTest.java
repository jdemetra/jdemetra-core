/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats;

import demetra.data.Data;
import demetra.processing.ProcessingLog;
import demetra.tramoseats.TramoSeatsSpec;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class TramoSeatsKernelTest {
    
    public TramoSeatsKernelTest() {
    }

    @Test
    public void testProd() {
        TramoSeatsKernel ts = TramoSeatsKernel.of(TramoSeatsSpec.RSAfull, null);
        ProcessingLog log=ProcessingLog.dummy();
        TramoSeatsResults rslt = ts.process(Data.TS_PROD, log);
        assertTrue(rslt.getFinals() != null);
        System.out.println(rslt.getFinals());
        Map<String, Class> dictionary = rslt.getDictionary();
        dictionary.forEach((s, c)->{System.out.print(s);System.out.print('\t');System.out.println(c.getCanonicalName());});
    }
    
    @Test
    public void testProdLegacyMissing() {
        ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1967, 0, Data.PROD, true);
        ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory.process(s, TramoSeatsSpecification.RSAfull);
    }
    
    @Test
    public void testDiagnostics(){
        
    }
    
}
