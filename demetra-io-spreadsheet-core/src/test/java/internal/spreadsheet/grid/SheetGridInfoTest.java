/*
 * Copyright 2017 National Bank of Belgium
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
package internal.spreadsheet.grid;

import ec.util.spreadsheet.html.HtmlBookFactory;
import ec.util.spreadsheet.poi.ExcelBookFactory;
import java.time.LocalDateTime;
import java.util.Date;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class SheetGridInfoTest {

    @Test
    public void test() {
        SheetGridInfo excel = SheetGridInfo.of(new ExcelBookFactory());
        assertThat(excel.isSupportedDataType(Date.class)).isTrue();
        assertThat(excel.isSupportedDataType(Number.class)).isTrue();
        assertThat(excel.isSupportedDataType(String.class)).isTrue();
        assertThat(excel.isSupportedDataType(LocalDateTime.class)).isTrue();
        assertThat(excel.isSupportedDataType(Object.class)).isFalse();

        SheetGridInfo html = SheetGridInfo.of(new HtmlBookFactory());
        assertThat(html.isSupportedDataType(Date.class)).isFalse();
        assertThat(html.isSupportedDataType(Number.class)).isFalse();
        assertThat(html.isSupportedDataType(String.class)).isTrue();
        assertThat(html.isSupportedDataType(LocalDateTime.class)).isFalse();
        assertThat(html.isSupportedDataType(Object.class)).isFalse();
    }
}
