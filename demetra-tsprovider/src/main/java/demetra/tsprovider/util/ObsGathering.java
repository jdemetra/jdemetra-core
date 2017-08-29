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
import javax.annotation.concurrent.Immutable;

/**
 * Parameters used when collecting observations in order to create time series.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@Immutable
@lombok.Value
@lombok.experimental.Wither
@lombok.Builder(builderClassName = "Builder")
public final class ObsGathering {

    public static final ObsGathering DEFAULT = new ObsGathering(TsUnit.UNDEFINED, AggregationType.None, true, true);

    @lombok.NonNull
    TsUnit unit;

    @lombok.NonNull
    AggregationType aggregationType;

    // FIXME: find a better name/description
    boolean complete;

    boolean skipMissingValues;

    public static final class Builder {

        TsUnit unit = TsUnit.UNDEFINED;
        AggregationType aggregationType = AggregationType.None;
        boolean complete = true;
        boolean skipMissingValues = true;
    }
}
