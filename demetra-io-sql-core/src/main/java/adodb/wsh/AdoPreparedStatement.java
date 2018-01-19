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
import static java.lang.String.format;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 * @since 2.1.0
 */
final class AdoPreparedStatement extends _PreparedStatement {

    @Nonnull
    static AdoPreparedStatement of(@Nonnull AdoConnection conn, @Nonnull String sql) {
        Objects.requireNonNull(conn);
        Objects.requireNonNull(sql);
        return new AdoPreparedStatement(conn, sql);
    }

    private final AdoConnection conn;
    private final String sql;
    private final List<String> parameters;
    private boolean closed;

    private AdoPreparedStatement(AdoConnection conn, String sql) {
        this.conn = conn;
        this.sql = sql;
        this.parameters = new ArrayList<>();
        this.closed = false;
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        checkState();
        try {
            return AdoResultSet.of(conn.getContext().preparedStatement(sql, parameters));
        } catch (IOException ex) {
            throw ex instanceof TsvReader.Err
                    ? new SQLException(ex.getMessage(), "", ((TsvReader.Err) ex).getNumber())
                    : new SQLException(format("Failed to execute query '%s'", sql), ex);
        }
    }

    @Override
    public void close() throws SQLException {
        closed = true;
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        checkState();
        parameters.add(parameterIndex - 1, x);
    }

    @Override
    public Connection getConnection() throws SQLException {
        checkState();
        return conn;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return closed;
    }

    private AdoPreparedStatement checkState() throws SQLException {
        if (closed) {
            throw new SQLException("PreparedStatement closed");
        }
        return this;
    }
}
