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
import demetra.maths.functions.ParametersRange;

/**
 *
 * @author palatej
 */
public class BoundedParameter implements IMstsParametersBlock {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private double lbound=0, ubound=Double.MAX_VALUE;
        private boolean open=true;
        private double value=.5;
        private boolean fixed=false;
        private String name="";
        
        public Builder bounds(double lbound, double ubound, boolean open){
            this.lbound=lbound;
            this.ubound=ubound;
            this.open=open;
            return this;
        }
        
        public Builder value(double value, boolean fixed){
            this.value=value;
            this.fixed=fixed;
            return this;
        }

        public Builder name(String name){
            this.name=name;
            return this;
        }

        public BoundedParameter build(){
            return new BoundedParameter(name, value, fixed, lbound, ubound, open);
        }
    }

    private static final double EPS = 1e-8;

    private final ParametersRange range;
    private double value;
    private boolean fixed;
    private final String name;

    private BoundedParameter(final String name, double value, boolean fixed, double lbound, double ubound, boolean open) {
        this.name = name;
        this.value=value;
        this.fixed = fixed;
        this.range=new ParametersRange(lbound, ubound, open);
    }

    @Override
    public String getName() {
        return name;
    }

    public double fix(double val) {
        double oldval = value;
        value = val;
        fixed = true;
        return oldval;
    }

    @Override
    public void free() {
        fixed = false;
    }

    @Override
    public void fixModelParameter(DoubleReader reader) {
        value = reader.next();
        fixed = true;
    }

    @Override
    public boolean isFixed() {
        return fixed;
    }

    @Override
    public boolean isPotentialSingularity() {
        return false;
    }

    @Override
    public IParametersDomain getDomain() {
        return range;
    }

    @Override
    public int decode(DoubleReader input, double[] buffer, int pos) {
        if (!fixed) {
            buffer[pos] = input.next();
        } else {
            buffer[pos] = value;
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
            buffer[pos] = value;
            return pos + 1;
        } else {
            return pos;
        }
    }

}
