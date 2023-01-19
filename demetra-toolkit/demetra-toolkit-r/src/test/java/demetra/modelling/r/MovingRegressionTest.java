/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.r;

import demetra.modelling.r.MovingRegression;
import demetra.data.Data;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsData;
import static jdplus.timeseries.simplets.TsDataToolkit.log;

import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class MovingRegressionTest {
    
    public MovingRegressionTest() {
    }

    @Test
    public void testSomeMethod() {
        TsData s=TsData.ofInternal(TsPeriod.monthly(1982, 4), Data.ABS_RETAIL);
//        long t0=System.currentTimeMillis();
        MovingRegression.Results regarima = MovingRegression.regarima(log(s), "TD7", 10);
//        System.out.println(regarima.getData("tdeffect", TsData.class));
//        System.out.println(regarima.getData("coefficients", MatrixType.class));
    }
    
}
