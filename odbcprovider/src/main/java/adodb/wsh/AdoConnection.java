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

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

/**
 *
 * @author Philippe Charles
 */
final class AdoConnection extends _Connection {

    private final Wsh wsh;
    private final String connectionString;
    private final Map<String, AdoConnection> connectionPool;
    private final AdoDatabaseMetaData metaData;

    AdoConnection(Wsh wsh, String connectionString, Map<String, AdoConnection> connectionPool) {
        this.wsh = wsh;
        this.connectionString = connectionString;
        this.connectionPool = connectionPool;
        this.metaData = new AdoDatabaseMetaData(wsh, connectionString);
    }

    @Override
    public void close() throws SQLException {
        connectionPool.put(connectionString, this);
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return metaData;
    }

    @Override
    public String getCatalog() throws SQLException {
        return metaData.getProperty(AdoDatabaseMetaData.CURRENT_CATALOG);
    }

    @Override
    public String getSchema() throws SQLException {
        return null;
    }

    @Override
    public Statement createStatement() throws SQLException {
        return new AdoStatement(wsh, connectionString);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return new AdoPreparedStatement(wsh, connectionString, sql);
    }
}
