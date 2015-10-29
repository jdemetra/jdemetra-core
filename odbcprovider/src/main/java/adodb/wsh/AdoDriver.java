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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * https://msdn.microsoft.com/en-us/library/aa478977.aspx
 *
 * @author Philippe Charles
 */
public final class AdoDriver extends _Driver {

    public static final String PREFIX = "jdbc:adodb:";

    private final Wsh wsh;
    private final Map<String, AdoConnection> connectionPool;

    public AdoDriver() {
        this.wsh = Wsh.getDefault();
        this.connectionPool = new ConcurrentHashMap<>();
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (!acceptsURL(url)) {
            throw new SQLException("Invalid database address: " + url);
        }
        String connectionString = url.trim().substring(PREFIX.length());
        Connection result = connectionPool.remove(connectionString);
        return result != null ? result : new AdoConnection(wsh, connectionString, connectionPool);
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return url != null && url.toLowerCase().startsWith(PREFIX);
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }
}
