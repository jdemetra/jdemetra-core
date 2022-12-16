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

import demetra.data.Parameter;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.calendars.TradingDaysType;
import nbbrd.design.Development;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class TradingDaysSpec {

    public static final boolean DEF_ADJUST = false, DEF_SIMPLIFIED = true;

    private static final TradingDaysSpec NONE = new TradingDaysSpec(null, null, TradingDaysType.NONE,
            LengthOfPeriodType.None, RegressionTestType.None, DEF_ADJUST, DEF_SIMPLIFIED, 0, AutoMethod.UNUSED, 0, null, null);

    public static TradingDaysSpec stockTradingDays(int w, RegressionTestType type) {
        return new TradingDaysSpec(null, null, TradingDaysType.TD7,
                LengthOfPeriodType.None, type, DEF_ADJUST, DEF_SIMPLIFIED, w, AutoMethod.UNUSED, 0, null, null);
    }

    public static TradingDaysSpec stockTradingDays(int w, @NonNull Parameter[] tdcoeff) {
        if (tdcoeff.length != 6) {
            throw new IllegalArgumentException();
        }
        return new TradingDaysSpec(null, null, TradingDaysType.TD7,
                LengthOfPeriodType.None, RegressionTestType.None, DEF_ADJUST, DEF_SIMPLIFIED, w, AutoMethod.UNUSED, 0, tdcoeff, null);
    }

    public static TradingDaysSpec none() {
        return NONE;
    }

    public static TradingDaysSpec userDefined(@NonNull String[] vars, RegressionTestType type) {
        return new TradingDaysSpec(null, vars, TradingDaysType.NONE,
                LengthOfPeriodType.None, type, DEF_ADJUST, DEF_SIMPLIFIED, 0, AutoMethod.UNUSED, 0, null, null);
    }

    public static TradingDaysSpec userDefined(@NonNull String[] vars, @NonNull Parameter[] coeff) {
        if (coeff.length != vars.length) {
            throw new IllegalArgumentException();
        }
        return new TradingDaysSpec(null, vars, TradingDaysType.NONE,
                LengthOfPeriodType.None, RegressionTestType.None, DEF_ADJUST, DEF_SIMPLIFIED, 0, AutoMethod.UNUSED, 0, coeff, null);
    }

    public static TradingDaysSpec automaticHolidays(String holidays, LengthOfPeriodType lp, AutoMethod automaticMethod, double probabilityForFTest, boolean autoadjust) {
        if (automaticMethod == AutoMethod.UNUSED) {
            throw new IllegalArgumentException();
        }
        return new TradingDaysSpec(holidays, null, TradingDaysType.TD7,
                lp, RegressionTestType.None, autoadjust, DEF_SIMPLIFIED, 0, automaticMethod, probabilityForFTest, null, null);
    }

    public static TradingDaysSpec automatic(LengthOfPeriodType lp, AutoMethod automaticMethod, double probabilityForFTest, boolean autoadjust) {
        if (automaticMethod == AutoMethod.UNUSED) {
            throw new IllegalArgumentException();
        }
        return new TradingDaysSpec(null, null, TradingDaysType.TD7,
                lp, RegressionTestType.None, autoadjust, DEF_SIMPLIFIED, 0, automaticMethod, probabilityForFTest, null, null);
    }

    public static TradingDaysSpec holidays(String holidays, TradingDaysType type, LengthOfPeriodType lp, RegressionTestType regtype, boolean autoadjust) {
        if (type == TradingDaysType.NONE) {
            throw new IllegalArgumentException();
        }
        return new TradingDaysSpec(holidays, null, type,
                lp, regtype, autoadjust, DEF_SIMPLIFIED, 0, AutoMethod.UNUSED, 0, null, null);
    }

    public static TradingDaysSpec holidays(String holidays, TradingDaysType type, LengthOfPeriodType lp, Parameter[] ctd, Parameter clp) {
        if (type == TradingDaysType.NONE) {
            throw new IllegalArgumentException();
        }
        return new TradingDaysSpec(holidays, null, type,
                lp, RegressionTestType.None, false, DEF_SIMPLIFIED, 0, AutoMethod.UNUSED, 0, ctd, clp);
    }

    public static TradingDaysSpec td(TradingDaysType type, LengthOfPeriodType lp, RegressionTestType regtype, boolean autoadjust) {
        if (type == TradingDaysType.NONE) {
            throw new IllegalArgumentException();
        }
        return new TradingDaysSpec(null, null, type,
                lp, regtype, autoadjust, DEF_SIMPLIFIED, 0, AutoMethod.UNUSED, 0, null, null);
    }

    public static TradingDaysSpec td(TradingDaysType type, LengthOfPeriodType lp, Parameter[] tdcoeff, Parameter lpcoeff) {
//        if (type == TradingDaysType.NONE) {
//            throw new IllegalArgumentException();
//        }
//        if (lp == LengthOfPeriodType.None && Parameter.isDefined(lpcoeff)) {
//            throw new IllegalArgumentException();
//        }
       if (type == TradingDaysType.NONE && lp == LengthOfPeriodType.None) {
            throw new IllegalArgumentException();
        }

        return new TradingDaysSpec(null, null, type,
                lp, RegressionTestType.None, DEF_ADJUST, DEF_SIMPLIFIED, 0, AutoMethod.UNUSED, 0, tdcoeff, lpcoeff);
    }

    public static enum AutoMethod {
        UNUSED,
        FTEST,
        WALD,
        BIC,
        AIC
    }

    public static final double DEF_PFTD = .01;

    private String holidays;
    private String[] userVariables;
    private TradingDaysType tradingDaysType;
    private LengthOfPeriodType lengthOfPeriodType;
    private RegressionTestType regressionTestType;
    private boolean autoAdjust;
    private boolean simplified;
    private int stockTradingDays;
    private AutoMethod automaticMethod;
    private double probabilityForFTest;

    private Parameter[] tdCoefficients;
    private Parameter lpCoefficient;

    public boolean isUsed() {
        return isAutomatic() || tradingDaysType != TradingDaysType.NONE || userVariables != null || stockTradingDays != 0;
    }

    public boolean isDefined() {
        return userVariables != null || (stockTradingDays != 0 && regressionTestType == RegressionTestType.None)
                || ((lengthOfPeriodType != LengthOfPeriodType.None || tradingDaysType != TradingDaysType.NONE)
                && (regressionTestType == RegressionTestType.None && automaticMethod == AutoMethod.UNUSED));
    }

    public boolean isAutomatic() {
        return automaticMethod != AutoMethod.UNUSED;
    }

    public boolean isStockTradingDays() {
        return stockTradingDays != 0;
    }

    public boolean isUserDefined() {
        return userVariables != null;
    }

    public boolean isDefaultTradingDays() {
        return userVariables == null && holidays == null && stockTradingDays == 0 && tradingDaysType != TradingDaysType.NONE;
    }

    public boolean isHolidays() {
        return holidays != null;
    }

    public boolean isValid() {
        if (isStockTradingDays() || isAutomatic()) {
            return true;
        }
        if (regressionTestType.isUsed()) {
            return tradingDaysType != TradingDaysType.NONE && lengthOfPeriodType != LengthOfPeriodType.None;
        }
//        if (tradingDaysType == TradingDaysType.NONE) {
//            return lengthOfPeriodType == LengthOfPeriodType.None;
//        }
        return true;
    }

    public boolean isTest() {
        return regressionTestType.isUsed();
    }

    public boolean isDefault() {
        return this.equals(NONE);
    }

    public TradingDaysSpec withCoefficients(Parameter[] tdc, Parameter lpc) {
        return new TradingDaysSpec(holidays, userVariables, tradingDaysType, lengthOfPeriodType,
                RegressionTestType.None, autoAdjust, simplified, stockTradingDays, AutoMethod.UNUSED, probabilityForFTest, tdc, lpc);
    }

    public boolean hasFixedCoefficients() {
        return (lpCoefficient != null && lpCoefficient.isFixed())
                || Parameter.hasFixedParameters(tdCoefficients);
    }
}
