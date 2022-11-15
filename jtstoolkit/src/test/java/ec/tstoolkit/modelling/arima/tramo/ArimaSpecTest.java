/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.information.InformationSet;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
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
        boolean verbose=false;
        ArimaSpec expected = new ArimaSpec();
        ArimaSpec actual = new ArimaSpec();
        InformationSet info;
        
        assertTrue(expected.isDefault());
        assertTrue(actual.isDefault());
        assertEquals(expected, actual);
        expected.setP(2);
        assertNotEquals(expected, actual);
        info = expected.write(verbose);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(2, actual.getP());
        
        expected.setD(1);
        info = expected.write(verbose);
        actual.read(info);
        assertEquals(1, actual.getD());
        
        expected.setQ(1);
        info = expected.write(verbose);
        actual.read(info);
        assertEquals(1, actual.getQ());
        
        expected.setBP(1);
        info = expected.write(verbose);
        actual.read(info);
        assertEquals(1, actual.getBP());
        
        expected.setBD(1);
        info = expected.write(verbose);
        actual.read(info);
        assertEquals(1, actual.getBD());
        
        expected.setBQ(1);
        info = expected.write(verbose);
        actual.read(info);
        assertEquals(1, actual.getBQ());
    }
    
    @Test
    public void testSetPUpperBound() {
        assertThatExceptionOfType(TramoException.class).isThrownBy(() -> new ArimaSpec().setP(5));
    }

    @Test
    public void testSetDUpperBound() {
        assertThatExceptionOfType(TramoException.class).isThrownBy(() -> new ArimaSpec().setD(5));
    }

    @Test
    public void testSetQUpperBound() {
        assertThatExceptionOfType(TramoException.class).isThrownBy(() ->new ArimaSpec().setQ(5) );
    }

    @Test
    public void testSetBPUpperBound() {
        assertThatExceptionOfType(TramoException.class).isThrownBy(() -> new ArimaSpec().setBP(5));
    }

    @Test
    public void testSetBDUpperBound() {
        assertThatExceptionOfType(TramoException.class).isThrownBy(() -> new ArimaSpec().setBD(5));
    }

    @Test
    public void testSetBQUpperBound() {
        assertThatExceptionOfType(TramoException.class).isThrownBy(() -> new ArimaSpec().setBQ(5));
    }
}
