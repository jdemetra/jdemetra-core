/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.maths.linearfilters;

import demetra.data.DoubleSeq;
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
            FiniteFilter mf = AsymmetricFilters.musgraveFilter(HendersonFilters.ofLength(23), q, 4 / (Math.PI * 3.5 * 3.5));
            FiniteFilter af = AsymmetricFilters.mmsreFilter(HendersonFilters.ofLength(23), q, 0, new double[]{2 / (Math.sqrt(Math.PI) * 3.5)}, null);
            assertTrue(IFiniteFilter.equals(mf, af, 1e-15));
        }
    }
    
    public static void main(String[] args) {
        SymmetricFilter H = HendersonFilters.ofLength(23);
        for (int q = 0; q <= 11; ++q) {
            FiniteFilter af = AsymmetricFilters.mmsreFilter(H, q, 0, new double[0], null);
            System.out.println(DoubleSeq.of(af.weightsToArray()));
        }
        for (int q = 0; q <= 11; ++q) {
            FiniteFilter af = AsymmetricFilters.mmsreFilter(H, q, 0, new double[]{2 / (Math.sqrt(Math.PI) * 3.5)}, null);
            System.out.println(DoubleSeq.of(af.weightsToArray()));
        }
        for (int q = 0; q <= 11; ++q) {
            FiniteFilter af = AsymmetricFilters.mmsreFilter(H, q, 0, new double[]{2 / (Math.sqrt(Math.PI) * 4.5)}, null);
            System.out.println(DoubleSeq.of(af.weightsToArray()));
        }
    }
}
