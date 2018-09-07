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
import demetra.maths.polynomials.Polynomial;
import demetra.sarima.SarimaMapping;

/**
 *
 * @author palatej
 */
public class ArParameters implements IMstsParametersBlock {

    private final String name;
    private final int degree;
    private final double[] values, defValue;
    private final Domain domain;

    public ArParameters(final String name, int degree, double[] values, double[] defValue) {
        this.name = name;
        this.values = values;
        this.defValue = defValue;
        this.degree = degree;
        this.domain = new Domain(degree);
    }

    public ArParameters(final String name, int degree, double[] values, double defValue) {
        this.name = name;
        this.values = values;
        this.defValue = new double[degree];
        for (int i = 0; i < degree; ++i) {
            this.defValue[i] = defValue;
        }
        this.degree = degree;
        this.domain = new Domain(degree);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isFixed() {
        return values != null;
    }

    @Override
    public int decode(DoubleReader reader, double[] buffer, int pos) {
        if (values == null) {
            for (int i = 0; i < degree; ++i) {
                buffer[pos++] = reader.next();
            }
        } else {
            for (int i = 0; i < degree; ++i) {
                buffer[pos++] = values[i];
            }
        }
        return pos;
    }

    @Override
    public int encode(DoubleReader reader, double[] buffer, int pos) {
        if (values == null) {
            for (int i = 0; i < degree; ++i) {
                buffer[pos++] = reader.next();
            }
        } else {
            for (int i = 0; i < degree; ++i) {
                reader.next();
            }
        }
        return pos;
    }

    @Override
    public IParametersDomain getDomain() {
        return domain;
    }

    @Override
    public int fillDefault(double[] buffer, int pos) {
        if (values == null) {
            for (int i = 0; i < degree; ++i) {
                buffer[pos + i] = defValue[i];
            }
            return pos + degree;
        } else {
            return pos;
        }
    }

    static class Domain implements IParametersDomain {

        private final int degree;

        Domain(int degree) {
            this.degree = degree;
        }

        @Override
        public boolean checkBoundaries(DoubleSequence inparams) {
            return SarimaMapping.checkStability(DoubleSequence.onMapping(inparams.length(), i -> -inparams.get(i)));
        }

        @Override
        public double epsilon(DoubleSequence inparams, int idx) {
            return 1e-6;
        }

        @Override
        public int getDim() {
            return degree;
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
