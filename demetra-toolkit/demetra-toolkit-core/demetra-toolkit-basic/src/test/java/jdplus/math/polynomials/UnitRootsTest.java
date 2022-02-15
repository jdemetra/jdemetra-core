/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package jdplus.math.polynomials;

import demetra.math.Complex;
import jdplus.data.analysis.Periodogram;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class UnitRootsTest {
    
    public UnitRootsTest() {
    }

    @Test
    public void testFrequency() {
        double f=Periodogram.getTradingDaysFrequencies(12)[0];
        Polynomial p=UnitRoots.forFrequency(f);
        Complex[] roots=p.roots();
        assertEquals(roots[0].absSquare(), 1, 1e-9);
        assertEquals(roots[0].arg(), f, 1e-9);
    }
    
}
