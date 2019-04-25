/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.ssf.implementations;

import demetra.data.DataBlock;
import demetra.maths.matrices.FastMatrix;
import demetra.ssf.ISsfLoading;
import java.util.function.IntToDoubleFunction;
import demetra.ssf.univariate.ISsfMeasurement;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class WeightedLoading implements ISsfLoading {
    
    public static WeightedLoading of(final ISsfLoading loading, final IntToDoubleFunction weights){
        return new WeightedLoading(loading, weights, false);
    }

     public static WeightedLoading of(final ISsfLoading loading, final double w){
        return new WeightedLoading(loading, i->w, true);
    }

    private final IntToDoubleFunction weights;
    private final ISsfLoading loading;
    private final boolean timeInvariantWeights;

    private WeightedLoading(final ISsfLoading loading, final IntToDoubleFunction weights, final boolean timeInvariant) {
        this.weights = weights;
        this.loading = loading;
        this.timeInvariantWeights=timeInvariant ;
    }

    public IntToDoubleFunction getWeights() {
        return weights;
    }

    @Override
    public void Z(int pos, DataBlock z) {
        loading.Z(pos, z);
        z.mul(weights.applyAsDouble(pos));
    }

    @Override
    public double ZX(int pos, DataBlock m) {
        return weights.applyAsDouble(pos) * loading.ZX(pos, m);
    }

    @Override
    public double ZVZ(int pos, FastMatrix V) {
        double w = weights.applyAsDouble(pos);
        return loading.ZVZ(pos, V) * w * w;
    }

    @Override
    public void VpZdZ(int pos, FastMatrix V, double d) {
        double w = weights.applyAsDouble(pos);
        loading.VpZdZ(pos, V, w * w * d);
    }

    @Override
    public void XpZd(int pos, DataBlock x, double d) {
        loading.XpZd(pos, x, d * weights.applyAsDouble(pos));
    }

    @Override
    public boolean isTimeInvariant() {
        return loading.isTimeInvariant() && this.timeInvariantWeights;
    }

}
