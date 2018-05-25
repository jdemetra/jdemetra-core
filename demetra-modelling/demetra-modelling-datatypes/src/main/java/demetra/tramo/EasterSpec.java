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
import demetra.tramo.TramoException;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class EasterSpec{

    public static enum Type {

        Unused, Standard, IncludeEaster, IncludeEasterMonday;

        public boolean containsEaster() {
            return this == IncludeEaster || this == IncludeEasterMonday;
        }

        public boolean containsEasterMonday() {
            return this == IncludeEasterMonday;
        }
    };
    private boolean test;
    private int duration = DEF_IDUR;
    private Type type = Type.Unused;
    private boolean julian=DEF_JULIAN;
    
    public static final int DEF_IDUR = 6;
    public static boolean DEF_JULIAN=false;
 
    public EasterSpec() {
    }

    public EasterSpec( EasterSpec other ){
        this.duration=other.duration;
        this.julian=other.julian;
        this.test=other.test;
        this.type=other.type;
    }

    public void reset() {
        test = false;
        duration = DEF_IDUR;
        type = Type.Unused;
    }

    public boolean isTest() {
        return test;
    }

    public void setTest(boolean value) {
        test = value;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int value) {
        if (value <= 0 || value > 15) {
            throw new TramoException("Duration should be inside [1, 15]");
        }
        duration = value;
    }

    public Type getOption() {
        return type;
    }

    public boolean isUsed() {
        return type != Type.Unused;
    }

    public boolean isDefined() {
        return type != Type.Unused && !test;
    }

    public void setOption(Type type) {
        this.type = type;
    }

    public boolean isJulian(){
        return julian;
    }
    
    public void setJulian(boolean julian){
        this.julian=julian;
    }

    public boolean isDefault() {
        return type == Type.Unused;
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
        return duration == other.duration && test == other.test
                && type == other.type && julian == other.julian;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + this.duration;
        hash = 67 * hash + type.hashCode();
        return hash;
    }

}
