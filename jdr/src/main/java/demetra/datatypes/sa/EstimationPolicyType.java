/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.datatypes.sa;

import ec.tstoolkit.design.IntValue;

/**
 *
 * @author Jean Palate
 */
public enum EstimationPolicyType implements IntValue{

    /**
     *
     */
    None(0),
    /**
     * The complete model is re-estimated
     */
    Complete(99),
    /**
     * The model is completely re-estimated excepting the log/level mode and the
     * automatic calendar options (if any)
     */
    Outliers_StochasticComponent(90),
    /**
     * The outliers are re-identified and all the parameters are re-estimated.
     * The form of the model itself (log/level, calendars, ARIMA) is not
     * reviewed
     */
    Outliers(80),
    /**
     * The last outliers are re-identified and all the parameters are
     * re-estimated. The form of the model itself (log/level, calendars, ARIMA)
     * and the first outliers are not reviewed
     */
    LastOutliers(75),
    /**
     * All the parameters are re-estimated, but the structural form of the model
     * is unchanged
     */
    FreeParameters(40),
    /**
     * The auto-regressive parameters used in the definition of the filters are considered as
     * fixed. In the case of Seats, the roots of the ARIMA model are always distributed the same way.
     */
    FixedAutoRegressiveParameters(35),
    /**
     * The parameters used in the definition of the filters are considered as
     * fixed. In the case of Seats, the ARIMA model is unchanged; in the case of
     * X13, both the ARIMA model and the x11 filters are unchanged.
     */
    FixedParameters(30),
    /**
     * All the parameters of the model are fixed (including the regression coefficients)
     */
    Fixed(20),
    /**
     * The new sa series is only based on previously computed seasonal factors (estimates+forecasts)
     */
    @Deprecated
    UseForecasts(10),
    /**
     * The new sa series is only based on previously computed seasonal factors (estimates+forecasts)
     * Nomenclature in line with the guidelines
     */
    Current(10),
    /**
     * 
     */
    Interactive(-1);
    
    /**
     * Enum correspondence to an integer
     * 
     * @param value
     *            Integer representation of the frequency
     * @return Enum representation of the frequency
     */
    public static EstimationPolicyType valueOf(int value) {
        return IntValue.valueOf(EstimationPolicyType.class, value).orElse(null);
    }

    private final int value;

  
    private EstimationPolicyType(final int value) {
	this.value = value;
    }

    /**
     * Integer representation of the policy
     * Higher value means more general estimation (except for interactive)
     * 
     * @return The number of events by year
     */
    @Override
    public int intValue() {
	return value;
    }
    
}
