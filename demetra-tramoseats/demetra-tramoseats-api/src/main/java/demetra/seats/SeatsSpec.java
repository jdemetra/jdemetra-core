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
package demetra.seats;

import demetra.design.Development;
import demetra.design.LombokWorkaround;
import demetra.seats.DecompositionSpec.ComponentsEstimationMethod;
import demetra.util.Validatable;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder", buildMethodName = "buildWithoutValidation")
public final class SeatsSpec implements Validatable<SeatsSpec> {

    private SeatsModelSpec modelSpec;
    private DecompositionSpec decompositionSpec;

    private static final SeatsSpec DEFAULT = SeatsSpec.builder().build();

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .modelSpec(null)
                .decompositionSpec(DecompositionSpec.DEFAULT);
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }

    public static class Builder implements Validatable.Builder<SeatsSpec> {
    }

    @Override
    public SeatsSpec validate() throws IllegalArgumentException {
        decompositionSpec.validate();
        if (modelSpec != null) {
            modelSpec.validate();
            if (decompositionSpec.getMethod() != ComponentsEstimationMethod.KalmanSmoother
                    && modelSpec.getSeries().getValues().count( z -> !Double.isFinite(z)) > 0) {
                throw new SeatsException(SeatsException.ERR_MISSING);
            }
        }
        return this;
    }

}
