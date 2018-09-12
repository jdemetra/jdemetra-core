/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.regarima;

import demetra.arima.IArimaModel;
import demetra.data.DoubleSequence;
import demetra.maths.functions.IParametricMapping;

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
    DoubleSequence parametersOf(M t);
}
