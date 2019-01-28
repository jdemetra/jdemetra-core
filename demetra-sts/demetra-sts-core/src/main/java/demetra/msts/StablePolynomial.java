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
import demetra.sarima.estimation.SarimaMapping;
import javax.annotation.Nonnull;

/**
 *
 * @author palatej
 */
public class StablePolynomial implements IMstsParametersBlock {

    private final String name;
    private final double[] values;
    private final Domain domain;
    private boolean fixed;

    public StablePolynomial(final String name, @Nonnull double[] values, boolean fixed) {
        this.name = name;
        this.values = values;
        this.fixed = fixed;
        this.domain = new Domain(values.length);
    }
    
    @Override
    public StablePolynomial duplicate(){
        return new StablePolynomial(name, values.clone(), fixed);
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
    public int decode(DoubleReader reader, double[] buffer, int pos) {
        if (!fixed) {
            for (int i = 0; i < values.length; ++i) {
                buffer[pos++] = reader.next();
            }
        } else {
            for (int i = 0; i < values.length; ++i) {
                buffer[pos++] = values[i];
            }
        }
        return pos;
    }

    @Override
    public int encode(DoubleReader reader, double[] buffer, int pos) {
        if (!fixed) {
            for (int i = 0; i < values.length; ++i) {
                buffer[pos++] = reader.next();
            }
        } else {
            reader.skip(values.length);
        }
        return pos;
    }

    @Override
    public void fixModelParameter(DoubleReader reader) {
        for (int i = 0; i < values.length; ++i) {
            values[i] = reader.next();
        }
        fixed = true;
    }

    @Override
    public void free(){
        fixed=false;
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

    static class Domain implements IParametersDomain {

        private final int degree;

        Domain(int degree) {
            this.degree = degree;
        }

        @Override
        public boolean checkBoundaries(DoubleSequence inparams) {
            return SarimaMapping.checkStability(inparams);
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
            Polynomial p = Polynomial.valueOf(1, ioparams.toArray());
            Polynomial np = SarimaMapping.stabilize(p);
            if (np.equals(p)) {
                return ParamValidation.Valid;
            } else {
                ioparams.copy(np.coefficients().drop(1, 0));
                return ParamValidation.Changed;
            }
        }

    }

}
