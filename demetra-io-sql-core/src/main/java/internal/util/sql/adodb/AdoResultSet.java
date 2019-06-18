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
package internal.util.sql.adodb;

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
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 * @since 2.1.0
 */
final class AdoResultSet extends _ResultSet {

    @NonNull
    static AdoResultSet of(@NonNull TsvReader tsv) throws IOException {
        try {
            return new AdoResultSet(tsv, AdoResultSetMetaData.of(tsv.getHeader(0), tsv.getHeader(1)));
        } catch (IllegalArgumentException ex) {
            throw new IOException("Invalid header", ex);
        }
    }

    private static final Locale EN_US = new Locale("en", "us");

    private final TsvReader reader;
    private final AdoResultSetMetaData metaData;
    private final DateFormat dateFormat;
    private final NumberFormat numberFormat;
    private final String[] currentRow;

    private AdoResultSet(TsvReader reader, AdoResultSetMetaData metaData) {
        this.reader = reader;
        this.metaData = metaData;
        this.dateFormat = new SimpleDateFormat("MM/dd/yyyy", EN_US);
        dateFormat.setLenient(false);
        this.numberFormat = NumberFormat.getInstance(EN_US);
        this.currentRow = new String[metaData.getColumnCount()];
    }

    @Override
    public boolean next() throws SQLException {
        try {
            return reader.readNextInto(currentRow);
        } catch (IOException ex) {
            throw ex instanceof TsvReader.Err
                    ? new SQLException(ex.getMessage(), "", ((TsvReader.Err) ex).getNumber())
                    : new SQLException("While reading next row", ex);
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
        return currentRow[columnIndex - 1];
    }

    @Override
    public String getString(int columnIndex) throws SQLException {
        return currentRow[columnIndex - 1];
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
        return new BigDecimal(currentRow[columnIndex - 1]);
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private java.util.Date parseDate(int columnIndex) throws SQLException {
        try {
            return dateFormat.parse(currentRow[columnIndex - 1]);
        } catch (ParseException ex) {
            throw new SQLException("While parsing date", ex);
        }
    }

    private Number parseNumber(int columnIndex) throws SQLException {
        try {
            return numberFormat.parse(currentRow[columnIndex - 1]);
        } catch (ParseException ex) {
            throw new SQLException("While parsing number", ex);
        }
    }
    //</editor-fold>
}
