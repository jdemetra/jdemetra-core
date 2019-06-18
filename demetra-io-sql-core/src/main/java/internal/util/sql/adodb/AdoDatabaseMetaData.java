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

import static internal.util.sql.adodb.AdoContext.SPECIAL_CHARACTERS;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import org.checkerframework.checker.nullness.qual.NonNull;
import static java.lang.String.format;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * @author Philippe Charles
 * @since 2.1.0
 */
final class AdoDatabaseMetaData extends _DatabaseMetaData {

    @NonNull
    static AdoDatabaseMetaData of(@NonNull AdoConnection conn) {
        return new AdoDatabaseMetaData(Objects.requireNonNull(conn));
    }

    private final AdoConnection conn;

    private AdoDatabaseMetaData(AdoConnection conn) {
        this.conn = conn;
    }

    @Override
    public boolean storesUpperCaseIdentifiers() throws SQLException {
        try {
            return conn.getContext().getIdentifierCaseType() == AdoContext.IdentifierCaseType.UPPER;
        } catch (IOException ex) {
            throw ex instanceof TsvReader.Err
                    ? new SQLException(ex.getMessage(), "", ((TsvReader.Err) ex).getNumber())
                    : new SQLException(format("Failed to get identifier case type of '%s'", conn.getContext().getConnectionString()), ex);
        }
    }

    @Override
    public boolean storesLowerCaseIdentifiers() throws SQLException {
        try {
            return conn.getContext().getIdentifierCaseType() == AdoContext.IdentifierCaseType.LOWER;
        } catch (IOException ex) {
            throw ex instanceof TsvReader.Err
                    ? new SQLException(ex.getMessage(), "", ((TsvReader.Err) ex).getNumber())
                    : new SQLException(format("Failed to get identifier case type of '%s'", conn.getContext().getConnectionString()), ex);
        }
    }

    @Override
    public boolean storesMixedCaseIdentifiers() throws SQLException {
        try {
            return conn.getContext().getIdentifierCaseType() == AdoContext.IdentifierCaseType.MIXED;
        } catch (IOException ex) {
            throw ex instanceof TsvReader.Err
                    ? new SQLException(ex.getMessage(), "", ((TsvReader.Err) ex).getNumber())
                    : new SQLException(format("Failed to get identifier case type of '%s'", conn.getContext().getConnectionString()), ex);
        }
    }

    @Override
    public String getIdentifierQuoteString() throws SQLException {
        return null;
    }

    @Override
    public String getSQLKeywords() throws SQLException {
        return "";
    }

    @Override
    public String getStringFunctions() throws SQLException {
        try {
            return conn.getContext().getStringFunctions()
                    .map(o -> o.getLabel())
                    .sorted()
                    .collect(Collectors.joining(","));
        } catch (IOException ex) {
            throw ex instanceof TsvReader.Err
                    ? new SQLException(ex.getMessage(), "", ((TsvReader.Err) ex).getNumber())
                    : new SQLException(format("Failed to get string functions of '%s'", conn.getContext().getConnectionString()), ex);
        }
    }

    @Override
    public String getExtraNameCharacters() throws SQLException {
        try {
            return conn.getContext().getProperty(SPECIAL_CHARACTERS);
        } catch (IOException ex) {
            throw ex instanceof TsvReader.Err
                    ? new SQLException(ex.getMessage(), "", ((TsvReader.Err) ex).getNumber())
                    : new SQLException(format("Failed to get extra name chars of '%s'", conn.getContext().getConnectionString()), ex);
        }
    }

    @Override
    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException {
        try {
            return AdoResultSet.of(conn.getContext().openSchema(catalog, schemaPattern, tableNamePattern, types));
        } catch (IOException ex) {
            throw ex instanceof TsvReader.Err
                    ? new SQLException(ex.getMessage(), "", ((TsvReader.Err) ex).getNumber())
                    : new SQLException(format("Failed to list tables with catalog='%s', schemaPattern='%s', tableNamePattern='%s', types='%s'", catalog, schemaPattern, tableNamePattern, types != null ? Arrays.toString(types) : null), ex);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return conn;
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return conn.isReadOnly();
    }
}
