/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.stats.tests;

import demetra.data.DoubleSeq;
import demetra.stats.StatisticalTest;
import jdplus.stats.tests.AdvancedOneWayAnova.Group;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class AdvancedOneWayAnovaTest {

    public static final double[] G1
            = {4, 6, 9, 12, 16, 15, 14, 12, 12, 8, 13, 9, 12, 12, 12, 10, 8, 12, 11, 8, 7, 9},
            G2 = {7, 7, 12, 10, 16, 15, 9, 8, 13, 12, 7, 6, 8, 9, 9, 8, 9, 13, 10, 8, 8, 10},
            G3 = {11, 7, 4, 7, 7, 6, 11, 14, 13, 9, 12, 13, 4, 13, 6, 12, 6, 11, 14, 8, 5, 8};

    public AdvancedOneWayAnovaTest() {
    }

    @Test
    public void testModel() {
        AdvancedOneWayAnova anova=new AdvancedOneWayAnova();
        anova.add(new Group("G1", DoubleSeq.of(G1)));
        anova.add(new Group("G2", DoubleSeq.of(G2)));
        anova.add(new Group("G3", DoubleSeq.of(G3)));
        
        StatisticalTest test = anova.build();
        assertEquals(test.getValue(), 1.13, 1e-2);
        assertEquals(test.getPvalue(), 0.3288, 1e-4);
        assertEquals(anova.getR2(), 0.034696, 1e-5);
        assertEquals(anova.rmse(), 3.0144, 1e-4);
        assertEquals(anova.cv(), .3079723, 1e-7);
    }

}
