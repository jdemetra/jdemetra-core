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
package demetra.modelling.regular;

import nbbrd.design.Development;
import nbbrd.design.LombokWorkaround;
import demetra.timeseries.TimeSelector;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true)
public final class EstimateSpec  {

    public static final double DEF_EPS = 1e-7, DEF_IEPS = 1e-4;

    @lombok.NonNull
    private TimeSelector span;
    private double precision, intermediatePrecision;

    public static final EstimateSpec DEFAULT = EstimateSpec.builder().build();

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .span(TimeSelector.all())
                .precision(DEF_EPS)
                .intermediatePrecision(DEF_IEPS);
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }

 }
