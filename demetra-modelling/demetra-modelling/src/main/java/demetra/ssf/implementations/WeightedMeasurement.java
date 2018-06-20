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
import demetra.maths.matrices.Matrix;
import demetra.ssf.univariate.ISsfMeasurement;
import java.util.function.IntToDoubleFunction;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class WeightedMeasurement implements ISsfMeasurement {
    
    public static WeightedMeasurement of(final ISsfMeasurement measurement, final IntToDoubleFunction weights){
        return new WeightedMeasurement(measurement, weights, false);
    }

     public static WeightedMeasurement of(final ISsfMeasurement measurement, final double w){
        return new WeightedMeasurement(measurement, i->w, true);
    }

    private final IntToDoubleFunction weights;
    private final ISsfMeasurement measurement;
    private final boolean timeInvariantWeights;

    private WeightedMeasurement(final ISsfMeasurement measurement, final IntToDoubleFunction weights, final boolean timeInvariant) {
        this.weights = weights;
        this.measurement = measurement;
        this.timeInvariantWeights=timeInvariant ;
    }

    public IntToDoubleFunction getWeights() {
        return weights;
    }

    @Override
    public void Z(int pos, DataBlock z) {
        measurement.Z(pos, z);
        z.mul(weights.applyAsDouble(pos));
    }

    @Override
    public boolean hasErrors() {
        return measurement.hasErrors();
    }

    @Override
    public boolean areErrorsTimeInvariant(){
        return !measurement.hasErrors() || (timeInvariantWeights && measurement.areErrorsTimeInvariant()) ;
    }

    @Override
    public boolean hasError(int pos) {
        return measurement.hasError(pos);
    }

    @Override
    public double errorVariance(int pos) {
        double v = measurement.errorVariance(pos);
        if (v == 0) {
            return 0;
        } else {
            double w = weights.applyAsDouble(pos);
            return v * w * w;
        }
    }

    @Override
    public double ZX(int pos, DataBlock m) {
        return weights.applyAsDouble(pos) * measurement.ZX(pos, m);
    }

    @Override
    public double ZVZ(int pos, Matrix V) {
        double w = weights.applyAsDouble(pos);
        return measurement.ZVZ(pos, V) * w * w;
    }

    @Override
    public void VpZdZ(int pos, Matrix V, double d) {
        double w = weights.applyAsDouble(pos);
        measurement.VpZdZ(pos, V, w * w * d);
    }

    @Override
    public void XpZd(int pos, DataBlock x, double d) {
        measurement.XpZd(pos, x, d * weights.applyAsDouble(pos));
    }

    @Override
    public boolean isTimeInvariant() {
        return measurement.isTimeInvariant() && this.timeInvariantWeights;
    }

}
