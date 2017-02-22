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
package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetSerializable;
import ec.tstoolkit.modelling.RegressionTestType;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
public class TradingDaysSpec implements Cloneable, InformationSetSerializable {

    public static final String AUTO = "auto", MAUTO = "mauto", PFTD = "pftd", TDOPTION = "option", LPOPTION = "leapyear", HOLIDAYS = "holidays", USER = "user", TEST = "test", TESTTYPE = "testtype", W = "stocktd";

    public static enum AutoMethod {

        Unused,
        FTest,
        WaldTest
    }

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, AUTO), Boolean.class);
        dic.put(InformationSet.item(prefix, MAUTO), String.class);
        dic.put(InformationSet.item(prefix, PFTD), Double.class);
        dic.put(InformationSet.item(prefix, TDOPTION), String.class);
        dic.put(InformationSet.item(prefix, LPOPTION), String.class);
        dic.put(InformationSet.item(prefix, USER), String[].class);
        dic.put(InformationSet.item(prefix, HOLIDAYS), String.class);
        dic.put(InformationSet.item(prefix, W), Integer.class);
        dic.put(InformationSet.item(prefix, TESTTYPE), String.class);
    }

    public static final double DEF_PFTD = .01;

    private String holidays_;
    private String[] users_;
    private TradingDaysType td_ = TradingDaysType.None;
    private boolean lp_;
    private RegressionTestType test_ = RegressionTestType.None;
    private int w_ = 0;
    private AutoMethod auto_ = AutoMethod.Unused;
    private double pftd_ = DEF_PFTD;

    public TradingDaysSpec() {
    }

    public void reset() {
        holidays_ = null;
        users_ = null;
        td_ = TradingDaysType.None;
        lp_ = false;
        test_ = RegressionTestType.None;
        w_ = 0;
        auto_ = AutoMethod.Unused;
        pftd_ = DEF_PFTD;
    }

    public TradingDaysType getTradingDaysType() {
        return td_;
    }

    public boolean isUsed() {
        return isAutomatic() || td_ != TradingDaysType.None || users_ != null || w_ != 0;
    }

    public boolean isDefined() {
        return users_ != null || (w_ != 0 && test_ == RegressionTestType.None)
                || ((lp_ || td_ != TradingDaysType.None)
                && (test_ == RegressionTestType.None && auto_ == AutoMethod.Unused));
    }

    public boolean isAutomatic() {
        return auto_ != AutoMethod.Unused;
    }

    public void setAutomatic(boolean value) {
        auto_ = value ? AutoMethod.FTest : AutoMethod.Unused;
    }

    public AutoMethod getAutomaticMethod() {
        return auto_;
    }

    public void setAutomaticMethod(AutoMethod m) {
        auto_ = m;
    }

    public double getProbabibilityForFTest() {
        return pftd_;
    }

    public void setProbabibilityForFTest(double f) {
        if (f <= 0 || f > .1) {
            throw new IllegalArgumentException();
        }
        pftd_ = f;
    }

    public void setTradingDaysType(TradingDaysType value) {
        td_ = value;
        users_ = null;
        w_=0;
    }

    public boolean isLeapYear() {
        return lp_;
    }

    public void setLeapYear(boolean value) {
        lp_ = value;
    }

    /**
     *
     * @param w 1-based day of the month. Should be in [1, 31]
     */
    public void setStockTradingDays(int w) {
        w_ = w;
        holidays_ = null;
        users_ = null;
        td_ = TradingDaysType.None;
        lp_ = false;
        auto_ = AutoMethod.Unused;
        pftd_ = DEF_PFTD;
    }

    public boolean isStockTradingDays() {
        return w_ != 0;
    }

    public int getStockTradingDays() {
        return w_;
    }

    public boolean isValid() {
        if (isStockTradingDays() || isAutomatic()) {
            return true;
        }
        if (test_.isUsed()) {
            return td_ != TradingDaysType.None && lp_;
        }
        if (td_ == TradingDaysType.None) {
            return !lp_;
        }
        return true;
    }

    public String getHolidays() {
        return holidays_;
    }

    public void setHolidays(String value) {
        holidays_ = value;
//                if (holidays_ == CalendarManager.DEF)
//                    holidays_ = null;
        if (holidays_ != null && holidays_.length() == 0) {
            holidays_ = null;
        }
        if (holidays_ != null) {
            users_ = null;
        }
    }

    public String[] getUserVariables() {
        return users_;
    }

    public void setUserVariables(String[] value) {
        users_ = value;
//        if (users_ != null && users_.length == 0) {
//            users_ = null;
//        }
        if (users_ != null) {
            holidays_ = null;
            td_ = TradingDaysType.None;
            lp_ = false;
            auto_ = AutoMethod.Unused;
            pftd_ = DEF_PFTD;
        }
    }

    public boolean isTest() {
        return test_.isUsed();
    }

    public void setTest(boolean test) {
        if (test) {
            test_ = RegressionTestType.Separate_T;
        } else {
            test_ = RegressionTestType.None;
        }
    }

    public RegressionTestType getRegressionTestType() {
        return test_;
    }

    public void setRegressionTestType(RegressionTestType value) {
        test_ = value;
    }

    public boolean isDefault() {

        return auto_ == AutoMethod.Unused && w_ == 0 && td_ == TradingDaysType.None && lp_ == false && holidays_ == null && users_ == null;
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
            if (users_ != null) {
                spec.users_ = users_.clone();
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
        if (!Arrays.deepEquals(users_, other.users_)
                || !Objects.equals(holidays_, other.holidays_) || w_ != other.w_ || auto_ != other.auto_) {
            return false;
        }

        if (auto_ != AutoMethod.Unused) {
            return pftd_ == other.pftd_;
        } else {
            return td_ == other.td_ && lp_ == other.lp_ && test_ == other.test_;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.td_);
        hash = 97 * hash + (this.lp_ ? 1 : 0);
        return hash;
    }

    public void disable() {
        holidays_ = null;
        users_ = null;
        td_ = TradingDaysType.None;
        test_ = RegressionTestType.None;
        lp_ = false;
        w_ = 0;
        auto_ = AutoMethod.Unused;
    }

    @Override
    public InformationSet write(boolean verbose) {
        if (!verbose && isDefault()) {
            return null;
        }
        InformationSet info = new InformationSet();
        if (verbose || auto_ != AutoMethod.Unused) {
            info.add(MAUTO, auto_.name());
        }
        if (verbose || pftd_ != DEF_PFTD) {
            info.add(PFTD, pftd_);
        }
        if (verbose || td_ != TradingDaysType.None) {
            info.add(TDOPTION, td_.name());
        }
        if (verbose || lp_) {
            info.add(LPOPTION, lp_);
        }
        if (holidays_ != null) {
            info.add(HOLIDAYS, holidays_);
        }
        if (users_ != null) {
            info.add(USER, users_);
        }
        if (verbose || w_ != 0) {
            info.add(W, w_);
        }
        if (verbose || test_.isUsed()) {
            info.add(TESTTYPE, test_.name());
        }
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        try {
            reset();
            Boolean auto = info.get(AUTO, Boolean.class);
            if (auto != null) {
                setAutomatic(auto);
            }
            String mauto = info.get(MAUTO, String.class);
            if (mauto != null) {
                auto_ = AutoMethod.valueOf(mauto);
            }
            Double pftd = info.get(PFTD, Double.class);
            if (pftd != null) {
                pftd_ = pftd;
            }
            String td = info.get(TDOPTION, String.class);
            if (td != null) {
                td_ = TradingDaysType.valueOf(td);
            }
            Boolean lp = info.get(LPOPTION, Boolean.class);
            if (lp != null) {
                lp_ = lp;
            }
            holidays_ = info.get(HOLIDAYS, String.class);
            users_ = info.get(USER, String[].class);
            Integer w = info.get(W, Integer.class);
            if (w != null) {
                w_ = w;
            }

            Boolean test = info.get(TEST, Boolean.class);
            if (test != null && test) {
                test_ = RegressionTestType.Separate_T;
            }
            String testtype = info.get(TESTTYPE, String.class);
            if (testtype != null) {
                test_ = RegressionTestType.valueOf(testtype);
            }
            return true;
        } catch (Exception err) {
            return false;
        }
    }
}
