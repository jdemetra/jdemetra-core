/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.data;

import java.util.function.IntToDoubleFunction;

/**
 * @since 2.2.0
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class WindowFunction {
    
    private final double[] window;
    
    public WindowFunction(WindowType type, int len){
        window=type.window(len);
    }
    
    public double computeSymmetric(IntToDoubleFunction fn){
        double v=fn.applyAsDouble(0)*window[0];
        for (int i=1; i<window.length; ++i){
            v+=2*window[i]*fn.applyAsDouble(i);
        }
        return v;
    }

    public double compute(IntToDoubleFunction fn){
        double v=fn.applyAsDouble(0)*window[0];
        for (int i=1; i<window.length; ++i){
            v+=window[i]*fn.applyAsDouble(i);
            v+=window[i]*fn.applyAsDouble(-i);
        }
        return v;
    }
    
    
    
}
