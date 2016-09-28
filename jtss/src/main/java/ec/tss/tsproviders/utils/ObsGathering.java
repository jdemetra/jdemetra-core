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

import com.google.common.base.MoreObjects;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Parameters used when collecting observations in order to create time series.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@ThreadSafe
public final class ObsGathering {

    @Nonnull
    public static ObsGathering includingMissingValues(@Nonnull TsFrequency frequency, @Nonnull TsAggregationType aggregationType) {
        return new ObsGathering(frequency, aggregationType, false);
    }

    @Nonnull
    public static ObsGathering excludingMissingValues(@Nonnull TsFrequency frequency, @Nonnull TsAggregationType aggregationType) {
        return new ObsGathering(frequency, aggregationType, true);
    }

    private final TsFrequency frequency;
    private final TsAggregationType aggregationType;
    private final boolean skipMissingValues;

    private ObsGathering(TsFrequency frequency, TsAggregationType aggregationType, boolean skipMissingValues) {
        this.frequency = frequency;
        this.aggregationType = aggregationType;
        this.skipMissingValues = skipMissingValues;
    }

    @Nonnull
    public TsFrequency getFrequency() {
        return frequency;
    }

    @Nonnull
    public TsAggregationType getAggregationType() {
        return aggregationType;
    }

    public boolean isSkipMissingValues() {
        return skipMissingValues;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof ObsGathering && equals((ObsGathering) obj));
    }

    private boolean equals(ObsGathering that) {
        return this.frequency == that.frequency
                && this.aggregationType == that.aggregationType
                && this.skipMissingValues == that.skipMissingValues;
    }

    @Override
    public int hashCode() {
        return Objects.hash(frequency, aggregationType, skipMissingValues);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(ObsGathering.class)
                .add("frequency", frequency)
                .add("aggregationType", aggregationType)
                .add("skipMissingValues", skipMissingValues)
                .toString();
    }
}
