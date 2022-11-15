/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.modelling.arima.x13;

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
    public void testSetPUpperBound() {
        assertThatExceptionOfType(X13Exception.class).isThrownBy(() -> new ArimaSpec().setP(7));
    }

    @Test
    public void testSetDUpperBound() {
        assertThatExceptionOfType(X13Exception.class).isThrownBy(() -> new ArimaSpec().setD(3));
    }

    @Test
    public void testSetQUpperBound() {
        assertThatExceptionOfType(X13Exception.class).isThrownBy(() -> new ArimaSpec().setQ(8));
    }

    @Test
    public void testSetBPUpperBound() {
        assertThatExceptionOfType(X13Exception.class).isThrownBy(() -> new ArimaSpec().setBP(2));
    }

    @Test
    public void testSetBDUpperBound() {
        assertThatExceptionOfType(X13Exception.class).isThrownBy(() -> new ArimaSpec().setBD(3));
    }
    
    @Test
    public void testSetBQUpperBound() {
        assertThatExceptionOfType(X13Exception.class).isThrownBy(() -> new ArimaSpec().setBQ(3));
    }
}
