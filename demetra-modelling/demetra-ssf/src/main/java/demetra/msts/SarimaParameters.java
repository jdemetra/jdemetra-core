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
import demetra.sarima.SarimaSpecification;

/**
 *
 * @author palatej
 */
public final class SarimaParameters implements IMstsParametersBlock {

    private final String name;
    private double[] values;
    private final SarimaMapping mapping;
    private final int np;
    private boolean fixed;

    public SarimaParameters(final String name, final SarimaSpecification spec, final double[] p, final boolean fixed) {
        this.name = name;
        this.mapping = SarimaMapping.of(spec);
        this.values = p == null ? mapping.getDefaultParameters().toArray() : p;
        this.np = spec.getParametersCount();
        this.fixed = fixed;
    }

    @Override
    public SarimaParameters duplicate(){
        return new SarimaParameters(name, mapping.getSpec(), values.clone(), fixed);
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
            for (int i = 0; i < np; ++i) {
                buffer[pos++] = reader.next();
            }
        } else {
            for (int i = 0; i < np; ++i) {
                buffer[pos++] = values[i];
            }
        }
        return pos;
    }

    @Override
    public int encode(DoubleReader reader, double[] buffer, int pos) {
        if (!fixed) {
            for (int i = 0; i < np; ++i) {
                buffer[pos++] = reader.next();
            }
        } else {
            reader.skip(np);
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
    public SarimaMapping getDomain() {
        return mapping;
    }

    @Override
    public int fillDefault(double[] buffer, int pos) {
        if (! fixed) {
           for (int i = 0; i < values.length; ++i) {
                buffer[pos + i] = values[i];
            }
            return pos + values.length;
        } else {
            return pos;
        }
    }

}
