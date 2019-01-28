/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramo;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class TramoSpecTest {

    public TramoSpecTest() {
    }

    @Test
    public void testClone() {
        assertTrue(TramoSpec.TR0.equals(TramoSpec.TR0.clone()));
        assertTrue(TramoSpec.TR1.equals(TramoSpec.TR1.clone()));
        assertTrue(TramoSpec.TR2.equals(TramoSpec.TR2.clone()));
        assertTrue(TramoSpec.TR3.equals(TramoSpec.TR3.clone()));
        assertTrue(TramoSpec.TR4.equals(TramoSpec.TR4.clone()));
        assertTrue(TramoSpec.TR5.equals(TramoSpec.TR5.clone()));
        assertTrue(TramoSpec.TRfull.equals(TramoSpec.TRfull.clone()));
    }

}
