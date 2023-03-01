/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package jdplus.x11;

import demetra.data.Data;
import demetra.data.DoubleSeq;
import jdplus.data.analysis.DiscreteKernel;
import jdplus.math.linearfilters.LocalPolynomialFilters;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author PALATEJ
 */
public class TrendCrossValidationTest {

    public TrendCrossValidationTest() {
    }

    @Test
    public void testProd() {
        double[] cv = TrendCrossValidation.process(DoubleSeq.of(Data.ABS_RETAIL), 12, true, 3, 25, h -> LocalPolynomialFilters.of(h, 3, DiscreteKernel.henderson(h)));
//        long t0 = System.currentTimeMillis();
//        for (int i = 0; i < 1000; ++i) {
//            cv = TrendCrossValidation.process(DoubleSeq.of(Data.ABS_RETAIL), 12, true, 3, 25, h -> LocalPolynomialFilters.of(h, 3, DiscreteKernel.biweight(h)));
//        }
//        long t1 = System.currentTimeMillis();
//        System.out.println(t1 - t0);
//        for (int i = 0; i < cv.length; ++i) {
//            System.out.println(cv[i]);
//        }
        assertTrue(cv != null);

    }

}
