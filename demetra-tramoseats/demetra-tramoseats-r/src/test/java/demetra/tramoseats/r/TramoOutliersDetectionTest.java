/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramoseats.r;

import demetra.data.Data;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class TramoOutliersDetectionTest {

    public TramoOutliersDetectionTest() {
    }

    @Test
    public void testProd() {

        TramoOutliersDetection.Results rslts = TramoOutliersDetection.process(Data.TS_PROD, new int[]{0, 1, 1}, new int[]{0, 1, 1}, false, null, true, true, false, true, 4, false);
        String[] outliers = rslts.getData(TramoOutliersDetection.Results.BNAMES, String[].class);
//        if (outliers != null) {
//            for (int i = 0; i < outliers.length; ++i) {
//                System.out.println(outliers[i]);
//            }
//        }
        assertTrue(outliers == null);
    }

}
