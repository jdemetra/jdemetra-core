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

import demetra.design.Development;
import demetra.design.LombokWorkaround;
import demetra.sa.DecompositionMode;
import demetra.util.Validatable;
import java.util.List;

/**
 *
 * @author Frank Osaer, Jean Palate, Mats Maggi
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder", buildMethodName = "buildWithoutValidation")
public final class X11Spec implements Validatable<X11Spec> {

    public static final double DEF_LSIGMA = 1.5, DEF_USIGMA = 2.5;
    public static final int DEF_FCASTS = -1, DEF_BCASTS = 0;

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
     * @param lsigma Lower sigma value for extreme values detection.
     */
    private double lsigma;

    /**
     * Upper sigma value for extreme values detection [sigmalim option in
     * X12-arima].
     *
     * @param usigma Upper sigma value for extreme values detection lsigma
     * should be lower than usigma and higher than .5.
     */
    private double usigma;

    /**
     * Length of the Henderson filter [trendma option in X12-Arima]. When the
     * length is 0, an automatic estimation of the length of the Henderson
     * filter is computed by the algorithm.
     *
     * @param hendersonFilterLength Length of the Henderson filter. When the
     * length is 0, an
     * automatic estimation is made by the program. Otherwise, the length should
     * be an odd number in the range [1, 101].
     */
    private int hendersonFilterLength;

    /**
     * Number of forecasts used in X11. By default, 0. When pre-processing is
     * used, the number of forecasts corresponds usually to 1 year.
     *
     * @param forecasts The forecasts horizon to set. When
     * forecastsHorizon is negative, its absolute value corresponds to the
     * number of years of forecasting. For example, setForecastHorizon(-1) is
     * equivalent to setForecastHorizon(12) for monthly data and to
     * setForecastHorizon(4) for quarterly data.
     */
    private int forecasts;

    /**
     * Number of backcasts used in X11. By default, 0. When pre-processing is
     * used, the number of backcasts corresponds usually to 1 year.
     *
     * @param backcasts The backcasts horizon to set. When
     * backcastsHorizon is negative, its absolute value corresponds to the
     * number of years of backcasting. For example, setBackcastHorizon(-1) is
     * equivalent to setBackcastHorizon(12) for monthly data and to
     * setBackcastHorizon(4) for quarterly data.
     */
    private int backcasts;

    /**
     * Option of Calendarsigma[X12], specifies the calculation of the standard
     * error calculation used for outlier detection in the X11 part
     */
    private CalendarSigma calendarSigma;
    private List<SigmavecOption> sigmavec;
    private boolean excludeForecast;
    private BiasCorrection bias;

    private static final X11Spec DEFAULT = X11Spec.builder().build();

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .calendarSigma(CalendarSigma.None)
                .excludeForecast(false)
                .bias(BiasCorrection.Legacy)
                .hendersonFilterLength(0)
                .forecasts(DEF_FCASTS)
                .backcasts(DEF_BCASTS)
                .seasonal(true)
                .lsigma(DEF_LSIGMA)
                .usigma(DEF_USIGMA)
                .mode(DecompositionMode.Undefined);
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }

    @Override
    public X11Spec validate() throws IllegalArgumentException {
        if (usigma <= lsigma || lsigma <= 0.5) {
            throw new IllegalArgumentException("Invalid sigma options");
        }

        if (hendersonFilterLength < 0 || hendersonFilterLength > 101
                || (hendersonFilterLength != 0 && hendersonFilterLength % 2 == 0)) {
            throw new IllegalArgumentException("Invalid henderson length");
        }

        return this;
    }

    public static class Builder implements Validatable.Builder<X11Spec> {

        /**
         * Parameters for extreme values detection [sigmalim option in
         * X12-arima].
         *
         * @param lsigma Lower sigma value for extreme values detection
         * @param usigma Upper sigma value for extreme values detection lsigma
         * should be lower than usigma and higher than .5.
         * @return Builder with new lsigma and usigma values
         */
        public Builder sigma(double lsigma, double usigma) {
            this.lsigma = lsigma;
            this.usigma = usigma;
            return this;
        }

        public Builder lsigma(double lsigma) {
            if (this.usigma <= lsigma) {
                return sigma(lsigma, lsigma + .5);
            } else {
                return sigma(lsigma, this.usigma);
            }
        }

        public Builder usigma(double usigma) {
            if (usigma <= this.lsigma) {
                return sigma(usigma - .5, usigma);
            } else {
                return sigma(this.lsigma, usigma);
            }
        }
    }

}
