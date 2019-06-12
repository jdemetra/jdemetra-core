/*
 * Copyright 2019 National Bank of Belgium
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
import demetra.design.LombokWorkaround;
import demetra.modelling.regression.InterventionVariable;
import demetra.modelling.regression.Ramp;
import demetra.modelling.regression.UserVariable;
import demetra.modelling.regression.IOutlier;
import demetra.util.Validatable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder", buildMethodName = "buildWithoutValidation")
public final class RegressionSpec implements Validatable<RegressionSpec> {

    private boolean mean;
    private CalendarSpec calendar;
    @lombok.Singular
    private List<IOutlier> outliers;
    @lombok.Singular
    private List<Ramp> ramps;
    @lombok.Singular
    private List<InterventionVariable> interventionVariables;
    @lombok.Singular
    private List<UserVariable> userDefinedVariables;

    // the maps with the coefficients use short names...
    private Map<String, double[]> fixedCoefficients;
    private Map<String, double[]> coefficients;

    private static final RegressionSpec DEFAULT = RegressionSpec.builder().build();

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .fixedCoefficients(new LinkedHashMap<>())
                .coefficients(new LinkedHashMap<>())
                .calendar(CalendarSpec.builder().build());
    }

    public boolean isUsed() {
        return mean || calendar.isUsed() || outliers.isEmpty()
                || !ramps.isEmpty() || !interventionVariables.isEmpty() 
                || !userDefinedVariables.isEmpty();
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }

    @Override
    public RegressionSpec validate() throws IllegalArgumentException {
        return this;
    }

    public static class Builder implements Validatable.Builder<RegressionSpec> {

        @LombokWorkaround
        public Builder fixedCoefficient(String key, double[] value) {
            if (fixedCoefficients == null) {
                fixedCoefficients = new LinkedHashMap<>();
            }
            fixedCoefficients.put(key, value);
            return this;
        }

        @LombokWorkaround
        public Builder coefficient(String key, double[] value) {
            if (coefficients == null) {
                coefficients = new LinkedHashMap<>();
            }
            coefficients.put(key, value);
            return this;
        }
    }
}
