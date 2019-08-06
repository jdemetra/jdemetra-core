/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.rkhs;

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

    public double distance(SymmetricFilter sf, FiniteFilter af) {
        DoubleUnaryOperator fn = x -> sf.frequencyResponse(x).squareDistance(af.frequencyResponse(x));
        return NumericalIntegration.integrate(fn, 0, Math.PI);
    }

    public double distance2(SymmetricFilter sf, FiniteFilter af) {
        DoubleUnaryOperator fn = x -> { double y=sf.gainFunction().applyAsDouble(x)-af.gainFunction().applyAsDouble(x); return y*y;};
        return NumericalIntegration.integrate(fn, 0, Math.PI);
    }

    public double distance3(FiniteFilter af, double a, double b) {
        DoubleUnaryOperator fn = x -> x == 0 ? 0 : Math.abs(af.phaseFunction().applyAsDouble(x))/x;
        return NumericalIntegration.integrate(fn, a*2*Math.PI, b*2*Math.PI);
    }

}
