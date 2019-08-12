/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.maths.linearfilters;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class AsymmetricFiltersTest {

    public AsymmetricFiltersTest() {
    }

    @Test
    public void testMusgrave() {
        for (int q = 0; q < 6; ++q) {
            FiniteFilter mf = AsymmetricFilters.musgraveFilter(HendersonFilters.ofLength(13), q, 4 / (Math.PI * 3.5 * 3.5));
            FiniteFilter af = AsymmetricFilters.mmsreFilter(HendersonFilters.ofLength(13), q, 0, new double[]{2 / (Math.sqrt(Math.PI) * 3.5)}, null);
            assertTrue(IFiniteFilter.equals(mf, af, 1e-15));
        }
    }

}
