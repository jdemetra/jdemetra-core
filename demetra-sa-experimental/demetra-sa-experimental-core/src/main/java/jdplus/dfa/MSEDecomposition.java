/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.dfa;

import demetra.maths.Complex;
import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;
import jdplus.maths.functions.integration.NumericalIntegration;

/**
 * See McElroy and Wildi, International Journal of Forecasting, 2019.
 * @author Jean Palate
 */
@lombok.Value
public class MSEDecomposition {
    
    private double accuracy, timeliness, smoothness, residual;
    
    /**
     * @param spectralDensity
     * @param frfTarget
     * @param frfProxy
     * @param passBand
     * @return 
     */
    public static MSEDecomposition of(DoubleUnaryOperator spectralDensity, DoubleFunction<Complex> frfTarget, DoubleFunction<Complex> frfProxy, double passBand){
        final DoubleUnaryOperator sp;
        if (spectralDensity == null)
            sp=x-> x == 0 ? 1 : 1/(2-2*Math.cos(x));
        else
            sp=spectralDensity;
        
        // accuracy
        DoubleUnaryOperator f=x->frfProxy.apply(x).minus(frfTarget.apply(x)).absSquare()*sp.applyAsDouble(x);
        double total=2*NumericalIntegration.integrate(f, 0, Math.PI);
        f=x->{
            double dg=frfTarget.apply(x).abs()-frfProxy.apply(x).abs();
            return dg*dg*sp.applyAsDouble(x);
        };
        double accuracy=2*NumericalIntegration.integrate(f, 0, passBand);
        double smoothness=2*NumericalIntegration.integrate(f, passBand, Math.PI);
        
        f=x->{
            Complex fr=frfTarget.apply(x), fra = frfProxy.apply(x);
            double g=fr.abs(), ga=fra.abs();
            double p=-fr.arg(), pa=-fra.arg();
            double s=Math.sin((p-pa)/2);
            return g*ga*s*s*sp.applyAsDouble(x);
        };
        
        double timeliness=8*NumericalIntegration.integrate(f, 0, passBand);
        double residual=total-accuracy-smoothness-timeliness;
        return new MSEDecomposition(accuracy, timeliness, smoothness, residual);
    }
    
}
