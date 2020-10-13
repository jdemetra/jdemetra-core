/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x13.r;

import demetra.data.Data;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class RegArimaOutliersDetectionTest {
    
    public RegArimaOutliersDetectionTest() {
    }

    @Test
    public void testProd() {

        RegArimaOutliersDetection.Results rslts = RegArimaOutliersDetection.process(Data.TS_PROD, new int[]{0, 1, 1}, new int[]{0, 1, 1}, false, null, true, true, false, true, 4);
        String[] outliers = rslts.getData(RegArimaOutliersDetection.Results.BNAMES, String[].class);
//        if (outliers != null) {
//            for (int i = 0; i < outliers.length; ++i) {
//                System.out.println(outliers[i]);
//            }
//        }
        assertTrue(outliers == null);
    }
    
}
