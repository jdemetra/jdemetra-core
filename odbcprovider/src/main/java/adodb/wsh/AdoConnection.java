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

import static adodb.wsh.AdoContext.CURRENT_CATALOG;
import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import org.checkerframework.checker.nullness.qual.NonNull;
import static java.lang.String.format;
import java.util.Objects;
import java.util.function.Consumer;

/**
 *
 * @author Philippe Charles
 * @since 2.1.0
 */
final class AdoConnection extends _Connection {

    @NonNull
    static AdoConnection of(@NonNull AdoContext context, @NonNull Consumer<AdoContext> onClose) {
        return new AdoConnection(Objects.requireNonNull(context), Objects.requireNonNull(onClose));
    }

    private final AdoContext context;
    private final Consumer<AdoContext> onClose;
    private boolean closed;

    private AdoConnection(AdoContext context, Consumer<AdoContext> onClose) {
        this.context = context;
        this.onClose = onClose;
        this.closed = false;
    }

    @Override
    public void close() throws SQLException {
        if (!closed) {
            onClose.accept(context);
            closed = true;
        }
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return AdoDatabaseMetaData.of(checkState());
    }

    @Override
    public String getCatalog() throws SQLException {
        checkState();
        try {
            return context.getProperty(CURRENT_CATALOG);
        } catch (IOException ex) {
            throw ex instanceof TsvReader.Err
                    ? new SQLException(ex.getMessage(), "", ((TsvReader.Err) ex).getNumber())
                    : new SQLException(format("Failed to get catalog name of '%s'", context.getConnectionString()), ex);
        }
    }

    @Override
    public String getSchema() throws SQLException {
        checkState();
        return null;
    }

    @Override
    public Statement createStatement() throws SQLException {
        return AdoStatement.of(checkState());
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return AdoPreparedStatement.of(checkState(), sql);
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        checkState();
        return true;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return closed;
    }

    @NonNull
    AdoContext getContext() {
        return context;
    }

    private AdoConnection checkState() throws SQLException {
        if (closed) {
            throw new SQLException(format("Connection '%s' closed", context.getConnectionString()));
        }
        return this;
    }
}
