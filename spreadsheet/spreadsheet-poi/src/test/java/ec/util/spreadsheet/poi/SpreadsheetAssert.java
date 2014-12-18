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
import ec.util.spreadsheet.Cell;
import ec.util.spreadsheet.Sheet;
import ec.util.spreadsheet.helpers.CellRefHelper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import org.junit.Assert;

/**
 *
 * @author Philippe Charles
 */
public class SpreadsheetAssert {

    public static void assertContentEquals(Book l, Book r) throws IOException {
        Assert.assertEquals(l.getSheetCount(), r.getSheetCount());
        for (int s = 0; s < l.getSheetCount(); s++) {
            assertContentEquals(l.getSheet(s), r.getSheet(s));
        }
    }

    public static void assertContentEquals(Sheet l, Sheet r) throws IOException {
        Assert.assertEquals(l.getName(), r.getName());
//        Assert.assertEquals(l.getRowCount(), r.getRowCount());
//        Assert.assertEquals(l.getColumnCount(), r.getColumnCount());
        for (int i = 0; i < l.getRowCount(); i++) {
            for (int j = 0; j < l.getColumnCount(); j++) {
                Cell cell = l.getCell(i, j);
                if (cell != null) {
                    assertContentEquals(CellRefHelper.getCellRef(i, j), cell, r.getCell(i, j));
                }
            }
        }
    }

    public static void assertContentEquals(String ref, Cell l, Cell r) throws IOException {
        if (l == null) {
            Assert.assertNull("Cell '" + ref + "' should be null", r);
        } else if (l.isDate()) {
            Assert.assertTrue("Cell '" + ref + "' should be a date", r.isDate());
            Assert.assertEquals(l.getDate(), r.getDate());
        } else if (l.isNumber()) {
            Assert.assertTrue("Cell '" + ref + "' should be a number", r.isNumber());
            Assert.assertEquals(l.getNumber(), r.getNumber());
        } else if (l.isString()) {
            Assert.assertTrue("Cell '" + ref + "' should be a string", r.isString());
            Assert.assertEquals(l.getString(), r.getString());
        }
    }

    public static void assertLoadStore(Book.Factory factory, URL sample) throws IOException {
        try (Book original = factory.load(sample)) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            factory.store(outputStream, original);
            byte[] data = outputStream.toByteArray();

            try (Book result = factory.load(new ByteArrayInputStream(data))) {
                SpreadsheetAssert.assertContentEquals(original, result);
            }
        }
    }
}
