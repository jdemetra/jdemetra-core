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
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
public class TradingDaysSpec {

    public static enum AutoMethod {

        Unused,
        FTest,
        WaldTest
    }

    public static final double DEF_PFTD = .01;

    private String holidays;
    private String[] users;
    private TradingDaysType td = TradingDaysType.None;
    private boolean lp;
    private RegressionTestType test = RegressionTestType.None;
    private int w = 0;
    private AutoMethod auto = AutoMethod.Unused;
    private double pftd = DEF_PFTD;

    public TradingDaysSpec() {
    }

    public TradingDaysSpec(TradingDaysSpec other) {
        this.auto = other.auto;
        this.holidays = other.holidays;
        this.lp = other.lp;
        this.pftd = other.pftd;
        this.td = other.td;
        this.test = other.test;
        if (other.users != null) {
            this.users = other.users.clone();
        }
        this.w=other.w;
    }

    public void reset() {
        holidays = null;
        users = null;
        td = TradingDaysType.None;
        lp = false;
        test = RegressionTestType.None;
        w = 0;
        auto = AutoMethod.Unused;
        pftd = DEF_PFTD;
    }

    public TradingDaysType getTradingDaysType() {
        return td;
    }

    public boolean isUsed() {
        return isAutomatic() || td != TradingDaysType.None || users != null || w != 0;
    }

    public boolean isDefined() {
        return users != null || (w != 0 && test == RegressionTestType.None)
                || ((lp || td != TradingDaysType.None)
                && (test == RegressionTestType.None && auto == AutoMethod.Unused));
    }

    public boolean isAutomatic() {
        return auto != AutoMethod.Unused;
    }

    public void setAutomatic(boolean value) {
        auto = value ? AutoMethod.FTest : AutoMethod.Unused;
    }

    public AutoMethod getAutomaticMethod() {
        return auto;
    }

    public void setAutomaticMethod(AutoMethod m) {
        auto = m;
    }

    public double getProbabibilityForFTest() {
        return pftd;
    }

    public void setProbabibilityForFTest(double f) {
        if (f <= 0 || f > .1) {
            throw new IllegalArgumentException();
        }
        pftd = f;
    }

    public void setTradingDaysType(TradingDaysType value) {
        td = value;
        users = null;
        w = 0;
    }

    public boolean isLeapYear() {
        return lp;
    }

    public void setLeapYear(boolean value) {
        lp = value;
    }

    /**
     *
     * @param w 1-based day of the month. Should be in [1, 31]
     */
    public void setStockTradingDays(int w) {
        this.w = w;
        holidays = null;
        users = null;
        td = TradingDaysType.None;
        lp = false;
        auto = AutoMethod.Unused;
        pftd = DEF_PFTD;
    }

    public boolean isStockTradingDays() {
        return w != 0;
    }

    public int getStockTradingDays() {
        return w;
    }

    public boolean isValid() {
        if (isStockTradingDays() || isAutomatic()) {
            return true;
        }
        if (test.isUsed()) {
            return td != TradingDaysType.None && lp;
        }
        if (td == TradingDaysType.None) {
            return !lp;
        }
        return true;
    }

    public String getHolidays() {
        return holidays;
    }

    public void setHolidays(String value) {
        holidays = value;
//                if (holidays_ == CalendarManager.DEF)
//                    holidays_ = null;
        if (holidays != null && holidays.length() == 0) {
            holidays = null;
        }
        if (holidays != null) {
            users = null;
        }
    }

    public String[] getUserVariables() {
        return users;
    }

    public void setUserVariables(String[] value) {
        users = value;
//        if (users_ != null && users_.length == 0) {
//            users_ = null;
//        }
        if (users != null) {
            holidays = null;
            td = TradingDaysType.None;
            lp = false;
            auto = AutoMethod.Unused;
            pftd = DEF_PFTD;
        }
    }

    public boolean isTest() {
        return test.isUsed();
    }

    public void setTest(boolean test) {
        if (test) {
            this.test = RegressionTestType.Separate_T;
        } else {
            this.test = RegressionTestType.None;
        }
    }

    public RegressionTestType getRegressionTestType() {
        return test;
    }

    public void setRegressionTestType(RegressionTestType value) {
        test = value;
    }

    public boolean isDefault() {

        return auto == AutoMethod.Unused && w == 0 && td == TradingDaysType.None && lp == false && holidays == null && users == null;
    }

//        public ICalendarProvider Provider(TSContext context)
//        {
//            ICalendarProvider provider = null;
//            if (context != null && holidays_ != null)
//                provider = context.Calendars[holidays_];
//            if (provider == null)
//                provider = new DefaultCalendarProvider();
//            return provider;
//        }
    @Override
    public TradingDaysSpec clone() {
        try {
            TradingDaysSpec spec = (TradingDaysSpec) super.clone();
            if (users != null) {
                spec.users = users.clone();
            }
            return spec;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof TradingDaysSpec && equals((TradingDaysSpec) obj));
    }

    private boolean equals(TradingDaysSpec other) {
        // type
        if (!Arrays.deepEquals(users, other.users)
                || !Objects.equals(holidays, other.holidays) || w != other.w || auto != other.auto) {
            return false;
        }

        if (auto != AutoMethod.Unused) {
            return pftd == other.pftd;
        } else {
            return td == other.td && lp == other.lp && test == other.test;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.td);
        hash = 97 * hash + (this.lp ? 1 : 0);
        return hash;
    }

    public void disable() {
        holidays = null;
        users = null;
        td = TradingDaysType.None;
        test = RegressionTestType.None;
        lp = false;
        w = 0;
        auto = AutoMethod.Unused;
    }

}
