/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.information.InformationSet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jadoull
 */
public class ArimaSpecTest {
    
    public ArimaSpecTest() {
    }

    @Test
    public void testInformationSet() {
        ArimaSpec expected = new ArimaSpec();
        ArimaSpec actual = new ArimaSpec();
        InformationSet info;
        
        assertTrue(expected.isDefault());
        assertTrue(actual.isDefault());
        assertEquals(expected, actual);
        expected.setP(2);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(2, actual.getP());
        
        expected.setD(1);
        info = expected.write(true);
        actual.read(info);
        assertEquals(1, actual.getD());
        
        expected.setQ(1);
        info = expected.write(true);
        actual.read(info);
        assertEquals(1, actual.getQ());
        
        expected.setBP(1);
        info = expected.write(true);
        actual.read(info);
        assertEquals(1, actual.getBP());
        
        expected.setBD(1);
        info = expected.write(true);
        actual.read(info);
        assertEquals(1, actual.getBD());
        
        expected.setBQ(1);
        info = expected.write(true);
        actual.read(info);
        assertEquals(1, actual.getBQ());
    }
    
    @Test(expected = TramoException.class)
    public void testSetPUpperBound() {
        ArimaSpec spec = new ArimaSpec();
        spec.setP(5);
    }
    
    @Test(expected = TramoException.class)
    public void testSetDUpperBound() {
        ArimaSpec spec = new ArimaSpec();
        spec.setD(5);
    }
    
    @Test(expected = TramoException.class)
    public void testSetQUpperBound() {
        ArimaSpec spec = new ArimaSpec();
        spec.setQ(5);
    }
    
    @Test(expected = TramoException.class)
    public void testSetBPUpperBound() {
        ArimaSpec spec = new ArimaSpec();
        spec.setBP(5);
    }
    
    @Test(expected = TramoException.class)
    public void testSetBDUpperBound() {
        ArimaSpec spec = new ArimaSpec();
        spec.setBD(5);
    }
    
    @Test(expected = TramoException.class)
    public void testSetBQUpperBound() {
        ArimaSpec spec = new ArimaSpec();
        spec.setBQ(5);
    }
}
