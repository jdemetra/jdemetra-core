/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.satoolkit.benchmarking;

import ec.benchmarking.simplets.TsCholette;
import ec.tstoolkit.information.InformationSet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jadoull
 */
public class SaBenchmarkingSpecTest {
    
    public SaBenchmarkingSpecTest() {
    }

    @Test
    public void testInformationSet() {
        SaBenchmarkingSpec expected = new SaBenchmarkingSpec();
        SaBenchmarkingSpec actual = new SaBenchmarkingSpec();
        InformationSet info;
        assertEquals(expected, actual);
        
        assertFalse(expected.isEnabled());
        expected.setEnabled(true);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertTrue(actual.isEnabled());
        
        expected.setTarget(SaBenchmarkingSpec.Target.Original);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(SaBenchmarkingSpec.Target.Original,actual.getTarget());
        
        expected.setRho(3.2);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(3.2, actual.getRho(),.0);
        
        expected.setLambda(2.5);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(2.5, actual.getLambda(),.0);
        
        expected.setBias(TsCholette.BiasCorrection.Multiplicative);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(TsCholette.BiasCorrection.Multiplicative, actual.getBias());

        expected.useForecast(true);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertTrue(actual.isUsingForecast());
        
    }
    
}
