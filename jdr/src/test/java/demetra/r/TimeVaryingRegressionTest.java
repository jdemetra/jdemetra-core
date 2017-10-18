/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.r;

import demetra.data.Data;
import demetra.maths.matrices.Matrix;
import static demetra.timeseries.simplets.TsDataToolkit.log;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class TimeVaryingRegressionTest {
    
    public TimeVaryingRegressionTest() {
    }

    @Test
    public void testTD() {
        Matrix m = TimeVaryingRegression.regarima(log(Data.TS_PROD), "TD3");
        System.out.println(m);
    }
    
}
