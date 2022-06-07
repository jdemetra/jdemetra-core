/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats;

import demetra.data.Data;
import demetra.processing.ProcQuality;
import demetra.processing.ProcessingLog;
import demetra.sa.SaDefinition;
import demetra.sa.SaItem;
import demetra.timeseries.Ts;
import demetra.timeseries.TsMoniker;
import demetra.toolkit.dictionaries.Dictionary;
import demetra.tramoseats.TramoSeats;
import demetra.tramoseats.TramoSeatsDictionaries;
import demetra.tramoseats.TramoSeatsSpec;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

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
        ProcessingLog log = ProcessingLog.dummy();
        TramoSeatsResults rslt = ts.process(Data.TS_PROD, log);
        assertTrue(rslt.getFinals() != null);
        TramoSeatsSpec nspec = TramoSeatsFactory.INSTANCE.generateSpec(TramoSeatsSpec.RSAfull, rslt);
        log = ProcessingLog.dummy();
//        System.out.println(nspec);
        ts = TramoSeatsKernel.of(nspec, null);
        TramoSeatsResults rslt2 = ts.process(Data.TS_PROD, log);
        assertTrue(rslt2.getFinals() != null);
        TramoSeatsSpec nspec2 = TramoSeatsFactory.INSTANCE.generateSpec(nspec, rslt2);
//        System.out.println(nspec2);
        assertEquals(rslt.getPreprocessing().getEstimation().getStatistics().getLogLikelihood(),
                rslt2.getPreprocessing().getEstimation().getStatistics().getLogLikelihood(), 1e-4);
    }
    
    @Test
    public void testProcessor(){
        ProcessingLog log=ProcessingLog.dummy();
        TramoSeatsResults rslts = (TramoSeatsResults) TramoSeatsFactory.INSTANCE.processor(TramoSeatsSpec.RSAfull).process(Data.TS_PROD, null, log);
    }
    
    @Test
    public void testSaItem(){
        Ts ts=Ts.builder()
                .moniker(TsMoniker.of())
                .name("prod")
                .data(Data.TS_PROD)
                .build();
        
        SaDefinition sadef=SaDefinition.builder()
                .domainSpec(TramoSeatsSpec.RSAfull)
                .ts(ts)
                .build();
        
        SaItem item=SaItem.builder()
                .name("prod")
                .definition(sadef)
                .build();
        item.process(null, false);
        assertTrue(item.getEstimation().getQuality() == ProcQuality.Good);
    }

    public static void main(String[] args){
        testDictionaries();
    }

    static void testDictionaries() {
        Dictionary dic = TramoSeatsDictionaries.TRAMOSEATSDICTIONARY;
        Map<String, Class> xdic = TramoSeats.outputDictionary(true);
        dic.entries().forEach(entry -> {
            System.out.print(entry.display());
            System.out.print('\t');
            if (xdic.containsKey(entry.fullName())) {
                System.out.println(1);
            } else {
                System.out.println(0);
            }

        }
        );

    }
}
