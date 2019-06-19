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

import ec.tss.tsproviders.db.DbAccessor;
import ec.tss.tsproviders.db.DbSeries;
import ec.tss.tsproviders.db.DbSetId;
import ec.tss.tsproviders.db.DbUtil;
import ec.tstoolkit.utilities.GuavaCaches;
import ec.util.jdbc.JdbcTable;
import ec.util.jdbc.SqlIdentifierQuoter;
import java.sql.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;

/**
 *
 * @author Demortier Jeremy
 * @author Philippe Charles
 */
public class JdbcAccessor<BEAN extends JdbcBean> extends DbAccessor.Commander<BEAN> {

    protected final Logger logger;
    protected final ConnectionSupplier supplier;

    public JdbcAccessor(@NonNull Logger logger, @NonNull BEAN dbBean, @NonNull ConnectionSupplier supplier) {
        super(dbBean);
        this.logger = logger;
        this.supplier = supplier;
    }

    @Override
    public Exception testDbBean() {
        Exception result = super.testDbBean();
        if (result != null) {
            return result;
        }
        try (Connection conn = supplier.getConnection(dbBean)) {
            DatabaseMetaData metaData = conn.getMetaData();
            String tableName = SqlIdentifierQuoter.create(metaData).quote(dbBean.getTableName(), false);
            if (JdbcTable.allOf(metaData, null, null, tableName, null).isEmpty()) {
                return new Exception("Table named '" + dbBean.getTableName() + "' does not exist");
            }
            return null;
        } catch (SQLException ex) {
            return ex;
        }
    }

    /**
     * Creates a function that returns a child id from the current record of a
     * ResultSet.
     *
     * @param metaData
     * @param columnIndex
     * @return
     * @throws SQLException
     */
    @NonNull
    protected ResultSetFunc<String> getChildFunc(@NonNull ResultSetMetaData metaData, int columnIndex) throws SQLException {
        return ResultSetFunc.onGetString(columnIndex);
    }

    /**
     * Creates a function that returns dimension values from the current record
     * of a ResultSet.
     *
     * @param metaData
     * @param firstColumnIndex
     * @param length
     * @return
     * @throws SQLException
     */
    @NonNull
    protected ResultSetFunc<String[]> getDimValuesFunc(@NonNull ResultSetMetaData metaData, int firstColumnIndex, int length) throws SQLException {
        return ResultSetFunc.onGetStringArray(firstColumnIndex, length);
    }

    /**
     * Creates a function that returns a period from the current record of a
     * ResultSet.
     *
     * @param metaData
     * @param columnIndex
     * @return
     * @throws SQLException
     */
    @NonNull
    protected ResultSetFunc<java.util.Date> getPeriodFunc(@NonNull ResultSetMetaData metaData, int columnIndex) throws SQLException {
        return ResultSetFunc.onDate(metaData, columnIndex, dateParser);
    }

    /**
     * Creates a function that returns a value from the current record of a
     * ResultSet.
     *
     * @param metaData
     * @param columnIndex
     * @return
     * @throws SQLException
     */
    @NonNull
    protected ResultSetFunc<Number> getValueFunc(@NonNull ResultSetMetaData metaData, int columnIndex) throws SQLException {
        return ResultSetFunc.onNumber(metaData, columnIndex, numberParser);
    }

    @Override
    protected Callable<List<DbSetId>> getAllSeriesQuery(DbSetId ref) {
        return new JdbcQuery<List<DbSetId>>(ref) {
            @Override
            protected String getQueryString(DatabaseMetaData metaData) throws SQLException {
                return SelectBuilder.from(getDbBean().getTableName())
                        .distinct(true)
                        .select(ref.selectColumns())
                        .filter(ref.filterColumns())
                        .orderBy(ref.selectColumns())
                        .withQuoter(SqlIdentifierQuoter.create(metaData))
                        .build();
            }

            @Override
            protected List<DbSetId> process(final ResultSet rs) throws SQLException {
                final ResultSetFunc<String[]> toDimValues = getDimValuesFunc(rs.getMetaData(), 1, ref.getDepth());

                DbUtil.AllSeriesCursor<SQLException> cursor = new DbUtil.AllSeriesCursor<SQLException>() {
                    @Override
                    public boolean next() throws SQLException {
                        boolean result = rs.next();
                        if (result) {
                            dimValues = toDimValues.apply(rs);
                        }
                        return result;
                    }
                };

                return DbUtil.getAllSeries(cursor, ref);
            }
        };
    }

    @Override
    protected Callable<List<DbSeries>> getAllSeriesWithDataQuery(DbSetId ref) {
        return new JdbcQuery<List<DbSeries>>(ref) {
            @Override
            protected String getQueryString(DatabaseMetaData metaData) throws SQLException {
                JdbcBean dbBean = getDbBean();
                return SelectBuilder.from(dbBean.getTableName())
                        .select(ref.selectColumns()).select(dbBean.getPeriodColumn(), dbBean.getValueColumn())
                        .filter(ref.filterColumns())
                        .orderBy(ref.selectColumns()).orderBy(dbBean.getPeriodColumn(), dbBean.getVersionColumn())
                        .withQuoter(SqlIdentifierQuoter.create(metaData))
                        .build();
            }

            @Override
            protected List<DbSeries> process(final ResultSet rs) throws SQLException {
                // Beware that some jdbc drivers require to get the columns values 
                // in the order of the query and only once.
                // So, call the following methods once per row and in this order.
                ResultSetMetaData metaData = rs.getMetaData();
                final ResultSetFunc<String[]> toDimValues = getDimValuesFunc(metaData, 1, ref.getDepth());
                final ResultSetFunc<java.util.Date> toPeriod = getPeriodFunc(metaData, ref.getDepth() + 1);
                final ResultSetFunc<Number> toValue = getValueFunc(metaData, ref.getDepth() + 2);

                DbUtil.AllSeriesWithDataCursor<SQLException> cursor = new DbUtil.AllSeriesWithDataCursor<SQLException>() {
                    @Override
                    public boolean next() throws SQLException {
                        boolean result = rs.next();
                        if (result) {
                            dimValues = toDimValues.apply(rs);
                            period = toPeriod.apply(rs);
                            value = period != null ? toValue.apply(rs) : null;
                        }
                        return result;
                    }
                };

                JdbcBean dbBean = getDbBean();
                return DbUtil.getAllSeriesWithData(cursor, ref, dbBean.getFrequency(), dbBean.getAggregationType());
            }
        };
    }

    @Override
    protected Callable<DbSeries> getSeriesWithDataQuery(DbSetId ref) {
        return new JdbcQuery<DbSeries>(ref) {
            @Override
            protected String getQueryString(DatabaseMetaData metaData) throws SQLException {
                JdbcBean dbBean = getDbBean();
                return SelectBuilder.from(dbBean.getTableName())
                        .select(dbBean.getPeriodColumn(), dbBean.getValueColumn())
                        .filter(ref.filterColumns())
                        .orderBy(dbBean.getPeriodColumn(), dbBean.getVersionColumn())
                        .withQuoter(SqlIdentifierQuoter.create(metaData))
                        .build();
            }

            @Override
            protected DbSeries process(final ResultSet rs) throws SQLException {
                // Beware that some jdbc drivers require to get the columns values 
                // in the order of the query and only once.
                // So, call the following methods once per row and in this order.
                ResultSetMetaData metaData = rs.getMetaData();
                final ResultSetFunc<java.util.Date> toPeriod = getPeriodFunc(metaData, 1);
                final ResultSetFunc<Number> toValue = getValueFunc(metaData, 2);

                DbUtil.SeriesWithDataCursor<SQLException> cursor = new DbUtil.SeriesWithDataCursor<SQLException>() {
                    @Override
                    public boolean next() throws SQLException {
                        boolean result = rs.next();
                        if (result) {
                            period = toPeriod.apply(rs);
                            value = period != null ? toValue.apply(rs) : null;
                        }
                        return result;
                    }
                };

                JdbcBean dbBean = getDbBean();
                return DbUtil.getSeriesWithData(cursor, ref, dbBean.getFrequency(), dbBean.getAggregationType());
            }
        };
    }

    @Override
    protected Callable<List<String>> getChildrenQuery(DbSetId ref) {
        return new JdbcQuery<List<String>>(ref) {
            @Override
            protected String getQueryString(DatabaseMetaData metaData) throws SQLException {
                String column = ref.getColumn(ref.getLevel());
                return SelectBuilder.from(getDbBean().getTableName())
                        .distinct(true)
                        .select(column)
                        .filter(ref.filterColumns())
                        .orderBy(column)
                        .withQuoter(SqlIdentifierQuoter.create(metaData))
                        .build();
            }

            @Override
            protected List<String> process(final ResultSet rs) throws SQLException {
                final ResultSetFunc<String> toChild = getChildFunc(rs.getMetaData(), 1);

                DbUtil.ChildrenCursor<SQLException> cursor = new DbUtil.ChildrenCursor<SQLException>() {
                    @Override
                    public boolean next() throws SQLException {
                        boolean result = rs.next();
                        if (result) {
                            child = toChild.apply(rs);
                        }
                        return result;
                    }
                };

                return DbUtil.getChildren(cursor);
            }
        };
    }

    @Override
    public DbAccessor<BEAN> memoize() {
        Duration duration = Duration.ofMillis(dbBean.getCacheTtl());
        return DbAccessor.BulkAccessor.from(this, dbBean.getCacheDepth(), GuavaCaches.ttlCache(duration));
    }

    /**
     * An implementation of Callable that handles SQL queries from Jdbc.
     *
     * @param <T>
     */
    protected abstract class JdbcQuery<T> implements Callable<T> {

        protected final DbSetId ref;

        protected JdbcQuery(@NonNull DbSetId ref) {
            this.ref = ref;
        }

        /**
         * Creates an SQL statement that may contain one or more '?' IN
         * parameter placeholders
         *
         * @return a SQL statement
         */
        @NonNull
        protected String getQueryString(DatabaseMetaData metaData) throws SQLException {
            return getQueryString();
        }

        @NonNull
        @Deprecated
        protected String getQueryString() {
            throw new RuntimeException("Deprecated");
        }

        protected void setParameters(@NonNull PreparedStatement statement) throws SQLException {
            for (int i = 0; i < ref.getLevel(); i++) {
                statement.setString(i + 1, ref.getValue(i));
            }
        }

        /**
         * Process the specified ResultSet in order to create the expected
         * result.
         *
         * @param rs the ResultSet to be processed
         * @return
         * @throws SQLException
         */
        @Nullable
        abstract protected T process(@NonNull ResultSet rs) throws SQLException;

        @Override
        public T call() throws SQLException {
            JdbcBean dbBean = getDbBean();
            synchronized (dbBean) {
                try (Connection conn = supplier.getConnection(dbBean)) {
                    String queryString = getQueryString(conn.getMetaData());
                    logger.debug(queryString);
                    try (PreparedStatement cmd = conn.prepareStatement(queryString)) {
                        setParameters(cmd);
                        try (ResultSet rs = cmd.executeQuery()) {
                            return process(rs);
                        }
                    }
                }
            }
        }
    }
}
