/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.regression;

import demetra.maths.matrices.Matrix;
import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.TsPeriod;

/**
 *
 * @author palatej
 * @param <X>
 */
public interface RegressionVariableFactory <X extends ITsVariableDefinition> {
    boolean fill(X var, TsPeriod start, Matrix buffer);
    
    <D extends TimeSeriesDomain> boolean fill(X var, D domain, Matrix buffer);
}
