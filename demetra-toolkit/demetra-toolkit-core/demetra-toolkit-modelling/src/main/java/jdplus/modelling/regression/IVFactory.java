/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.modelling.regression;

import demetra.modelling.regression.InterventionVariable;
import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.TsPeriod;
import jdplus.maths.matrices.FastMatrix;

/**
 *
 * @author palatej
 */
class IVFactory implements RegressionVariableFactory<InterventionVariable>{

     static IVFactory FACTORY=new IVFactory();

    private IVFactory(){}

    @Override
    public boolean fill(InterventionVariable var, TsPeriod start, FastMatrix buffer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <D extends TimeSeriesDomain> boolean fill(InterventionVariable var, D domain, FastMatrix buffer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
