/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.modelling.arima.x13;

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

    @Test(expected = X13Exception.class)
    public void testSetPUpperBound() {
        ArimaSpec expected = new ArimaSpec();
        expected.setP(7);
    }
    
    @Test(expected = X13Exception.class)
    public void testSetDUpperBound() {
        ArimaSpec expected = new ArimaSpec();
        expected.setD(3);
    }
    
    @Test(expected = X13Exception.class)
    public void testSetQUpperBound() {
        ArimaSpec expected = new ArimaSpec();
        expected.setQ(8);
    }
    
    @Test(expected = X13Exception.class)
    public void testSetBPUpperBound() {
        ArimaSpec expected = new ArimaSpec();
        expected.setBP(2);
    }
    
    @Test(expected = X13Exception.class)
    public void testSetBDUpperBound() {
        ArimaSpec expected = new ArimaSpec();
        expected.setBD(3);
    }
    
    @Test(expected = X13Exception.class)
    public void testSetBQUpperBound() {
        ArimaSpec expected = new ArimaSpec();
        expected.setBQ(3);
    }
}
