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
package demetra.modelling.regular;

import nbbrd.design.Development;
import nbbrd.design.LombokWorkaround;
import demetra.timeseries.TimeSelector;
import demetra.timeseries.calendars.LengthOfPeriodType;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
@lombok.Value
@lombok.Builder(toBuilder = true)
public final class SeriesSpec {

    public static final boolean DEF_CHECK=true;

    @lombok.NonNull
    private TimeSelector span;
    private boolean preliminaryCheck;
 
    public static final SeriesSpec DEFAULT = SeriesSpec.builder().build();

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .span(TimeSelector.all())
                .preliminaryCheck(DEF_CHECK);
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }
}
