/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.highfreq.r;

import demetra.data.MatrixSerializer;
import demetra.data.WeeklyData;
import java.io.File;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;
import demetra.data.DoubleSeq;
import demetra.highfreq.FractionalAirlineDecomposition;
import demetra.highfreq.FractionalAirlineEstimation;
import demetra.math.matrices.MatrixType;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class FractionalAirlineProcessorTest {

    public FractionalAirlineProcessorTest() {
    }

    @Test
    public void testWeeklyDecomp() {
        FractionalAirlineDecomposition rslt = FractionalAirlineProcessor.decompose(WeeklyData.US_CLAIMS2, 59, true, false);
        assertTrue(null != rslt.getData("sa", double[].class));
    }
    
   @Test
    public void testWeeklyEstimation() {
        FractionalAirlineEstimation rslt = FractionalAirlineProcessor.estimate(WeeklyData.US_CLAIMS2, null, false, new double[]{365.25/7}, new String[]{"ao", "wo"}, 5, 1e-12, true);
        System.out.println(rslt.getLikelihood());
        System.out.println();
    }
    

}
