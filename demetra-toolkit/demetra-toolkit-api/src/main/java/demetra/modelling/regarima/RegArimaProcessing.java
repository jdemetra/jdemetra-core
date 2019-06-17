/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.regarima;

import demetra.arima.ArimaType;
import demetra.arima.SarimaModel;
import demetra.modelling.regression.PreadjustmentVariable;
import demetra.modelling.regression.Variable;
import demetra.regarima.RegArimaModel;
import demetra.timeseries.TsData;
import demetra.timeseries.calendars.LengthOfPeriodType;
import java.util.List;

/**
 *
 * @author Jean Palate
 * @param <S> Type of the Arima model
 * @param <P> Describes a specification for an Arima model of type S
 */
public interface RegArimaProcessing<S extends ArimaType, P extends ArimaSpecification<S> >  {

    /**
     * Complete specification of the model
     * @param <S>
     * @param <P> 
     */
    @lombok.Value
    @lombok.Builder
    public static class Specification <S extends ArimaType, P extends ArimaSpecification<S> >{

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
        @lombok.Singular
        /**
         * Pre-adjustment variables (with their corresponding coefficients)
         */
        private List<PreadjustmentVariable> preadjustmentVariables;
        /**
         * (Trend)constant correction. The exact meaning of the constant depends
         * on the differencing degree of the Sarima model
         */
        private boolean mean;
        @lombok.Singular
        /**
         * Regression variables
         */
        private List<Variable> variables;

        /**
         * Specification of the Sarima model (orders + initial/fixed parameters,
         * if any)
         */
        private P arima;
    }
    
    /**
     * Gets the specification of the processing (series, transformations, regression
     * variables (perhaps with pre-specified effects) and Arima model
     * 
     * @return 
     */
    Specification<S, P> getSpecification();
    
    /**
     * Gets the estimation of the processing.
     * The estimation should not contain pre-specified variables; moreover, the
     * ML information should be defined without the fixed parameters, if any.
     * @return 
     */
    RegArimaModel<S> getEstimation();
}
