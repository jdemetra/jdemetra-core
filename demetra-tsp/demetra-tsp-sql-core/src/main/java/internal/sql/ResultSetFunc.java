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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import nbbrd.io.text.Parser;
import nbbrd.sql.jdbc.SqlFunc;
import nbbrd.sql.jdbc.SqlTypes;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

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
    T applyWithSql(@NonNull ResultSet t) throws SQLException;

    @NonNull
    @SuppressWarnings("null")
    static ResultSetFunc<String> onNull() {
        return rs -> null;
    }

    @NonNull
    static ResultSetFunc<String> onGetString(int columnIndex) {
        return rs -> rs.getString(columnIndex);
    }

    @NonNull
    static ResultSetFunc<String[]> onGetStringArray(int firstColumnIndex, int length) {
        return rs -> {
            String[] result = new String[length];
            for (int i = 0; i < length; i++) {
                result[i] = rs.getString(firstColumnIndex + i);
            }
            return result;
        };
    }

    @NonNull
    static ResultSetFunc<String> onGetObjectToString(int columnIndex) {
        return rs -> rs.getObject(columnIndex).toString();
    }

    @NonNull
    @SuppressWarnings("null")
    static <X> ResultSetFunc<X> compose(int columnIndex, @NonNull Parser<X> parser) {
        return rs -> {
            Object value = rs.getObject(columnIndex);
            return value != null ? parser.parse(value.toString()) : null;
        };
    }

    @NonNull
    static ResultSetFunc<java.util.Date> onDate(@NonNull ResultSetMetaData meta, int columnIndex, @NonNull Parser<java.util.Date> dateParser) throws SQLException {
        ResultSetFunc<java.util.Date> result = dateBySqlType(meta.getColumnType(columnIndex), columnIndex);
        return result != null ? result : compose(columnIndex, dateParser);
    }

    @NonNull
    static ResultSetFunc<Number> onNumber(@NonNull ResultSetMetaData meta, int columnIndex, @NonNull Parser<Number> numberParser) throws SQLException {
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
