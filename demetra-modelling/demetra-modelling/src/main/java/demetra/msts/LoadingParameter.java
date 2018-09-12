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

/**
 *
 * @author palatej
 */
public class LoadingParameter implements IMstsParametersBlock {

    private static final double DEF_VALUE = .1;

    private double loading;
    private boolean fixed;
    private final String name;

    public LoadingParameter(final String name) {
        this.name=name;
        loading=DEF_VALUE;
        fixed=false;
    }

    public LoadingParameter(final String name, double loading, boolean fixed) {
        this.name=name;
        this.loading=loading;
        this.fixed=fixed;
    }

    @Override
    public String getName(){
        return name;
    }

    public void fix(double val) {
        loading=val;
        fixed=true;
    }

    public void free() {
        fixed=false;
    }

    @Override
    public boolean isFixed() {
        return fixed;
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
        double l = input.next();
        if (!fixed) {
            buffer[pos]=l;
            return pos + 1;
        } else {
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
