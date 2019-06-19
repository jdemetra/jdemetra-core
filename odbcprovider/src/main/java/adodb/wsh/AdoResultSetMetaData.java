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
package adodb.wsh;

import java.sql.Types;
import java.util.Arrays;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 * @since 2.1.0
 */
final class AdoResultSetMetaData extends _ResultSetMetaData {

    @NonNull
    static AdoResultSetMetaData of(@NonNull String[] names, @NonNull String[] dataTypes) throws IllegalArgumentException {
        if (names.length != dataTypes.length) {
            throw new IllegalArgumentException(String.format("Invalid data type length: expected '%s', found '%s'", names.length, dataTypes.length));
        }
        return new AdoResultSetMetaData(names, Arrays.stream(dataTypes).mapToInt(Integer::parseInt).mapToObj(DataTypeEnum::parse).toArray(DataTypeEnum[]::new));
    }

    private final String[] names;
    private final DataTypeEnum[] types;

    private AdoResultSetMetaData(String[] names, DataTypeEnum[] types) {
        this.names = names;
        this.types = types;
    }

    @Override
    public int getColumnCount() {
        return names.length;
    }

    @Override
    public String getColumnName(int column) {
        return names[column - 1];
    }

    @Override
    public int getColumnType(int column) {
        return types[column - 1].sqlType;
    }

    @Override
    public String getColumnClassName(int column) {
        // FIXME: potential bug?
        return String.class.getName();
    }

    @Override
    public int getColumnDisplaySize(int column) {
        return -1;
    }

    @Override
    public String getColumnLabel(int column) {
        return getColumnName(column);
    }

    @Override
    public String getColumnTypeName(int column) {
        return types[column - 1].name();
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private enum DataTypeEnum {

        adEmpty(0, Types.OTHER),
        adSmallInt(2, Types.SMALLINT),
        adInteger(3, Types.INTEGER),
        adSingle(4, Types.OTHER),
        adDouble(5, Types.DOUBLE),
        adCurrency(6, Types.OTHER),
        adDate(7, Types.DATE),
        adBSTR(8, Types.VARCHAR),
        adIDispatch(9, Types.OTHER),
        adError(10, Types.OTHER),
        adBoolean(11, Types.BOOLEAN),
        adVariant(12, Types.OTHER),
        adIUnknown(13, Types.OTHER),
        adDecimal(14, Types.DECIMAL),
        adTinyInt(16, Types.TINYINT),
        adUnsignedTinyInt(17, Types.TINYINT),
        adUnsignedSmallInt(18, Types.SMALLINT),
        adUnsignedInt(19, Types.INTEGER),
        adBigInt(20, Types.BIGINT),
        adUnsignedBigInt(21, Types.BIGINT),
        adFileTime(64, Types.OTHER),
        adGUID(72, Types.OTHER),
        adBinary(128, Types.OTHER),
        adChar(129, Types.CHAR),
        adWChar(130, Types.OTHER),
        adNumeric(131, Types.NUMERIC),
        adUserDefined(132, Types.OTHER),
        adDBDate(133, Types.DATE),
        adDBTime(134, Types.TIME),
        adDBTimeStamp(135, Types.TIMESTAMP),
        adChapter(136, Types.OTHER),
        adPropVariant(138, Types.OTHER),
        adVarNumeric(139, Types.OTHER),
        adVarChar(200, Types.VARCHAR),
        adLongVarChar(201, Types.OTHER),
        adVarWChar(202, Types.OTHER),
        adLongVarWChar(203, Types.OTHER),
        adVarBinary(204, Types.VARBINARY),
        adLongVarBinary(205, Types.VARBINARY),
        adArray(0x2000, Types.ARRAY);

        private final int value;
        private final int sqlType;

        private DataTypeEnum(int value, int sqlType) {
            this.value = value;
            this.sqlType = sqlType;
        }

        public static DataTypeEnum parse(int value) {
            for (DataTypeEnum o : values()) {
                if (o.value == value) {
                    return o;
                }
            }
            return DataTypeEnum.adIUnknown;
        }
    }
    //</editor-fold>
}
