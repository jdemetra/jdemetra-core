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
package demetra.regarima;

import demetra.design.Development;
import demetra.design.LombokWorkaround;
import demetra.timeseries.regression.IOutlier;
import demetra.timeseries.regression.InterventionVariable;
import demetra.timeseries.regression.Ramp;
import demetra.timeseries.regression.UserVariable;
import java.util.*;
import demetra.util.Validatable;

/**
 *
 * @author Jean Palate, Mats Maggi
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder", buildMethodName = "buildWithoutValidation")
public final class RegressionSpec implements Validatable<RegressionSpec> {

    public static final double DEF_AICCDIFF = 0;

    private static final RegressionSpec DEFAULT = RegressionSpec.builder().build();

    private double aicDiff;

    private boolean mean;
    @lombok.NonNull
    private TradingDaysSpec tradingDays;
    @lombok.NonNull
    private EasterSpec easter;
    @lombok.Singular
    private List<IOutlier> outliers;
    @lombok.Singular
    private List<UserVariable> userDefinedVariables;
    @lombok.Singular
    private List<InterventionVariable> interventionVariables;
    @lombok.Singular
    private List<Ramp> ramps;

    private Map<String, double[]> fixedCoefficients;
    private Map<String, double[]> coefficients;

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .aicDiff(DEF_AICCDIFF)
                .easter(EasterSpec.builder().build())
                .fixedCoefficients(new LinkedHashMap<>())
                .coefficients(new LinkedHashMap<>())
                .tradingDays(TradingDaysSpec.builder().build());
    }

    public boolean isUsed() {
        return tradingDays.isUsed() || easter.isUsed()
                || !outliers.isEmpty() || !userDefinedVariables.isEmpty()
                || !ramps.isEmpty() || !interventionVariables.isEmpty();
    }

    public int getOutliersCount() {
        return outliers.size();
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }

    @Override
    public RegressionSpec validate() throws IllegalArgumentException {
        tradingDays.validate();
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
