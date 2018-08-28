/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package msts;

import demetra.data.DataBlock;
import demetra.data.DoubleReader;
import demetra.data.DoubleSequence;
import demetra.maths.functions.IParametersDomain;
import demetra.maths.functions.ParamValidation;

/**
 *
 * @author palatej
 */
public class VarianceParameter implements IMstsParametersBlock {

    private static final double DEF_STDE = .1;

    private Double fixedVariance;

    public VarianceParameter() {

    }

    public VarianceParameter(double var) {
        fixedVariance = var;
    }

    public void fix(double val) {
        fixedVariance = val;
    }


    public void free() {
        fixedVariance = null;
    }

    @Override
    public boolean isFixed() {
        return fixedVariance != null;
    }

    @Override
    public IParametersDomain getDomain() {
        return Domain.INSTANCE;
    }

    @Override
    public int decode(DoubleReader input, double[] buffer, int pos) {
        if (fixedVariance == null) {
            double e = input.next();
            buffer[pos] = e * e;
        } else {
            buffer[pos] = fixedVariance;
        }
        return pos + 1;
    }

    @Override
    public int encode(DoubleReader input, double[] buffer, int pos) {
        if (fixedVariance == null) {
            double v = input.next();
            buffer[pos] = Math.sqrt(v);
            return pos + 1;
        } else {
            return pos;
        }
    }

    @Override
    public int fillDefault(double[] buffer, int pos) {
        if (fixedVariance == null) {
            buffer[pos] = DEF_STDE;
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
            return Math.max(1e-8, Math.abs(inparams.get(0)) * 1e-4);
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
