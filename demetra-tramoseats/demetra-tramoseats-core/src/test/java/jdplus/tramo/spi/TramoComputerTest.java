/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramo.spi;

import demetra.data.Data;
import demetra.processing.ProcResults;
import demetra.timeseries.regression.ModellingContext;
import demetra.tramo.TramoSpec;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author palatej
 */
public class TramoComputerTest {
    
    public TramoComputerTest() {
    }

    @Test
    public void testBasic() {
        List<String> items=new ArrayList<>();
        items.add("likelihoog.ll");
        ProcResults rslt = demetra.tramo.Tramo.process(Data.TS_PROD, TramoSpec.TRfull, ModellingContext.getActiveContext(), items);
        
//        System.out.println(rslt.getEstimation().getStatistics());
//        System.out.println(rslt.getDescription().getStochasticComponent());
    }
    
}
