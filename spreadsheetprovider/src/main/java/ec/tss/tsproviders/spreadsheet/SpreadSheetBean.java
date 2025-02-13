/*
 * Copyright 2013 National Bank of Belgium
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
package ec.tss.tsproviders.spreadsheet;

import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.IDataSourceBean;
import ec.tss.tsproviders.IFileBean;
import ec.tss.tsproviders.utils.DataFormat;
import ec.tss.tsproviders.utils.IParam;
import ec.tss.tsproviders.utils.Params;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.io.File;
import java.nio.file.Paths;

/**
 *
 * @author Philippe Charles
 */
public class SpreadSheetBean implements IFileBean, IDataSourceBean {

    static final IParam<DataSource, File> X_FILE = Params.onFile(Paths.get("").toFile(), "file");
    static final IParam<DataSource, DataFormat> X_DATAFORMAT = Params.onDataFormat(DataFormat.DEFAULT, "locale", "datePattern", "numberPattern");
    static final IParam<DataSource, TsFrequency> X_FREQUENCY = Params.onEnum(TsFrequency.Undefined, "frequency");
    static final IParam<DataSource, TsAggregationType> X_AGGREGATION_TYPE = Params.onEnum(TsAggregationType.None, "aggregationType");
    static final IParam<DataSource, Boolean> X_CLEAN_MISSING = Params.onBoolean(true, "cleanMissing");
    //
    File file;
    DataFormat dataFormat;
    TsFrequency frequency;
    TsAggregationType aggregationType;
    boolean cleanMissing;

    public SpreadSheetBean() {
        this.file = X_FILE.defaultValue();
        this.dataFormat = X_DATAFORMAT.defaultValue();
        this.frequency = X_FREQUENCY.defaultValue();
        this.aggregationType = X_AGGREGATION_TYPE.defaultValue();
        this.cleanMissing = X_CLEAN_MISSING.defaultValue();
    }

    public SpreadSheetBean(DataSource dataSource) {
        this.file = X_FILE.get(dataSource);
        this.dataFormat = X_DATAFORMAT.get(dataSource);
        this.frequency = X_FREQUENCY.get(dataSource);
        this.aggregationType = X_AGGREGATION_TYPE.get(dataSource);
        this.cleanMissing = X_CLEAN_MISSING.get(dataSource);
    }

    //<editor-fold defaultstate="collapsed" desc="Getters/Setters">
    @Override
    public File getFile() {
        return file;
    }

    @Override
    public void setFile(File file) {
        this.file = file;
    }

    public DataFormat getDataFormat() {
        return dataFormat;
    }

    public void setDataFormat(DataFormat dataFormat) {
        this.dataFormat = dataFormat;
    }

    public TsFrequency getFrequency() {
        return frequency;
    }

    public void setFrequency(TsFrequency frequency) {
        this.frequency = frequency;
    }

    public TsAggregationType getAggregationType() {
        return aggregationType;
    }

    public void setAggregationType(TsAggregationType aggregationType) {
        this.aggregationType = aggregationType;
    }

    public boolean isCleanMissing() {
        return cleanMissing;
    }

    public void setCleanMissing(boolean clean) {
        this.cleanMissing = clean;
    }
    //</editor-fold>

    @Override
    public DataSource toDataSource(String providerName, String version) {
        DataSource.Builder builder = DataSource.builder(providerName, version);
        X_FILE.set(builder, file);
        X_DATAFORMAT.set(builder, dataFormat);
        X_FREQUENCY.set(builder, frequency);
        X_AGGREGATION_TYPE.set(builder, aggregationType);
        X_CLEAN_MISSING.set(builder, cleanMissing);
        return builder.build();
    }

    @Deprecated
    public DataSource toDataSource() {
        return toDataSource(SpreadSheetProvider.SOURCE, SpreadSheetProvider.VERSION);
    }

    @Deprecated
    public static SpreadSheetBean from(DataSource dataSource) {
        return new SpreadSheetBean(dataSource);
    }
}
