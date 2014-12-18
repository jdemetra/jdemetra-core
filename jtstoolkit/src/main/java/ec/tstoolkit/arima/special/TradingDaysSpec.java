/*
 * Copyright 2013-2014 National Bank of Belgium
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
package ec.tstoolkit.arima.special;

import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetSerializable;
import ec.tstoolkit.modelling.RegressionTestType;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
public class TradingDaysSpec implements Cloneable, InformationSetSerializable {

    public static final String TDOPTION = "option", LPOPTION = "leapyear", HOLIDAYS = "holidays", USER = "user", W = "stocktd";

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, TDOPTION), String.class);
        dic.put(InformationSet.item(prefix, LPOPTION), String.class);
        dic.put(InformationSet.item(prefix, USER), String[].class);
        dic.put(InformationSet.item(prefix, HOLIDAYS), String.class);
        dic.put(InformationSet.item(prefix, W), Integer.class);
    }

    private String holidays_;
    private String[] users_;
    private TradingDaysType td_ = TradingDaysType.None;
    private boolean lp_;
    private int w_ = 0;

    public TradingDaysSpec() {
    }

    public void reset() {
        holidays_ = null;
        users_ = null;
        td_ = TradingDaysType.None;
        lp_ = false;
        w_ = 0;
    }

    public TradingDaysType getTradingDaysType() {
        return td_;
    }

    public boolean isUsed() {
        return td_ != TradingDaysType.None || users_ != null || w_ != 0;
    }

    public void setTradingDaysType(TradingDaysType value) {
        td_ = value;
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
    }

    public boolean isStockTradingDays() {
        return w_ > 0;
    }

    public int getStockTradingDays() {
        return w_;
    }

    public boolean isValid() {
        if (isStockTradingDays()) {
            return true;
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
        if (users_ != null) {
            holidays_ = null;
            td_ = TradingDaysType.None;
            lp_ = false;
        }
    }

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
        return Arrays.deepEquals(users_, other.users_)
                && Objects.equals(holidays_, other.holidays_) && w_ == other.w_
                && td_ == other.td_ && lp_ == other.lp_;
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
        lp_ = false;
        w_ = 0;
    }

    @Override
    public InformationSet write(boolean verbose) {
        if (!verbose && !isUsed()) {
            return null;
        }
        InformationSet info = new InformationSet();
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
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        try {
            reset();
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
            return true;
        } catch (Exception err) {
            return false;
        }
    }
}
