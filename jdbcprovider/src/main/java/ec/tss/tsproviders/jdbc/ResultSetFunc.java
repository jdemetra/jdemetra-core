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

import ec.tss.tsproviders.db.DbUtil;
import ec.tss.tsproviders.utils.IParser;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Defines a function that creates a new object from the current ResultSet.
 *
 * @author Philippe Charles
 */
public abstract class ResultSetFunc<T> implements DbUtil.Func<ResultSet, T, SQLException> {

    @NonNull
    public static ResultSetFunc<String> onNull() {
        return NullResultSetFunc.INSTANCE;
    }

    @NonNull
    public static ResultSetFunc<String> onGetString(final int columnIndex) {
        return new ResultSetFunc<String>() {
            @Override
            public String apply(ResultSet rs) throws SQLException {
                return rs.getString(columnIndex);
            }
        };
    }

    @NonNull
    public static ResultSetFunc<String[]> onGetStringArray(final int firstColumnIndex, final int length) {
        return new ResultSetFunc<String[]>() {
            @Override
            public String[] apply(ResultSet rs) throws SQLException {
                String[] result = new String[length];
                for (int i = 0; i < length; i++) {
                    result[i] = rs.getString(firstColumnIndex + i);
                }
                return result;
            }
        };
    }

    @NonNull
    public static ResultSetFunc<String> onGetObjectToString(final int columnIndex) {
        return new ResultSetFunc<String>() {
            @Override
            public String apply(ResultSet rs) throws SQLException {
                return rs.getObject(columnIndex).toString();
            }
        };
    }

    @NonNull
    public static <X> ResultSetFunc<X> compose(final int columnIndex, @NonNull final IParser<X> parser) {
        return new ResultSetFunc<X>() {
            @Override
            public X apply(ResultSet rs) throws SQLException {
                return parser.parse(rs.getObject(columnIndex).toString());
            }
        };
    }

    @Deprecated
    public static ResultSetFunc<java.util.Date> onDate(ResultSet rs, int columnIndex, IParser<java.util.Date> dateParser) throws SQLException {
        return onDate(rs.getMetaData(), columnIndex, dateParser);
    }

    @NonNull
    public static ResultSetFunc<java.util.Date> onDate(@NonNull ResultSetMetaData metaData, int columnIndex, @NonNull IParser<java.util.Date> dateParser) throws SQLException {
        ResultSetFunc<java.util.Date> result = dateBySqlType(metaData.getColumnType(columnIndex), columnIndex);
        return result != null ? result : compose(columnIndex, dateParser);
    }

    @Deprecated
    public static ResultSetFunc<Number> onNumber(ResultSet rs, int columnIndex, IParser<Number> numberParser) throws SQLException {
        return onNumber(rs.getMetaData(), columnIndex, numberParser);
    }

    @NonNull
    public static ResultSetFunc<Number> onNumber(@NonNull ResultSetMetaData metaData, int columnIndex, @NonNull IParser<Number> numberParser) throws SQLException {
        ResultSetFunc<Number> result = numberBySqlType(metaData.getColumnType(columnIndex), columnIndex);
        return result != null ? result : compose(columnIndex, numberParser);
    }

    private static final class NullResultSetFunc extends ResultSetFunc<String> {

        static final ResultSetFunc<String> INSTANCE = new NullResultSetFunc();

        @Override
        public String apply(ResultSet input) throws SQLException {
            return null;
        }
    }

    @Nullable
    private static ResultSetFunc<java.util.Date> dateBySqlType(int columnType, final int columnIndex) {
        switch (columnType) {
            case Types.DATE:
                return new ResultSetFunc<java.util.Date>() {
                    @Override
                    public java.util.Date apply(ResultSet o) throws SQLException {
                        return new java.util.Date(o.getDate(columnIndex).getTime());
                    }
                };
            case Types.TIMESTAMP:
                return new ResultSetFunc<java.util.Date>() {
                    @Override
                    public java.util.Date apply(ResultSet o) throws SQLException {
                        Timestamp timestamp = o.getTimestamp(columnIndex);
                        return new java.util.Date(timestamp.getTime() + (timestamp.getNanos() / 1000000));
                    }
                };
        }
        return null;
    }

    @Nullable
    private static ResultSetFunc<Number> numberBySqlType(int columnType, final int columnIndex) {
        switch (columnType) {
            case Types.BIGINT:
                return new ResultSetFunc<Number>() {
                    @Override
                    public Number apply(ResultSet o) throws SQLException {
                        return o.getLong(columnIndex);
                    }
                };
            case Types.DOUBLE:
            case Types.FLOAT:
                return new ResultSetFunc<Number>() {
                    @Override
                    public Number apply(ResultSet o) throws SQLException {
                        return o.getDouble(columnIndex);
                    }
                };
            case Types.INTEGER:
                return new ResultSetFunc<Number>() {
                    @Override
                    public Number apply(ResultSet o) throws SQLException {
                        return o.getInt(columnIndex);
                    }
                };
            case Types.DECIMAL:
            case Types.NUMERIC:
                return new ResultSetFunc<Number>() {
                    @Override
                    public Number apply(ResultSet o) throws SQLException {
                        return o.getBigDecimal(columnIndex);
                    }
                };
            case Types.REAL:
                return new ResultSetFunc<Number>() {
                    @Override
                    public Number apply(ResultSet o) throws SQLException {
                        return o.getFloat(columnIndex);
                    }
                };
            case Types.SMALLINT:
                return new ResultSetFunc<Number>() {
                    @Override
                    public Number apply(ResultSet o) throws SQLException {
                        return o.getShort(columnIndex);
                    }
                };
        }
        return null;
    }
}
