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
package ec.util.spreadsheet.helpers;

import ec.util.spreadsheet.Cell;
import ec.util.spreadsheet.Sheet;
import static ec.util.spreadsheet.junit.CellMatcher.valueEqualTo;
import static ec.util.spreadsheet.junit.SheetMatcher.nameEqualTo;
import static ec.util.spreadsheet.junit.SheetMatcher.sameDimensionAs;
import static ec.util.spreadsheet.junit.SpreadsheetAssert.assertCellValueEquals;
import static ec.util.spreadsheet.junit.SpreadsheetAssert.assertSheetEquals;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class ArraySheetTest {

    final String STRING = "a";
    final Date DATE = new Date(1234);
    final Number NUMBER = 12.34;

    Serializable[] sample() {
        return new Serializable[]{STRING, DATE, NUMBER, null};
    }

    Serializable[] empty() {
        return new Serializable[]{};
    }

    @Test
    public void testGetRowCount() {
        assertEquals(0, new ArraySheet("", 0, 0, empty(), false).getRowCount());
        assertEquals(2, new ArraySheet("", 2, 4, empty(), false).getRowCount());
    }

    @Test
    public void testGetColumnCount() {
        assertEquals(0, new ArraySheet("", 0, 0, empty(), false).getColumnCount());
        assertEquals(4, new ArraySheet("", 2, 4, empty(), false).getColumnCount());
    }

    @Test
    public void testGetName() {
        assertThat(new ArraySheet("", 0, 0, empty(), false), nameEqualTo(""));
        assertThat(new ArraySheet("hello", 2, 4, empty(), false), nameEqualTo("hello"));
    }

    @Test
    public void testGetCell() {
        ArraySheet sheet = new ArraySheet("hello", 2, 2, sample(), false);
        assertThat(sheet.getCell(0, 0), valueEqualTo(STRING));
        assertThat(sheet.getCell(0, 1), valueEqualTo(DATE));
        assertThat(sheet.getCell(1, 0), valueEqualTo(NUMBER));
        assertNull(sheet.getCell(1, 1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetCellIndexOutOfBoundsException() {
        new ArraySheet("hello", 2, 2, sample(), false).getCell(2, 2);
    }

    @Test
    public void testGetCellValue() {
        ArraySheet sheet = new ArraySheet("hello", 2, 2, sample(), false);
        assertEquals(STRING, sheet.getCellValue(0, 0));
        assertEquals(DATE, sheet.getCellValue(0, 1));
        assertEquals(NUMBER, sheet.getCellValue(1, 0));
        assertNull(sheet.getCellValue(1, 1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetCellValueIndexOutOfBoundsException() {
        ArraySheet sheet = new ArraySheet("hello", 2, 2, sample(), false);
        sheet.getCellValue(2, 2);
    }

    @Test
    public void testRename() {
        ArraySheet sheet = new ArraySheet("hello", 2, 2, sample(), false);
        // same name
        assertSame(sheet, sheet.rename("hello"));
        // new name
        ArraySheet other = sheet.rename("world");
        assertThat(sheet, nameEqualTo("hello"));
        assertThat(other, nameEqualTo("world"));
        assertThat(other, sameDimensionAs(sheet));
        assertCellValueEquals(sheet, other);
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings("null")
    public void testRenameNullPointerException() {
        new ArraySheet("hello", 2, 2, sample(), false).rename(null);
    }

    @Test
    public void testInv() {
        ArraySheet sheet = new ArraySheet("hello", 2, 2, sample(), false).inv();
        assertThat(sheet.getCell(0, 0), valueEqualTo(STRING));
        assertThat(sheet.getCell(1, 0), valueEqualTo(DATE));
        assertThat(sheet.getCell(0, 1), valueEqualTo(NUMBER));
        assertNull(sheet.getCell(1, 1));
        assertSheetEquals(sheet, sheet.inv().inv());
    }

    @Test
    public void testDeepCopy() {
        // 1. copy of ArraySheet
        ArraySheet s1 = new ArraySheet("hello", 2, 2, sample(), false);
        assertNotSame(s1, ArraySheet.copyOf(s1));
        assertSheetEquals(s1, ArraySheet.copyOf(s1));
        // 2. copy of some other impl
        Sheet s2 = new Sheet() {

            final ArraySheet.FlyweightCell cell = new ArraySheet.FlyweightCell();
            final Object[] values = sample();

            @Override
            public int getRowCount() {
                return 2;
            }

            @Override
            public int getColumnCount() {
                return 2;
            }

            @Override
            public Cell getCell(int rowIdx, int columnIdx) throws IndexOutOfBoundsException {
                Object value = values[rowIdx * getColumnCount() + columnIdx];
                return value != null ? cell.withValue(value) : null;
            }

            @Override
            public String getName() {
                return "fake";
            }
        };
        assertNotSame(s2, ArraySheet.copyOf(s2));
        assertSheetEquals(s2, ArraySheet.copyOf(s2));
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        ArraySheet sheet = new ArraySheet("hello", 2, 2, sample(), false);
        ArraySheet other = writeRead(sheet);
        assertNotSame(sheet, other);
        assertSheetEquals(sheet, other);
    }

    private <T extends Serializable> T writeRead(T input) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(byteArray)) {
            out.writeObject(input);
        }
        try (ObjectInputStream out = new ObjectInputStream(new ByteArrayInputStream(byteArray.toByteArray()))) {
            return (T) out.readObject();
        }
    }

    @Test
    public void testEquals() {
        ArraySheet.Builder b = ArraySheet.builder();

        ArraySheetAssert.assertThat(b.clear().build())
                .isEqualTo(b.clear().build())
                .isNotEqualTo(b.clear().row(0, 0, "other").build())
                .isNotEqualTo(b.clear().name("new name").build());

        ArraySheetAssert.assertThat(b.clear().row(1, 2, STRING, 3.14, DATE).build())
                .isEqualTo(b.clear().row(1, 2, STRING, 3.14, DATE).build())
                .isNotEqualTo(b.clear().row(0, 0, "other").build())
                .isNotEqualTo(b.clear().row(1, 2, STRING, 123, DATE).build())
                .isNotEqualTo(b.clear().row(1, 2, STRING, 3.14, DATE).name("new name").build())
                .isEqualTo(b.clear().column(2, 1, STRING, 3.14, DATE).build().inv());
    }

    @Test
    public void testHashCode() {
        ArraySheet.Builder b = ArraySheet.builder();

        ArraySheet s;

        s = b.clear().build();
        assertEquals(s.hashCode(), s.copy().hashCode());
        assertEquals(s.hashCode(), b.clear().build().hashCode());
        assertNotEquals(s.hashCode(), b.clear().row(0, 0, "world").build().hashCode());
        assertNotEquals(s.hashCode(), s.rename("new name").hashCode());

        s = b.clear().row(1, 2, STRING, 3.14, DATE).build();
        assertEquals(s.hashCode(), s.copy().hashCode());
        assertEquals(s.hashCode(), b.clear().row(1, 2, STRING, 3.14, DATE).build().hashCode());
        assertNotEquals(s.hashCode(), b.clear().row(0, 0, "other").build().hashCode());
        assertNotEquals(s.hashCode(), b.clear().row(1, 2, STRING, 123, DATE).build().hashCode());
        assertNotEquals(s.hashCode(), s.rename("new name").hashCode());
        assertEquals(s.hashCode(), b.clear().column(2, 1, STRING, 3.14, DATE).build().inv().hashCode());
    }
}
