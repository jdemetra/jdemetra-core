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
package ec.tss.tsproviders.utils;

import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import org.checkerframework.checker.nullness.qual.NonNull;
import net.jcip.annotations.Immutable;

/**
 * Parameters used when collecting observations in order to create time series.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@Immutable
@lombok.Value
@lombok.With
public final class ObsGathering {

    @NonNull
    public static ObsGathering includingMissingValues(@NonNull TsFrequency frequency, @NonNull TsAggregationType aggregationType) {
        return new ObsGathering(frequency, aggregationType, false);
    }

    @NonNull
    public static ObsGathering excludingMissingValues(@NonNull TsFrequency frequency, @NonNull TsAggregationType aggregationType) {
        return new ObsGathering(frequency, aggregationType, true);
    }

    public static final ObsGathering DEFAULT = excludingMissingValues(TsFrequency.Undefined, TsAggregationType.None);

    @lombok.NonNull
    private TsFrequency frequency;

    @lombok.NonNull
    private TsAggregationType aggregationType;

    private boolean skipMissingValues;

    private ObsGathering(TsFrequency frequency, TsAggregationType aggregationType, boolean skipMissingValues) {
        this.frequency = frequency;
        this.aggregationType = aggregationType;
        this.skipMissingValues = skipMissingValues;
    }
}
