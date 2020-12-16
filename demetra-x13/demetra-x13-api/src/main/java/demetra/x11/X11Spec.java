/*
 * Copyright 2019 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
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
package demetra.x11;

import demetra.sa.DecompositionMode;
import demetra.util.Validatable;
import java.util.List;
import nbbrd.design.Development;
import nbbrd.design.LombokWorkaround;

/**
 *
 * @author Frank Osaer, Jean Palate, Mats Maggi
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder", buildMethodName = "buildWithoutValidation")
public final class X11Spec implements Validatable<X11Spec> {

    public static final double DEFAULT_LOWER_SIGMA = 1.5, DEFAULT_UPPER_SIGMA = 2.5;
    public static final int DEFAULT_FORECAST_HORIZON = 0, DEFAULT_BACKCAST_HORIZON = 0;

    /**
     * Decomposition mode of X11
     */
    private DecompositionMode mode;
    private boolean seasonal;

    @lombok.Singular
    private List<SeasonalFilterOption> filters;

    /**
     * Lower sigma value for extreme values detection [sigmalim option in
     * X12-arima].
     *
     * @param lowerSigma Lower sigma value for extreme values detection.
     * lowerSigma
     * should be lower than upperSigma and higher than .5.
     */
    private double lowerSigma;

    /**
     * Upper sigma value for extreme values detection [sigmalim option in
     * X12-arima].
     *
     * @param upperSigma Upper sigma value for extreme values detection
     */
    private double upperSigma;

    /**
     * Length of the Henderson filter [trendma option in X12-Arima]. When the
     * length is 0, an automatic estimation of the length of the Henderson
     * filter is computed by the algorithm. Otherwise, the length should
     * be an odd number in the range [1, 101].
     */
    private int hendersonFilterLength;

    /**
     * Number of forecasts used in X11. By default, 0. When pre-processing is
     * used, the number of forecasts corresponds usually to 1 year.
     *
     * @param forecastHorizon The forecasts horizon to set. When
     * forecastsHorizon is negative, its absolute value corresponds to the
     * number of years of forecasting. For example, setForecastHorizon(-1) is
     * equivalent to setForecastHorizon(12) for monthly data and to
     * setForecastHorizon(4) for quarterly data.
     */
    private int forecastHorizon;

    /**
     * Number of backcasts used in X11. By default, 0. When pre-processing is
     * used, the number of backcasts corresponds usually to 1 year.
     *
     * @param backcastHorizon The backcasts horizon to set. When
     * backcastsHorizon is negative, its absolute value corresponds to the
     * number of years of backcasting. For example, setBackcastHorizon(-1) is
     * equivalent to setBackcastHorizon(12) for monthly data and to
     * setBackcastHorizon(4) for quarterly data.
     */
    private int backcastHorizon;

    /**
     * Option of Calendarsigma[X12], specifies the calculation of the standard
     * error calculation used for outlier detection in the X11 part
     */
    private CalendarSigmaOption calendarSigma;
    @lombok.Singular("calendarSigma")
    private List<SigmaVecOption> sigmaVec;
    
    private boolean excludeForecast;
    private BiasCorrection bias;

    public static final X11Spec DEFAULT_UNDEFINED = X11Spec.builder()
            .forecastHorizon(-1)
            .mode(DecompositionMode.Undefined)
            .build();

    public static final X11Spec DEFAULT = X11Spec.builder()
            .build();

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .filter(SeasonalFilterOption.Msr)
                .calendarSigma(CalendarSigmaOption.None)
                .excludeForecast(false)
                .bias(BiasCorrection.Legacy)
                .hendersonFilterLength(0)
                .forecastHorizon(DEFAULT_FORECAST_HORIZON)
                .backcastHorizon(DEFAULT_BACKCAST_HORIZON)
                .seasonal(true)
                .lowerSigma(DEFAULT_LOWER_SIGMA)
                .upperSigma(DEFAULT_UPPER_SIGMA)
                .mode(DecompositionMode.Multiplicative);
    }

    public boolean isDefault() {
        return this.equals(DEFAULT_UNDEFINED);
    }

    @Override
    public X11Spec validate() throws IllegalArgumentException {
        if (upperSigma <= lowerSigma || lowerSigma <= 0.5) {
            throw new IllegalArgumentException("Invalid sigma options");
        }
        if (hendersonFilterLength < 0 || hendersonFilterLength > 101
                || (hendersonFilterLength != 0 && hendersonFilterLength % 2 == 0)) {
            throw new IllegalArgumentException("Invalid henderson length");
        }
        if (!calendarSigma.equals(CalendarSigmaOption.Signif) && ! sigmaVec.isEmpty()) {
            throw new X11Exception("Sigmavec mustn't be used without CalendarSigmaOption Signif");
        }
        if (calendarSigma.equals(CalendarSigmaOption.Signif) && sigmaVec.isEmpty()) {
            throw new X11Exception("SigmavecOptions not set for CalendarSigmaOption Signif");
        }
        return this;
    }

    public static class Builder implements Validatable.Builder<X11Spec> {
    }

}
