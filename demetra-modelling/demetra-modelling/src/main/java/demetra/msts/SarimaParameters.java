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
public class SarimaParameters implements IMstsParametersBlock {

    private final String name;
    private final double[] values;
    private final SarimaMapping mapping;
    private final int np;

    public SarimaParameters(final String name, final SarimaSpecification spec, final double[] p) {
        this.name = name;
        this.values = p;
        this.mapping = SarimaMapping.of(spec);
        this.np = spec.getParametersCount();
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
        if (values == null) {
            for (int i = 0; i < np; ++i) {
                buffer[pos++] = reader.next();
            }
        } else {
            for (int i = 0; i < np; ++i) {
                reader.next();
            }
        }
        return pos;
    }

    @Override
    public IParametersDomain getDomain() {
        return mapping;
    }

    @Override
    public int fillDefault(double[] buffer, int pos) {
        if (values == null) {
            DoubleSequence p = mapping.getDefaultParameters();
            p.copyTo(buffer, pos);
            return pos + np;
        } else {
            return pos;
        }
    }

}
