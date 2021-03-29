/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x13.r;

import demetra.data.Data;
import demetra.x13.X13Spec;
import demetra.x13.io.protobuf.X13Output;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class X13Test {
    
    public X13Test() {
    }

    @Test
    public void testProd() {

        X13Output rslt = X13.fullProcess(Data.TS_PROD, "rsa0");
//        if (outliers != null) {
//            for (int i = 0; i < outliers.length; ++i) {
//                System.out.println(outliers[i]);
//            }
//        }
        byte[] sbytes = X13.toBuffer(rslt.getEstimationSpec());
        X13Spec spec = X13.specOf(sbytes);
        
        assertTrue(spec != null);
    }
    
}
