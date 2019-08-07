/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.rkhs;

import demetra.maths.Complex;
import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;
import jdplus.maths.functions.integration.NumericalIntegration;
import jdplus.maths.linearfilters.FiniteFilter;
import jdplus.maths.linearfilters.SymmetricFilter;
import jdplus.stats.Kernel;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class AsymmetricFiltersUtility {

    public double frequencyResponseDistance(DoubleUnaryOperator spectralDensity, SymmetricFilter sf, FiniteFilter af) {
        final DoubleUnaryOperator sp;
        if (spectralDensity == null) {
            sp = x -> 1;
        } //            sp=x-> x == 0 ? 1 : 1/(2-2*Math.cos(x));
        else {
            sp = spectralDensity;
        }
        DoubleUnaryOperator fn = x -> sp.applyAsDouble(x) * sf.frequencyResponse(x).squareDistance(af.frequencyResponse(x));
        return 2 * NumericalIntegration.integrate(fn, 0, Math.PI);
    }

    public double frequencyResponseDistance(SymmetricFilter sf, FiniteFilter af) {
        return frequencyResponseDistance(null, sf, af);
    }

    public double accuracyDistance(DoubleUnaryOperator spectralDensity, SymmetricFilter sf, FiniteFilter af, double passBand) {
        final DoubleUnaryOperator sp;
        if (spectralDensity == null) {
            sp = x -> 1;
        } //            sp=x-> x == 0 ? 1 : 1/(2-2*Math.cos(x));
        else {
            sp = spectralDensity;
        }
        DoubleUnaryOperator fn = x -> {
            double y = sf.gainFunction().applyAsDouble(x) - af.gainFunction().applyAsDouble(x);
            return y * y * sp.applyAsDouble(x);
        };
        return 2 * NumericalIntegration.integrate(fn, 0, passBand);
    }

    public double accuracyDistance(SymmetricFilter sf, FiniteFilter af, double passBand) {
        return accuracyDistance(null, sf, af, passBand);
    }

    public double smoothnessDistance(DoubleUnaryOperator spectralDensity, SymmetricFilter sf, FiniteFilter af, double passBand) {
        final DoubleUnaryOperator sp;
        if (spectralDensity == null) {
            sp = x -> 1;
        } //            sp=x-> x == 0 ? 1 : 1/(2-2*Math.cos(x));
        else {
            sp = spectralDensity;
        }
        DoubleUnaryOperator fn = x -> {
            double y = sf.gainFunction().applyAsDouble(x) - af.gainFunction().applyAsDouble(x);
            return y * y * sp.applyAsDouble(x);
        };
        return 2 * NumericalIntegration.integrate(fn, passBand, Math.PI);
    }

    public double smoothnessDistance(SymmetricFilter sf, FiniteFilter af, double passBand) {
        return smoothnessDistance(null, sf, af, passBand);
    }

    public double timelinessDistance(DoubleUnaryOperator spectralDensity, SymmetricFilter sf, FiniteFilter af, double passBand) {
        final DoubleUnaryOperator sp;
        if (spectralDensity == null) {
            sp = x -> 1;
        } //            sp=x-> x == 0 ? 1 : 1/(2-2*Math.cos(x));
        else {
            sp = spectralDensity;
        }
        DoubleUnaryOperator fn = x -> {
            double g = Math.abs(sf.realFrequencyResponse(x));
            double ga = af.frequencyResponse(x).abs();
            double s = Math.sin(af.frequencyResponse(x).arg() / 2);
            return g * ga * s * s * sp.applyAsDouble(x);
        };
        return 8 * NumericalIntegration.integrate(fn, 0, passBand);
    }

    public double timelinessDistance(SymmetricFilter sf, FiniteFilter af, double passBand) {
        return timelinessDistance(null, sf, af, passBand);
    }

    public double timelinessDistance2(DoubleUnaryOperator spectralDensity, FiniteFilter af, double a, double b) {
        final DoubleUnaryOperator sp;
        if (spectralDensity == null) {
            sp = x -> 1;
        } //            sp=x-> x == 0 ? 1 : 1/(2-2*Math.cos(x));
        else {
            sp = spectralDensity;
        }
        DoubleUnaryOperator fn = x -> {
            double p = af.frequencyResponse(x).arg();
            return x == 0 ? 0 : Math.abs(p / x) * sp.applyAsDouble(x);
        };
        return 8 * NumericalIntegration.integrate(fn, a, b);
    }

    public double timelinessDistance2(FiniteFilter af, double a, double b) {
        return timelinessDistance2(null, af, a, b);
    }

}
