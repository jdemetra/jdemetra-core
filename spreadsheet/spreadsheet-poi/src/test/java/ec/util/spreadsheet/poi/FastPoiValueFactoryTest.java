/*
 * Copyright 2013 National Bank of Belgium
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
package ec.util.spreadsheet.poi;

import static ec.util.spreadsheet.poi.FastPoiValueFactory.DATE_TYPE;
import static ec.util.spreadsheet.poi.FastPoiValueFactory.INLINE_STRING_TYPE;
import static ec.util.spreadsheet.poi.FastPoiValueFactory.NUMBER_TYPE;
import static ec.util.spreadsheet.poi.FastPoiValueFactory.SHARED_STRING_TYPE;
import static ec.util.spreadsheet.poi.FastPoiValueFactory.STRING_TYPE;
import java.util.Date;
import org.apache.poi.ss.usermodel.DateUtil;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class FastPoiValueFactoryTest {

    private static final String NO_TYPE = null;
    private static final String INVALID_TYPE = "azerty";
    private static final String NO_STYLE = null;
    private static final String OUT_OF_BOUNDS_STYLE = "-1";
    private static final String INVALID_STYLE = "azerty";

    private static final FastPoiContext CONTEXT = new FastPoiContext(
            new String[]{"hello", "world"},
            new boolean[]{false, true},
            true);

    private Date toDate(int year, int month, int day) {
        return new Date(year - 1900, month - 1, day);
    }

    @Test
    public void testGetNull() {
        FastPoiValueFactory f = new FastPoiValueFactory(CONTEXT);

        assertThat(f.getValue("other", NO_TYPE, NO_STYLE)).isNull();
        assertThat(f.getValue("2010-02-01", NO_TYPE, NO_STYLE)).isNull();

        assertThat(f.getValue("1", NO_TYPE, OUT_OF_BOUNDS_STYLE)).isNull();
        assertThat(f.getValue("1", NO_TYPE, INVALID_STYLE)).isNull();
        assertThat(f.getValue("3.14", NO_TYPE, OUT_OF_BOUNDS_STYLE)).isNull();
        assertThat(f.getValue("3.14", NO_TYPE, INVALID_STYLE)).isNull();

        assertThat(f.getValue("1", INVALID_TYPE, NO_STYLE)).isNull();
        assertThat(f.getValue("3.14", INVALID_TYPE, NO_STYLE)).isNull();
        assertThat(f.getValue("other", INVALID_TYPE, NO_STYLE)).isNull();
        assertThat(f.getValue("2010-02-01", INVALID_TYPE, NO_STYLE)).isNull();

        assertThat(f.getValue("1", NUMBER_TYPE, OUT_OF_BOUNDS_STYLE)).isNull();
        assertThat(f.getValue("1", NUMBER_TYPE, INVALID_STYLE)).isNull();

        assertThat(f.getValue("1", DATE_TYPE, NO_STYLE)).isNull();
        assertThat(f.getValue("3.14", DATE_TYPE, NO_STYLE)).isNull();
        assertThat(f.getValue("other", DATE_TYPE, NO_STYLE)).isNull();
    }

    @Test
    public void testGetNumber() {
        FastPoiValueFactory f = new FastPoiValueFactory(CONTEXT);

        assertThat(f.getValue("1", NO_TYPE, NO_STYLE)).isEqualTo(1d);

        assertThat(f.getValue("3.14", NO_TYPE, NO_STYLE)).isEqualTo(3.14);
        assertThat(f.getValue("3.14", NO_TYPE, "0")).isEqualTo(3.14);
        assertThat(f.getValue("3.14", NO_TYPE, "1")).isNotInstanceOf(Number.class);

        assertThat(f.getValue("3.14", NUMBER_TYPE, NO_STYLE)).isEqualTo(3.14);
        assertThat(f.getValue("3.14", NUMBER_TYPE, "0")).isEqualTo(3.14);
        assertThat(f.getValue("3.14", NUMBER_TYPE, "1")).isNotInstanceOf(Number.class);
    }

    @Test
    public void testGetDate() {
        FastPoiValueFactory f = new FastPoiValueFactory(CONTEXT);

        assertThat(f.getValue("1", NO_TYPE, NO_STYLE)).isNotInstanceOf(Date.class);
        assertThat(f.getValue("1", NO_TYPE, "0")).isNotInstanceOf(Date.class);
        assertThat(f.getValue("1", NO_TYPE, "1")).isEqualTo(DateUtil.getJavaDate(1, true));

        assertThat(f.getValue("1", NUMBER_TYPE, NO_STYLE)).isNotInstanceOf(Date.class);
        assertThat(f.getValue("1", NUMBER_TYPE, "0")).isNotInstanceOf(Date.class);
        assertThat(f.getValue("1", NUMBER_TYPE, "1")).isEqualTo(DateUtil.getJavaDate(1, true));

        assertThat(f.getValue("2010-02-01", DATE_TYPE, NO_STYLE)).isEqualTo(toDate(2010, 2, 1));

        assertThat(f.getValue("3.14", NO_TYPE, "1")).isInstanceOf(Date.class).isNotEqualTo(f.getValue("3", NO_TYPE, "1"));
        assertThat(f.getValue("3.99", NO_TYPE, "1")).isInstanceOf(Date.class).isNotEqualTo(f.getValue("4", NO_TYPE, "1"));
    }

    @Test
    public void testGetString() {
        FastPoiValueFactory f = new FastPoiValueFactory(CONTEXT);

        assertThat(f.getValue("0", SHARED_STRING_TYPE, NO_STYLE)).isEqualTo("hello");
        assertThat(f.getValue("1", SHARED_STRING_TYPE, NO_STYLE)).isEqualTo("world");
        assertThat(f.getValue("-1", SHARED_STRING_TYPE, NO_STYLE)).isNull();
        assertThat(f.getValue("other", SHARED_STRING_TYPE, NO_STYLE)).isNull();

        assertThat(f.getValue("0", STRING_TYPE, NO_STYLE)).isEqualTo("0");
        assertThat(f.getValue("other", STRING_TYPE, NO_STYLE)).isEqualTo("other");

        assertThat(f.getValue("0", INLINE_STRING_TYPE, NO_STYLE)).isEqualTo("0");
        assertThat(f.getValue("other", INLINE_STRING_TYPE, NO_STYLE)).isEqualTo("other");
    }
}
