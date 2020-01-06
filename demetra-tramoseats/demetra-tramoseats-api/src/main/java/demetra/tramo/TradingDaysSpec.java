/*
 * Copyright 2019 National Bank of Belgium
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
package demetra.tramo;

import demetra.design.Development;
import demetra.design.LombokWorkaround;
import demetra.timeseries.regression.RegressionTestType;
import demetra.timeseries.regression.TradingDaysType;
import demetra.util.Validatable;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder", buildMethodName = "buildWithoutValidation")
public final class TradingDaysSpec implements Validatable<TradingDaysSpec> {

    private static final TradingDaysSpec DEFAULT = TradingDaysSpec.builder().build();

    public static enum AutoMethod {
        Unused,
        FTest,
        WaldTest
    }

    public static final double DEF_PFTD = .01;

    private String holidays;
    private List<String> userVariables;
    private TradingDaysType tradingDaysType;
    private boolean leapYear;
    private RegressionTestType regressionTestType;
    private int stockTradingDays;
    private AutoMethod automaticMethod;
    private double probabilityForFTest;

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .tradingDaysType(TradingDaysType.None)
                .regressionTestType(RegressionTestType.None)
                .stockTradingDays(0)
                .automaticMethod(AutoMethod.Unused)
                .probabilityForFTest(DEF_PFTD);
    }

    @Override
    public TradingDaysSpec validate() throws IllegalArgumentException {
        if (probabilityForFTest <= 0 || probabilityForFTest > .1) {
            throw new IllegalArgumentException("Probability for FTest must be > 0 and < 0.1");
        }
        return this;
    }

    public boolean isUsed() {
        return isAutomatic() || tradingDaysType != TradingDaysType.None || userVariables != null || stockTradingDays != 0;
    }

    public boolean isDefined() {
        return userVariables != null || (stockTradingDays != 0 && regressionTestType == RegressionTestType.None)
                || ((leapYear || tradingDaysType != TradingDaysType.None)
                && (regressionTestType == RegressionTestType.None && automaticMethod == AutoMethod.Unused));
    }

    public boolean isAutomatic() {
        return automaticMethod != AutoMethod.Unused;
    }

    public boolean isStockTradingDays() {
        return stockTradingDays != 0;
    }

    public boolean isValid() {
        if (isStockTradingDays() || isAutomatic()) {
            return true;
        }
        if (regressionTestType.isUsed()) {
            return tradingDaysType != TradingDaysType.None && leapYear;
        }
        if (tradingDaysType == TradingDaysType.None) {
            return !leapYear;
        }
        return true;
    }

    public boolean isTest() {
        return regressionTestType.isUsed();
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }

    public static class Builder implements Validatable.Builder<TradingDaysSpec> {

        public Builder automatic(boolean value) {
            this.automaticMethod = value ? AutoMethod.FTest : AutoMethod.Unused;
            return this;
        }

        public Builder test(boolean test) {
            if (test) {
                this.regressionTestType = RegressionTestType.Separate_T;
            } else {
                this.regressionTestType = RegressionTestType.None;
            }
            return this;
        }

        // TODO : Check if it should be done here
        public Builder userVariables(List<String> value) {
            userVariables = value;
            if (userVariables != null) {
                holidays = null;
                tradingDaysType = TradingDaysType.None;
                leapYear = false;
                automaticMethod = AutoMethod.Unused;
                probabilityForFTest = DEF_PFTD;
            }
            return this;
        }

        // TODO : Check if it should be done here
        public Builder holidays(String value) {
            holidays = value;
            if (holidays != null && holidays.length() == 0) {
                holidays = null;
            }
            if (holidays != null) {
                userVariables = null;
            }
            return this;
        }

        /**
         *
         * @param w 1-based day of the month. Should be in [1, 31]
         * @return
         */
        public Builder stockTradingDays(int w) {
            this.stockTradingDays = w;
            holidays = null;
            userVariables = null;
            tradingDaysType = TradingDaysType.None;
            leapYear = false;
            automaticMethod = AutoMethod.Unused;
            probabilityForFTest = DEF_PFTD;
            return this;
        }

        public Builder tradingDaysType(TradingDaysType type) {
            tradingDaysType = type;
            userVariables = null;
            stockTradingDays = 0;
            return this;
        }
    }
}
