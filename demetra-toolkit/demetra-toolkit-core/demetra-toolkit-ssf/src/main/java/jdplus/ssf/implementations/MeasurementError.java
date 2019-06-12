/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.ssf.implementations;

import jdplus.ssf.univariate.ISsfError;
import java.util.function.IntToDoubleFunction;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class MeasurementError {

    public ISsfError of(double var) {
        return var == 0 ? null : new Error(var);
    }

    public ISsfError of(IntToDoubleFunction var) {
        return new VarError(var);
    }
    
    static class Error implements ISsfError {

        private final double var;

        Error(double var) {
            this.var = var;
        }

        @Override
        public double at(int pos) {
            return var;
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }
    }

    static class VarError implements ISsfError {

        private final IntToDoubleFunction var;

        VarError(IntToDoubleFunction var) {
            this.var = var;
        }

        @Override
        public double at(int pos) {
            return var.applyAsDouble(pos);
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }
    }
}
