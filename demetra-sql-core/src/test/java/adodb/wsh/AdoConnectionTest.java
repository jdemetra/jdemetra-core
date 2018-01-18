/*
 * Copyright 2016 National Bank of Belgium
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

import static adodb.wsh.AdoConnection.of;
import java.sql.SQLException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Philippe Charles
 */
public class AdoConnectionTest {

    @Test
    @SuppressWarnings("null")
    public void testFactory() {
        assertThat(good())
                .as("Factory must return a non-null connection")
                .isNotNull();

        assertThatThrownBy(() -> of(null, DO_NOTHING))
                .as("Factory must throw NullPointerException if context is null")
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> of(AdoContextTest.good(), null))
                .as("Factory must throw NullPointerException if onClose is null")
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testGetContext() {
        AdoContext context = AdoContextTest.good();
        assertThat(of(context, DO_NOTHING).getContext())
                .as("Context must be non-null and equal to the one specified in factory")
                .isEqualTo(context);
    }

    @Test
    public void testClose() throws SQLException {
        AtomicBoolean isClosed = new AtomicBoolean(false);
        of(AdoContextTest.good(), o -> isClosed.set(true)).close();
        assertThat(isClosed.get())
                .as("Close event must be propagated to observer")
                .isEqualTo(true);

        AdoConnection conn = good();
        assertThat(conn.isClosed()).isFalse();
        conn.close();
        assertThat(conn.isClosed()).isTrue();
        conn.close(); // no-op
    }

    @Test
    public void testGetMetaData() throws SQLException {
        assertThat(good().getMetaData())
                .as("MetaData must be non-null")
                .isNotNull();

        assertThatThrownBy(closed()::getMetaData)
                .as("MetaData must throw SQLException if called on a closed connection")
                .isInstanceOf(SQLException.class)
                .hasMessageContaining(CONN_STRING);
    }

    @Test
    public void testGetCatalog() throws SQLException {
        assertThat(good().getCatalog())
                .as("Catalog must return expected value")
                .isEqualTo("master");

        assertThatThrownBy(bad()::getCatalog)
                .as("Catalog must throw SQLException if IOException is raised")
                .isInstanceOf(SQLException.class)
                .hasMessageContaining(CONN_STRING)
                .hasCauseInstanceOf(IOException.class);

        assertThatThrownBy(ugly()::getCatalog)
                .as("Catalog must throw SQLException if content is invalid")
                .isInstanceOf(SQLException.class)
                .hasMessageContaining(CONN_STRING)
                .hasCauseInstanceOf(IOException.class);

        assertThatThrownBy(err()::getCatalog)
                .as("Catalog must throw SQLException if underlying exception is raised")
                .isInstanceOf(SQLException.class)
                .hasMessageContaining("name not found")
                .hasNoCause();

        assertThatThrownBy(closed()::getCatalog)
                .as("Catalog must throw SQLException if called on a closed connection")
                .isInstanceOf(SQLException.class)
                .hasMessageContaining(CONN_STRING);
    }

    @Test
    public void testSchema() throws SQLException {
        assertThatThrownBy(closed()::getSchema)
                .as("Schema must throw SQLException if called on a closed connection")
                .isInstanceOf(SQLException.class)
                .hasMessageContaining(CONN_STRING);
    }

    @Test
    public void testCreateStatement() throws SQLException {
        assertThat(good().createStatement())
                .as("Statement must be non-null")
                .isNotNull();

        assertThatThrownBy(closed()::createStatement)
                .as("Statement must throw SQLException if called on a closed connection")
                .isInstanceOf(SQLException.class)
                .hasMessageContaining(CONN_STRING);
    }

    @Test
    public void testPrepareStatement() throws SQLException {
        assertThat(good().prepareStatement(""))
                .as("PreparedStatement must be non-null")
                .isNotNull();

        assertThatThrownBy(() -> closed().prepareStatement(""))
                .as("PreparedStatement must throw SQLException if called on a closed connection")
                .isInstanceOf(SQLException.class)
                .hasMessageContaining(CONN_STRING);
    }

    @Test
    public void testIsReadOnly() throws SQLException {
        assertThat(good().isReadOnly())
                .as("ReadOnly must return expected value")
                .isEqualTo(true);

        assertThatThrownBy(closed()::isReadOnly)
                .as("ReadOnly must throw SQLException if called on a closed connection")
                .isInstanceOf(SQLException.class)
                .hasMessageContaining(CONN_STRING);
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    static final String CONN_STRING = "MyDb";

    static final Consumer<AdoContext> DO_NOTHING = o -> {
    };

    static AdoConnection good() {
        return of(AdoContextTest.good(), DO_NOTHING);
    }

    static AdoConnection bad() {
        return of(AdoContextTest.bad(), DO_NOTHING);
    }

    static AdoConnection ugly() {
        return of(AdoContextTest.ugly(), DO_NOTHING);
    }

    static AdoConnection err() {
        return of(AdoContextTest.err(), DO_NOTHING);
    }

    static AdoConnection closed() throws SQLException {
        AdoConnection result = good();
        result.close();
        return result;
    }
    //</editor-fold>
}
