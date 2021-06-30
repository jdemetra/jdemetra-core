/*
 * Copyright 2016 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
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
package demetra.timeseries.util;

import demetra.data.AggregationType;
import demetra.timeseries.TsUnit;
import nbbrd.design.LombokWorkaround;

/**
 * Parameters used when collecting observations in order to create time series.
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public class ObsGathering {

    public static final ObsGathering DEFAULT = builder().build();

    @lombok.NonNull
    private TsUnit unit;

    @lombok.NonNull
    private AggregationType aggregationType;

    private boolean allowPartialAggregation;

    private boolean includeMissingValues;

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .unit(TsUnit.UNDEFINED)
                .aggregationType(AggregationType.None)
                .allowPartialAggregation(false)
                .includeMissingValues(false);
    }
}
