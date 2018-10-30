/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.msts;

import demetra.data.DataBlock;
import demetra.data.DoubleReader;
import demetra.data.DoubleSequence;
import demetra.maths.functions.IParametersDomain;
import demetra.maths.functions.ParamValidation;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author palatej
 */
public final class LoadingParameter implements IMstsParametersBlock {

    private static final double DEF_VALUE = .1;

    private double loading;
    private boolean fixed;
    private final String name;

    public LoadingParameter(final String name) {
        this.name = name;
        loading = DEF_VALUE;
        fixed = false;
    }

    public LoadingParameter(final String name, double loading, boolean fixed) {
        this.name = name;
        this.loading = loading;
        this.fixed = fixed;
    }
    
    @Override
    public LoadingParameter duplicate(){
        return new LoadingParameter(name, loading, fixed);
    }

    @Override
    public String getName() {
        return name;
    }

    public double fix(double val) {
        double oldval=loading;
        loading = val;
        fixed = true;
        return oldval;
    }

    @Override
    public void free() {
        fixed = false;
    }

    @Override
    public void fixModelParameter(DoubleReader reader) {
        loading = reader.next();
        fixed = true;
    }

    @Override
    public boolean isFixed() {
        return fixed;
    }

    @Override
    public boolean isPotentialInstability() {
        return true;
    }

    @Override
    public IParametersDomain getDomain() {
        return Domain.INSTANCE;
    }

    @Override
    public int decode(DoubleReader input, double[] buffer, int pos) {
        if (!fixed) {
            buffer[pos] = input.next();
        } else {
            buffer[pos] = loading;
        }
        return pos + 1;
    }

    @Override
    public int encode(DoubleReader input, double[] buffer, int pos) {
        if (!fixed) {
            buffer[pos] = input.next();
            return pos + 1;
        } else {
            input.skip(1);
            return pos;
        }
    }

    @Override
    public int fillDefault(double[] buffer, int pos) {
        if (!fixed) {
            buffer[pos] = loading;
            return pos + 1;
        } else {
            return pos;
        }
    }

    static class Domain implements IParametersDomain {

        static final Domain INSTANCE = new Domain();

        @Override
        public boolean checkBoundaries(DoubleSequence inparams) {
            return true;
        }

        @Override
        public double epsilon(DoubleSequence inparams, int idx) {
            return 1e-8;
        }

        @Override
        public int getDim() {
            return 1;
        }

        @Override
        public double lbound(int idx) {
            return -Double.MAX_VALUE;
        }

        @Override
        public double ubound(int idx) {
            return -Double.MIN_VALUE;
        }

        @Override
        public ParamValidation validate(DataBlock ioparams) {
            return ParamValidation.Valid;
        }
    }

}
