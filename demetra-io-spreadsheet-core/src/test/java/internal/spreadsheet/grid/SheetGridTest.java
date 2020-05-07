/*
 * Copyright 2018 National Bank of Belgium
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

import demetra.timeseries.TsCollection;
import java.io.File;
import java.io.IOException;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;
import _test.DataForTest;
import demetra.tsprovider.grid.GridReader;
import ec.util.spreadsheet.html.HtmlBookFactory;
import ec.util.spreadsheet.poi.ExcelBookFactory;
import java.time.LocalDateTime;
import java.util.Date;

/**
 *
 * @author Philippe Charles
 */
public class SheetGridTest {

    @Test
    public void test() throws IOException {
        SheetGrid grid = SheetGrid.of(new File(""), DataForTest.FACTORY, GridReader.DEFAULT);

        assertThat(grid.getSheetByName("s1")).map(TsCollection::getName).contains("s1");
        assertThat(grid.getSheetByName("s2")).map(TsCollection::getName).contains("s2");
        assertThat(grid.getSheetByName("other")).isEmpty();
        assertThat(grid.getSheetNames()).containsExactly("s1", "s2");
        assertThat(grid.getSheets()).extracting(o -> o.getName()).containsExactly("s1", "s2");
    }

    @Test
    public void testDataTypes() {
        SheetGrid excel = SheetGrid.of(new File(""), new ExcelBookFactory(), GridReader.DEFAULT);
        assertThat(excel.isSupportedDataType(Date.class)).isTrue();
        assertThat(excel.isSupportedDataType(Number.class)).isTrue();
        assertThat(excel.isSupportedDataType(String.class)).isTrue();
        assertThat(excel.isSupportedDataType(LocalDateTime.class)).isTrue();
        assertThat(excel.isSupportedDataType(Object.class)).isFalse();

        SheetGrid html = SheetGrid.of(new File(""), new HtmlBookFactory(), GridReader.DEFAULT);
        assertThat(html.isSupportedDataType(Date.class)).isFalse();
        assertThat(html.isSupportedDataType(Number.class)).isFalse();
        assertThat(html.isSupportedDataType(String.class)).isTrue();
        assertThat(html.isSupportedDataType(LocalDateTime.class)).isFalse();
        assertThat(html.isSupportedDataType(Object.class)).isFalse();
    }
}
