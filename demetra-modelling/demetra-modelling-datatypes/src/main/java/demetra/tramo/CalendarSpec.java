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

import demetra.design.Development;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class CalendarSpec {

    public static final String TD = "td", EASTER = "easter";

    private TradingDaysSpec td;
    private EasterSpec easter = new EasterSpec();

    public CalendarSpec() {
        td = new TradingDaysSpec();
        easter = new EasterSpec();
    }

    public CalendarSpec(CalendarSpec other) {
        td = new TradingDaysSpec(other.td);
        easter = new EasterSpec(other.easter);
    }

    public TradingDaysSpec getTradingDays() {
        return td;
    }

    public void setTradingDays(TradingDaysSpec spec) {
        if (spec == null) {
            throw new java.lang.IllegalArgumentException(TD);
        }
        td = spec;
    }

    public EasterSpec getEaster() {
        return easter;
    }

    public void setEaster(EasterSpec spec) {
        if (spec == null) {
            throw new java.lang.IllegalArgumentException(EASTER);
        }
        easter = spec;
    }

    public boolean isUsed() {
        return easter.isUsed() || td.isUsed();
    }

    public boolean isDefault() {
        return easter.isDefault()
                && td.isDefault();
    }

//        public ICalendarProvider Provider(TSContext context)
//        {
//            ICalendarProvider provider = null;
//            if (context != null && m_holidays != null)
//                provider = context.Calendars[m_holidays];
//            if (provider == null)
//                provider = new DefaultCalendarProvider();
//            return provider;
//        }
    @Override
    public CalendarSpec clone() {
        try {
            CalendarSpec spec = (CalendarSpec) super.clone();
            spec.td = td.clone();
            spec.easter = easter.clone();
            return spec;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof CalendarSpec && equals((CalendarSpec) obj));
    }

    private boolean equals(CalendarSpec other) {
        return Objects.equals(easter, other.easter) && Objects.equals(td, other.td);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + td.hashCode();
        hash = 61 * hash + easter.hashCode();
        return hash;
    }

}
