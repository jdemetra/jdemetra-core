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
package demetra.sa.benchmarking;

import nbbrd.design.Development;
import nbbrd.design.LombokWorkaround;
import demetra.util.Validatable;

/**
 * This class specifies the way the uni-variate benchmarking routine (Cholette)
 * will be used. See for instance the X13 documentation for further details on
 * the method and on the parameters The main parameters are: - enabled (false by
 * default!) - target: the target series; original(default) or calendar adjusted
 * - rho: the smoothing parameter (should be in [0,1]; 0 = no time structure, 1
 * = "Denton"-like - lambda: modifies the penalty function; 0 = additive, 0.5 =
 * proportional, 1 = multiplicative
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder", buildMethodName = "buildWithoutValidation")
public final class SaBenchmarkingSpec implements Validatable<SaBenchmarkingSpec> {

    public static double DEF_LAMBDA = 1, DEF_RHO = 1;
    public static final String ENABLED = "enabled",
            TARGET = "target",
            FORECAST = "forecast",
            LAMBDA = "lambda",
            RHO = "rho",
            BIAS = "bias";

    public static enum Target {
        Original,
        CalendarAdjusted
    }

    public static enum BiasCorrection {
        None, Additive, Multiplicative
    };

    private boolean enabled, forecast;
    @lombok.NonNull
    private Target target;
    private double rho;
    private double lambda;
    @lombok.NonNull
    private BiasCorrection biasCorrection;

    public static final SaBenchmarkingSpec DEFAULT = SaBenchmarkingSpec.builder().build();

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .enabled(false)
                .forecast(false)
                .target(Target.CalendarAdjusted)
                .rho(DEF_RHO)
                .lambda(DEF_LAMBDA)
                .biasCorrection(BiasCorrection.None);
    }

    @Override
    public SaBenchmarkingSpec validate() throws IllegalArgumentException {
        return this;
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }

    public static class Builder implements Validatable.Builder<SaBenchmarkingSpec> {
    }
}
