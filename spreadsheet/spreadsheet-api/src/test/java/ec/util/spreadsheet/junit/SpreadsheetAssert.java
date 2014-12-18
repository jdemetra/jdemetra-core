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
package ec.util.spreadsheet.junit;

import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.Cell;
import ec.util.spreadsheet.Sheet;
import ec.util.spreadsheet.helpers.CellRefHelper;
import static ec.util.spreadsheet.junit.SheetMatcher.sameDimensionAs;
import static ec.util.spreadsheet.junit.SheetMatcher.sameNameAs;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import static org.hamcrest.CoreMatchers.allOf;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Philippe Charles
 */
public final class SpreadsheetAssert {

    private SpreadsheetAssert() {
        // static class
    }

    public static void assertCellValueEquals(Sheet expected, Sheet actual) {
        for (int i = 0; i < expected.getRowCount(); i++) {
            for (int j = 0; j < expected.getColumnCount(); j++) {
                assertEquals(CellRefHelper.getCellRef(i, j), expected.getCellValue(i, j), actual.getCellValue(i, j));
            }
        }
    }

    public static void assertSheetEquals(Sheet expected, Sheet actual) {
        assertThat(actual, allOf(sameNameAs(expected), sameDimensionAs(expected)));
        assertCellValueEquals(expected, actual);
    }

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
        assertCellValueEquals(l, r);
    }

    public static void assertContentEquals(String ref, Cell l, Cell r) {
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
