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
import static java.lang.String.format;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 * @since 2.1.0
 */
final class AdoStatement extends _Statement {

    @Nonnull
    static AdoStatement of(@Nonnull AdoConnection conn) {
        return new AdoStatement(Objects.requireNonNull(conn));
    }

    private final AdoConnection conn;

    private AdoStatement(AdoConnection conn) {
        this.conn = conn;
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        try {
            return AdoResultSet.of(conn.getContext().preparedStatement(sql, Collections.emptyList()));
        } catch (IOException ex) {
            throw ex instanceof TsvReader.Err
                    ? new SQLException(ex.getMessage(), "", ((TsvReader.Err) ex).getNumber())
                    : new SQLException(format("Failed to execute query '%s'", sql), ex);
        }
    }

    @Override
    public void close() throws SQLException {
    }

    @Override
    public Connection getConnection() throws SQLException {
        return conn;
    }
}
