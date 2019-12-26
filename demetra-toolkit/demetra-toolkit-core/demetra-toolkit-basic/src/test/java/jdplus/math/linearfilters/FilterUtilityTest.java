/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.linearfilters;

import demetra.data.Data;
import demetra.data.DoubleSeq;
import jdplus.data.analysis.DiscreteKernel;
import jdplus.stats.Kernels;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class FilterUtilityTest {

    public FilterUtilityTest() {
    }

    @Test
    public void testFiltering() {
        SymmetricFilter sf = LocalPolynomialFilters.of(6, 2, DiscreteKernel.biweight(6));
        IFiniteFilter[] afilters = AsymmetricFilters.mmsreFilters(sf, 0, new double[]{1}, null);
        DoubleSeq seq1 = FilterUtility.filter(DoubleSeq.of(Data.NILE), sf, afilters);
        IFiniteFilter[] lfilters = afilters.clone();
        for (int i = 0; i < lfilters.length; ++i) {
            lfilters[i] = lfilters[i].mirror();
        }
        DoubleSeq seq2 = FilterUtility.filter(DoubleSeq.of(Data.NILE), sf, lfilters, afilters);
        assertTrue(seq1.distance(seq2)<1e-9);
    }

    public static void main(String[] args){
        int h=11;
        DoubleSeq input = DoubleSeq.of(Data.NILE);
        int n=input.length();
        System.out.println(input);
        SymmetricFilter sf = LocalPolynomialFilters.of(h, 2, DiscreteKernel.henderson(h));
        IFiniteFilter[] afilters = AsymmetricFilters.mmsreFilters(sf, 0, new double[]{1}, null);
        double[] f = AsymmetricFilters.implicitForecasts(sf, afilters, input.range(n-h-1, n));
        System.out.println(DoubleSeq.of(f));
        
        afilters = AsymmetricFilters.mmsreFilters(sf, 0, new double[0], null);
        f = AsymmetricFilters.implicitForecasts(sf, afilters, input.range(n-h-1, n));
        System.out.println(DoubleSeq.of(f));
    }
}
