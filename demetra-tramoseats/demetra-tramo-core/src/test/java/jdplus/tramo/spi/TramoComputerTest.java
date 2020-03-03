/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramo.spi;

import demetra.arima.SarimaModel;
import demetra.data.Data;
import demetra.timeseries.regression.modelling.LinearModelEstimation;
import demetra.timeseries.regression.modelling.ModellingContext;
import demetra.tramo.TramoResults;
import demetra.tramo.TramoSpec;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class TramoComputerTest {
    
    public TramoComputerTest() {
    }

    @Test
    public void testBasic() {
        TramoResults rslt = demetra.tramo.TramoProcessor.compute(Data.TS_PROD, TramoSpec.TRfull, ModellingContext.getActiveContext(), null);
        System.out.println(rslt.getRegarima().getStatistics());
        System.out.println(rslt.getRegarima().getStochasticComponent());
    }
    
}
