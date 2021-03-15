/*
 * Copyright 2020 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.timeseries.regression.modelling;

import demetra.timeseries.TsData;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.regression.Variable;

/**
 * Describes a regression model, with any stochastic component
 * The parameters of the model (regression and stochastic) can be provided
 * (fixed or not). 
 * 
 * @author Jean Palate
 * @param <M>
 */
@lombok.Value
@lombok.Builder
public class LinearModelSpec<M> {

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
     * Regression variables (including mean correction)
     */
    private Variable[] variables;
    
    /**
     * For instance SarimaSpec
     */
    private M stochasticComponent;
}
