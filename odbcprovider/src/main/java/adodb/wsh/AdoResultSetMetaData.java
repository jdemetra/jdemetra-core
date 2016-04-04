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

import java.sql.SQLException;
import java.sql.Types;

/**
 *
 * @author Philippe Charles
 */
final class AdoResultSetMetaData extends _ResultSetMetaData {

    private final AdoColumn[] columns;

    AdoResultSetMetaData(String[] columnNames, int[] columnValues) {
        this.columns = new AdoColumn[columnNames.length];
        for (int i = 0; i < columns.length; i++) {
            this.columns[i] = new AdoColumn(columnNames[i], DataTypeEnum.parse(columnValues[i]));
        }
    }

    @Override
    public int getColumnCount() throws SQLException {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) throws SQLException {
        return getColumn(column).name;
    }

    @Override
    public int getColumnType(int column) throws SQLException {
        return getColumn(column).type.sqlType;
    }

    @Override
    public String getColumnClassName(int column) throws SQLException {
        return String.class.getName();
    }

    @Override
    public int getColumnDisplaySize(int column) throws SQLException {
        return -1;
    }

    @Override
    public String getColumnLabel(int column) throws SQLException {
        return getColumnName(column);
    }

    @Override
    public String getColumnTypeName(int column) throws SQLException {
        return getColumn(column).type.name();
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private AdoColumn getColumn(int column) {
        return columns[column - 1];
    }

    private static final class AdoColumn {

        final String name;
        final DataTypeEnum type;

        public AdoColumn(String name, DataTypeEnum type) {
            this.name = name;
            this.type = type;
        }
    }

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
