/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.highfreq.r;

import demetra.data.DoubleSeq;
import demetra.data.WeeklyData;
import org.junit.Test;
import static org.junit.Assert.*;
import demetra.highfreq.FractionalAirlineDecomposition;
import demetra.highfreq.FractionalAirlineEstimation;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class FractionalAirlineProcessorTest {

    public FractionalAirlineProcessorTest() {
    }

    @Test
    public void testWeeklyDecomp() {
        DoubleSeq y=DoubleSeq.of(WeeklyData.US_CLAIMS2).log();
        FractionalAirlineDecomposition rslt = FractionalAirlineProcessor.decompose(y.toArray(), 365.25/7, false, false, true);
//        System.out.println(DoubleSeq.of(rslt.getT()));
//        System.out.println(DoubleSeq.of(rslt.getS()));
//        System.out.println(DoubleSeq.of(rslt.getI()));
//        System.out.println(DoubleSeq.of(rslt.getStdeT()));
//        System.out.println(DoubleSeq.of(rslt.getStdeS()));
//        System.out.println(DoubleSeq.of(rslt.getStdeI()));
        assertTrue(null != rslt.getData("sa", double[].class));
    }
    
   @Test
    public void testWeeklyEstimation() {
        FractionalAirlineEstimation rslt = FractionalAirlineProcessor.estimate(WeeklyData.US_CLAIMS2, null, false, new double[]{365.25/7}, new String[]{"ao", "wo"}, 5, 1e-12, true);
        System.out.println(rslt.getLikelihood());
        System.out.println();
    }

}
