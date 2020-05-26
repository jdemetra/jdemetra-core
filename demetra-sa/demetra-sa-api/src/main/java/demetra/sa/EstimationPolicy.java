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
package demetra.sa;

/**
 *
 * @author PALATEJ
 */
public enum EstimationPolicy{

    /**
     *
     */
    None,
    /**
     * The complete model is re-estimated
     */
    Complete,
    /**
     * The model is completely re-estimated excepting the log/level mode and the
     * automatic calendar options (if any)
     */
    Outliers_StochasticComponent,
    /**
     * The outliers are re-identified and all the parameters are re-estimated.
     * The form of the model itself (log/level, calendars, ARIMA) is not
     * reviewed
     */
    Outliers,
    /**
     * The last outliers are re-identified and all the parameters are
     * re-estimated. The form of the model itself (log/level, calendars, ARIMA)
     * and the first outliers are not reviewed
     */
    LastOutliers,
    /**
     * All the parameters are re-estimated, but the structural form of the model
     * is unchanged
     */
    FreeParameters,
    /**
     * The auto-regressive parameters used in the definition of the filters are considered as
     * fixed. In the case of Seats, the roots of the ARIMA model are always distributed the same way.
     */
    FixedAutoRegressiveParameters,
    /**
     * The parameters used in the definition of the filters are considered as
     * fixed. In the case of Seats, the ARIMA model is unchanged; in the case of
     * X13, both the ARIMA model and the x11 filters are unchanged.
     */
    FixedParameters,
    /**
     * All the parameters of the model are fixed (including the regression coefficients)
     */
    Fixed,
    /**
     * The new sa series is only based on previously computed seasonal factors (estimates+forecasts)
     * Nomenclature in line with the guidelines
     */
    Current,
    /**
     * 
     */
    Interactive,
    /**
     * 
     */
    Custom;
    
}
