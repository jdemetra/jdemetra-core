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

import nbbrd.design.Development;
import nbbrd.design.LombokWorkaround;
import demetra.modelling.TransformationType;
import demetra.timeseries.TimeSelector;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.util.Validatable;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
@lombok.Value
@lombok.Builder(toBuilder = true,  buildMethodName = "buildWithoutValidation")
public final class TransformSpec implements Validatable<TransformSpec> {

    public static final double DEF_FCT = 0.95;
    public static final boolean DEF_OUTLIERS=false, DEF_CHECK=true;
    public static final LengthOfPeriodType DEF_ADJUST=LengthOfPeriodType.None;

    @lombok.NonNull
    private TimeSelector span;
    private double fct;
    private boolean preliminaryCheck;
    private boolean outliersCorrection;
    private TransformationType function;
    private LengthOfPeriodType adjust;

    public static final TransformSpec DEFAULT_UNUSED = TransformSpec.builder().build(),
            DEFAULT_AUTO = TransformSpec.builder().function(TransformationType.Auto).build();

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .span(TimeSelector.all())
                .fct(DEF_FCT)
                .preliminaryCheck(DEF_CHECK)
                .outliersCorrection(DEF_OUTLIERS)
                .function(TransformationType.None)
                .adjust(DEF_ADJUST);
    }

    public boolean isDefault() {
        return this.equals(DEFAULT_UNUSED);
    }

    @Override
    public TransformSpec validate() throws IllegalArgumentException {
        return this;
    }

    public static class Builder implements Validatable.Builder<TransformSpec> {
    }
}
