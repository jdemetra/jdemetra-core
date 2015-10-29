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

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Philippe Charles
 */
final class AdoDatabaseMetaData extends _DatabaseMetaData {

    // https://msdn.microsoft.com/en-us/library/ms676695%28v=vs.85%29.aspx
    public static final String CURRENT_CATALOG = "Current Catalog";
    public static final String SPECIAL_CHARACTERS = "Special Characters";

    private final Wsh wsh;
    private final String connectionString;
    private Map<String, String> properties;

    AdoDatabaseMetaData(Wsh wsh, String connectionString) {
        this.wsh = wsh;
        this.connectionString = connectionString;
        this.properties = null;
    }

    @Override
    public boolean storesUpperCaseIdentifiers() throws SQLException {
        return false;
    }

    @Override
    public boolean storesLowerCaseIdentifiers() throws SQLException {
        return false;
    }

    @Override
    public boolean storesMixedCaseIdentifiers() throws SQLException {
        return true;
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
        return null;
    }

    @Override
    public String getExtraNameCharacters() throws SQLException {
        return getProperty(SPECIAL_CHARACTERS);
    }

    @Override
    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException {
        try {
            String[] args = new String[4 + (types != null ? types.length : 0)];
            args[0] = connectionString;
            args[1] = catalog != null ? catalog : "\"\"";
            args[2] = schemaPattern != null && !schemaPattern.equals("%") ? schemaPattern : "\"\"";
            args[3] = tableNamePattern != null && !tableNamePattern.equals("%") ? tableNamePattern : "\"\"";
            for (int i = 4; i < args.length; i++) {
                args[i] = types[i - 4];
            }
            return new AdoResultSet(wsh.exec("OpenSchema", args));
        } catch (IOException ex) {
            throw new SQLException("While executing query", ex);
        }
    }

    String getProperty(String name) throws SQLException {
        if (properties == null) {
            properties = loadProperties();
        }
        return properties.get(name);
    }

    private Map<String, String> loadProperties() throws SQLException {
        try {
            try (ResultSet rs = new AdoResultSet(wsh.exec("DbProperties", connectionString, CURRENT_CATALOG, SPECIAL_CHARACTERS))) {
                Map<String, String> result = new HashMap<>();
                while (rs.next()) {
                    result.put(rs.getString(1), rs.getString(2));
                }
                return result;
            }
        } catch (IOException ex) {
            throw new SQLException("While loading properties", ex);
        }
    }
}
