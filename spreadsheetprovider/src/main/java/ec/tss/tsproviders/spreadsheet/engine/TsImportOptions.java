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

import ec.tss.tsproviders.utils.DataFormat;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
public final class TsImportOptions {

    @Nonnull
    public static TsImportOptions create(DataFormat dataFormat, TsFrequency frequency, TsAggregationType aggregationType, boolean cleanMissing) {
        return new TsImportOptions(dataFormat, frequency, aggregationType, cleanMissing);
    }

    @Nonnull
    public static TsImportOptions getDefault() {
        return new TsImportOptions(DataFormat.DEFAULT, TsFrequency.Undefined, TsAggregationType.None, true);
    }

    private final DataFormat dataFormat;
    private final TsFrequency frequency;
    private final TsAggregationType aggregationType;
    private final boolean cleanMissing;

    //<editor-fold defaultstate="collapsed" desc="Generated code">
    private TsImportOptions(DataFormat dataFormat, TsFrequency frequency, TsAggregationType aggregationType, boolean cleanMissing) {
        this.dataFormat = dataFormat;
        this.frequency = frequency;
        this.aggregationType = aggregationType;
        this.cleanMissing = cleanMissing;
    }

    public DataFormat getDataFormat() {
        return dataFormat;
    }

    public TsFrequency getFrequency() {
        return frequency;
    }

    public TsAggregationType getAggregationType() {
        return aggregationType;
    }

    public boolean isCleanMissing() {
        return cleanMissing;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.dataFormat);
        hash = 29 * hash + Objects.hashCode(this.frequency);
        hash = 29 * hash + Objects.hashCode(this.aggregationType);
        hash = 29 * hash + (this.cleanMissing ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TsImportOptions other = (TsImportOptions) obj;
        if (!Objects.equals(this.dataFormat, other.dataFormat)) {
            return false;
        }
        if (this.frequency != other.frequency) {
            return false;
        }
        if (this.aggregationType != other.aggregationType) {
            return false;
        }
        if (this.cleanMissing != other.cleanMissing) {
            return false;
        }
        return true;
    }
    //</editor-fold>
}
