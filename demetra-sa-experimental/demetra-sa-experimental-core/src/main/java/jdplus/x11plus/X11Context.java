/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x11plus;

import jdplus.data.DataBlock;
import demetra.sa.DecompositionMode;
import demetra.data.DoubleSeq;
import jdplus.filters.IFiltering;
import jdplus.filters.LocalPolynomialFilterFactory;
import jdplus.filters.LocalPolynomialFilterSpec;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
@lombok.Builder(builderClassName="Builder")
public class X11Context {

    @lombok.NonNull
    DecompositionMode mode;
    @lombok.NonNull
    Number period;
    double lowerSigma, upperSigma;
    
    IFiltering trendFiltering;
    IFiltering initialSeasonalFiltering, finalSeasonalFiltering;
    

    public static Builder builder() {
        Builder builder = new Builder();
        builder.mode = DecompositionMode.Multiplicative;
        builder.trendFiltering=LocalPolynomialFilterFactory.of(new LocalPolynomialFilterSpec());
        
        builder.lowerSigma = 1.5;
        builder.upperSigma = 2.5;
        return builder;
    }

    public DoubleSeq remove(DoubleSeq l, DoubleSeq r) {
        if (mode == DecompositionMode.Multiplicative || mode == DecompositionMode.PseudoAdditive) {
            return DoubleSeq.onMapping(l.length(), i -> l.get(i) / r.get(i));
        } else {
            return DoubleSeq.onMapping(l.length(), i -> l.get(i) - r.get(i));
        }
    }

    public DoubleSeq add(DoubleSeq l, DoubleSeq r) {
        if (mode == DecompositionMode.Multiplicative || mode == DecompositionMode.PseudoAdditive) {
            return DoubleSeq.onMapping(l.length(), i -> l.get(i) * r.get(i));
        } else {
            return DoubleSeq.onMapping(l.length(), i -> l.get(i) + r.get(i));
        }
    }

    public void remove(DoubleSeq l, DoubleSeq r, DataBlock q) {
        if (mode == DecompositionMode.Multiplicative || mode == DecompositionMode.PseudoAdditive) {
            q.set(l, r, (x, y) -> x / y);
        } else {
            q.set(l, r, (x, y) -> x - y);
        }
    }

    public void add(DoubleSeq l, DoubleSeq r, DataBlock q) {
        if (mode == DecompositionMode.Multiplicative || mode == DecompositionMode.PseudoAdditive) {
            q.set(l, r, (x, y) -> x * y);
        } else {
            q.set(l, r, (x, y) -> x + y);
        }
    }
}
