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

import static adodb.wsh.AdoConnectionTest.CONN_STRING;
import static adodb.wsh.AdoDatabaseMetaData.of;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class AdoDatabaseMetaDataTest {

    @Test
    @SuppressWarnings("null")
    public void testFactory() throws SQLException {
        assertThat(good())
                .as("Factory must return a non-null DataBaseMetaData")
                .isNotNull();

        assertThatThrownBy(() -> of(null))
                .as("Factory must throw NullPointerException if connection is null")
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testStoresUpperCaseIdentifiers() throws SQLException {
        assertThat(good().storesUpperCaseIdentifiers()).isFalse();
        assertThatThrownBy(bad()::storesUpperCaseIdentifiers).isInstanceOf(SQLException.class);
        assertThatThrownBy(ugly()::storesUpperCaseIdentifiers).isInstanceOf(SQLException.class);
        assertThatThrownBy(err()::storesUpperCaseIdentifiers).isInstanceOf(SQLException.class);
    }

    @Test
    public void testStoresLowerCaseIdentifiers() throws SQLException {
        assertThat(good().storesLowerCaseIdentifiers()).isFalse();
        assertThatThrownBy(bad()::storesLowerCaseIdentifiers).isInstanceOf(SQLException.class);
        assertThatThrownBy(ugly()::storesLowerCaseIdentifiers).isInstanceOf(SQLException.class);
        assertThatThrownBy(err()::storesLowerCaseIdentifiers).isInstanceOf(SQLException.class);
    }

    @Test
    public void testStoresMixedCaseIdentifiers() throws SQLException {
        assertThat(good().storesMixedCaseIdentifiers()).isTrue();
        assertThatThrownBy(bad()::storesMixedCaseIdentifiers).isInstanceOf(SQLException.class);
        assertThatThrownBy(ugly()::storesMixedCaseIdentifiers).isInstanceOf(SQLException.class);
        assertThatThrownBy(err()::storesMixedCaseIdentifiers).isInstanceOf(SQLException.class);
    }

    @Test
    public void testGetExtraNameCharacters() throws SQLException {
        assertThat(good().getExtraNameCharacters())
                .as("ExtraNameCharacters must return expected value")
                .isNotEmpty();

        assertThatThrownBy(bad()::getExtraNameCharacters)
                .as("ExtraNameCharacters must throw SQLException if IOException is raised")
                .isInstanceOf(SQLException.class)
                .hasMessageContaining(CONN_STRING)
                .hasCauseInstanceOf(IOException.class);

        assertThatThrownBy(ugly()::getExtraNameCharacters)
                .as("ExtraNameCharacters must throw SQLException if content is invalid")
                .isInstanceOf(SQLException.class)
                .hasMessageContaining(CONN_STRING)
                .hasCauseInstanceOf(IOException.class);

        assertThatThrownBy(err()::getExtraNameCharacters)
                .as("ExtraNameCharacters must throw SQLException if underlying exception is raised")
                .isInstanceOf(SQLException.class)
                .hasMessageContaining("name not found")
                .hasNoCause();
    }

    @Test
    public void testGetSQLKeywords() throws SQLException {
        assertThat(good().getSQLKeywords()).isNotNull();
    }

    @Test
    public void testGetStringFunctions() throws SQLException {
        assertThat(good().getStringFunctions().split(",", -1))
                .as("StringFunctions must return expected value")
                .containsOnly("CONCAT", "INSERT", "LEFT", "LTRIM", "LENGTH", "LOCATE", "LCASE", "REPEAT", "REPLACE", "RIGHT", "RTRIM", "SUBSTRING", "UCASE", "ASCII", "CHAR", "DIFFERENCE", "LOCATE_2", "SOUNDEX", "SPACE", "BIT_LENGTH", "OCTET_LENGTH");

        assertThatThrownBy(bad()::getStringFunctions)
                .as("StringFunctions must throw SQLException if IOException is raised")
                .isInstanceOf(SQLException.class)
                .hasMessageContaining(CONN_STRING)
                .hasCauseInstanceOf(IOException.class);

        assertThatThrownBy(ugly()::getStringFunctions)
                .as("StringFunctions must throw SQLException if content is invalid")
                .isInstanceOf(SQLException.class)
                .hasMessageContaining(CONN_STRING)
                .hasCauseInstanceOf(IOException.class);

        assertThatThrownBy(err()::getStringFunctions)
                .as("StringFunctions must throw SQLException if underlying exception is raised")
                .isInstanceOf(SQLException.class)
                .hasMessageContaining("name not found")
                .hasNoCause();
    }

    @Test
    public void testGetConnection() throws SQLException {
        AdoConnection conn = AdoConnectionTest.good();
        assertThat(of(conn).getConnection()).isEqualTo(conn);
    }

    @Test
    public void testGetTables() throws SQLException {
        try (ResultSet rs = good().getTables(null, null, null, null)) {
            int index = 0;
            while (rs.next()) {
                switch (index++) {
                    case 0:
                        assertThat(rs.getString(3)).isEqualTo("sysmatrixageforget");
                        break;
                }
            }
            assertThat(index).isEqualTo(13);
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    static AdoDatabaseMetaData good() {
        return of(AdoConnectionTest.good());
    }

    static AdoDatabaseMetaData bad() {
        return of(AdoConnectionTest.bad());
    }

    static AdoDatabaseMetaData ugly() {
        return of(AdoConnectionTest.ugly());
    }

    static AdoDatabaseMetaData err() {
        return of(AdoConnectionTest.err());
    }
    //</editor-fold>
}
