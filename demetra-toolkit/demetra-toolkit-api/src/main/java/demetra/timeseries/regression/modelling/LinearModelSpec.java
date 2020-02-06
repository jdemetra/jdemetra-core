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
import demetra.timeseries.regression.PreadjustmentVariable;
import demetra.timeseries.regression.Variable;

/**
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
     * Pre-adjustment variables (with their corresponding coefficients)
     */
    private PreadjustmentVariable[] preadjustmentVariables;

    /**
     * Regression variables
     */
    private boolean meanCorrection;
    private Variable[] variables;

    private M stochasticComponent;
}
