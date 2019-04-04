/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.msts;

import demetra.data.DoubleSeqCursor;
import demetra.maths.functions.IParametersDomain;

/**
 *
 * @author palatej
 */
public class GenericParameters implements ParameterInterpreter {

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
    public GenericParameters duplicate(){
        return new GenericParameters(name, domain, parameters.clone(), fixed);
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
    public int decode(DoubleSeqCursor reader, double[] buffer, int pos) {
        int n = parameters.length;
        if (!fixed) {
            for (int i = 0; i < n; ++i) {
                buffer[pos++] = reader.getAndNext();
            }
        } else {
            for (int i = 0; i < n; ++i) {
                buffer[pos++] = parameters[i];
            }
        }
        return pos;
    }

    @Override
    public int encode(DoubleSeqCursor reader, double[] buffer, int pos) {
        int n = parameters.length;
        if (!fixed) {
            for (int i = 0; i < n; ++i) {
                buffer[pos++] = reader.getAndNext();
            }
        } else {
            reader.skip(n);
        }

        return pos;
    }

    @Override
    public void fixModelParameter(DoubleSeqCursor reader) {
        for (int i = 0; i < parameters.length; ++i) {
            parameters[i] = reader.getAndNext();
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
    
    @Override
    public int rescaleVariances(double factor, double[] buffer, int pos) {
        return pos+parameters.length;
    }

    @Override
    public boolean isScaleSensitive(boolean variance) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
