/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.sts.r;

import demetra.data.Data;
import demetra.math.matrices.MatrixType;
import demetra.sts.BasicStructuralModel;
import demetra.sts.BsmEstimation;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import static org.junit.Assert.*;
import org.junit.Test;

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
        MatrixType fcast = Bsm.forecast(s, "none", 24);
        System.out.println(fcast.column(0));
        System.out.println(fcast.column(1));
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
    public void testEstimation() {
        TsData s = TsData.ofInternal(TsPeriod.monthly(1992,1), Data.RETAIL_BOOKSTORES);
        BasicStructuralModel bsm = Bsm.process(s, null, 1, 1, -1, 1, "Trigonometric", 1e-12);
        byte[] bytes = Bsm.toBuffer(bsm);
        assertTrue(bytes != null);
    }

}
