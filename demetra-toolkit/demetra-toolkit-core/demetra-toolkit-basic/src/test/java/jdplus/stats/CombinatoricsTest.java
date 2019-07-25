/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.stats;

import jdplus.maths.MathException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class CombinatoricsTest {

    public CombinatoricsTest() {
    }

    @Test
    public void testBinomial() {
        long c = Combinatorics.binomialCoefficient(50, 40);
        c = Combinatorics.binomialCoefficient(65, 40);
        boolean failed = false;
        try {
            c = Combinatorics.binomialCoefficient(90, 20);
        } catch (MathException err) {
            failed = true;
        }
        assertTrue(failed);
    }

}
