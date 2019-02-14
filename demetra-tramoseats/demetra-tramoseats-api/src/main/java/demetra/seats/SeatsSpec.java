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

import demetra.design.Development;
import demetra.design.LombokWorkaround;
import demetra.util.Validatable;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder", buildMethodName = "buildWithoutValidation")
public final class SeatsSpec implements Validatable<SeatsSpec> {

    public static enum ApproximationMode {
        None, Legacy, Noisy
    };

    public static enum EstimationMethod {
        Burman, KalmanSmoother, McElroyMatrix
    }

    public static final double DEF_EPSPHI = 2, DEF_RMOD = .5, DEF_SMOD1 = .8, DEF_SMOD = .8, DEF_XL = .95;

    private ApproximationMode approximationMode;
    private EstimationMethod method;
    private double xlBoundary;
    private double seasTolerance;
    private double trendBoundary, seasBoundary, seasBoundaryAtPi;

    private static final SeatsSpec DEFAULT = SeatsSpec.builder().build();

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .approximationMode(ApproximationMode.Legacy)
                .method(EstimationMethod.Burman)
                .xlBoundary(DEF_XL)
                .seasTolerance(DEF_EPSPHI)
                .trendBoundary(DEF_RMOD)
                .seasBoundary(DEF_SMOD)
                .seasBoundaryAtPi(DEF_SMOD1);
    }

    @Override
    public SeatsSpec validate() throws IllegalArgumentException {
        if (xlBoundary < 0.9 || xlBoundary > 1) {
            throw new IllegalArgumentException("XL should belong to [0.9, 1]");
        }

        if (seasTolerance < 0 || seasTolerance > 10) {
            throw new IllegalArgumentException("EPSPHI (expressed in degrees) should belong to [0, 10]");
        }

        if (trendBoundary < 0 || trendBoundary > 1) {
            throw new IllegalArgumentException("RMOD should belong to [0, 1]");
        }

        if (seasBoundary < 0 || seasBoundary > 1) {
            throw new IllegalArgumentException("SMOD should belong to [0, 1]");
        }

        if (seasBoundaryAtPi < 0 || seasBoundaryAtPi > 1) {
            throw new IllegalArgumentException("SMOD1 should belong to [0, 1]");
        }
        return this;
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }

    public static class Builder implements Validatable.Builder<SeatsSpec> {

    }
}
