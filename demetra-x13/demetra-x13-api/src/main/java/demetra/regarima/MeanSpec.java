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
package demetra.regarima;

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
@lombok.Builder(toBuilder = true, buildMethodName = "buildWithoutValidation")
public final class MeanSpec implements Validatable<MeanSpec> {

    boolean trendConstant;
    boolean test;

    // optional coefficient.
    Parameter coefficient;

    public static final MeanSpec DEFAULT_UNUSED = new Builder().test(false).trendConstant(false).build(),
            DEFAULT_USED = new Builder().test(false).trendConstant(true).coefficient(Parameter.undefined()).build();


    @Override
    public MeanSpec validate() throws IllegalArgumentException {
        if (test && Parameter.isFixed(coefficient)) {
            throw new IllegalArgumentException("Fixed coefficient should not be used with testing");
        }
        return this;
    }

    public boolean isUsed() {
        return trendConstant;
    }

    public boolean isDefined() {
        return trendConstant && !test;
    }

    public boolean isDefault() {
        return this.equals(DEFAULT_UNUSED);
    }

    public static MeanSpec none() {
        return DEFAULT_UNUSED;
    }

    public static MeanSpec mean(Parameter p) {
        return new MeanSpec(true, false, p);
    }

    public boolean hasFixedCoefficient() {
        return Parameter.isFixed(coefficient);
    }
    
    public static class Builder implements Validatable.Builder<MeanSpec> {
    }
}
