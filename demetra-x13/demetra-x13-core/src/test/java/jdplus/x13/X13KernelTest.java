/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13;

import demetra.data.Data;
import demetra.processing.ProcessingLog;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDataTable;
import demetra.x13.X13Spec;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 *
 * @author palatej
 */
public class X13KernelTest {
    
    public X13KernelTest() {
    }

    @Test
    public void testProd() {
        X13Kernel x13=X13Kernel.of(X13Spec.RSA4, null);
        ProcessingLog log=ProcessingLog.dummy();
        X13Results rslt = x13.process(Data.TS_PROD, log);
        
        List<TsData> all=new ArrayList<>();
        
        all.add(rslt.getFinals().getD16());
        all.add(rslt.getFinals().getD11final());
        all.add(rslt.getFinals().getD12final());
        all.add(rslt.getFinals().getD13final());
        all.add(rslt.getFinals().getD16());
        all.add(rslt.getFinals().getD18());
        TsDataTable table=TsDataTable.of(all);
        
//        System.out.println(table);
   }
    
    @Test
    public void testX11Prod() {
        X13Kernel x13=X13Kernel.of(X13Spec.RSAX11, null);
        ProcessingLog log=ProcessingLog.dummy();
        X13Results rslt = x13.process(Data.TS_PROD, log);
        
        List<TsData> all=new ArrayList<>();
        
        all.add(rslt.getData("decomposition.b1", TsData.class));
        all.add(rslt.getData("preadjust.a1", TsData.class));
        all.removeIf(s->s == null);
        TsDataTable table=TsDataTable.of(all);
        
        System.out.println(table);
   }
    
}
