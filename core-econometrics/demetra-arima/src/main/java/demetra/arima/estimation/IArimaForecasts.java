/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.arima.estimation;

import demetra.arima.IArimaModel;
import demetra.data.DoubleSequence;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface IArimaForecasts {
    /**
     * Initiliazes the forecasts routine
     * @param model The Arima model used for forecazsting
     * @param mean Mean correction. The correction must be computed
     * @return True if the initialization was succesful, false otherwise
     */
    boolean prepare(final IArimaModel model, final boolean mean);
    
    /**
     * 
     * @param model
     * @param mean Mean correction (provided)
     * @return 
     */
    boolean prepare(final IArimaModel model, final double mean);
    /**
     * 
     * @param data The forecasted series
     * @param nforecasts The number of forecasts
     * @return The forecasts
     */
    DoubleSequence forecasts(DoubleSequence data, final int nforecasts);
    
    default DoubleSequence backcasts(DoubleSequence data, final int nbackcasts){
        return forecasts(data.reverse(), nbackcasts).reverse();
    }
}
