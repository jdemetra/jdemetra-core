/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.sts.r;

import demetra.data.Data;
import demetra.math.matrices.MatrixType;
import demetra.timeseries.TsData;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class BsmTest {
    
    public BsmTest() {
    }

    @Test
    public void testForecasts() {
        TsData s=Data.TS_PROD;
        MatrixType fcast = Bsm.forecast(s, "none", 24);
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
    
}
