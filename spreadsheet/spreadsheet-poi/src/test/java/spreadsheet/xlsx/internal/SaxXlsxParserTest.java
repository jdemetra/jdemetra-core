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
import java.util.Arrays;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.atIndex;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.assertFalse;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import spreadsheet.xlsx.XlsxNumberingFormat;
import spreadsheet.xlsx.XlsxSheetBuilder;
import spreadsheet.xlsx.internal.util.IOUtil;

/**
 *
 * @author Philippe Charles
 */
public class SaxXlsxParserTest {

    private XMLReader reader;
    private IOUtil.ByteResource files;
    private IOUtil.ByteSource empty;
    private IOUtil.ByteSource throwing;

    @Before
    public void before() throws SAXException {
        reader = XMLReaderFactory.createXMLReader();
        files = IOUtil.ByteResource.of(SaxXlsxParserTest.class);
        empty = IOUtil.ByteSource.empty();
        throwing = IOUtil.ByteSource.throwing(CustomIOException::new);
    }

    @Test
    public void testWorkbookSax2EventHandler() throws IOException {
        SaxXlsxParser parser = new SaxXlsxParser(reader);

        XlsxBook.WorkbookData data = XlsxBook.parseWorkbook(files.asSource("/workbook.xml"), parser);
        assertThat(data.getSheets())
                .extracting("name", "relationId")
                .containsExactly(
                        tuple("Top 5 Browsers - Monthly", "rId1"),
                        tuple("Top 5 Browsers - Quarterly", "rId2"));
        assertFalse(data.isDate1904());

        assertThatThrownBy(() -> XlsxBook.parseWorkbook(empty, parser))
                .isInstanceOf(IOException.class)
                .hasCauseInstanceOf(SAXException.class)
                .hasMessageContaining("workbook");

        assertThatThrownBy(() -> XlsxBook.parseWorkbook(throwing, parser))
                .isInstanceOf(CustomIOException.class);
    }

    @Test
    public void testSheetSax2EventHandler() throws IOException {
        SaxXlsxParser parser = new SaxXlsxParser(reader);

        XlsxSheetBuilder b = XlsxSheetBuilder.Factory.getDefault()
                .create(XlsxDateSystems.X1904,
                        Arrays.asList("1", "2", "3", "4", "5", "6", "7")::get,
                        Arrays.asList(false, true)::get);

        SheetAssert.assertThat(XlsxBook.parseSheet("regular", b, files.asSource("/RegularXlsxSheet.xml"), parser))
                .hasName("regular")
                .hasColumnCount(7)
                .hasRowCount(42);

        SheetAssert.assertThat(XlsxBook.parseSheet("formulas", b, files.asSource("/FormulasXlsxSheet.xml"), parser))
                .hasName("formulas")
                .hasColumnCount(7)
                .hasRowCount(42);

        assertThatThrownBy(() -> XlsxBook.parseSheet("missing", b, empty, parser))
                .isInstanceOf(IOException.class)
                .hasCauseInstanceOf(SAXException.class)
                .hasMessageContaining("sheet");

        assertThatThrownBy(() -> XlsxBook.parseSheet("missing", b, throwing, parser))
                .isInstanceOf(CustomIOException.class);
    }

    @Test
    public void testSharedStringsSax2EventHandler() throws IOException {
        SaxXlsxParser parser = new SaxXlsxParser(reader);

        assertThat(XlsxBook.parseSharedStrings(files.asSource("/Sst.xml"), parser))
                .contains("Cell A1", atIndex(0))
                .contains("Cell B2", atIndex(4));

        assertThatThrownBy(() -> XlsxBook.parseSharedStrings(empty, parser))
                .isInstanceOf(IOException.class)
                .hasCauseInstanceOf(SAXException.class)
                .hasMessageContaining("shared strings");

        assertThatThrownBy(() -> XlsxBook.parseSharedStrings(throwing, parser))
                .isInstanceOf(CustomIOException.class);
    }

    @Test
    public void testStylesSax2EventHandler() throws IOException {
        SaxXlsxParser parser = new SaxXlsxParser(reader);

        XlsxNumberingFormat df = XlsxNumberingFormat.getDefault();

        assertThat(XlsxBook.parseStyles(df, files.asSource("/styles.xml"), parser))
                .containsExactly(false, true);

        assertThatThrownBy(() -> XlsxBook.parseStyles(df, empty, parser))
                .isInstanceOf(IOException.class)
                .hasCauseInstanceOf(SAXException.class)
                .hasMessageContaining("styles");

        assertThatThrownBy(() -> XlsxBook.parseStyles(df, throwing, parser))
                .isInstanceOf(CustomIOException.class);
    }
}
