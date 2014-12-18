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
package ec.tss.tsproviders.db;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.IDataSourceBean;
import ec.tss.tsproviders.utils.DataFormat;
import ec.tss.tsproviders.utils.IParam;
import static ec.tss.tsproviders.utils.Params.*;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
public class DbBean implements IDataSourceBean {

    public static final IParam<DataSource, String> X_DBNAME = onString("", "dbName");
    public static final IParam<DataSource, String> X_TABLENAME = onString("", "tableName");
    public static final IParam<DataSource, String> X_DIMCOLUMNS = onString("", "dimColumns");
    public static final IParam<DataSource, String> X_PERIODCOLUMN = onString("", "periodColumn");
    public static final IParam<DataSource, String> X_VALUECOLUMN = onString("", "valueColumn");
    public static final IParam<DataSource, DataFormat> X_DATAFORMAT = onDataFormat(DataFormat.DEFAULT, "locale", "datePattern", "numberPattern");
    public static final IParam<DataSource, String> X_VERSIONCOLUMN = onString("", "versionColumn");
    public static final IParam<DataSource, TsFrequency> X_FREQUENCY = onEnum(TsFrequency.Undefined, "frequency");
    public static final IParam<DataSource, TsAggregationType> X_AGGREGATION_TYPE = onEnum(TsAggregationType.None, "aggregationType");
    //
    protected String dbName;
    protected String tableName;
    protected String dimColumns;
    protected String periodColumn;
    protected String valueColumn;
    protected DataFormat dataFormat;
    protected String versionColumn;
    protected TsFrequency frequency;
    protected TsAggregationType aggregationType;

    public DbBean() {
        this.dbName = X_DBNAME.defaultValue();
        this.tableName = X_TABLENAME.defaultValue();
        this.dimColumns = X_DIMCOLUMNS.defaultValue();
        this.periodColumn = X_PERIODCOLUMN.defaultValue();
        this.valueColumn = X_VALUECOLUMN.defaultValue();
        this.dataFormat = X_DATAFORMAT.defaultValue();
        this.versionColumn = X_VERSIONCOLUMN.defaultValue();
        this.frequency = X_FREQUENCY.defaultValue();
        this.aggregationType = X_AGGREGATION_TYPE.defaultValue();
    }

    public DbBean(@Nonnull DataSource id) {
        this.dbName = X_DBNAME.get(id);
        this.tableName = X_TABLENAME.get(id);
        this.dimColumns = X_DIMCOLUMNS.get(id);
        this.periodColumn = X_PERIODCOLUMN.get(id);
        this.valueColumn = X_VALUECOLUMN.get(id);
        this.dataFormat = X_DATAFORMAT.get(id);
        this.versionColumn = X_VERSIONCOLUMN.get(id);
        this.frequency = X_FREQUENCY.get(id);
        this.aggregationType = X_AGGREGATION_TYPE.get(id);
    }

    //<editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getDimColumns() {
        return dimColumns;
    }

    public void setDimColumns(String dimColumns) {
        this.dimColumns = dimColumns;
    }

    public String getPeriodColumn() {
        return periodColumn;
    }

    public void setPeriodColumn(String periodColumn) {
        this.periodColumn = periodColumn;
    }

    public String getValueColumn() {
        return valueColumn;
    }

    public void setValueColumn(String valueColumn) {
        this.valueColumn = valueColumn;
    }

    public DataFormat getDataFormat() {
        return dataFormat;
    }

    public void setDataFormat(DataFormat dataFormat) {
        this.dataFormat = dataFormat;
    }

    public String getVersionColumn() {
        return versionColumn;
    }

    public void setVersionColumn(String versionColumn) {
        this.versionColumn = versionColumn;
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
    //</editor-fold>

    @Override
    public DataSource toDataSource(String providerName, String version) {
        DataSource.Builder builder = DataSource.builder(providerName, version);
        X_DBNAME.set(builder, dbName);
        X_TABLENAME.set(builder, tableName);
        X_DIMCOLUMNS.set(builder, dimColumns);
        X_PERIODCOLUMN.set(builder, periodColumn);
        X_VALUECOLUMN.set(builder, valueColumn);
        X_DATAFORMAT.set(builder, dataFormat);
        X_VERSIONCOLUMN.set(builder, versionColumn);
        X_FREQUENCY.set(builder, frequency);
        X_AGGREGATION_TYPE.set(builder, aggregationType);
        return builder.build();
    }

    @Nonnull
    public String[] getDimArray() {
        return getDimArray(dimColumns);
    }
    static final Splitter DIM_ARRAY_SPLITTER = Splitter.onPattern("\\W+").trimResults().omitEmptyStrings();

    static String[] getDimArray(String dimColumns) {
        return Iterables.toArray(DIM_ARRAY_SPLITTER.split(dimColumns), String.class);
    }

    @Nonnull
    public static String[] getDimArray(@Nonnull DataSource dataSource) {
        return getDimArray(X_DIMCOLUMNS.get(dataSource));
    }

    public static class BulkBean extends DbBean {

        public static final IParam<DataSource, Long> X_CACHE_TTL = onLong(TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES), "cacheTtl");
        public static final IParam<DataSource, Integer> X_CACHE_DEPTH = onInteger(1, "cacheDepth");
        //
        protected long cacheTtl;
        protected int cacheDepth;

        public BulkBean() {
            super();
            this.cacheTtl = X_CACHE_TTL.defaultValue();
            this.cacheDepth = X_CACHE_DEPTH.defaultValue();
        }

        public BulkBean(@Nonnull DataSource id) {
            super(id);
            this.cacheTtl = X_CACHE_TTL.get(id);
            this.cacheDepth = X_CACHE_DEPTH.get(id);
        }

        //<editor-fold defaultstate="collapsed" desc="Getters/Setters">
        public long getCacheTtl() {
            return cacheTtl;
        }

        public void setCacheTtl(long cacheTtl) {
            this.cacheTtl = cacheTtl >= 0 ? cacheTtl : 0;
        }

        public int getCacheDepth() {
            return cacheDepth;
        }

        public void setCacheDepth(int cacheDepth) {
            this.cacheDepth = cacheDepth >= 0 ? cacheDepth : 0;
        }
        //</editor-fold>

        @Override
        public DataSource toDataSource(String providerName, String version) {
            DataSource.Builder builder = DataSource.builder(providerName, version);
            X_DBNAME.set(builder, dbName);
            X_TABLENAME.set(builder, tableName);
            X_DIMCOLUMNS.set(builder, dimColumns);
            X_PERIODCOLUMN.set(builder, periodColumn);
            X_VALUECOLUMN.set(builder, valueColumn);
            X_DATAFORMAT.set(builder, dataFormat);
            X_VERSIONCOLUMN.set(builder, versionColumn);
            X_FREQUENCY.set(builder, frequency);
            X_AGGREGATION_TYPE.set(builder, aggregationType);
            X_CACHE_TTL.set(builder, cacheTtl);
            X_CACHE_DEPTH.set(builder, cacheDepth);
            return builder.build();
        }
    }
}
