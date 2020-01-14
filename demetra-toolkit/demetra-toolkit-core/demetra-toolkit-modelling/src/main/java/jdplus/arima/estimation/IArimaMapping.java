/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.arima.estimation;

import jdplus.arima.IArimaModel;
import jdplus.math.functions.IParametricMapping;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 * @param <M>
 */
public interface IArimaMapping<M extends IArimaModel> extends IParametricMapping<M>{
    IArimaMapping<M> stationaryMapping();

    /**
     * Generates the parameters corresponding to the given Arima model
     * @param t
     * @return 
     */
    DoubleSeq parametersOf(M t);
}
