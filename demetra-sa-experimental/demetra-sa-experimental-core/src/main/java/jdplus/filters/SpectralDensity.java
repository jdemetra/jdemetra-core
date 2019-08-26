/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.filters;

import java.util.function.DoubleUnaryOperator;

/**
 *
 * @author Jean Palate
 */
public enum SpectralDensity {
    Undefined,
    WhiteNoise,
    RandomWalk;
    
    public DoubleUnaryOperator asFunction(){
        switch (this){
            case RandomWalk: return x->1/(2-2*Math.cos(x));
            default: return x->1;
        }
    }
}
