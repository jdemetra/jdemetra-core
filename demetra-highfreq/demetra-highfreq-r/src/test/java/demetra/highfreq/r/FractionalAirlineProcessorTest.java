/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.highfreq.r;

import demetra.data.DoubleSeq;
import demetra.data.WeeklyData;
import demetra.highfreq.FractionalAirlineDecomposition;
import demetra.highfreq.FractionalAirlineEstimation;
import demetra.math.matrices.Matrix;
import jdplus.ssf.extractors.SsfUcarimaEstimation;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class FractionalAirlineProcessorTest {

    public FractionalAirlineProcessorTest() {
    }

    @Test
    public void testWeeklyDecomp() {
        DoubleSeq y = DoubleSeq.of(WeeklyData.US_CLAIMS2).log();
        FractionalAirlineDecomposition rslt = FractionalAirlineProcessor.decompose(y.toArray(), 52, false, true, 10, 53);
//        System.out.println(rslt.component("t").getData());
//        System.out.println(rslt.component("s").getData());
//        System.out.println(rslt.component("i").getData());
//        System.out.println(rslt.component("t").getStde());
//        System.out.println(rslt.component("s").getStde());
//        System.out.println(rslt.component("i").getStde());
        assertTrue(null != rslt.getData("sa", double[].class));
    }

    @Test
    public void testWeeklyEstimation() {
        FractionalAirlineEstimation rslt = FractionalAirlineProcessor.estimate(WeeklyData.US_CLAIMS2, null, false, new double[]{365.25 / 7}, -1, false, new String[]{"ao", "wo"}, 5, 1e-12, true);
//        System.out.println(rslt.getLikelihood());
//        System.out.println();
    }

    @Test
    public void testWeeklySsf() {
        FractionalAirlineDecomposition rslt = FractionalAirlineProcessor.decompose(WeeklyData.US_CLAIMS2, new double[]{365.25 / 7}, -1, false, true, 7, 7);
        SsfUcarimaEstimation details = FractionalAirlineProcessor.ssfDetails(rslt);
        assertTrue(null != details.getData("smoothing.states", Matrix.class));
    }
}
