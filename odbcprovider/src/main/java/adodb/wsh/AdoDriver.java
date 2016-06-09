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

import ec.tstoolkit.design.VisibleForTesting;
import static java.lang.String.format;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * https://msdn.microsoft.com/en-us/library/aa478977.aspx
 *
 * @author Philippe Charles
 * @since 2.1.0
 */
public final class AdoDriver extends _Driver {

    public static final String PREFIX = "jdbc:adodb:";

    private final Wsh wsh;
    private final ConcurrentMap<String, AdoContext> pool;

    public AdoDriver() {
        this(Wsh.getDefault(), new ConcurrentHashMap<>());
    }

    @VisibleForTesting
    AdoDriver(Wsh wsh, ConcurrentMap<String, AdoContext> pool) {
        this.wsh = wsh;
        this.pool = pool;
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (!acceptsURL(url)) {
            throw new SQLException(format("Invalid database url: '%s'", url));
        }
        String connectionString = url.trim().substring(PREFIX.length());
        return AdoConnection.of(getOrCreate(connectionString), this::recycle);
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return url != null && url.toLowerCase().startsWith(PREFIX);
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    private AdoContext getOrCreate(String connectionString) {
        AdoContext result = pool.remove(connectionString);
        return result != null ? result : AdoContext.of(wsh, connectionString);
    }

    private void recycle(AdoContext o) {
        pool.put(o.getConnectionString(), o);
    }
    //</editor-fold>
}
