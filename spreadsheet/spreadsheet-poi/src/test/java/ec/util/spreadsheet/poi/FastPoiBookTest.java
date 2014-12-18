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
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class FastPoiBookTest {

    static final URL TOP5_URL = FastPoiBookTest.class.getResource("/Top5Browsers.xlsx");
    static final URL WORKBOOK_URL = FastPoiBookTest.class.getResource("/workbook.xml");
    static final URL REGULAR_URL = FastPoiBookTest.class.getResource("/RegularXlsxSheet.xml");
    static final URL FORMULAS_URL = FastPoiBookTest.class.getResource("/FormulasXlsxSheet.xml");
    static final URL SST_URL = FastPoiBookTest.class.getResource("/Sst.xml");
    static final URL STYLES_URL = FastPoiBookTest.class.getResource("/styles.xml");

    private static FastPoiBook.ByteSource asByteSource(final URL url) {
        return new FastPoiBook.ByteSource() {
            @Override
            public InputStream openStream() throws IOException {
                return url.openStream();
            }
        };
    }

    @Test
    public void testGetSheetCount() throws IOException, OpenXML4JException {
        try (InputStream stream = TOP5_URL.openStream()) {
            try (Book book = FastPoiBook.create(stream)) {
                assertEquals(3, book.getSheetCount());
            }
        }
    }

    @Test
    public void testGetSheet() throws IOException, OpenXML4JException {
        try (InputStream stream = TOP5_URL.openStream()) {
            try (Book book = FastPoiBook.create(stream)) {
                assertNotNull(book.getSheet(0));
                assertNotNull(book.getSheet(1));
                assertNotNull(book.getSheet(2));
            }
        }
    }

    @Test
    public void testWorkbookDataSax2EventHandler() throws IOException {
        WorkbookData data = new WorkbookDataSax2EventHandler().parse(asByteSource(WORKBOOK_URL));
        assertEquals("Top 5 Browsers - Monthly", data.sheets.get(0).name);
        assertEquals("rId1", data.sheets.get(0).relationId);
        assertEquals("Top 5 Browsers - Quarterly", data.sheets.get(1).name);
        assertEquals("rId2", data.sheets.get(1).relationId);
        assertFalse(data.date1904);
    }

    @Test
    public void testSheetSax2EventHandler() throws IOException {
        FastPoiBook.SheetContext context = new FastPoiBook.SheetContext(
                Arrays.asList("1", "2", "3", "4", "5", "6", "7"),
                Arrays.asList(new FastPoiBook.Style(0, null), new FastPoiBook.Style(14, null)),
                true
        );
        {
            Sheet regular = new SheetSax2EventHandler("regular", context).parse(asByteSource(REGULAR_URL));
            assertEquals("regular", regular.getName());
            assertEquals(7, regular.getColumnCount());
            assertEquals(42, regular.getRowCount());
        }
        {
            Sheet formulas = new SheetSax2EventHandler("formulas", context).parse(asByteSource(FORMULAS_URL));
            assertEquals("formulas", formulas.getName());
            assertEquals(7, formulas.getColumnCount());
            assertEquals(42, formulas.getRowCount());
        }
    }

    @Test
    public void testSharedStringsDataSax2EventHandler() throws IOException {
        List<String> sharedStrings = new SharedStringsDataSax2EventHandler().parse(asByteSource(SST_URL));
        assertArrayEquals(new String[]{"Cell A1", "Cell B1", "My Cell", "Cell A2", "Cell B2"}, sharedStrings.toArray());
    }

    @Test
    public void test() throws IOException {
        List<FastPoiBook.Style> styles = new StylesDataSax2EventHandler().parse(asByteSource(STYLES_URL));
        assertEquals(2, styles.size());
        assertEquals(0, styles.get(0).formatId);
        assertNull(styles.get(0).formatString);
        assertEquals(14, styles.get(1).formatId);
        assertNull(styles.get(1).formatString);
    }
}
