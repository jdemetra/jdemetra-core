/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.msts;

import demetra.data.DoubleReader;
import demetra.maths.functions.IParametersDomain;

/**
 *
 * @author palatej
 */
public class GenericParameters implements IMstsParametersBlock {

    private boolean fixed;
    private final double[] parameters;
    private final IParametersDomain domain;
    private final String name;

    public GenericParameters(String name, IParametersDomain domain, double[] parameters, boolean fixed) {
        this.name = name;
        this.domain = domain;
        this.fixed = fixed;
        this.parameters = parameters;
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
        int n = parameters.length;
        if (!fixed) {
            for (int i = 0; i < n; ++i) {
                buffer[pos++] = reader.next();
            }
        } else {
            for (int i = 0; i < n; ++i) {
                buffer[pos++] = parameters[i];
            }
        }
        return pos;
    }

    @Override
    public int encode(DoubleReader reader, double[] buffer, int pos) {
        int n = parameters.length;
        if (!fixed) {
            for (int i = 0; i < n; ++i) {
                buffer[pos++] = reader.next();
            }
        } else {
            reader.skip(n);
        }

        return pos;
    }

    @Override
    public void fixModelParameter(DoubleReader reader) {
        for (int i = 0; i < parameters.length; ++i) {
            parameters[i] = reader.next();
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
            System.arraycopy(parameters, 0, buffer, pos, parameters.length);
            return pos + parameters.length;
        } else {
            return pos;
        }
    }
}
