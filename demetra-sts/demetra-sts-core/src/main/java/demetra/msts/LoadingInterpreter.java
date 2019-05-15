/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.msts;

import jd.data.DataBlock;
import demetra.data.DoubleSeqCursor;
import demetra.maths.functions.IParametersDomain;
import demetra.maths.functions.ParamValidation;
import demetra.data.DoubleSeq;

/**
 *
 * @author palatej
 */
public final class LoadingInterpreter implements ParameterInterpreter {

    private static final double DEF_VALUE = .1;

    private double c;
    private boolean fixed;
    private final String name;

    public LoadingInterpreter(final String name) {
        this.name = name;
        c = DEF_VALUE;
        fixed = false;
    }

    public LoadingInterpreter(final String name, double loading, boolean fixed) {
        this.name = name;
        this.c = loading;
        this.fixed = fixed;
    }

    @Override
    public LoadingInterpreter duplicate() {
        return new LoadingInterpreter(name, c, fixed);
    }

    @Override
    public String getName() {
        return name;
    }

    public double fix(double val) {
        double oldval = c;
        c = val;
        fixed = true;
        return oldval;
    }

    public double value() {
        return c;
    }

    @Override
    public void free() {
        fixed = false;
    }

    @Override
    public void fixModelParameter(DoubleSeqCursor reader) {
        c = reader.getAndNext();
        fixed = true;
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
    public int decode(DoubleSeqCursor input, double[] buffer, int pos) {
        if (!fixed) {
            buffer[pos] = input.getAndNext();
        } else {
            buffer[pos] = c;
        }
        return pos + 1;
    }

    @Override
    public int encode(DoubleSeqCursor input, double[] buffer, int pos) {
        if (!fixed) {
            buffer[pos] = input.getAndNext();
            return pos + 1;
        } else {
            input.skip(1);
            return pos;
        }
    }

    @Override
    public int fillDefault(double[] buffer, int pos) {
        if (!fixed) {
            buffer[pos] = c;
            return pos + 1;
        } else {
            return pos;
        }
    }

    @Override
    public boolean isScaleSensitive(boolean variance) {
        return true;
    }

    @Override
    public int rescaleVariances(double factor, double[] buffer, int pos) {
        return pos+1;
    }

    static class Domain implements IParametersDomain {

        static final Domain INSTANCE = new Domain();

        @Override
        public boolean checkBoundaries(DoubleSeq inparams) {
            return true;
        }

        private static final double EPS = 1e-4;

        @Override
        public double epsilon(DoubleSeq inparams, int idx) {
            double c = inparams.get(0);
            if (c >= 0) {
                return Math.max(EPS, c * EPS);
            } else {
                return -Math.max(EPS, -c * EPS);
            }
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
            return Double.MAX_VALUE;
        }

        @Override
        public ParamValidation validate(DataBlock ioparams) {
            return ParamValidation.Valid;
        }
    }

}
