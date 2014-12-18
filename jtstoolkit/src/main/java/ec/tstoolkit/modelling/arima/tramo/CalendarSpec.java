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

import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetSerializable;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class CalendarSpec implements Cloneable, InformationSetSerializable {

    public static final String TD = "td", EASTER = "easter";

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
         EasterSpec.fillDictionary(InformationSet.item(prefix, EASTER), dic);
         TradingDaysSpec.fillDictionary(InformationSet.item(prefix, TD), dic);
   }

    private TradingDaysSpec td_ = new TradingDaysSpec();
    private EasterSpec easter_ = new EasterSpec();

    public CalendarSpec() {
    }

    public TradingDaysSpec getTradingDays() {
        return td_;
    }

    public void setTradingDays(TradingDaysSpec spec) {
        if (spec == null) {
            throw new java.lang.IllegalArgumentException(TD);
        }
        td_ = spec;
    }

    public EasterSpec getEaster() {
        return easter_;
    }

    public void setEaster(EasterSpec spec) {
        if (spec == null) {
            throw new java.lang.IllegalArgumentException(EASTER);
        }
        easter_ = spec;
    }

    public boolean isUsed() {
        return easter_.isUsed() || td_.isUsed();
    }

    public boolean isDefault() {
        return easter_.isDefault()
                && td_.isDefault();
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
            spec.td_ = td_.clone();
            spec.easter_ = easter_.clone();
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
        return Objects.equals(easter_, other.easter_) && Objects.equals(td_, other.td_);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + td_.hashCode();
        hash = 61 * hash + easter_.hashCode();
        return hash;
    }

    @Override
    public InformationSet write(boolean verbose) {
        if (!verbose && isDefault()) {
            return null;
        }
        InformationSet specInfo = new InformationSet();
        if (verbose || !td_.isDefault()) {
            InformationSet tdinfo = td_.write(verbose);
            if (tdinfo != null) {
                specInfo.add(TD, tdinfo);
            }
        }
        if (verbose || !easter_.isDefault()) {
            InformationSet einfo = easter_.write(verbose);
            if (einfo != null) {
                specInfo.add(EASTER, einfo);
            }
        }
        return specInfo;
    }

    @Override
    public boolean read(InformationSet info) {
        InformationSet tdinfo = info.getSubSet(TD);
        if (tdinfo != null) {
            boolean tok = td_.read(tdinfo);
            if (!tok) {
                return false;
            }
        }
        InformationSet einfo = info.getSubSet(EASTER);
        if (einfo != null) {
            boolean tok = easter_.read(einfo);
            if (!tok) {
                return false;
            }
        }
        return true;
    }

}
