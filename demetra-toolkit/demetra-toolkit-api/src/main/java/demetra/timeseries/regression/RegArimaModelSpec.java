/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.timeseries.regression;

/**
 *
 * @author palatej
 * @param <S> Stochastic component. Typically, (S)arima models (+ mean effect)
 */
@lombok.Value
public class RegArimaModelSpec<S> {

    private LinearModelSpec linearModel;
    @lombok.NonNull
    private S arima;

}
