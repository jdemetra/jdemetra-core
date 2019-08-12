/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.filters;

import java.util.function.DoubleUnaryOperator;
import jdplus.maths.functions.integration.NumericalIntegration;
import jdplus.maths.linearfilters.FiniteFilter;
import jdplus.maths.linearfilters.SymmetricFilter;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class AsymmetricFiltersUtility {

    private final double Q = 1 / (2 * Math.PI);

    public static interface Distance {

        double compute(SymmetricFilter sf, FiniteFilter af);
    }
    
    public Distance frequencyResponseDistance(@lombok.NonNull DoubleUnaryOperator spectralDensity) {
        return (SymmetricFilter sf, FiniteFilter af) -> {
            DoubleUnaryOperator fn = x -> spectralDensity.applyAsDouble(x) * sf.frequencyResponse(x).squareDistance(af.frequencyResponse(x));
            return 2 * NumericalIntegration.integrate(fn, 0, Math.PI);
        };
    }

    public Distance frequencyResponseDistance() {

        return frequencyResponseDistance(x -> Q);
    }

    public Distance accuracyDistance(@lombok.NonNull DoubleUnaryOperator spectralDensity, double passBand) {
        return (SymmetricFilter sf, FiniteFilter af) -> {
            DoubleUnaryOperator fn = x -> {
                double y = sf.gainFunction().applyAsDouble(x) - af.gainFunction().applyAsDouble(x);
                return y * y * spectralDensity.applyAsDouble(x);
            };
            return 2 * NumericalIntegration.integrate(fn, 0, passBand);
        };
    }

    public Distance accuracyDistance(SymmetricFilter sf, FiniteFilter af, double passBand) {
        return accuracyDistance(x -> Q, passBand);
    }

    public Distance smoothnessDistance(DoubleUnaryOperator spectralDensity, double passBand) {
        return (SymmetricFilter sf, FiniteFilter af) -> {
            DoubleUnaryOperator fn = x -> {
                double y = sf.gainFunction().applyAsDouble(x) - af.gainFunction().applyAsDouble(x);
                return y * y * spectralDensity.applyAsDouble(x);
            };
            return 2 * NumericalIntegration.integrate(fn, passBand, Math.PI);
        };
    }

    public Distance smoothnessDistance(double passBand) {
        return smoothnessDistance(x -> Q, passBand);
    }

    public Distance timelinessDistance(DoubleUnaryOperator spectralDensity, double passBand) {
        return (SymmetricFilter sf, FiniteFilter af) -> {
            DoubleUnaryOperator fn = x -> {
                double g = Math.abs(sf.realFrequencyResponse(x));
                double ga = af.frequencyResponse(x).abs();
                double s = Math.sin(af.frequencyResponse(x).arg() / 2);
                return g * ga * s * s * spectralDensity.applyAsDouble(x);
            };
            return 8 * NumericalIntegration.integrate(fn, 0, passBand);
        };
    }

    public Distance timelinessDistance(double passBand) {
        return timelinessDistance(x -> Q, passBand);
    }

    public Distance timelinessDistance2(DoubleUnaryOperator spectralDensity, double a, double b) {
        return (SymmetricFilter sf, FiniteFilter af) -> {
            DoubleUnaryOperator fn = x -> {
                double p = af.frequencyResponse(x).arg();
                return x == 0 ? 0 : Math.abs(p / x) * spectralDensity.applyAsDouble(x);
            };
            return 8 * NumericalIntegration.integrate(fn, a, b);
        };
    }

    public Distance timelinessDistance2(double a, double b) {
        return timelinessDistance2(x -> Q, a, b);
    }

}
