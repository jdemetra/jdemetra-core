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
package spreadsheet.xlsx.internal;

import java.util.Calendar;
import java.util.GregorianCalendar;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import spreadsheet.xlsx.XlsxDateSystem;

/**
 *
 * @author Philippe Charles
 */
public class XlsxDateSystemsTest {

    private final double EARLIEST_DATE_1900 = 1;
    private final double EARLIEST_DATE_1900_PLUS_1_SECOND = 1.000011574074074;
    private final double EARLIEST_DATE_1900_PLUS_1_YEAR = 367;

    private final double EARLIEST_DATE_1904 = 0;
    private final double EARLIEST_DATE_1904_PLUS_1_SECOND = 1.1574074074074073E-5;
    private final double EARLIEST_DATE_1904_PLUS_1_YEAR = 366;

    @Test
    public void testGetJavaDate() {
        Calendar cal = new GregorianCalendar();

        XlsxDateSystem x1900 = XlsxDateSystems.X1900;
        assertThat(x1900.getJavaDate(cal, EARLIEST_DATE_1900)).isEqualTo("1900-01-01T00:00:00.000");
        assertThat(x1900.getJavaDate(cal, EARLIEST_DATE_1900_PLUS_1_SECOND)).isEqualTo("1900-01-01T00:00:01.000");
        assertThat(x1900.getJavaDate(cal, EARLIEST_DATE_1900_PLUS_1_YEAR)).isEqualTo("1901-01-01T00:00:00.000");

        XlsxDateSystem x1904 = XlsxDateSystems.X1904;
        assertThat(x1904.getJavaDate(cal, EARLIEST_DATE_1904)).isEqualTo("1904-01-01T00:00:00.000");
        assertThat(x1904.getJavaDate(cal, EARLIEST_DATE_1904_PLUS_1_SECOND)).isEqualTo("1904-01-01T00:00:01.000");
        assertThat(x1904.getJavaDate(cal, EARLIEST_DATE_1904_PLUS_1_YEAR)).isEqualTo("1905-01-01T00:00:00.000");
    }
}
