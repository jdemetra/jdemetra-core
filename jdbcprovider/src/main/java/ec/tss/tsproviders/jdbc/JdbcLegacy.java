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
package ec.tss.tsproviders.jdbc;

import ec.tss.tsproviders.DataSet;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.legacy.IStringHandler;
import ec.tss.tsproviders.legacy.LinearIdBuilder;
import ec.tss.tsproviders.legacy.StringHandlers;
import ec.tss.tsproviders.utils.IParser;
import ec.tss.tsproviders.utils.Parsers;
import ec.tstoolkit.design.Immutable;

/**
 *
 * @author Philippe Charles
 */
public final class JdbcLegacy {

    private JdbcLegacy() {
        // static class
    }

    static Parsers.Parser<DataSource> dbParser(final String providerName, final String version) {
        return new Parsers.Parser<DataSource>() {
            @Override
            public DataSource parse(CharSequence input) throws NullPointerException {
                DbDataSourceId id = DbDataSourceId.parse(input);
                if (id == null) {
                    return null;
                }
                JdbcBean dbBean = new JdbcBean() {
                };
                dbBean.setDbName(id.getDbName());
                dbBean.setTableName(id.getTableName());
                dbBean.setDimColumns(id.getDomainColumn() + "," + id.getTsColumn());
                dbBean.setPeriodColumn(id.getPeriodColumn());
                dbBean.setValueColumn(id.getValueColumn());
                return dbBean.toDataSource(providerName, version);
            }
        };
    }

    static Parsers.Parser<DataSet> domainSeriesParser(final IParser<DataSource> legacyDbParser) {
        return new Parsers.Parser<DataSet>() {
            @Override
            public DataSet parse(CharSequence input) throws NullPointerException {
                FromDatabaseId id = FromDatabaseId.parse(input.toString());
                if (id != null && id.getDomain() != null) {
                    DataSource dataSource = legacyDbParser.parse(id.getDatabase());
                    if (dataSource != null) {
                        String[] dimColumns = JdbcBean.getDimArray(dataSource);
                        if (id.getSeries() != null) {
                            DataSet.Builder builder = DataSet.builder(dataSource, DataSet.Kind.SERIES);
                            builder.put(dimColumns[0], id.getDomain());
                            builder.put(dimColumns[1], id.getSeries());
                            return builder.build();
                        }
                        DataSet.Builder builder = DataSet.builder(dataSource, DataSet.Kind.COLLECTION);
                        builder.put(dimColumns[0], id.getDomain());
                        return builder.build();
                    }
                }
                return null;
            }
        };
    }

    /**
     *
     * @author Demortier Jeremy
     */
    @Deprecated
    static class FromDatabaseId {

        public static FromDatabaseId parse(String input) {
            LinearIdBuilder idbuild = LinearIdBuilder.parse(StringHandlers.PLAIN, input);
            if (idbuild.getCount() >= 6) {
                DbDataSourceId sourceId = DbDataSourceId.from(idbuild.get(0), idbuild.get(1), idbuild.get(2), idbuild.get(3), idbuild.get(4), idbuild.get(5));
                String domain = null;
                if (idbuild.getCount() >= 7) {
                    domain = idbuild.get(6);
                }
                String series = null;
                if (idbuild.getCount() >= 8) {
                    series = idbuild.get(7);
                }
                return new FromDatabaseId(sourceId, domain, series);
            }
            return null;
        }
        private DbDataSourceId m_sourceId;
        private String m_domain = null;
        private String m_series = null;

        public DbDataSourceId getDatabase() {
            return m_sourceId;
        }

        public String getDomain() {
            return m_domain;
        }

        public String getSeries() {
            return m_series;
        }

        private FromDatabaseId(DbDataSourceId sourceId, String domain, String series) {
            this.m_sourceId = sourceId;
            this.m_domain = domain;
            this.m_series = series;
        }

        public boolean isCollection() {
            return null == m_series && null != m_domain;
        }

        public boolean isMultiCollection() {
            return null == m_series && null == m_domain;
        }
    }

    /**
     *
     * @author Demortier Jeremy
     */
    @Immutable
    @Deprecated
    public static final class DbDataSourceId implements CharSequence {

        private static final IStringHandler SH = StringHandlers.PLAIN;

        // Factory Methods ->
        public static DbDataSourceId parse(CharSequence input) {
            return input instanceof DbDataSourceId ? (DbDataSourceId) input : parse(input.toString());
        }

        public static DbDataSourceId parse(String input) {
            LinearIdBuilder id = LinearIdBuilder.parse(SH, input);
            if (id == null || id.getCount() < 6) {
                return null;
            }
            return new DbDataSourceId(id);
        }

        public static DbDataSourceId from(String dbName, String tableName, String domainCol,
                String tsCol, String periodCol, String valueCol) {
            return new DbDataSourceId(LinearIdBuilder.from(SH, dbName, tableName, domainCol,
                    tsCol, periodCol, valueCol));
        }
        // <-
        private final LinearIdBuilder id;

        private DbDataSourceId(LinearIdBuilder id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return id.toString();
        }

        public String getDbName() {
            return id.get(0);
        }

        public String getTableName() {
            return id.get(1);
        }

        public String getDomainColumn() {
            return id.get(2);
        }

        public String getTsColumn() {
            return id.get(3);
        }

        public String getPeriodColumn() {
            return id.get(4);
        }

        public String getValueColumn() {
            return id.get(5);
        }

        @Override
        public int length() {
            return id.toString().length();
        }

        @Override
        public char charAt(int index) {
            return id.toString().charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return id.toString().subSequence(start, end);
        }

        @Override
        public int hashCode() {
            return getDbName().hashCode() + getTableName().hashCode()
                    + getDomainColumn().hashCode() + getTsColumn().hashCode()
                    + getPeriodColumn().hashCode() + getValueColumn().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || (obj instanceof DbDataSourceId && equals((DbDataSourceId) obj));
        }

        private boolean equals(DbDataSourceId other) {
            return this.getDbName().equals(other.getDbName())
                    && this.getTableName().equals(other.getTableName())
                    && this.getDomainColumn().equals(other.getDomainColumn())
                    && this.getTsColumn().equals(other.getTsColumn())
                    && this.getPeriodColumn().equals(other.getPeriodColumn())
                    && this.getValueColumn().equals(other.getValueColumn());
        }
    }
}
