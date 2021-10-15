/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.arima.r;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class SarimaModelsTest {
    
    public SarimaModelsTest() {
    }

    @Test
    public void testRandom() {
        double[] rnd=SarimaModels.random(200, 12, new double[]{-.2, -.5}, 1, new double[]{-.5}, null, 1, new double[]{-.8}, 10);
        assertTrue(rnd.length == 200);
    }
    
}
