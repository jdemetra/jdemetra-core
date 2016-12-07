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

import ec.util.spreadsheet.SheetAssert;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import spreadsheet.xlsx.XlsxNumberingFormat;
import spreadsheet.xlsx.XlsxSheetBuilder;
import spreadsheet.xlsx.internal.SaxXlsxParser.SharedStringsDataSaxEventHandler;
import spreadsheet.xlsx.internal.SaxXlsxParser.SheetSaxEventHandler;
import spreadsheet.xlsx.internal.SaxXlsxParser.StylesDataSaxEventHandler;
import spreadsheet.xlsx.internal.SaxXlsxParser.WorkbookDataSaxEventHandler;

/**
 *
 * @author Philippe Charles
 */
public class SaxXlsxParserTest {

    private static final SaxUtil.ByteSource WORKBOOK = asByteSource("/workbook.xml");
    private static final SaxUtil.ByteSource REGULAR = asByteSource("/RegularXlsxSheet.xml");
    private static final SaxUtil.ByteSource FORMULAS = asByteSource("/FormulasXlsxSheet.xml");
    private static final SaxUtil.ByteSource SST = asByteSource("/Sst.xml");
    private static final SaxUtil.ByteSource STYLES = asByteSource("/styles.xml");
    private static final SaxUtil.ByteSource NO_STREAM = () -> null;

    private static SaxUtil.ByteSource asByteSource(String name) {
        return () -> SaxXlsxParserTest.class.getResource(name).openStream();
    }

    private static XMLReader XML_READER;

    @BeforeClass
    public static void beforeClass() throws SAXException {
        XML_READER = XMLReaderFactory.createXMLReader();
    }

    @Test
    public void testWorkbookDataSax2EventHandler() throws IOException {
        XlsxBook.WorkbookDataVisitorImpl visitor = new XlsxBook.WorkbookDataVisitorImpl();
        new WorkbookDataSaxEventHandler(visitor).parse(XML_READER, WORKBOOK);
        assertThat(visitor.build().sheets)
                .extracting("name", "relationId")
                .containsExactly(
                        tuple("Top 5 Browsers - Monthly", "rId1"),
                        tuple("Top 5 Browsers - Quarterly", "rId2"));
        assertFalse(visitor.build().date1904);

        visitor.build().sheets.clear();
        new WorkbookDataSaxEventHandler(visitor).parse(XML_READER, NO_STREAM);
        assertEquals(0, visitor.build().sheets.size());
        assertFalse(visitor.build().date1904);
    }

    @Test
    public void testSheetSax2EventHandler() throws IOException {
        XlsxSheetBuilder b = XlsxSheetBuilder.Factory.getDefault()
                .create(XlsxDateSystems.X1904,
                        Arrays.asList("1", "2", "3", "4", "5", "6", "7")::get,
                        Arrays.asList(false, true)::get);

        XlsxBook.SheetVisitorImpl regular = new XlsxBook.SheetVisitorImpl("regular", b);
        new SheetSaxEventHandler(regular).parse(XML_READER, REGULAR);
        SheetAssert.assertThat(regular.build())
                .hasName("regular")
                .hasColumnCount(7)
                .hasRowCount(42);

        XlsxBook.SheetVisitorImpl formulas = new XlsxBook.SheetVisitorImpl("formulas", b);
        new SheetSaxEventHandler(formulas).parse(XML_READER, FORMULAS);
        SheetAssert.assertThat(formulas.build())
                .hasName("formulas")
                .hasColumnCount(7)
                .hasRowCount(42);

        XlsxBook.SheetVisitorImpl missing = new XlsxBook.SheetVisitorImpl("missing", b);
        new SheetSaxEventHandler(missing).parse(XML_READER, NO_STREAM);
        SheetAssert.assertThat(missing.build())
                .hasName("missing")
                .hasColumnCount(0)
                .hasRowCount(0);
    }

    @Test
    public void testSharedStringsDataSax2EventHandler() throws IOException {
        List<String> sharedStrings = new ArrayList<>();
        new SharedStringsDataSaxEventHandler(sharedStrings::add).parse(XML_READER, SST);
        assertThat(sharedStrings).containsExactly("Cell A1", "Cell B1", "My Cell", "Cell A2", "Cell B2");

        List<String> missing = new ArrayList<>();
        new SharedStringsDataSaxEventHandler(missing::add).parse(XML_READER, NO_STREAM);
        assertThat(missing).isEmpty();
    }

    @Test
    public void testStylesDataSax2EventHandler() throws IOException {
        XlsxNumberingFormat dateFormat = XlsxNumberingFormat.getDefault();

        XlsxBook.StylesDataVisitorImpl styles = new XlsxBook.StylesDataVisitorImpl(dateFormat);
        new StylesDataSaxEventHandler(styles).parse(XML_READER, STYLES);
        assertThat(styles.build().test(0)).isFalse();
        assertThat(styles.build().test(1)).isTrue();

        XlsxBook.StylesDataVisitorImpl missing = new XlsxBook.StylesDataVisitorImpl(dateFormat);
        new StylesDataSaxEventHandler(missing).parse(XML_READER, NO_STREAM);
        assertThatThrownBy(() -> missing.build().test(0)).isInstanceOf(IndexOutOfBoundsException.class);
    }
}
