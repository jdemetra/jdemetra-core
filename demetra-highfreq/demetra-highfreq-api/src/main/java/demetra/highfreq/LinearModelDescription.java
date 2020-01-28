/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.highfreq;

import demetra.data.Range;
import demetra.timeseries.TimeSeriesData;
import demetra.timeseries.TimeSeriesObs;
import demetra.timeseries.regression.PreadjustmentVariable;
import demetra.timeseries.regression.Variable;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author Jean Palate
 * @param <P> Time period
 * @param <O> Observation
 */
@lombok.Value
@lombok.Builder
public class LinearModelDescription<P extends Range<LocalDateTime>, O extends TimeSeriesObs<P> >  {

    /**
     * Original series
     */
    private TimeSeriesData<P, O> series;
    /**
     * Log transformation
     */
    private boolean logTransformation;

    /**
     * Pre-adjustment variables (with their corresponding coefficients)
     */
    private PreadjustmentVariable[] preadjustmentVariables;

    /**
     * Regression variables
     */
    private Variable[] variables;

}
