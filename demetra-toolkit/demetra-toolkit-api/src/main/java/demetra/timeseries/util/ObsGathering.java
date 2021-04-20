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
import nbbrd.design.LombokWorkaround;
import demetra.timeseries.TsUnit;

/**
 * Parameters used when collecting observations in order to create time series.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@lombok.Value
@lombok.Builder( toBuilder = true)
@lombok.With
public class ObsGathering {

    public static final ObsGathering DEFAULT = builder().build();

    @lombok.NonNull
    private TsUnit unit;

    @lombok.NonNull
    private AggregationType aggregationType;

    // FIXME: find a better name/description
    private boolean complete;

    private boolean skipMissingValues;

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .unit(TsUnit.UNDEFINED)
                .aggregationType(AggregationType.None)
                .complete(true)
                .skipMissingValues(true);
    }
}
