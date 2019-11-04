/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.regression;

import demetra.timeseries.TsData;
import demetra.timeseries.calendars.LengthOfPeriodType;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.Builder
public class LinearRegressionComponent {

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
    @lombok.Singular
    private List<PreadjustmentVariable> preadjustmentVariables;

    /**
     * Regression variables
     */
    @lombok.Singular
    private List<Variable> variables;

    /**
     * Specification of the Sarima model (orders + initial/fixed parameters, if
     * any)
     */
}
