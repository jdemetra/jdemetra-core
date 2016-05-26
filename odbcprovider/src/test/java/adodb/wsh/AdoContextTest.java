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

import static adodb.wsh.AdoContext.CURRENT_CATALOG;
import static adodb.wsh.AdoContext.IDENTIFIER_CASE_SENSITIVITY;
import static adodb.wsh.AdoContext.SPECIAL_CHARACTERS;
import static adodb.wsh.AdoContext.STRING_FUNCTIONS;
import static adodb.wsh.AdoContext.of;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class AdoContextTest {

    @Test
    @SuppressWarnings("null")
    public void testGetProperty() throws IOException, SQLException {
        AdoContext c = good();
        assertThat(c.getProperty(CURRENT_CATALOG)).isEqualTo("master");
        assertThat(c.getProperty(SPECIAL_CHARACTERS)).isNotEmpty();
        assertThat(c.getProperty(IDENTIFIER_CASE_SENSITIVITY)).isEqualTo("8");
        assertThat(c.getProperty(STRING_FUNCTIONS)).isEqualTo("5242879");
        assertThat(c.getProperty("stuff")).isNull();

        assertThatThrownBy(() -> good().getProperty(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> bad().getProperty(CURRENT_CATALOG)).isInstanceOf(FileNotFoundException.class);
        assertThatThrownBy(() -> ugly().getProperty(CURRENT_CATALOG)).isInstanceOf(IOException.class);
        assertThatThrownBy(() -> err().getProperty(CURRENT_CATALOG)).isInstanceOf(TsvReader.Err.class);
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    static final String CONN_STRING = "MyDb";

    static AdoContext good() {
        return of(WshTest.good(), CONN_STRING);
    }

    static AdoContext bad() {
        return of(WshTest.bad(), CONN_STRING);
    }

    static AdoContext ugly() {
        return of(WshTest.ugly(), CONN_STRING);
    }

    static AdoContext err() {
        return of(WshTest.err(), CONN_STRING);
    }
    //</editor-fold>
}
