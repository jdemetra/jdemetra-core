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
package internal.sql;

import demetra.util.Parser;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import util.sql.SqlFunc;
import util.sql.SqlTypes;

/**
 * Defines a function that creates a new object from the current ResultSet.
 *
 * @author Philippe Charles
 * @param <T>
 */
@FunctionalInterface
public interface ResultSetFunc<T> extends SqlFunc<ResultSet, T> {

    @Override
    @Nullable
    T applyWithSql(@Nonnull ResultSet t) throws SQLException;

    @Nonnull
    @SuppressWarnings("null")
    static ResultSetFunc<String> onNull() {
        return rs -> null;
    }

    @Nonnull
    static ResultSetFunc<String> onGetString(int columnIndex) {
        return rs -> rs.getString(columnIndex);
    }

    @Nonnull
    static ResultSetFunc<String[]> onGetStringArray(int firstColumnIndex, int length) {
        return rs -> {
            String[] result = new String[length];
            for (int i = 0; i < length; i++) {
                result[i] = rs.getString(firstColumnIndex + i);
            }
            return result;
        };
    }

    @Nonnull
    static ResultSetFunc<String> onGetObjectToString(int columnIndex) {
        return rs -> rs.getObject(columnIndex).toString();
    }

    @Nonnull
    @SuppressWarnings("null")
    static <X> ResultSetFunc<X> compose(int columnIndex, @Nonnull Parser<X> parser) {
        return rs -> {
            Object value = rs.getObject(columnIndex);
            return value != null ? parser.parse(value.toString()) : null;
        };
    }

    @Nonnull
    static ResultSetFunc<java.util.Date> onDate(@Nonnull ResultSetMetaData meta, int columnIndex, @Nonnull Parser<java.util.Date> dateParser) throws SQLException {
        ResultSetFunc<java.util.Date> result = dateBySqlType(meta.getColumnType(columnIndex), columnIndex);
        return result != null ? result : compose(columnIndex, dateParser);
    }

    @Nonnull
    static ResultSetFunc<Number> onNumber(@Nonnull ResultSetMetaData meta, int columnIndex, @Nonnull Parser<Number> numberParser) throws SQLException {
        ResultSetFunc<Number> result = numberBySqlType(meta.getColumnType(columnIndex), columnIndex);
        return result != null ? result : compose(columnIndex, numberParser);
    }

    @Nullable
    static ResultSetFunc<java.util.Date> dateBySqlType(int columnType, int columnIndex) {
        switch (columnType) {
            case Types.DATE:
                return o -> SqlTypes.getJavaDate(o.getDate(columnIndex));
            case Types.TIMESTAMP:
                return o -> SqlTypes.getJavaDate(o.getTimestamp(columnIndex));
        }
        return null;
    }

    @Nullable
    static ResultSetFunc<Number> numberBySqlType(int columnType, int columnIndex) {
        switch (columnType) {
            case Types.BIGINT:
                return o -> o.getLong(columnIndex);
            case Types.DOUBLE:
            case Types.FLOAT:
                return o -> o.getDouble(columnIndex);
            case Types.INTEGER:
                return o -> o.getInt(columnIndex);
            case Types.DECIMAL:
            case Types.NUMERIC:
                return o -> o.getBigDecimal(columnIndex);
            case Types.REAL:
                return o -> o.getFloat(columnIndex);
            case Types.SMALLINT:
                return o -> o.getShort(columnIndex);
        }
        return null;
    }
}
