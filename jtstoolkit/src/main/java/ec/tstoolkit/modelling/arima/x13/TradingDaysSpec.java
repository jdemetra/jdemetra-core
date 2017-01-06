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
package ec.tstoolkit.modelling.arima.x13;

import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetSerializable;
import ec.tstoolkit.modelling.ChangeOfRegimeSpec;
import ec.tstoolkit.modelling.RegressionTestSpec;
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

    private String holidays_;
    private String[] users_;
    private TradingDaysType type_ = TradingDaysType.None;
    private LengthOfPeriodType lp_ = LengthOfPeriodType.None;
    private RegressionTestSpec test_ = RegressionTestSpec.None;
    private boolean autoAdjust_ = true;
    private int w_ = 0;
    private ChangeOfRegimeSpec changeofregime_;

    public TradingDaysSpec() {
    }

    public void reset() {
        holidays_ = null;
        users_ = null;
        type_ = TradingDaysType.None;
        lp_ = LengthOfPeriodType.None;
        test_ = RegressionTestSpec.None;
        autoAdjust_ = true;
        w_ = 0;
        changeofregime_ = null;

    }

    public TradingDaysType getTradingDaysType() {
        return type_;
    }

    public boolean isUsed() {
        return type_ != TradingDaysType.None || users_ != null || w_ != 0;
    }

    boolean isDefined() {
        return isUsed() && test_ == RegressionTestSpec.None;
    }

    public void setTradingDaysType(TradingDaysType value) {
        type_ = value;
    }

    public LengthOfPeriodType getLengthOfPeriod() {
        return lp_;
    }

    public void setLengthOfPeriod(LengthOfPeriodType value) {
        lp_ = value;
    }

    public boolean isAutoAdjust() {
        return autoAdjust_;
    }

    public void setAutoAdjust(boolean value) {
        autoAdjust_ = value;
    }

    /**
     *
     * @param w 1-based day of the month. Should be in [1, 31]
     */
    public void setStockTradingDays(int w) {
        w_ = w;
    }

    public boolean isStockTradingDays() {
        return w_ != 0;
    }

    public int getStockTradingDays() {
        return w_;
    }

    public boolean isValid() {
        if (isStockTradingDays()) {
            return true;
        }
        if (test_ != RegressionTestSpec.None) {
            return type_ != TradingDaysType.None && lp_ != LengthOfPeriodType.None;
        }
        if (type_ == TradingDaysType.None) {
            return lp_ == LengthOfPeriodType.None;
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
            type_ = TradingDaysType.None;
            lp_ = LengthOfPeriodType.None;
            autoAdjust_ = false;
        }
    }

    public RegressionTestSpec getTest() {
        return test_;
    }

    public void setTest(RegressionTestSpec value) {
        test_ = value;
    }

    public boolean isDefault() {
        return w_ == 0 && type_ == TradingDaysType.None && lp_ == LengthOfPeriodType.None && holidays_ == null && users_ == null;
    }

    public ChangeOfRegimeSpec getChangeOfRegime() {
        return changeofregime_;
    }

    public void setChangeOfRegime(ChangeOfRegimeSpec value) {
        changeofregime_ = value;
    }

    @Override
    public TradingDaysSpec clone() {
        try {
            TradingDaysSpec spec = (TradingDaysSpec) super.clone();
            if (users_ != null) {
                spec.users_ = users_.clone();
            }
            if (changeofregime_ != null) {
                spec.changeofregime_ = changeofregime_.clone();
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
        return Arrays.deepEquals(users_, other.users_)
                && Objects.equals(holidays_, other.holidays_) && w_ == other.w_
                && Objects.equals(changeofregime_, other.changeofregime_)
                && type_ == other.type_ && lp_ == other.lp_
                && test_ == other.test_ && autoAdjust_ == other.autoAdjust_;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.holidays_);
        hash = 47 * hash + Arrays.deepHashCode(this.users_);
        hash = 47 * hash + Objects.hashCode(this.type_);
        hash = 47 * hash + Objects.hashCode(this.test_);
        hash = 47 * hash + this.w_;
        return hash;
    }

    public void disable() {
        holidays_ = null;
        users_ = null;
        type_ = TradingDaysType.None;
        test_ = RegressionTestSpec.None;
        lp_ = LengthOfPeriodType.None;
        w_ = 0;
    }

    @Override
    public InformationSet write(boolean verbose) {
        if (!verbose && isDefault()) {
            return null;
        }
        InformationSet info = new InformationSet();
        if (verbose || type_ != TradingDaysType.None) {
            info.add(TDOPTION, type_.name());
        }
        if (verbose || lp_ != LengthOfPeriodType.None) {
            info.add(LPOPTION, lp_.name());
        }
        if (verbose || autoAdjust_ != true) {
            info.add(ADJUST, autoAdjust_);
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
        if (verbose || test_ != RegressionTestSpec.None) {
            info.add(TEST, test_.name());
        }
        if (changeofregime_ != null) {
            info.add(CHANGEOFREGIME, changeofregime_.toString());
        }
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        try {
            reset();
            String td = info.get(TDOPTION, String.class);
            if (td != null) {
                type_ = TradingDaysType.valueOf(td);
            }
            String lp = info.get(LPOPTION, String.class);
            if (lp != null) {
                lp_ = LengthOfPeriodType.valueOf(lp);
            }
            Boolean adj = info.get(ADJUST, Boolean.class);
            if (adj != null) {
                autoAdjust_ = adj;
            }
            holidays_ = info.get(HOLIDAYS, String.class);
            users_ = info.get(USER, String[].class);
            Integer w = info.get(W, Integer.class);
            if (w != null) {
                w_ = w;
            }
            String test = info.get(TEST, String.class);
            if (test != null) {
                test_ = RegressionTestSpec.valueOf(test);
            }
            String cr = info.get(CHANGEOFREGIME, String.class);
            if (cr != null) {
                changeofregime_ = ChangeOfRegimeSpec.fromString(cr);
                if (changeofregime_ == null) {
                    return false;
                }
            }
            return true;
        } catch (Exception err) {
            return false;
        }
    }

    public static final String TDOPTION = "option", LPOPTION = "leapyear",
            ADJUST = "autoadjust", HOLIDAYS = "holidays", USER = "user",
            TEST = "test", W = "stocktd", CHANGEOFREGIME = "changeofregime";

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, ADJUST), Boolean.class);
        dic.put(InformationSet.item(prefix, TDOPTION), String.class);
        dic.put(InformationSet.item(prefix, LPOPTION), String.class);
        dic.put(InformationSet.item(prefix, USER), String[].class);
        dic.put(InformationSet.item(prefix, HOLIDAYS), String.class);
        dic.put(InformationSet.item(prefix, W), Integer.class);
        dic.put(InformationSet.item(prefix, TEST), String.class);
        dic.put(InformationSet.item(prefix, CHANGEOFREGIME), String.class);
    }

}
