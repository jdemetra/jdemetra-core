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
import ec.util.spreadsheet.poi.FastPoiBook.SharedStringsDataSax2EventHandler;
import ec.util.spreadsheet.poi.FastPoiBook.SheetSax2EventHandler;
import ec.util.spreadsheet.poi.FastPoiBook.StylesDataSax2EventHandler;
import ec.util.spreadsheet.poi.FastPoiBook.WorkbookData;
import ec.util.spreadsheet.poi.FastPoiBook.WorkbookDataSax2EventHandler;
import static ec.util.spreadsheet.Assertions.assertThat;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @author Philippe Charles
 */
public class FastPoiBookTest {

    private static final Callable<InputStream> TOP5 = asByteSource("/Top5Browsers.xlsx");
    private static final Callable<InputStream> WORKBOOK = asByteSource("/workbook.xml");
    private static final Callable<InputStream> REGULAR = asByteSource("/RegularXlsxSheet.xml");
    private static final Callable<InputStream> FORMULAS = asByteSource("/FormulasXlsxSheet.xml");
    private static final Callable<InputStream> SST = asByteSource("/Sst.xml");
    private static final Callable<InputStream> STYLES = asByteSource("/styles.xml");
    private static final Callable<InputStream> NO_STREAM = () -> null;

    private static Callable<InputStream> asByteSource(String name) {
        return FastPoiBookTest.class.getResource(name)::openStream;
    }

    private static XMLReader XML_READER;

    @BeforeClass
    public static void beforeClass() throws SAXException {
        XML_READER = XMLReaderFactory.createXMLReader();
    }

    @Test
    public void testGetSheetCount() throws Exception {
        try (InputStream stream = TOP5.call()) {
            try (Book book = FastPoiBook.create(XML_READER, stream)) {
                assertEquals(3, book.getSheetCount());
            }
        }
    }

    @Test
    public void testGetSheet() throws Exception {
        try (InputStream stream = TOP5.call()) {
            try (Book book = FastPoiBook.create(XML_READER, stream)) {
                book.forEach((sheet, index) -> assertNotNull(sheet));
            }
        }
    }

    @Test
    public void testWorkbookDataSax2EventHandler() throws IOException {
        WorkbookData data = new WorkbookDataSax2EventHandler().parse(XML_READER, WORKBOOK);
        assertThat(data.sheets)
                .extracting("name", "relationId")
                .containsExactly(
                        tuple("Top 5 Browsers - Monthly", "rId1"),
                        tuple("Top 5 Browsers - Quarterly", "rId2"));
        assertFalse(data.date1904);

        WorkbookData missing = new WorkbookDataSax2EventHandler().parse(XML_READER, NO_STREAM);
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

        assertThat(new SheetSax2EventHandler("regular", context).parse(XML_READER, REGULAR))
                .hasName("regular")
                .hasColumnCount(7)
                .hasRowCount(42);

        assertThat(new SheetSax2EventHandler("formulas", context).parse(XML_READER, FORMULAS))
                .hasName("formulas")
                .hasColumnCount(7)
                .hasRowCount(42);

        assertThat(new SheetSax2EventHandler("missing", context).parse(XML_READER, NO_STREAM))
                .hasName("missing")
                .hasColumnCount(0)
                .hasRowCount(0);
    }

    @Test
    public void testSharedStringsDataSax2EventHandler() throws IOException {
        String[] sharedStrings = new SharedStringsDataSax2EventHandler().parse(XML_READER, SST);
        assertThat(sharedStrings).containsExactly("Cell A1", "Cell B1", "My Cell", "Cell A2", "Cell B2");

        String[] missing = new SharedStringsDataSax2EventHandler().parse(XML_READER, NO_STREAM);
        assertThat(missing).isEmpty();
    }

    @Test
    public void testStylesDataSax2EventHandler() throws IOException {
        boolean[] styles = new StylesDataSax2EventHandler().parse(XML_READER, STYLES);
        assertThat(styles).containsExactly(false, true);

        boolean[] missing = new StylesDataSax2EventHandler().parse(XML_READER, NO_STREAM);
        assertThat(missing).isEmpty();
    }
}
