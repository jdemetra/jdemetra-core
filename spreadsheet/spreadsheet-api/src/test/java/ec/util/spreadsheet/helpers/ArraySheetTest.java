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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class ArraySheetTest {

    static final String STRING = "a";
    static final Date DATE = new Date(1234);
    static final Number NUMBER = 12.34;

    static Serializable[] sample() {
        return new Serializable[]{STRING, DATE, NUMBER, null};
    }

    static Serializable[] empty() {
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
        assertThat(new ArraySheet("", 0, 0, empty(), false).getName()).isEqualTo("");
        assertThat(new ArraySheet("hello", 2, 4, empty(), false).getName()).isEqualTo("hello");
    }

    @Test
    public void testGetCell() {
        ArraySheet sheet = new ArraySheet("hello", 2, 2, sample(), false);
        assertThat(sheet.getCell(0, 0).getString()).isEqualTo(STRING);
        assertThat(sheet.getCell(0, 1).getDate()).isEqualTo(DATE);
        assertThat(sheet.getCell(1, 0).getNumber()).isEqualTo(NUMBER);
        assertThat(sheet.getCell(1, 1)).isNull();
        assertThatThrownBy(() -> sheet.getCell(-1, -1)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> sheet.getCell(2, 2)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    public void testGetCellValue() {
        ArraySheet sheet = new ArraySheet("hello", 2, 2, sample(), false);
        assertThat(sheet.getCellValue(0, 0)).isEqualTo(STRING);
        assertThat(sheet.getCellValue(0, 1)).isEqualTo(DATE);
        assertThat(sheet.getCellValue(1, 0)).isEqualTo(NUMBER);
        assertThat(sheet.getCellValue(1, 1)).isNull();
        assertThatThrownBy(() -> sheet.getCellValue(-1, -1)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> sheet.getCellValue(2, 2)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @SuppressWarnings("null")
    @Test
    public void testRename() {
        ArraySheet sheet = new ArraySheet("hello", 2, 2, sample(), false);
        // same name
        assertSame(sheet, sheet.rename("hello"));
        // new name
        ArraySheet other = sheet.rename("world");
        assertThat(sheet.getName()).isEqualTo("hello");
        assertThat(other.getName()).isEqualTo("world");
        assertThat(other.getColumnCount()).isEqualTo(sheet.getColumnCount());
        assertThat(other.getRowCount()).isEqualTo(sheet.getRowCount());
        assertCellValueEquals(sheet, other);
        // null name
        assertThatThrownBy(() -> sheet.rename(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testInv() {
        ArraySheet sheet = new ArraySheet("hello", 2, 2, sample(), false).inv();
        assertThat(sheet.getCell(0, 0).getString()).isEqualTo(STRING);
        assertThat(sheet.getCell(1, 0).getDate()).isEqualTo(DATE);
        assertThat(sheet.getCell(0, 1).getNumber()).isEqualTo(NUMBER);
        assertThat(sheet.getCell(1, 1)).isNull();
        assertSheetEquals(sheet, sheet.inv().inv());
    }

    @Test
    public void testDeepCopy() {
        // 1. copy of ArraySheet
        ArraySheet s1 = new ArraySheet("hello", 2, 2, sample(), false);
        assertNotSame(s1, ArraySheet.copyOf(s1));
        assertSheetEquals(s1, ArraySheet.copyOf(s1));
        // 2. copy of some other impl
        Sheet s2 = new FakeSheet();
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

    private static void assertCellValueEquals(Sheet expected, Sheet actual) {
        expected.forEachValue((i, j, v) -> assertEquals(CellRefHelper.getCellRef(i, j), v, actual.getCellValue(i, j)));
        actual.forEachValue((i, j, v) -> assertEquals(CellRefHelper.getCellRef(i, j), v, expected.getCellValue(i, j)));
    }

    private static void assertSheetEquals(Sheet expected, Sheet actual) {
        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getColumnCount()).isEqualTo(expected.getColumnCount());
        assertThat(actual.getRowCount()).isEqualTo(expected.getRowCount());
        assertCellValueEquals(expected, actual);
    }

    private static final class FakeSheet extends Sheet {

        private final ArraySheet.FlyweightCell cell = new ArraySheet.FlyweightCell();
        private final Object[] values = sample();

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
    }
}
