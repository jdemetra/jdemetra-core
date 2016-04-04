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

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 *
 * @author Philippe Charles
 */
final class AdoResultSet extends _ResultSet {

    private static final String DELIMITER = "\t";
    private static final Locale EN_US = new Locale("en", "us");

    private final BufferedReader reader;
    private final AdoResultSetMetaData metaData;
    private final DateFormat dateFormat;
    private final NumberFormat numberFormat;
    private final String[] currentRow;

    AdoResultSet(BufferedReader reader) throws IOException, SQLException {
        this.reader = reader;
        this.metaData = createMetaData(reader);
        this.dateFormat = new SimpleDateFormat("MM/dd/yyyy", EN_US);
        dateFormat.setLenient(false);
        this.numberFormat = NumberFormat.getInstance(EN_US);
        this.currentRow = new String[metaData.getColumnCount()];
    }

    @Override
    public boolean next() throws SQLException {
        try {
            String line = reader.readLine();
            if (line != null) {
                splitInto(line, currentRow);
                return true;
            }
            return false;
        } catch (IOException ex) {
            throw new SQLException("While reading next row", ex);
        }
    }

    @Override
    public void close() throws SQLException {
        try {
            reader.close();
        } catch (IOException ex) {
            throw new SQLException("While closing reader", ex);
        }
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return metaData;
    }

    @Override
    public Object getObject(int columnIndex) throws SQLException {
        return getItem(columnIndex);
    }

    @Override
    public String getString(int columnIndex) throws SQLException {
        return getItem(columnIndex);
    }

    @Override
    public Date getDate(int columnIndex) throws SQLException {
        return new Date(parseDate(columnIndex).getTime());
    }

    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        return new Timestamp(parseDate(columnIndex).getTime());
    }

    @Override
    public double getDouble(int columnIndex) throws SQLException {
        return parseNumber(columnIndex).doubleValue();
    }

    @Override
    public float getFloat(int columnIndex) throws SQLException {
        return parseNumber(columnIndex).floatValue();
    }

    @Override
    public long getLong(int columnIndex) throws SQLException {
        return parseNumber(columnIndex).longValue();
    }

    @Override
    public int getInt(int columnIndex) throws SQLException {
        return parseNumber(columnIndex).intValue();
    }

    @Override
    public short getShort(int columnIndex) throws SQLException {
        return parseNumber(columnIndex).shortValue();
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        return new BigDecimal(getItem(columnIndex));
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private String getItem(int columnIndex) throws IndexOutOfBoundsException {
        return currentRow[columnIndex - 1];
    }

    private java.util.Date parseDate(int columnIndex) throws SQLException {
        try {
            return dateFormat.parse(getItem(columnIndex));
        } catch (ParseException ex) {
            throw new SQLException(ex);
        }
    }

    private Number parseNumber(int columnIndex) throws SQLException {
        try {
            return numberFormat.parse(getItem(columnIndex));
        } catch (ParseException ex) {
            throw new SQLException(ex);
        }
    }

    private static String[] split(String line) {
        return line.split(DELIMITER, -1);
    }

    private static void splitInto(String line, String[] array) {
        int start = 0;
        for (int i = 0; i < array.length - 1; i++) {
            int stop = line.indexOf(DELIMITER, start);
            array[i] = line.substring(start, stop);
            start = stop + DELIMITER.length();
        }
        array[array.length - 1] = line.substring(start);
    }

    private static AdoResultSetMetaData createMetaData(BufferedReader reader) throws IOException {
        String line;
        if ((line = reader.readLine()) == null) {
            throw new IOException("Missing column names");
        }
        String[] columnsNames = split(line);

        if ((line = reader.readLine()) == null) {
            throw new IOException("Missing column types");
        }
        String[] tmp = split(line);
        int[] columnsValues = new int[tmp.length];
        for (int i = 0; i < columnsValues.length; i++) {
            columnsValues[i] = Integer.parseInt(tmp[i]);
        }

        return new AdoResultSetMetaData(columnsNames, columnsValues);
    }
    //</editor-fold>
}
