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
import demetra.timeseries.Fixme;
import demetra.timeseries.TsFrequency;
import javax.annotation.Nonnull;
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
public final class ObsGathering {

    @Nonnull
    public static ObsGathering includingMissingValues(@Nonnull TsFrequency frequency, @Nonnull AggregationType aggregationType) {
        return new ObsGathering(frequency, aggregationType, false);
    }

    @Nonnull
    public static ObsGathering excludingMissingValues(@Nonnull TsFrequency frequency, @Nonnull AggregationType aggregationType) {
        return new ObsGathering(frequency, aggregationType, true);
    }

    public static final ObsGathering DEFAULT = excludingMissingValues(Fixme.Undefined, AggregationType.None);

    @lombok.NonNull
    TsFrequency frequency;

    @lombok.NonNull
    AggregationType aggregationType;

    boolean skipMissingValues;

    private ObsGathering(TsFrequency frequency, AggregationType aggregationType, boolean skipMissingValues) {
        this.frequency = frequency;
        this.aggregationType = aggregationType;
        this.skipMissingValues = skipMissingValues;
    }
}
