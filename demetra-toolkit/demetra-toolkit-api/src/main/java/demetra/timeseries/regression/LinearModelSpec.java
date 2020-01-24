/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.timeseries.regression;

import demetra.timeseries.TsData;
import demetra.timeseries.calendars.LengthOfPeriodType;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.Builder
public class LinearModelSpec {

    /**
     * Original series
     */
    private TsData series;
    /**
     * Log transformation
     */
    private boolean logTransformation;
    /**
     * Transformation for leap year or length of period
     */
    private LengthOfPeriodType lengthOfPeriodTransformation;
    /**
     * Pre-adjustment variables (with their corresponding coefficients)
     */
    private PreadjustmentVariable[] preadjustmentVariables;

    /**
     * Regression variables
     */
    private Variable[] variables;

}
