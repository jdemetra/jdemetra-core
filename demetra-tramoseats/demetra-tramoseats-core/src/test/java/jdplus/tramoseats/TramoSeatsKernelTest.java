/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats;

import demetra.data.Data;
import demetra.processing.ProcessingLog;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import demetra.tramoseats.TramoSeatsSpec;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class TramoSeatsKernelTest {

    public TramoSeatsKernelTest() {
    }

    @Test
    public void testProd() {
        TramoSeatsKernel ts = TramoSeatsKernel.of(TramoSeatsSpec.RSA5, null);
        ProcessingLog log = ProcessingLog.dummy();
        TsData s = TsData.ofInternal(TsPeriod.monthly(2001, 1), Data.RETAIL_ALLHOME);
        TramoSeatsResults rslt = ts.process(s, log);
        assertTrue(rslt.getFinals() != null);
        TramoSeatsDiagnostics diags = TramoSeatsDiagnostics.of(rslt);
        assertTrue(diags != null);
//        System.out.println(rslt.getDecomposition().getInitialComponents());
//        System.out.println(rslt.getFinals());
//        Map<String, Class> dictionary = rslt.getDictionary();
//        dictionary.forEach((s, c)->{System.out.print(s);System.out.print('\t');System.out.println(c.getCanonicalName());});
    }

    @Test
    public void testProdLegacyMissing() {
        ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1992, 0, Data.RETAIL_FUELDEALERS, true);
        ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory.process(s, TramoSeatsSpecification.RSAfull);
    }

    @Test
    public void testDiagnostics() {

    }

    @Test
    public void tesIPI() {
        TramoSeatsKernel ts = TramoSeatsKernel.of(TramoSeatsSpec.RSAfull, null);
        ProcessingLog log = ProcessingLog.dummy();
        TramoSeatsResults rslt = ts.process(Data.SP_IPI, log);
    }

    @Test
    public void tesIPI10() {
        TramoSeatsKernel ts = TramoSeatsKernel.of(TramoSeatsSpec.RSAfull, null);
        ProcessingLog log = ProcessingLog.dummy();
        TramoSeatsResults rslt = ts.process(Data.SP_IPI_10, log);
    }

    @Test
    public void tesIPI72() {
        TramoSeatsKernel ts = TramoSeatsKernel.of(TramoSeatsSpec.RSAfull, null);
        ProcessingLog log = ProcessingLog.dummy();
        TramoSeatsResults rslt = ts.process(Data.SP_IPI_72, log);
    }
}
