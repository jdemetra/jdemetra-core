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
package ec.util.spreadsheet.helpers;

import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.Sheet;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class ArrayBookBuilderTest {

    final Sheet emptySheet = ArraySheet.builder().name("test").build();

    @Test
    public void testEmptyBuilder() {
        Book book = ArrayBook.builder().build();
        Assert.assertEquals(0, book.getSheetCount());
    }

    @Test
    public void testEmptySheet() throws IOException {
        Book book = ArrayBook.builder().sheet(emptySheet).build();
        Assert.assertEquals(1, book.getSheetCount());
        Sheet sheet = book.getSheet(0);
        Assert.assertEquals("test", sheet.getName());
        Assert.assertEquals(0, sheet.getRowCount());
        Assert.assertEquals(0, sheet.getColumnCount());
    }

    @Test
    public void testClear() throws IOException {
        Book book = ArrayBook.builder().sheet(emptySheet).clear().build();
        Assert.assertEquals(0, book.getSheetCount());
    }
}
