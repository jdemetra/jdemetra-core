/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.regression;

import demetra.maths.matrices.FastMatrix;
import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.TsPeriod;

/**
 *
 * @author Jean Palate
 */
class ConstantFactory implements RegressionVariableFactory<Constant>{

    static ConstantFactory FACTORY=new ConstantFactory();

    private ConstantFactory(){}

    @Override
    public boolean fill(Constant var, TsPeriod start, FastMatrix buffer) {
        buffer.set(1);
        return true;
    }

    @Override
    public <D extends TimeSeriesDomain> boolean fill(Constant var, D domain, FastMatrix buffer) {
        buffer.set(1);
        return true;
    }
    
}
