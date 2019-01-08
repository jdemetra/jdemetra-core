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

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Data
public final class EasterSpec implements Cloneable {

    public static enum Type {

        Unused, Standard, IncludeEaster, IncludeEasterMonday;

        public boolean containsEaster() {
            return this == IncludeEaster || this == IncludeEasterMonday;
        }

        public boolean containsEasterMonday() {
            return this == IncludeEasterMonday;
        }
    };

    public static final int DEF_IDUR = 6;
    public static boolean DEF_JULIAN = false;

    private boolean test;
    private int duration = DEF_IDUR;
    private Type type = Type.Unused;
    private boolean julian = DEF_JULIAN;

    public EasterSpec() {
    }

    @Override
    public EasterSpec clone() {
        try {
            return (EasterSpec) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
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

    public boolean isJulian() {
        return julian;
    }

    public void setJulian(boolean julian) {
        this.julian = julian;
    }

    public boolean isDefault() {
        return type == Type.Unused;
    }

}
