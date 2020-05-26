/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.msts;

import jdplus.data.DataBlock;
import demetra.data.DoubleSeqCursor;
import jdplus.math.functions.IParametersDomain;
import jdplus.math.functions.ParamValidation;
import jdplus.math.polynomials.Polynomial;
import jdplus.sarima.estimation.SarimaMapping;
import org.checkerframework.checker.nullness.qual.NonNull;
import demetra.data.DoubleSeq;

/**
 *
 * @author palatej
 */
public class ArInterpreter implements ParameterInterpreter {

    private final String name;
    private final double[] values;
    private boolean fixed;
    private final Domain domain;

    public ArInterpreter(@NonNull final String name, @NonNull double[] values, boolean fixed) {
        this.name = name;
        this.values = values;
        this.fixed = fixed;
        this.domain = new Domain(values.length);
    }

    public ArInterpreter(final String name, int degree, double defValue) {
        this.name = name;
        this.values = new double[degree];
        for (int i = 0; i < degree; ++i) {
            this.values[i] = defValue;
        }
        this.domain = new Domain(degree);
    }

    @Override
    public ArInterpreter duplicate() {
        return new ArInterpreter(name, values.clone(), fixed);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isFixed() {
        return fixed;
    }

    @Override
    public void fixModelParameter(DoubleSeqCursor reader) {
        for (int i = 0; i < values.length; ++i) {
            values[i] = reader.getAndNext();
        }
        fixed = true;
    }

    @Override
    public void free() {
        fixed = false;
    }

    @Override
    public int decode(DoubleSeqCursor reader, double[] buffer, int pos) {
        if (!fixed) {
            for (int i = 0; i < values.length; ++i) {
                buffer[pos++] = reader.getAndNext();
            }
        } else {
            for (int i = 0; i < values.length; ++i) {
                buffer[pos++] = values[i];
            }
        }
        return pos;
    }

    @Override
    public int encode(DoubleSeqCursor reader, double[] buffer, int pos) {
        if (!fixed) {
            for (int i = 0; i < values.length; ++i) {
                buffer[pos++] = reader.getAndNext();
            }
        } else {
            reader.skip(values.length);
        }
        return pos;
    }

    @Override
    public IParametersDomain getDomain() {
        return domain;
    }

    @Override
    public int fillDefault(double[] buffer, int pos) {
        if (!fixed) {
            for (int i = 0; i < values.length; ++i) {
                buffer[pos + i] = values[i];
            }
            return pos + values.length;
        } else {
            return pos;
        }
    }

    @Override
    public boolean isScaleSensitive(boolean variance) {
        return false;
    }

    @Override
    public int rescaleVariances(double factor, double[] buffer, int pos) {
        return pos+values.length;
    }


    static class Domain implements IParametersDomain {
        
        private double EPS=1e-6;

        private final int degree;

        Domain(int degree) {
            this.degree = degree;
        }

        @Override
        public boolean checkBoundaries(DoubleSeq inparams) {
            if (degree==1)
                return Math.abs(inparams.get(0))<1-EPS;
            return SarimaMapping.checkStability(DoubleSeq.onMapping(inparams.length(), i -> -inparams.get(i)));
        }

        @Override
        public double epsilon(DoubleSeq inparams, int idx) {
            return 1e-6;
        }

        @Override
        public int getDim() {
            return degree;
        }

        @Override
        public double lbound(int idx) {
            if (degree == 1)
                return -1+EPS;
            else
            return -Double.MAX_VALUE;
        }

        @Override
        public double ubound(int idx) {
            if (degree == 1)
                return 1-EPS;
            else
            return Double.MAX_VALUE;
        }

        @Override
        public ParamValidation validate(DataBlock ioparams) {
            
            double[] z = ioparams.toArray();
            for (int i = 0; i < z.length; ++i) {
                z[i] = -z[i];
            }
            Polynomial p = Polynomial.valueOf(1, z);
            Polynomial np = SarimaMapping.stabilize(p);
            if (np.equals(p)) {
                return ParamValidation.Valid;
            } else {
                for (int i = 0; i < z.length; ++i) {
                    z[i] = -np.get(i + 1);
                }

                ioparams.copyFrom(z, 0);
                return ParamValidation.Changed;
            }
        }

    }

}
