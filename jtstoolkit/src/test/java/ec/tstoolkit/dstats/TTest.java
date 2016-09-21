/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.dstats;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class TTest {
    
    public TTest() {
    }

    @Test
    public void testProbinverse() {
        T dist = new T();
        dist.setDegreesofFreedom(20);
        double step = 0.001;
        for (double p = step; p <= 1 - step; p += step) {
            double x = dist.getProbabilityInverse(p, ProbabilityType.Lower);
            double np = dist.getProbability(x, ProbabilityType.Lower);
            double nx = dist.getProbabilityInverse(np, ProbabilityType.Lower);
            assertTrue(Math.abs(x-nx) <1e-6);
        }
    }
    
}
