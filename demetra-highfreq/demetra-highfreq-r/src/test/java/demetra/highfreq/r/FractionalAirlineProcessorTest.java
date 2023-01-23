/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.highfreq.r;

import demetra.data.DoubleSeq;
import demetra.data.MatrixSerializer;
import demetra.data.WeeklyData;
import jdplus.highfreq.LightExtendedAirlineDecomposition;
import jdplus.highfreq.ExtendedAirlineEstimation;
import demetra.math.matrices.Matrix;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import jdplus.highfreq.ExtendedAirlineMapping;
import jdplus.ssf.extractors.SsfUcarimaEstimation;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

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
        LightExtendedAirlineDecomposition rslt = FractionalAirlineProcessor.decompose(y.toArray(), 52, false, true, 10, 53);
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
        ExtendedAirlineEstimation rslt = FractionalAirlineProcessor.estimate(WeeklyData.US_CLAIMS2, null, false, new double[]{365.25 / 7}, -1, false, new String[]{"ao", "wo"}, 5, 1e-12, true);
//        System.out.println(rslt.getLikelihood());
//        System.out.println();
    }

    @Test
    public void testComponentEstimation() {
        double[] data = new double[2 * WeeklyData.US_CLAIMS2.length];
        data[2] = 1;
        data[WeeklyData.US_CLAIMS2.length] = 1;
    
        Matrix x = Matrix.of(data, WeeklyData.US_CLAIMS2.length, 2);
        ExtendedAirlineEstimation rslt = FractionalAirlineProcessor.estimate(WeeklyData.US_CLAIMS2, x, true, new double[]{365.25 / 7}, -1, false, new String[]{"ao", "wo", "ls"}, 5, 1e-12, true);

        for (int i = 0; i < rslt.component_ls().length; i++) {
           boolean comp_out=Math.abs(rslt.component_ls()[i] + rslt.component_ao()[i] + rslt.component_wo()[i] - rslt.component_outliers()[i]) > 0.00000001;
            if (comp_out) {
                System.out.println("Fehler");
                  assertTrue(comp_out, "The outlier componets don't");
            }
        }
    //    System.out.println("Fertig_ Outlier");

        for (int i = 0; i < rslt.component_ls().length; i++) {
            boolean comp=Math.abs(WeeklyData.US_CLAIMS2[i] -  rslt.component_outliers()[i]- rslt.component_userdef_reg_variables()[i] - rslt.linearized()[i]) > 0.00000001;
            if (comp) {
                System.out.println("Fehler: " + i);
                assertTrue(false, "The componets don't sum up to the lin");
            }
        }
    //    System.out.println("Fertig Lin");

    //       System.out.println("y");
    //       System.out.println(Arrays.toString(WeeklyData.US_CLAIMS2));
        
     
//        System.out.println();
    }

    @Test
    public void testWeeklySsf() {
        LightExtendedAirlineDecomposition rslt = FractionalAirlineProcessor.decompose(WeeklyData.US_CLAIMS2, new double[]{365.25 / 7}, -1, false, true, 7, 7);
        SsfUcarimaEstimation details = FractionalAirlineProcessor.ssfDetails(rslt);
        assertTrue(null != details.getData("smoothing.states", Matrix.class));
    }

//    final static DoubleSeq EDF;
//
//    static {
//        DoubleSeq y;
//        try {
//            InputStream stream = ExtendedAirlineMapping.class.getResourceAsStream("/edf.txt");
//            Matrix edf = MatrixSerializer.read(stream);
//            y = edf.column(0);
//        } catch (IOException ex) {
//            y = null;
//        }
//        EDF = y;
//    }
//    
//    @Test
//    public void testRandom() {
//        DoubleSeq y = EDF;
//        double[] rnd = FractionalAirlineProcessor.random(new double[]{7, 365.25}, .1, new double[]{.7, .85}, false, 2000, y.range(0,374).log().toArray(), .01, 0);
//        System.out.println(DoubleSeq.of(rnd));
//    }
//    
}
