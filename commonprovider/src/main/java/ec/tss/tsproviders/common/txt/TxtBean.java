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
package ec.tss.tsproviders.common.txt;

import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.IDataSourceBean;
import ec.tss.tsproviders.IFileBean;
import ec.tss.tsproviders.utils.DataFormat;
import ec.tss.tsproviders.utils.IParam;
import static ec.tss.tsproviders.utils.Params.*;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Locale;

/**
 *
 * @author Philippe Charles
 */
public class TxtBean implements IFileBean, IDataSourceBean {

    public enum Delimiter {

        TAB, SEMICOLON, COMMA, SPACE
    }

    public enum TextQualifier {

        NONE, QUOTE, DOUBLE_QUOTE
    }
    //
    static final IParam<DataSource, File> FILE = onFile(Paths.get("").toFile(), "file");
    static final IParam<DataSource, DataFormat> DATAFORMAT = onDataFormat(DataFormat.of(Locale.ENGLISH, "yyyy-MM-DD", null), "locale", "datePattern", "numberPattern");
    static final IParam<DataSource, Charset> CHARSET = onCharset(StandardCharsets.UTF_8, "charset");
    static final IParam<DataSource, Delimiter> DELIMITER = onEnum(Delimiter.TAB, "delimiter");
    static final IParam<DataSource, Boolean> HEADERS = onBoolean(true, "headers");
    static final IParam<DataSource, Integer> SKIPLINES = onInteger(0, "skipLines");
    static final IParam<DataSource, TextQualifier> TEXT_QUALIFIER = onEnum(TextQualifier.NONE, "textQualifier");
    static final IParam<DataSource, TsFrequency> X_FREQUENCY = onEnum(TsFrequency.Undefined, "frequency");
    static final IParam<DataSource, TsAggregationType> X_AGGREGATION_TYPE = onEnum(TsAggregationType.None, "aggregationType");
    static final IParam<DataSource, Boolean> X_CLEAN_MISSING = onBoolean(false, "cleanMissing");
    //
    File file;
    DataFormat dataFormat;
    Charset charset;
    Delimiter delimiter;
    TextQualifier textQualifier;
    boolean headers;
    int skipLines;
    TsFrequency frequency;
    TsAggregationType aggregationType;
    boolean cleanMissing;

    public TxtBean() {
        file = FILE.defaultValue();
        dataFormat = DATAFORMAT.defaultValue();
        charset = CHARSET.defaultValue();
        delimiter = DELIMITER.defaultValue();
        textQualifier = TEXT_QUALIFIER.defaultValue();
        headers = HEADERS.defaultValue();
        skipLines = SKIPLINES.defaultValue();
        frequency = X_FREQUENCY.defaultValue();
        aggregationType = X_AGGREGATION_TYPE.defaultValue();
        this.cleanMissing = X_CLEAN_MISSING.defaultValue();
    }

    public TxtBean(DataSource dataSource) {
        file = FILE.get(dataSource);
        dataFormat = DATAFORMAT.get(dataSource);
        charset = CHARSET.get(dataSource);
        delimiter = DELIMITER.get(dataSource);
        textQualifier = TEXT_QUALIFIER.get(dataSource);
        headers = HEADERS.get(dataSource);
        skipLines = SKIPLINES.get(dataSource);
        frequency = X_FREQUENCY.get(dataSource);
        aggregationType = X_AGGREGATION_TYPE.get(dataSource);
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

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public Delimiter getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(Delimiter delimiter) {
        this.delimiter = delimiter;
    }

    public TextQualifier getTextQualifier() {
        return textQualifier;
    }

    public void setTextQualifier(TextQualifier textQualifier) {
        this.textQualifier = textQualifier;
    }

    public int getSkipLines() {
        return skipLines;
    }

    public void setSkipLines(int skipLines) {
        this.skipLines = skipLines;
    }

    public boolean isHeaders() {
        return headers;
    }

    public void setHeaders(boolean headers) {
        this.headers = headers;
    }

    public TsFrequency getFrequency() {
        return frequency;
    }

    public void setFrequency(TsFrequency frequency) {
        this.frequency = frequency;
        if (frequency == TsFrequency.Undefined) {
            aggregationType = TsAggregationType.None;
        }
    }

    public TsAggregationType getAggregationType() {
        return aggregationType;
    }

    public void setAggregationType(TsAggregationType aggregationType) {
        if (frequency != TsFrequency.Undefined) {
            this.aggregationType = aggregationType;
        }
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
        FILE.set(builder, file);
        DATAFORMAT.set(builder, dataFormat);
        CHARSET.set(builder, charset);
        DELIMITER.set(builder, delimiter);
        TEXT_QUALIFIER.set(builder, textQualifier);
        HEADERS.set(builder, headers);
        SKIPLINES.set(builder, skipLines);
        X_FREQUENCY.set(builder, frequency);
        X_AGGREGATION_TYPE.set(builder, aggregationType);
        X_CLEAN_MISSING.set(builder, cleanMissing);
        return builder.build();
    }

    @Deprecated
    public DataSource toDataSource() {
        return toDataSource(TxtProvider.SOURCE, TxtProvider.VERSION);
    }

    @Deprecated
    public static TxtBean from(DataSource dataSource) {
        return new TxtBean(dataSource);
    }
}
