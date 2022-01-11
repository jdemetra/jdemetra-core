/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.timeseries.calendars;

import demetra.data.DoubleSeq;
import demetra.timeseries.calendars.HolidayPattern.Shape;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class HolidayPatternTest {

    public HolidayPatternTest() {
    }

    @Test
    public void testSimple() {
        HolidayPattern p = HolidayPattern.of(0, Shape.Constant, 10);
        double[] weights = p.getWeights();
        assertTrue(weights[0] == 1 && weights[9] == 1);
        p = HolidayPattern.of(0, Shape.LinearDown, 10);
        weights = p.getWeights();
        assertTrue(weights[0] == 1 && weights[9] != 0);
        p = HolidayPattern.of(0, Shape.LinearUp, 10);
        weights = p.getWeights();
        assertTrue(weights[0] != 0 && weights[9] == 1);
    }

    @Test
    public void testComplex() {
        HolidayPattern p = HolidayPattern.of(-15, Shape.LinearUp, 10, Shape.Constant, 10, Shape.LinearDown, 10);
        DoubleSeq w = DoubleSeq.of(p.getWeights());
//        System.out.println(w);
        assertTrue(w.count(q -> q == 1) == 12);
        p = HolidayPattern.of(-15, Shape.LinearUp, 10, Shape.Zero, 10, Shape.LinearDown, 10);
        w = DoubleSeq.of(p.getWeights());
//        System.out.println(w);
        assertTrue(w.count(q -> q == 1) == 2 && w.count(q -> q == 0) == 10);
    }

}
