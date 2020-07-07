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
package ec.tss.tsproviders.spreadsheet.engine;

import static ec.tss.tsproviders.spreadsheet.engine.SpreadSheetCollection.AlignType.HORIZONTAL;
import static ec.tss.tsproviders.spreadsheet.engine.SpreadSheetCollection.AlignType.VERTICAL;
import static ec.tss.tsproviders.spreadsheet.engine.SpreadSheetFactory.CellParser.onDateType;
import static ec.tss.tsproviders.spreadsheet.engine.SpreadSheetFactory.CellParser.onNumberType;
import static ec.tss.tsproviders.spreadsheet.engine.SpreadSheetFactory.CellParser.onStringType;
import ec.tss.tsproviders.spreadsheet.engine.SpreadSheetFactory.Context;
import static ec.tss.tsproviders.spreadsheet.engine.TestUtils.date;
import static ec.tss.tsproviders.spreadsheet.engine.TestUtils.sheet;
import static ec.tss.tsproviders.spreadsheet.engine.TestUtils.top5Excel;
import static ec.tss.tsproviders.spreadsheet.engine.TestUtils.top5ExcelClassic;
import static ec.tss.tsproviders.spreadsheet.engine.TestUtils.top5OpenDocument;
import static ec.tss.tsproviders.spreadsheet.engine.TestUtils.top5Xmlss;
import static ec.tss.tsproviders.spreadsheet.engine.Top5BrowsersHelper.testContent;
import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.od.OpenDocumentBookFactory;
import ec.util.spreadsheet.poi.ExcelBookFactory;
import ec.util.spreadsheet.poi.ExcelClassicBookFactory;
import ec.util.spreadsheet.xmlss.XmlssBookFactory;
import java.io.IOException;
import java.net.URL;
import org.junit.Test;
import static ec.tss.tsproviders.spreadsheet.engine.SpreadSheetCollectionAssert.assertThat;
import static ec.tss.tsproviders.spreadsheet.engine.SpreadSheetFactory.DefaultImpl.parseCollection;
import static ec.tss.tsproviders.spreadsheet.engine.TestUtils.data;
import ec.tss.tsproviders.utils.ObsGathering;
import static ec.tstoolkit.timeseries.TsAggregationType.None;
import static ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly;
import static ec.tstoolkit.timeseries.simplets.TsFrequency.Undefined;
import java.util.Date;
import org.junit.Ignore;

/**
 *
 * @author Philippe Charles
 */
public class SpreadSheetFactoryTest {

    private final Date jan2010 = date(2010, 0, 1);
    private final Date feb2010 = date(2010, 1, 1);
    private final Date mar2010 = date(2010, 2, 1);

    @Test
    public void testParseCollectionHorizontal() {
        Context context = new Context(onStringType(), onDateType(), onNumberType(), ObsGathering.excludingMissingValues(Undefined, None));

        Object[][] basic = {
            {null, jan2010, feb2010, mar2010},
            {"S1", 3.14, 4.56, 7.89}
        };

        assertThat(parseCollection(sheet(basic), 0, context))
                .hasAlignType(HORIZONTAL)
                .containsExactly("S1")
                .containsExactly(data(Monthly, 2010, 0, 3.14, 4.56, 7.89));

        Object[][] withDateHeader = {
            {"Date", jan2010, feb2010, mar2010},
            {"S1", 3.14, 4.56, 7.89}
        };

        assertThat(parseCollection(sheet(withDateHeader), 0, context))
                .hasAlignType(HORIZONTAL)
                .containsExactly("S1")
                .containsExactly(data(Monthly, 2010, 0, 3.14, 4.56, 7.89));

        Object[][] withoutHeader = {
            {jan2010, feb2010, mar2010},
            {3.14, 4.56, 7.89}
        };

        assertThat(parseCollection(sheet(withoutHeader), 0, context))
                .hasAlignType(HORIZONTAL)
                .containsExactly("S1")
                .containsExactly(data(Monthly, 2010, 0, 3.14, 4.56, 7.89));

        Object[][] withMultipleHeaders = {
            {null, null, jan2010, feb2010, mar2010},
            {"G1", "S1", 3.14, 4.56, 7.89},
            {null, "S2", 3, 4, 5},
            {"G2", "S1", 7, 8, 9},
            {"S1", null, 0, 1, 2}
        };

        assertThat(parseCollection(sheet(withMultipleHeaders), 0, context))
                .hasAlignType(HORIZONTAL)
                .containsExactly("G1\nS1", "G1\nS2", "G2\nS1", "S1")
                .containsExactly(
                        data(Monthly, 2010, 0, 3.14, 4.56, 7.89),
                        data(Monthly, 2010, 0, 3, 4, 5),
                        data(Monthly, 2010, 0, 7, 8, 9),
                        data(Monthly, 2010, 0, 0, 1, 2));
    }

    @Test
    public void testParseCollectionVertical() {
        Context context = new Context(onStringType(), onDateType(), onNumberType(), ObsGathering.excludingMissingValues(Undefined, None));

        Object[][] basic = {
            {null, "S1"},
            {jan2010, 3.14},
            {feb2010, 4.56},
            {mar2010, 7.89}
        };

        assertThat(parseCollection(sheet(basic), 0, context))
                .hasAlignType(VERTICAL)
                .containsExactly(data(Monthly, 2010, 0, 3.14, 4.56, 7.89));

        Object[][] withDateHeader = {
            {"Date", "S1"},
            {jan2010, 3.14},
            {feb2010, 4.56},
            {mar2010, 7.89}
        };

        assertThat(parseCollection(sheet(withDateHeader), 0, context))
                .hasAlignType(VERTICAL)
                .containsExactly(data(Monthly, 2010, 0, 3.14, 4.56, 7.89));

        Object[][] withoutHeader = {
            {jan2010, 3.14},
            {feb2010, 4.56},
            {mar2010, 7.89}
        };

        assertThat(parseCollection(sheet(withoutHeader), 0, context))
                .hasAlignType(VERTICAL)
                .containsExactly("S1")
                .containsExactly(data(Monthly, 2010, 0, 3.14, 4.56, 7.89));

        Object[][] withMultipleHeaders = {
            {null, "G1", null, "G2", "S1"},
            {null, "S1", "S2", "S1", null},
            {jan2010, 3.14, 3, 7, 0},
            {feb2010, 4.56, 4, 8, 1},
            {mar2010, 7.89, 5, 9, 2}
        };

        assertThat(parseCollection(sheet(withMultipleHeaders), 0, context))
                .hasAlignType(VERTICAL)
                .containsExactly("G1\nS1", "G1\nS2", "G2\nS1", "S1")
                .containsExactly(
                        data(Monthly, 2010, 0, 3.14, 4.56, 7.89),
                        data(Monthly, 2010, 0, 3, 4, 5),
                        data(Monthly, 2010, 0, 7, 8, 9),
                        data(Monthly, 2010, 0, 0, 1, 2));
    }

    private static void testFactory(Book.Factory bookFactory, URL url) throws IOException {
        try (Book book = bookFactory.load(url)) {
            testContent(SpreadSheetFactory.getDefault().toSource(book, TsImportOptions.getDefault()));
        }
    }

    @Test
    public void testExcel() throws IOException {
        ExcelBookFactory factory = new ExcelBookFactory();
        factory.setFast(false);
        testFactory(factory, top5Excel());

        testFactory(new ExcelBookFactory(), top5Excel());
    }

    @Test
    public void testExcelClassic() throws IOException {
        testFactory(new ExcelClassicBookFactory(), top5ExcelClassic());
    }

    @Test
    public void testOpenDocument() throws IOException {
        testFactory(new OpenDocumentBookFactory(), top5OpenDocument());
    }

    @Test
    public void testXmlss() throws IOException {
        testFactory(new XmlssBookFactory(), top5Xmlss());
    }
}
