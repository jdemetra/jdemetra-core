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
package ec.util.spreadsheet.poi;

import ec.util.spreadsheet.Book;
import static ec.util.spreadsheet.SheetAssert.assertThat;
import java.io.InputStream;
import java.util.concurrent.Callable;
import org.assertj.core.util.DateUtil;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class PoiBookTest {

    private static final Callable<InputStream> XLSX = asByteSource("/Top5Browsers.xlsx");
    private static final Callable<InputStream> XLS = asByteSource("/Top5Browsers.xls");

    private static Callable<InputStream> asByteSource(String name) {
        return PoiBookTest.class.getResource(name)::openStream;
    }

    @Test
    @SuppressWarnings("null")
    public void testCellTypes() throws Exception {
        try (InputStream stream = XLSX.call()) {
            try (Book book = PoiBook.create(stream)) {
                assertThat(book.getSheet(2))
                        .hasCellValue(0, 0, null)
                        .hasCellValue(1, 0, DateUtil.parse("2008-07-01"))
                        .hasCellValue(1, 2, 26.14)
                        .hasCellValue(0, 1, "IE");
            }
        }
        try (InputStream stream = XLS.call()) {
            try (Book book = PoiBook.createClassic(stream)) {
                assertThat(book.getSheet(2))
                        .hasCellValue(0, 0, null)
                        .hasCellValue(1, 0, DateUtil.parse("2008-07-01"))
                        .hasCellValue(1, 2, 26.14)
                        .hasCellValue(0, 1, "IE");
            }
        }
    }
}
