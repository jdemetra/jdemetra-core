/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.r;

import demetra.data.Data;
import demetra.data.DoubleSequence;
import demetra.maths.MatrixType;
import static demetra.r.TimeVaryingRegressionTest.FURNITURE;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsData;
import static demetra.timeseries.simplets.TsDataToolkit.log;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class MovingRegressionTest {
    
    public MovingRegressionTest() {
    }

    @Test
    public void testSomeMethod() {
        TsData s=TsData.of(TsPeriod.monthly(1982, 4), DoubleSequence.ofInternal(Data.ABS_RETAIL));
//        long t0=System.currentTimeMillis();
        MovingRegression.Results regarima = MovingRegression.regarima(log(s), "TD7", 10);
//        System.out.println(regarima.getData("tdeffect", TsData.class));
//        System.out.println(regarima.getData("coefficients", MatrixType.class));
    }
    
}
