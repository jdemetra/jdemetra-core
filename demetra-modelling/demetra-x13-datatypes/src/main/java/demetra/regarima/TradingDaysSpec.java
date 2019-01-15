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
package demetra.regarima;

import demetra.modelling.ChangeOfRegimeSpec;
import demetra.modelling.RegressionTestSpec;
import demetra.modelling.regression.TradingDaysType;
import demetra.timeseries.calendars.LengthOfPeriodType;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
public class TradingDaysSpec {

    private String holidays;
    private String[] users;
    private TradingDaysType type = TradingDaysType.None;
    private LengthOfPeriodType lp = LengthOfPeriodType.None;
    private RegressionTestSpec test = RegressionTestSpec.None;
    private boolean autoAdjust = true;
    private int w = 0;
    private ChangeOfRegimeSpec changeofregime;

    public TradingDaysSpec() {
    }

    public TradingDaysSpec(TradingDaysSpec other) {
        this.autoAdjust=other.autoAdjust;
        this.changeofregime=other.changeofregime;
        this.holidays=other.holidays;
        this.lp=other.lp;
        this.test=other.test;
        this.type=other.type;
        this.users=users != null ? users.clone() : null;
        this.w=other.w;                
    }

    public void reset() {
        holidays = null;
        users = null;
        type = TradingDaysType.None;
        lp = LengthOfPeriodType.None;
        test = RegressionTestSpec.None;
        autoAdjust = true;
        w = 0;
        changeofregime = null;

    }

    public TradingDaysType getTradingDaysType() {
        return type;
    }

    public boolean isUsed() {
        return type != TradingDaysType.None || users != null || w != 0;
    }

    boolean isDefined() {
        return isUsed() && test == RegressionTestSpec.None;
    }

    public void setTradingDaysType(TradingDaysType value) {
        type = value;
        users = null;
        w = 0;
    }

    public LengthOfPeriodType getLengthOfPeriod() {
        return lp;
    }

    public void setLengthOfPeriod(LengthOfPeriodType value) {
        lp = value;
    }

    public boolean isAutoAdjust() {
        return autoAdjust;
    }

    public void setAutoAdjust(boolean value) {
        autoAdjust = value;
    }

    /**
     *
     * @param w 1-based day of the month. Should be in [1, 31]
     */
    public void setStockTradingDays(int w) {
        this.w = w;
        holidays = null;
        users = null;
        type = TradingDaysType.None;
        lp = LengthOfPeriodType.None;
    }

    public boolean isStockTradingDays() {
        return w != 0;
    }

    public int getStockTradingDays() {
        return w;
    }

    public boolean isValid() {
        if (isStockTradingDays()) {
            return true;
        }
        if (test != RegressionTestSpec.None) {
            return type != TradingDaysType.None && lp != LengthOfPeriodType.None;
        }
        if (type == TradingDaysType.None) {
            return lp == LengthOfPeriodType.None;
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
            w = 0;
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
            type = TradingDaysType.None;
            lp = LengthOfPeriodType.None;
            autoAdjust = false;
        }
    }

    public RegressionTestSpec getTest() {
        return test;
    }

    public void setTest(RegressionTestSpec value) {
        test = value;
    }

    public boolean isDefault() {
        return w == 0 && type == TradingDaysType.None && lp == LengthOfPeriodType.None && holidays == null && users == null;
    }

    public ChangeOfRegimeSpec getChangeOfRegime() {
        return changeofregime;
    }

    public void setChangeOfRegime(ChangeOfRegimeSpec value) {
        changeofregime = value;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof TradingDaysSpec && equals((TradingDaysSpec) obj));
    }

    private boolean equals(TradingDaysSpec other) {
        return Arrays.deepEquals(users, other.users)
                && Objects.equals(holidays, other.holidays) && w == other.w
                && Objects.equals(changeofregime, other.changeofregime)
                && type == other.type && lp == other.lp
                && test == other.test && autoAdjust == other.autoAdjust;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.holidays);
        hash = 47 * hash + Arrays.deepHashCode(this.users);
        hash = 47 * hash + Objects.hashCode(this.type);
        hash = 47 * hash + Objects.hashCode(this.test);
        hash = 47 * hash + this.w;
        return hash;
    }

    public void disable() {
        holidays = null;
        users = null;
        type = TradingDaysType.None;
        test = RegressionTestSpec.None;
        lp = LengthOfPeriodType.None;
        w = 0;
    }

}
