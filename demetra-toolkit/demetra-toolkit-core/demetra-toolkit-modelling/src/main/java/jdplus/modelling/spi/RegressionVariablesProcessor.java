/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.modelling.spi;

import demetra.maths.matrices.Matrix;
import demetra.timeseries.regression.ITsVariable;
import demetra.timeseries.regression.RegressionVariables;
import demetra.timeseries.TimeSeriesDomain;
import jdplus.modelling.regression.Regression;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(RegressionVariables.Processor.class)
public class RegressionVariablesProcessor implements RegressionVariables.Processor {
    
    public static final RegressionVariablesProcessor PROCESSOR=new RegressionVariablesProcessor();

    @Override
    public <D extends TimeSeriesDomain> Matrix matrix(D domain, ITsVariable... vars) {
        return Regression.matrix(domain, vars).unmodifiable();
    }

}

