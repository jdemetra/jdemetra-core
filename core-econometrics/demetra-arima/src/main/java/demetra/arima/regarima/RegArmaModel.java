/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.arima.regarima;

import demetra.arima.IArimaModel;
import demetra.design.Immutable;
import demetra.linearmodel.LinearModel;

/**
 * Linear model with stationary ARMA process
 *
 * @author Jean Palate <jean.palate@nbb.be>
 * @param <M>
 */
@Immutable
@lombok.Value
public class RegArmaModel<M extends IArimaModel> {

    @lombok.NonNull
    LinearModel linearModel;
    @lombok.NonNull
    M arma;
    int missingCount;

}
