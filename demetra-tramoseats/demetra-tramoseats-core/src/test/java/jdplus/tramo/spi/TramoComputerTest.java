/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramo.spi;

import demetra.arima.SarimaModel;
import demetra.data.Data;
import demetra.timeseries.regression.ModellingContext;
import demetra.timeseries.regression.modelling.GeneralLinearModel;
import demetra.tramo.TramoSpec;
import org.junit.Test;

/**
 *
 * @author palatej
 */
public class TramoComputerTest {
    
    public TramoComputerTest() {
    }

    @Test
    public void testBasic() {
        GeneralLinearModel<SarimaModel> rslt = demetra.tramo.Tramo.process(Data.TS_PROD, TramoSpec.TRfull, ModellingContext.getActiveContext(), null);
        System.out.println(rslt.getEstimation().getStatistics());
        System.out.println(rslt.getDescription().getStochasticComponent());
    }
    
}
