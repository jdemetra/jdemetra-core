/*
 * Copyright 2015 National Bank of Belgium
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
package ec.tss.tsproviders.spreadsheet.engine;

import com.google.common.base.MoreObjects;
import ec.tss.tsproviders.utils.DataFormat;
import ec.tss.tsproviders.utils.ObsGathering;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 * @since 2.1.0
 */
public final class TsImportOptions {

    @Deprecated
    @Nonnull
    public static TsImportOptions create(DataFormat dataFormat, TsFrequency frequency, TsAggregationType aggregationType, boolean cleanMissing) {
        ObsGathering gathering = cleanMissing
                ? ObsGathering.excludingMissingValues(frequency, aggregationType)
                : ObsGathering.includingMissingValues(frequency, aggregationType);
        return new TsImportOptions(dataFormat, gathering);
    }

    /**
     *
     * @param dataFormat
     * @param gathering
     * @return a non-null object
     * @since 2.2.0
     */
    @Nonnull
    public static TsImportOptions create(DataFormat dataFormat, ObsGathering gathering) {
        return new TsImportOptions(dataFormat, gathering);
    }

    @Nonnull
    public static TsImportOptions getDefault() {
        return DEFAULT;
    }

    private static final TsImportOptions DEFAULT = create(DataFormat.DEFAULT, ObsGathering.excludingMissingValues(TsFrequency.Undefined, TsAggregationType.None));

    private final DataFormat dataFormat;
    private final ObsGathering gathering;

    private TsImportOptions(DataFormat dataFormat, ObsGathering gathering) {
        this.dataFormat = dataFormat;
        this.gathering = gathering;
    }

    @Nonnull
    public DataFormat getDataFormat() {
        return dataFormat;
    }

    /**
     * Gets the observation collection parameters
     *
     * @return a non-null object
     * @since 2.2.0
     */
    @Nonnull
    public ObsGathering getObsGathering() {
        return gathering;
    }

    @Deprecated
    @Nonnull
    public TsFrequency getFrequency() {
        return gathering.getFrequency();
    }

    @Deprecated
    @Nonnull
    public TsAggregationType getAggregationType() {
        return gathering.getAggregationType();
    }

    @Deprecated
    public boolean isCleanMissing() {
        return gathering.isSkipMissingValues();
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataFormat, gathering);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof TsImportOptions && equals((TsImportOptions) obj));
    }

    private boolean equals(TsImportOptions that) {
        return Objects.equals(this.dataFormat, that.dataFormat)
                && Objects.equals(this.gathering, that.gathering);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(TsImportOptions.class)
                .add("dataFormat", dataFormat)
                .add("collectionParams", gathering)
                .toString();
    }
}
