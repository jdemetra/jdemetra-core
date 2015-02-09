/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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

import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.Sheet;
import ec.util.spreadsheet.poi.FastPoiBook.SharedStringsDataSax2EventHandler;
import ec.util.spreadsheet.poi.FastPoiBook.SheetSax2EventHandler;
import ec.util.spreadsheet.poi.FastPoiBook.StylesDataSax2EventHandler;
import ec.util.spreadsheet.poi.FastPoiBook.WorkbookData;
import ec.util.spreadsheet.poi.FastPoiBook.WorkbookDataSax2EventHandler;
import java.io.IOException;
import java.io.InputStream;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class FastPoiBookTest {

    private static final ByteSource TOP5 = asByteSource("/Top5Browsers.xlsx");
    private static final ByteSource WORKBOOK = asByteSource("/workbook.xml");
    private static final ByteSource REGULAR = asByteSource("/RegularXlsxSheet.xml");
    private static final ByteSource FORMULAS = asByteSource("/FormulasXlsxSheet.xml");
    private static final ByteSource SST = asByteSource("/Sst.xml");
    private static final ByteSource STYLES = asByteSource("/styles.xml");

    private static ByteSource asByteSource(String name) {
        return ByteSource.fromURL(FastPoiBookTest.class.getResource(name));
    }

    @Test
    public void testGetSheetCount() throws IOException, OpenXML4JException {
        try (InputStream stream = TOP5.openStream()) {
            try (Book book = FastPoiBook.create(stream)) {
                assertEquals(3, book.getSheetCount());
            }
        }
    }

    @Test
    public void testGetSheet() throws IOException, OpenXML4JException {
        try (InputStream stream = TOP5.openStream()) {
            try (Book book = FastPoiBook.create(stream)) {
                assertNotNull(book.getSheet(0));
                assertNotNull(book.getSheet(1));
                assertNotNull(book.getSheet(2));
            }
        }
    }

    @Test
    public void testWorkbookDataSax2EventHandler() throws IOException {
        WorkbookData data = new WorkbookDataSax2EventHandler().parse(WORKBOOK);
        assertEquals("Top 5 Browsers - Monthly", data.sheets.get(0).name);
        assertEquals("rId1", data.sheets.get(0).relationId);
        assertEquals("Top 5 Browsers - Quarterly", data.sheets.get(1).name);
        assertEquals("rId2", data.sheets.get(1).relationId);
        assertFalse(data.date1904);

        WorkbookData missing = new WorkbookDataSax2EventHandler().parse(ByteSource.noStream());
        assertEquals(0, missing.sheets.size());
        assertFalse(missing.date1904);
    }

    @Test
    public void testSheetSax2EventHandler() throws IOException {
        FastPoiContext context = new FastPoiContext(
                new String[]{"1", "2", "3", "4", "5", "6", "7"},
                new boolean[]{false, true},
                true
        );

        Sheet regular = new SheetSax2EventHandler("regular", context).parse(REGULAR);
        assertEquals("regular", regular.getName());
        assertEquals(7, regular.getColumnCount());
        assertEquals(42, regular.getRowCount());

        Sheet formulas = new SheetSax2EventHandler("formulas", context).parse(FORMULAS);
        assertEquals("formulas", formulas.getName());
        assertEquals(7, formulas.getColumnCount());
        assertEquals(42, formulas.getRowCount());

        Sheet missing = new SheetSax2EventHandler("missing", context).parse(ByteSource.noStream());
        assertEquals("missing", missing.getName());
        assertEquals(0, missing.getColumnCount());
        assertEquals(0, missing.getRowCount());
    }

    @Test
    public void testSharedStringsDataSax2EventHandler() throws IOException {
        String[] sharedStrings = new SharedStringsDataSax2EventHandler().parse(SST);
        assertThat(sharedStrings).containsExactly("Cell A1", "Cell B1", "My Cell", "Cell A2", "Cell B2");

        String[] missing = new SharedStringsDataSax2EventHandler().parse(ByteSource.noStream());
        assertThat(missing).isEmpty();
    }

    @Test
    public void testStylesDataSax2EventHandler() throws IOException {
        boolean[] styles = new StylesDataSax2EventHandler().parse(STYLES);
        assertThat(styles).containsExactly(false, true);

        boolean[] missing = new StylesDataSax2EventHandler().parse(ByteSource.noStream());
        assertThat(missing).isEmpty();
    }
}
