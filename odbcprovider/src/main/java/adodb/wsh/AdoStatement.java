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

/**
 *
 * @author Philippe Charles
 */
final class AdoStatement extends _Statement {

    private final Wsh wsh;
    private final String connectionString;

    AdoStatement(Wsh wsh, String connectionString) {
        this.wsh = wsh;
        this.connectionString = connectionString;
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        try {
            return new AdoResultSet(wsh.exec("PreparedStatement", connectionString, sql));
        } catch (IOException ex) {
            throw new SQLException(ex);
        }
    }

    @Override
    public void close() throws SQLException {
    }
}
