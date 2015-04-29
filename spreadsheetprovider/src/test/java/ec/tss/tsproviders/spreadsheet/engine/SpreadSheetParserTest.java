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

import ec.tss.tsproviders.spreadsheet.engine.SpreadSheetCollection.AlignType;
import static ec.tss.tsproviders.spreadsheet.engine.SpreadSheetCollection.AlignType.HORIZONTAL;
import static ec.tss.tsproviders.spreadsheet.engine.SpreadSheetCollection.AlignType.VERTICAL;
import static ec.tss.tsproviders.spreadsheet.engine.SpreadSheetParser.CellParser.onDateType;
import static ec.tss.tsproviders.spreadsheet.engine.SpreadSheetParser.CellParser.onNumberType;
import static ec.tss.tsproviders.spreadsheet.engine.SpreadSheetParser.CellParser.onStringType;
import ec.tss.tsproviders.spreadsheet.engine.SpreadSheetParser.Context;
import static ec.tss.tsproviders.spreadsheet.engine.SpreadSheetParser.parseCollection;
import static ec.tss.tsproviders.spreadsheet.engine.TestUtils.date;
import static ec.tss.tsproviders.spreadsheet.engine.TestUtils.sheet;
import static ec.tss.tsproviders.spreadsheet.engine.TestUtils.top5Excel;
import static ec.tss.tsproviders.spreadsheet.engine.TestUtils.top5ExcelClassic;
import static ec.tss.tsproviders.spreadsheet.engine.TestUtils.top5OpenDocument;
import static ec.tss.tsproviders.spreadsheet.engine.TestUtils.top5Xmlss;
import static ec.tss.tsproviders.spreadsheet.engine.Top5BrowsersHelper.testContent;
import ec.tss.tsproviders.utils.DataFormat;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.od.OpenDocumentBookFactory;
import ec.util.spreadsheet.poi.ExcelBookFactory;
import ec.util.spreadsheet.poi.ExcelClassicBookFactory;
import ec.util.spreadsheet.xmlss.XmlssBookFactory;
import java.io.IOException;
import java.net.URL;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import static ec.tss.tsproviders.spreadsheet.engine.SpreadSheetCollectionAssert.assertThat;
import static ec.tss.tsproviders.spreadsheet.engine.TestUtils.data;
import static ec.tstoolkit.timeseries.TsAggregationType.None;
import static ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly;
import static ec.tstoolkit.timeseries.simplets.TsFrequency.Undefined;

/**
 *
 * @author Philippe Charles
 */
public class SpreadSheetParserTest {

    private static AlignType parseAlignType(Object[][] table) {
        return SpreadSheetParser.parseAlignType(sheet(table), onStringType(), onDateType());
    }

    @Test
    public void testParseAlignType() {
        Object[][] horizontal = {
            {null, date(2010, 1, 1)},
            {"title", 3.14}
        };
        assertThat(parseAlignType(horizontal)).isEqualTo(HORIZONTAL);

        Object[][] vertical = {
            {null, "title"},
            {date(2010, 1, 1), 3.14}
        };
        assertThat(parseAlignType(vertical)).isEqualTo(VERTICAL);
    }

    @Test
    public void testParseCollection() {
        Context context = new Context(onStringType(), onDateType(), onNumberType(), Undefined, None, true);

        Object[][] vertical = {
            {null, "title"},
            {date(2010, 0, 1), 3.14},
            {date(2010, 1, 1), 4.56},
            {date(2010, 2, 1), 7.89}
        };

        assertThat(parseCollection(sheet(vertical), 0, context))
                .hasAlignType(VERTICAL)
                .containsExactly(data(Monthly, 2010, 0, 3.14, 4.56, 7.89));
    }

    private static void testFactory(Book.Factory bookFactory, URL url) throws IOException {
        try (Book book = bookFactory.load(url)) {
            DataFormat df = DataFormat.DEFAULT;
            testContent(SpreadSheetParser.getDefault().parse(book, df.dateParser(), df.numberParser(), TsFrequency.Undefined, TsAggregationType.None, true));
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
