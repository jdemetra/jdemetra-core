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

import demetra.design.Development;
import demetra.design.LombokWorkaround;
import demetra.timeseries.TimeSelector;
import demetra.util.Validatable;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder", buildMethodName = "buildWithoutValidation")
public final class EstimateSpec implements Validatable<EstimateSpec> {

    public static final double DEF_TOL = 1e-7, DEF_UBP = .96;

    @lombok.NonNull
    private TimeSelector span;
    private boolean maximumLikelihood;
    private double tol;
    private double ubp;

    public static final EstimateSpec DEFAULT = EstimateSpec.builder().build();

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .span(TimeSelector.all())
                .maximumLikelihood(true)
                .tol(DEF_TOL)
                .ubp(DEF_UBP);
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }

    @Override
    public EstimateSpec validate() throws IllegalArgumentException {
        if (tol <= 0 || tol > 1e-2) {
            throw new IllegalArgumentException("Invalid Tol parameter");
        }
        return this;
    }

    public static class Builder implements Validatable.Builder<EstimateSpec> {
    }
}
