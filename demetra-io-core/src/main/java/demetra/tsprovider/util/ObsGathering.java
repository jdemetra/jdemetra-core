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
package demetra.tsprovider.util;

import demetra.data.AggregationType;
import demetra.timeseries.TsUnit;

/**
 * Parameters used when collecting observations in order to create time series.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
@lombok.experimental.Wither
public class ObsGathering {

    public static final ObsGathering DEFAULT = builder().build();

    @lombok.NonNull
    @lombok.Builder.Default
    private TsUnit unit = TsUnit.UNDEFINED;

    @lombok.NonNull
    @lombok.Builder.Default
    private AggregationType aggregationType = AggregationType.None;

    // FIXME: find a better name/description
    @lombok.Builder.Default
    private boolean complete = true;

    @lombok.Builder.Default
    private boolean skipMissingValues = true;
}
