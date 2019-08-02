/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.maths.linearfilters;

import java.util.function.IntToDoubleFunction;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class AsymmetricFilters {
    public FiniteFilter cutAndNormalizeFilter(final SymmetricFilter s, final int q) {
        IntToDoubleFunction weights = s.weights();
        int l = s.getLowerBound();
        double[] w = new double[q - l + 1];
        double n = 0;
        for (int i = 0; i < w.length; ++i) {
            w[i] = weights.applyAsDouble(l + i);
            n += w[i];
        }
        for (int i = 0; i < w.length; ++i) {
            w[i] /= n;
        }
        return FiniteFilter.ofInternal(w, l);
    }

    
}
