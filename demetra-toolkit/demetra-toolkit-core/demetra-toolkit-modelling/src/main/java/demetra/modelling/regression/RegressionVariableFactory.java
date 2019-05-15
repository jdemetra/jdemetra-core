/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.regression;

import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.TsPeriod;
import jd.maths.matrices.FastMatrix;

/**
 *
 * @author palatej
 * @param <X>
 */
public interface RegressionVariableFactory <X extends ITsVariable> {
    boolean fill(X var, TsPeriod start, FastMatrix buffer);
    
    <D extends TimeSeriesDomain> boolean fill(X var, D domain, FastMatrix buffer);
}
