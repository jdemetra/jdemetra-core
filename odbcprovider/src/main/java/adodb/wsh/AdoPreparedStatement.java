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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Philippe Charles
 */
final class AdoPreparedStatement extends _PreparedStatement {

    private final Wsh wsh;
    private final String connectionString;
    private final String sql;
    private final List<String> parameters;

    AdoPreparedStatement(Wsh wsh, String connectionString, String sql) {
        this.wsh = wsh;
        this.connectionString = connectionString;
        this.sql = sql;
        this.parameters = new ArrayList<>();
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        try {
            String[] args = new String[2 + parameters.size()];
            args[0] = connectionString;
            args[1] = sql;
            for (int i = 0; i < parameters.size(); i++) {
                args[i + 2] = parameters.get(i);
            }
            return new AdoResultSet(wsh.exec("PreparedStatement", args));
        } catch (IOException ex) {
            throw new SQLException("While executing query", ex);
        }
    }

    @Override
    public void close() throws SQLException {
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        parameters.add(parameterIndex - 1, x);
    }
}
