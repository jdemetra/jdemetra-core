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
package demetra.regarima;

import demetra.design.Development;
import demetra.design.LombokWorkaround;
import demetra.modelling.ChangeOfRegimeSpec;
import demetra.modelling.RegressionTestSpec;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.regression.TradingDaysType;
import demetra.util.Validatable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jean Palate, Mats Maggi
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder", buildMethodName = "buildWithoutValidation")
public final class TradingDaysSpec implements Validatable<TradingDaysSpec> {

    private static final TradingDaysSpec DEFAULT = TradingDaysSpec.builder().build();

    private String holidays;
    private List<String> userVariables;
    private TradingDaysType type;
    private LengthOfPeriodType lengthOfPeriodTime;
    private RegressionTestSpec test;
    private boolean autoAdjust;
    private int stockTradingDays;
    private ChangeOfRegimeSpec changeOfRegime;

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .type(TradingDaysType.None)
                .lengthOfPeriodTime(LengthOfPeriodType.None)
                .test(RegressionTestSpec.None)
                .autoAdjust(true)
                .stockTradingDays(0);
    }

    public boolean isUsed() {
        return type != TradingDaysType.None || userVariables != null || stockTradingDays != 0;
    }

    boolean isDefined() {
        return isUsed() && test == RegressionTestSpec.None;
    }

    public boolean isStockTradingDays() {
        return stockTradingDays != 0;
    }

    // TODO : Include in validate() ?
    public boolean isValid() {
        if (isStockTradingDays()) {
            return true;
        }
        if (test != RegressionTestSpec.None) {
            return type != TradingDaysType.None && lengthOfPeriodTime != LengthOfPeriodType.None;
        }
        if (type == TradingDaysType.None) {
            return lengthOfPeriodTime == LengthOfPeriodType.None;
        }
        return true;
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }

    @Override
    public TradingDaysSpec validate() throws IllegalArgumentException {
        return this;
    }

    public static class Builder implements Validatable.Builder<TradingDaysSpec> {

        public Builder type(TradingDaysType type) {
            this.type = type;
            this.userVariables = null;
            this.stockTradingDays = 0;
            return this;
        }

        /**
         *
         * @param w 1-based day of the month. Should be in [1, 31]
         * @return
         */
        public Builder stockTradingDays(int w) {
            this.stockTradingDays = w;
            this.holidays = null;
            this.userVariables = null;
            this.type = TradingDaysType.None;
            this.lengthOfPeriodTime = LengthOfPeriodType.None;
            return this;
        }

        public Builder holidays(String h) {
            this.holidays = h;
            if (holidays != null && holidays.length() == 0) {
                holidays = null;
            }
            if (holidays != null) {
                this.userVariables = null;
                stockTradingDays = 0;
            }
            return this;
        }

        public Builder userVariables(List<String> userVariables) {
            if (userVariables != null) {
                this.userVariables = new ArrayList<>(userVariables);
                this.holidays = null;
                this.type = TradingDaysType.None;
                this.lengthOfPeriodTime = LengthOfPeriodType.None;
                this.autoAdjust = false;
            }else
                this.userVariables=null;
            return this;
        }
    }

}
