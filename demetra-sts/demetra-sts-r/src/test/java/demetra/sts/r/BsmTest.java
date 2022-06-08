/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.sts.r;

import demetra.data.Data;
import demetra.modelling.r.Variables;
import jdplus.sts.BasicStructuralModel;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import demetra.math.matrices.Matrix;

/**
 *
 * @author PALATEJ
 */
public class BsmTest {

    public BsmTest() {
    }

    @Test
    public void testForecasts() {
        TsData s = TsData.ofInternal(TsPeriod.monthly(1992,1), Data.RETAIL_BOOKSTORES);
        Matrix fcast = Bsm.forecast(s, "none", 24);
//        System.out.println(fcast.column(0));
//        System.out.println(fcast.column(1));
        fcast = Bsm.forecast(s, "td2", 24);
//        System.out.println(fcast.column(0));
//        System.out.println(fcast.column(1));
        fcast = Bsm.forecast(s, "td3", 24);
//        System.out.println(fcast.column(0));
//        System.out.println(fcast.column(1));
        fcast = Bsm.forecast(s, "td7", 24);
//        System.out.println(fcast.column(0));
//        System.out.println(fcast.column(1));
        fcast = Bsm.forecast(s, "full", 24);
//        System.out.println(fcast.column(0));
//        System.out.println(fcast.column(1));
    }

    @Test
    public void testYearly() {
        TsData s = TsData.ofInternal(TsPeriod.yearly(1992), Data.RETAIL_BOOKSTORES);
        Matrix fcast = Bsm.forecast(s, "full", 24);
        assertTrue(fcast.getRowsCount() == 24);
//        System.out.println(fcast.column(0));
//        System.out.println(fcast.column(1));
    }
    
    @Test
    public void testEstimation() {
        TsData s = TsData.ofInternal(TsPeriod.monthly(1992,1), Data.RETAIL_BOOKSTORES);
        BasicStructuralModel bsm = Bsm.process(s, null, 1, 1, -1, 1, "Trigonometric", true, 1e-12);
        byte[] bytes = Bsm.toBuffer(bsm);
        assertTrue(bytes != null);
    }

    @Test
    public void testEstimationWithRegs() {
         TsData s = TsData.ofInternal(TsPeriod.monthly(1992,1), Data.RETAIL_BOOKSTORES);
        Matrix td = Variables.td(s.getDomain(), new int[]{1,1,1,1,2,3,0}, true);
        BasicStructuralModel bsm = Bsm.process(s, td, 1, 1, -1, 1, "Trigonometric", false, 1e-12);
        byte[] bytes = Bsm.toBuffer(bsm);
        assertTrue(bytes != null);
    }
}
