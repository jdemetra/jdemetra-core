/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x11plus;

import jdplus.data.DataBlock;
import jdplus.data.analysis.DiscreteKernel;
import jdplus.maths.linearfilters.FiniteFilter;
import jdplus.maths.linearfilters.SymmetricFilter;
import demetra.sa.DecompositionMode;
import java.util.function.IntToDoubleFunction;
import demetra.data.DoubleSeq;
import jdplus.filters.IFiltering;
import jdplus.filters.LocalPolynomialFilterFactory;
import jdplus.filters.LocalPolynomialFilterSpec;
import jdplus.rkhs.RKHSFilterSpec;

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
    @lombok.NonNull
    SeasonalFilterOption initialSeasonalFilter;
    @lombok.NonNull
    SeasonalFilterOption finalSeasonalFilter;
    double lowerSigma, upperSigma;
    
    IFiltering trendFiltering;

    public static Builder builder() {
        Builder builder = new Builder();
        builder.mode = DecompositionMode.Multiplicative;
        builder.trendFiltering=LocalPolynomialFilterFactory.of(new LocalPolynomialFilterSpec());
        
        builder.initialSeasonalFilter = SeasonalFilterOption.S3X3;
        builder.finalSeasonalFilter = SeasonalFilterOption.S3X5;
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
