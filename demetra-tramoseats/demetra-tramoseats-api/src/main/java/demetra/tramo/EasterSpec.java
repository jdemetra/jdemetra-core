/*
 * Copyright 2020 National Bank of Belgium
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

import demetra.data.Parameter;
import nbbrd.design.Development;
import nbbrd.design.LombokWorkaround;
import demetra.util.Validatable;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true,  buildMethodName = "buildWithoutValidation")
public final class EasterSpec implements Validatable<EasterSpec> {

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
    public static final boolean DEF_JULIAN = false;

    boolean test;
    int duration;
    Type type;
    boolean julian;
    
    // optional coefficient.
    Parameter coefficient;

    public static final EasterSpec DEFAULT_UNUSED = EasterSpec.builder().build();

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .test(false)
                .julian(DEF_JULIAN)
                .type(Type.Unused)
                .duration(DEF_IDUR);
    }

    @Override
    public EasterSpec validate() throws IllegalArgumentException {
        if (duration <= 0 || duration > 15) {
            throw new IllegalArgumentException("Duration should be inside [1, 15]");
        }
        if (test && Parameter.isFixed(coefficient))
            throw new IllegalArgumentException("Fixed coefficient should not be used with testing");
        return this;
    }

    public boolean isUsed() {
        return type != Type.Unused;
    }

    public boolean isDefined() {
        return type != Type.Unused && !test;
    }

    public boolean isDefault() {
        return this.equals(DEFAULT_UNUSED);
    }
    
    public static EasterSpec none(){
        return DEFAULT_UNUSED;
    }
    
    public boolean hasFixedCoefficient(){
        return coefficient != null && coefficient.isFixed();
    }

    public static class Builder implements Validatable.Builder<EasterSpec> {
    }
}
