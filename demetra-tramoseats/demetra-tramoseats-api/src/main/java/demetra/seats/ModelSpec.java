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
package demetra.seats;

import demetra.arima.SarimaSpec;
import demetra.design.Development;
import demetra.design.LombokWorkaround;
import demetra.util.Validatable;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder", buildMethodName = "buildWithoutValidation")
public final class ModelSpec implements Validatable<ModelSpec> {

    public static final double DEF_XL = .95;

    private boolean log, meanCorrection;
    private SarimaSpec sarimaSpec;
    private double xlBoundary;

    private static final SarimaSpec AIRLINE = SarimaSpec.airline();

    public static final ModelSpec DEFAULT = ModelSpec.builder().build();

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .sarimaSpec(AIRLINE)
                .xlBoundary(DEF_XL)
                .log(false)
                .meanCorrection(false);
    }

    @Override
    public ModelSpec validate() throws IllegalArgumentException {
        if (xlBoundary < 0.9 || xlBoundary > 1) {
            throw new IllegalArgumentException("XL should belong to [0.9, 1]");
        }
        return this;
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }

    public static class Builder implements Validatable.Builder<ModelSpec> {

    }
}
