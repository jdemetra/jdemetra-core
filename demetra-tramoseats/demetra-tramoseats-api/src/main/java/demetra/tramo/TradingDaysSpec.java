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
package demetra.tramo;

import demetra.modelling.regression.RegressionTestType;
import demetra.modelling.regression.TradingDaysType;

/**
 *
 * @author Jean Palate
 */
@lombok.Data
public final class TradingDaysSpec implements Cloneable {

    public static enum AutoMethod {
        Unused,
        FTest,
        WaldTest
    }

    public static final double DEF_PFTD = .01;

    private String holidays;
    private String[] userVariables;
    private TradingDaysType tradingDaysType = TradingDaysType.None;
    private boolean leapYear;
    private RegressionTestType regressionTestType = RegressionTestType.None;
    private int stockTradingDays = 0;
    private AutoMethod automaticMethod = AutoMethod.Unused;
    private double probabilityForFTest = DEF_PFTD;

    public TradingDaysSpec() {
    }

    @Override
    public TradingDaysSpec clone() {
        try {
            TradingDaysSpec c = (TradingDaysSpec) super.clone();
             if (userVariables != null) {
                c.userVariables = userVariables.clone();
            }
             return c;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    public void reset() {
        holidays = null;
        userVariables = null;
        tradingDaysType = TradingDaysType.None;
        leapYear = false;
        regressionTestType = RegressionTestType.None;
        stockTradingDays = 0;
        automaticMethod = AutoMethod.Unused;
        probabilityForFTest = DEF_PFTD;
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

    public void setAutomatic(boolean value) {
        automaticMethod = value ? AutoMethod.FTest : AutoMethod.Unused;
    }

 
    public void setProbabibilityForFTest(double f) {
        if (f <= 0 || f > .1) {
            throw new IllegalArgumentException();
        }
        probabilityForFTest = f;
    }

    public void setTradingDaysType(TradingDaysType value) {
        tradingDaysType = value;
        userVariables = null;
        stockTradingDays = 0;
    }

    /**
     *
     * @param w 1-based day of the month. Should be in [1, 31]
     */
    public void setStockTradingDays(int w) {
        this.stockTradingDays = w;
        holidays = null;
        userVariables = null;
        tradingDaysType = TradingDaysType.None;
        leapYear = false;
        automaticMethod = AutoMethod.Unused;
        probabilityForFTest = DEF_PFTD;
    }

    public boolean isStockTradingDays() {
        return stockTradingDays != 0;
    }

    public int getStockTradingDays() {
        return stockTradingDays;
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

    public void setHolidays(String value) {
        holidays = value;
        if (holidays != null && holidays.length() == 0) {
            holidays = null;
        }
        if (holidays != null) {
            userVariables = null;
        }
    }

    public void setUserVariables(String[] value) {
        userVariables = value;
        if (userVariables != null) {
            holidays = null;
            tradingDaysType = TradingDaysType.None;
            leapYear = false;
            automaticMethod = AutoMethod.Unused;
            probabilityForFTest = DEF_PFTD;
        }
    }

    public boolean isTest() {
        return regressionTestType.isUsed();
    }

    public void setTest(boolean test) {
        if (test) {
            this.regressionTestType = RegressionTestType.Separate_T;
        } else {
            this.regressionTestType = RegressionTestType.None;
        }
    }

    public boolean isDefault() {

        return automaticMethod == AutoMethod.Unused && stockTradingDays == 0 && tradingDaysType == TradingDaysType.None && leapYear == false && holidays == null && userVariables == null;
    }

    public void disable() {
        holidays = null;
        userVariables = null;
        tradingDaysType = TradingDaysType.None;
        regressionTestType = RegressionTestType.None;
        leapYear = false;
        stockTradingDays = 0;
        automaticMethod = AutoMethod.Unused;
    }

}
