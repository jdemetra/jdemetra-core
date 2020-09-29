/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.sts.r;

import demetra.data.Data;
import demetra.math.matrices.MatrixType;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class StsOutliersDetectionTest {
    
    public StsOutliersDetectionTest() {
    }

    @Test
    public void testSomeMethod() {
        TsData y=TsData.ofInternal(TsPeriod.monthly(1992,1), Data.RETAIL_BOOKSTORES);
        StsOutliersDetection.Results rslt = StsOutliersDetection.process(y, 1, 1, -1, 1, "Trigonometric", null, 0, 0, "Score", "Point");
    }
    
}
