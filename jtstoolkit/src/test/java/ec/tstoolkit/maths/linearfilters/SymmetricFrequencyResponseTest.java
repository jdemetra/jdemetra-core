/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.maths.linearfilters;

import ec.tstoolkit.maths.matrices.Matrix;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class SymmetricFrequencyResponseTest {
    
    public SymmetricFrequencyResponseTest() {
    }

    @Test
    public void testTransform() {
        Matrix t = SymmetricFrequencyResponse.transform(100);
        assertTrue(t.get(99, 99) != 0);
        Matrix l = SymmetricFrequencyResponse.ltransform(53);
        Matrix d = SymmetricFrequencyResponse.dtransform(53);
        assertTrue(l.equals(d, 1e-18));
    }
}