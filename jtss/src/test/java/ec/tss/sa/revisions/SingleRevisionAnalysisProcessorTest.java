/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tss.sa.revisions;

import data.Data;
import ec.tss.sa.SaManager;
import ec.tss.sa.processors.TramoSeatsProcessor;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataTable;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class SingleRevisionAnalysisProcessorTest {

    static {
        SaManager.instance.add(new TramoSeatsProcessor());
    }

    public SingleRevisionAnalysisProcessorTest() {
    }

    @Test
    public void testDefaultRevisions() {
        RevisionAnalysisSpec spec = new RevisionAnalysisSpec();
        SingleRevisionAnalysisProcessor processor = new SingleRevisionAnalysisProcessor(spec, Data.P);
        processor.process();
        List<TsData> sa = processor.items("sa", TsData.class);
        TsDataTable table = new TsDataTable();
        for (TsData s : sa) {
            table.insert(-1, s);
        }
        System.out.println(table);
    }

}
