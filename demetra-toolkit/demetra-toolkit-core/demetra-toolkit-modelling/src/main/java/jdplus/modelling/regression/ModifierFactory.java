/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.modelling.regression;

import demetra.timeseries.regression.ITsVariable;
import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.TsPeriod;
import jdplus.math.matrices.FastMatrix;
import demetra.timeseries.TimeSeriesInterval;
import demetra.timeseries.TsDomain;
import demetra.timeseries.regression.ModifiedTsVariable;

/**
 *
 * @author palatej
 * @param <X>
 */
public interface ModifierFactory <X extends ModifiedTsVariable.Modifier> {
    
    /**
     * Gets the domain necessary for computing the data corresponding to the given domain.
     * @param modifier
     * @param domain
     * @return The returned domain could have a different span and/or a different periodicity
     */
    TsDomain needFor(X modifier, TsDomain domain);
    TimeSeriesDomain needForGeneric(X modifier, TimeSeriesDomain domain);
    
    /**
     * Computes the output corresponding to the given input (starting at the given period)
     * @param modifier
     * @param start
     * @param input
     * @param output
     * @return 
     */
    boolean fill(X modifier, TsPeriod start, FastMatrix input, FastMatrix output);
    <P extends TimeSeriesInterval<?>, D extends TimeSeriesDomain<P>> boolean fill(X var, D domain, FastMatrix input, FastMatrix output);
}
