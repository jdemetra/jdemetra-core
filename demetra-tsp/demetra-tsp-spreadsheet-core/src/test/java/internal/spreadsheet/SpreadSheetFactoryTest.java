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
package internal.spreadsheet;

import demetra.tsprovider.grid.GridReader;
import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.od.OpenDocumentBookFactory;
import ec.util.spreadsheet.poi.ExcelBookFactory;
import ec.util.spreadsheet.poi.ExcelClassicBookFactory;
import ec.util.spreadsheet.xmlss.XmlssBookFactory;
import static internal.spreadsheet.Top5Browsers.testContent;
import static internal.spreadsheet.Top5Browsers.top5Excel;
import static internal.spreadsheet.Top5Browsers.top5ExcelClassic;
import static internal.spreadsheet.Top5Browsers.top5OpenDocument;
import static internal.spreadsheet.Top5Browsers.top5Xmlss;
import internal.spreadsheet.grid.SheetGrid;
import java.io.File;
import java.io.IOException;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class SpreadSheetFactoryTest {

    private static void testFactory(Book.Factory bookFactory, File file) throws IOException {
        testContent(SheetGrid.of(file, bookFactory, GridReader.DEFAULT));
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
