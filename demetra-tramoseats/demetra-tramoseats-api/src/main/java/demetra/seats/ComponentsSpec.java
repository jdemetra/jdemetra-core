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
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public final class ComponentsSpec {

    public static enum ComponentsEstimationMethod {
        Burman, KalmanSmoother, McElroyMatrix
    }

    public static final int DEF_FORECASTS = -2, DEF_BACKCASTS = 0;

    private ComponentsEstimationMethod method;
    private int backCastCount, forecastCount;

    public static final ComponentsSpec DEFAULT = ComponentsSpec.builder().build();

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .method(ComponentsEstimationMethod.Burman)
                .backCastCount(DEF_BACKCASTS)
                .forecastCount(DEF_FORECASTS);
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }
}
