/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class PolynomialTypeTest {
    
    public PolynomialTypeTest() {
    }

    @Test
    public void testConstructor() {
        System.out.println(PolynomialType.of(1, .2, .5));
        System.out.println(PolynomialType.of(5));
    }
    
}
