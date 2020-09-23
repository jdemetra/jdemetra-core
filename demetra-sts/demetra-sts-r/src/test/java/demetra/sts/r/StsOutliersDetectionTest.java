/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.sts.r;

import demetra.data.Data;
import demetra.math.matrices.MatrixType;
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
        StsOutliersDetection.Results rslt = StsOutliersDetection.process(Data.insee()[0], 1, 1, 1, 1, "HarrisonStevens", null, 0, 0, "Score", "Point");
    }
    
}
