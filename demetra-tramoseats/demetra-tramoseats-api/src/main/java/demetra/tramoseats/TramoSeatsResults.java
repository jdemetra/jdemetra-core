/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramoseats;

import demetra.arima.SarimaModel;
import demetra.seats.SeatsResults;
import demetra.timeseries.regression.modelling.LinearModelEstimation;

/**
 *
 * @author palatej
 */
@lombok.Value
public class TramoSeatsResults {
    private LinearModelEstimation<SarimaModel> preprocessing;
    private SeatsResults decomposition;
}
