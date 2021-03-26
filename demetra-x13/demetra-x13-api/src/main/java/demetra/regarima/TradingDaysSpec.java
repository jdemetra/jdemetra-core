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

import demetra.data.Parameter;
import nbbrd.design.Development;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.calendars.TradingDaysType;
import demetra.util.Validatable;
import lombok.NonNull;

/**
 *
 * @author Jean Palate, Mats Maggi
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class TradingDaysSpec implements Validatable<TradingDaysSpec> {

    private String holidays;
    private String[] userVariables;
    private TradingDaysType tradingDaysType;
    private LengthOfPeriodType lengthOfPeriodType;
    private RegressionTestSpec regressionTestType;
    private boolean autoAdjust;
    private int stockTradingDays;
    private Parameter[] tdCoefficients;
    private Parameter lpCoefficient;

    private static final TradingDaysSpec NONE = new TradingDaysSpec(null, null, TradingDaysType.None,
            LengthOfPeriodType.None, RegressionTestSpec.None, false, 0, null, null);

    public static TradingDaysSpec stockTradingDays(int w, RegressionTestSpec test) {
        return new TradingDaysSpec(null, null, TradingDaysType.None,
                LengthOfPeriodType.None, test, false, w, null, null);
    }

    public static TradingDaysSpec stockTradingDays(int w, @NonNull Parameter[] tdc) {
        return new TradingDaysSpec(null, null, TradingDaysType.None,
                LengthOfPeriodType.None, RegressionTestSpec.None, false, w, tdc, null);
    }

    public static TradingDaysSpec none() {
        return NONE;
    }

    public static TradingDaysSpec userDefined(@NonNull String[] vars, RegressionTestSpec test) {
        return new TradingDaysSpec(null, vars, TradingDaysType.None,
                LengthOfPeriodType.None, test, false, 0, null, null);
    }

    public static TradingDaysSpec userDefined(@NonNull String[] vars, @NonNull Parameter[] tdcoeff) {
        return new TradingDaysSpec(null, vars, TradingDaysType.None,
                LengthOfPeriodType.None, RegressionTestSpec.None, false, 0, tdcoeff, null);
    }

    public static TradingDaysSpec holidays(String holidays, TradingDaysType type, LengthOfPeriodType lp, RegressionTestSpec test, boolean autoAdjust) {
        if (type == TradingDaysType.None) {
            throw new IllegalArgumentException();
        }
        return new TradingDaysSpec(holidays, null, type,
                lp, test, autoAdjust, 0, null, null);
    }

    public static TradingDaysSpec holidays(String holidays, TradingDaysType type, LengthOfPeriodType lp, Parameter[] tdcoeff, Parameter lpcoeff) {
        if (type == TradingDaysType.None) {
            throw new IllegalArgumentException();
        }
        return new TradingDaysSpec(holidays, null, type,
                lp, RegressionTestSpec.None, false, 0, tdcoeff, lpcoeff);
    }

    public static TradingDaysSpec td(TradingDaysType type, LengthOfPeriodType lp, RegressionTestSpec test, boolean autoAdjust) {
        if (type == TradingDaysType.None) {
            throw new IllegalArgumentException();
        }
        return new TradingDaysSpec(null, null, type,
                lp, test, autoAdjust, 0, null, null);
    }

    public static TradingDaysSpec td(TradingDaysType type, LengthOfPeriodType lp, Parameter[] tdcoeff, Parameter lpcoeff) {
        if (type == TradingDaysType.None) {
            throw new IllegalArgumentException();
        }
        return new TradingDaysSpec(null, null, type,
                lp, RegressionTestSpec.None, false, 0, tdcoeff, lpcoeff);
    }

    public TradingDaysSpec withCoefficients(Parameter[] tdc, Parameter lpc) {
        return new TradingDaysSpec(holidays, userVariables, tradingDaysType, lengthOfPeriodType,
                RegressionTestSpec.None, false, stockTradingDays, tdc, lpc);
    }

    public boolean isUsed() {
        return tradingDaysType != TradingDaysType.None || userVariables != null || stockTradingDays != 0;
    }

    boolean isDefined() {
        return isUsed() && regressionTestType == RegressionTestSpec.None;
    }

    public boolean isStockTradingDays() {
        return stockTradingDays != 0;
    }

    public boolean isUserDefined() {
        return userVariables != null;
    }

    public boolean isDefaultTradingDays() {
        return userVariables == null && holidays == null && stockTradingDays == 0 && tradingDaysType != TradingDaysType.None;
    }

    public boolean isHolidays() {
        return holidays != null;
    }

    // TODO : Include in validate() ?
    public boolean isValid() {
        if (isStockTradingDays()) {
            return true;
        }
        if (regressionTestType != RegressionTestSpec.None) {
            return tradingDaysType != TradingDaysType.None && lengthOfPeriodType != LengthOfPeriodType.None;
        }
        if (tradingDaysType == TradingDaysType.None) {
            return lengthOfPeriodType == LengthOfPeriodType.None;
        }
        return true;
    }

    public boolean isDefault() {
        return this.equals(NONE);
    }

    @Override
    public TradingDaysSpec validate() throws IllegalArgumentException {
        return this;
    }

}
