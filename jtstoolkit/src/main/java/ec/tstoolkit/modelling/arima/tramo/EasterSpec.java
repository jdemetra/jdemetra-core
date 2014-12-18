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

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class EasterSpec implements Cloneable, InformationSetSerializable {

    public static final String DURATION = "duration", TYPE = "type", TEST = "test";

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, TYPE), String.class);
        dic.put(InformationSet.item(prefix, DURATION), Integer.class);
        dic.put(InformationSet.item(prefix, TEST), String.class);
    }

    public static enum Type {

        Unused, Standard, IncludeEaster, IncludeEasterMonday;

        public boolean containsEaster() {
            return this == IncludeEaster || this == IncludeEasterMonday;
        }

        public boolean containsEasterMonday() {
            return this == IncludeEasterMonday;
        }
    };
    private boolean test_;
    private int duration_ = DEF_IDUR;
    private Type type_ = Type.Unused;
    public static final int DEF_IDUR = 6;

    public EasterSpec() {
    }

    public void reset() {
        test_ = false;
        duration_ = DEF_IDUR;
        type_ = Type.Unused;
    }

    public boolean isTest() {
        return test_;
    }

    public void setTest(boolean value) {
        test_ = value;
    }

    public int getDuration() {
        return duration_;
    }

    public void setDuration(int value) {
        if (value <= 0 || value > 15) {
            throw new TramoException("Duration", "Should be inside [1, 15]");
        }
        duration_ = value;
    }

    public Type getOption() {
        return type_;
    }

    public boolean isUsed() {
        return type_ != Type.Unused;
    }

    public void setOption(Type type) {
        type_ = type;
    }

    public boolean isDefault() {
        return type_ == Type.Unused;
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
    public EasterSpec clone() {
        try {
            EasterSpec spec = (EasterSpec) super.clone();
            return spec;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof EasterSpec && equals((EasterSpec) obj));
    }

    private boolean equals(EasterSpec other) {
        return duration_ == other.duration_ && test_ == other.test_
                && type_ == other.type_;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + this.duration_;
        hash = 67 * hash + type_.hashCode();
        return hash;
    }

    @Override
    public InformationSet write(boolean verbose) {
        if (!verbose && isDefault()) {
            return null;
        }
        InformationSet info = new InformationSet();
        if (verbose || duration_ != DEF_IDUR) {
            info.add(DURATION, duration_);
        }
        if (verbose || type_ != Type.Unused) {
            info.add(TYPE, type_.name());
        }
        if (verbose || test_) {
            info.add(TEST, test_);
        }
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        try {
            reset();
            Integer d = info.get(DURATION, Integer.class);
            if (d != null) {
                duration_ = d;
            }
            String type = info.get(TYPE, String.class);
            if (type != null) {
                type_ = Type.valueOf(type);
            }
            Boolean test = info.get(TEST, Boolean.class);
            if (test != null) {
                test_ = test;
            }
            return true;
        } catch (Exception err) {
            return false;
        }
    }
}
