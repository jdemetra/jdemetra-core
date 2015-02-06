/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ec.tstoolkit.stats;

import data.DataCochran;
import ec.satoolkit.DecompositionMode;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Christiane Hofer
 */
public class CochranTestTest {
     
    @Test
    public void TestCochranTestStartJan() {
     CochranTest cochranTestTestStartJan;
        System.out.println("Cochran Test");
      
        DecompositionMode mode = DecompositionMode.Additive;

        cochranTestTestStartJan = new CochranTest(DataCochran.CStartJan, mode);
        cochranTestTestStartJan.calcCochranTest();
        assertEquals(true, cochranTestTestStartJan.getTestResult());
        System.out.println("Testvalue: " + cochranTestTestStartJan.getTestValue());
        System.out.println("Criticalvalue: " + cochranTestTestStartJan.getCriticalValue());

        for (int i = 0; i <= cochranTestTestStartJan.getS().length - 1; i++) {
            System.out.println("Standarddeviation of period(" + i + ") = " + cochranTestTestStartJan.getS()[i]);
        }
        assertEquals(3535.889, cochranTestTestStartJan.getS()[0], 0.0001);
        assertEquals(4465.5955, cochranTestTestStartJan.getS()[1], 0.0001);
        assertEquals(9870.1095, cochranTestTestStartJan.getS()[2], 0.0001);
        assertEquals(13583.4685, cochranTestTestStartJan.getS()[3], 0.0001);
        assertEquals(13937.615, cochranTestTestStartJan.getS()[4], 0.0001);
        assertEquals(15341.996, cochranTestTestStartJan.getS()[5], 0.0001);
        assertEquals(16068.224, cochranTestTestStartJan.getS()[6], 0.0001);
        assertEquals(14279.8785, cochranTestTestStartJan.getS()[7], 0.0001);
        assertEquals(17062.8645, cochranTestTestStartJan.getS()[8], 0.0001);
        assertEquals(16839.1775, cochranTestTestStartJan.getS()[9], 0.0001);
        assertEquals(15099.7847, cochranTestTestStartJan.getS()[10], 0.0001);
        assertEquals(6722.4789, cochranTestTestStartJan.getS()[11], 0.0001);

        assertEquals(0.116226, cochranTestTestStartJan.getTestValue(), 0.000001);
        assertEquals(0.166, cochranTestTestStartJan.getCriticalValue(), 0.000001);
        assertEquals(true, cochranTestTestStartJan.getTestResult());
    }

        @Test
    public void TestCochranTestStartAprl() {
        System.out.println("Cochran Test");
           CochranTest cochranTestTestStartApril;
        DecompositionMode mode = DecompositionMode.Additive;

        cochranTestTestStartApril = new CochranTest(DataCochran.CStartAprl, mode);
        cochranTestTestStartApril.calcCochranTest();
        assertEquals(true, cochranTestTestStartApril.getTestResult());
        System.out.println("Testvalue: " + cochranTestTestStartApril.getTestValue());
        System.out.println("Criticalvalue: " + cochranTestTestStartApril.getCriticalValue());

        for (int i = 0; i <= cochranTestTestStartApril.getS().length - 1; i++) {
            System.out.println("Standarddeviation of period(" + i + ") = " + cochranTestTestStartApril.getS()[i]);
        }
        assertEquals(16839.1775, cochranTestTestStartApril.getS()[0], 0.0001);
        assertEquals(15099.7847, cochranTestTestStartApril.getS()[1], 0.0001);
        assertEquals(6722.4789, cochranTestTestStartApril.getS()[2], 0.0001);        
        assertEquals(3535.889, cochranTestTestStartApril.getS()[3], 0.0001);
        assertEquals(4465.5955, cochranTestTestStartApril.getS()[4], 0.0001);
        assertEquals(9870.1095, cochranTestTestStartApril.getS()[5], 0.0001);
        assertEquals(13583.4685, cochranTestTestStartApril.getS()[6], 0.0001);
        assertEquals(13937.615, cochranTestTestStartApril.getS()[7], 0.0001);
        assertEquals(15341.996, cochranTestTestStartApril.getS()[8], 0.0001);
        assertEquals(16068.224, cochranTestTestStartApril.getS()[9], 0.0001);
        assertEquals(14279.8785, cochranTestTestStartApril.getS()[10], 0.0001);
        assertEquals(17062.8645, cochranTestTestStartApril.getS()[11], 0.0001);


        assertEquals(0.116226, cochranTestTestStartApril.getTestValue(), 0.000001);
        assertEquals(0.166, cochranTestTestStartApril.getCriticalValue(), 0.000001);
        assertEquals(true, cochranTestTestStartApril.getTestResult());
     
    }
    
}
