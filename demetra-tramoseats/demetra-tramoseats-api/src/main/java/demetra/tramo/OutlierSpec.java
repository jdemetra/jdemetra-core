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

import nbbrd.design.Development;
import nbbrd.design.LombokWorkaround;
import demetra.timeseries.TimeSelector;
import demetra.util.Validatable;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder", buildMethodName = "buildWithoutValidation")
public final class OutlierSpec implements Validatable<OutlierSpec> {

    public static final double DEF_DELTATC = .7;
    public static final boolean DEF_EML = false;

    public static final OutlierSpec DEFAULT_DISABLED = OutlierSpec.builder().build();
    public static final OutlierSpec DEFAULT_ENABLED = OutlierSpec.builder().ao(true).ls(true).tc(true).build();

    private boolean ao, ls, tc, so;
    private double deltaTC;
    private boolean maximumLikelihood;
    private double criticalValue;
    @lombok.NonNull
    private TimeSelector span;

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .deltaTC(DEF_DELTATC)
                .maximumLikelihood(DEF_EML)
                .criticalValue(0)
                .span(TimeSelector.all());
    }

    public boolean isDefault() {
        return this.equals(DEFAULT_DISABLED);
    }

    public boolean isUsed() {
        return ao || ls || tc || so;
    }

    @Override
    public OutlierSpec validate() throws IllegalArgumentException {
        if (deltaTC != 0 && deltaTC < .3 || deltaTC >= 1) {
            throw new IllegalArgumentException("TC should belong to [0.3, 1.0[");
        }
        if (criticalValue != 0 && criticalValue < 2) {
            throw new IllegalArgumentException("Critical value should be greater than 2.0");
        }
        return this;
    }

    public static class Builder implements Validatable.Builder<OutlierSpec> {
    }
}
