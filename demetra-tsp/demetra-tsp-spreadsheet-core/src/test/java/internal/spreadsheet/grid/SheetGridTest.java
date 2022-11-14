/*
 * Copyright 2018 National Bank of Belgium
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
package internal.spreadsheet.grid;

import _test.DataForTest;
import demetra.timeseries.TsCollection;
import demetra.tsprovider.grid.GridReader;
import ec.util.spreadsheet.Book;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class SheetGridTest {

    @Test
    public void test() throws IOException {
        SheetGrid grid = SheetGrid.of(new File(""), DataForTest.FACTORY, GridReader.DEFAULT);

        assertThat(grid.getSheetByName("s1")).map(TsCollection::getName).contains("s1");
        assertThat(grid.getSheetByName("s2")).map(TsCollection::getName).contains("s2");
        assertThat(grid.getSheetByName("other")).isEmpty();
        assertThat(grid.getSheetNames()).containsExactly("s1", "s2");
        assertThat(grid.getSheets()).extracting(o -> o.getName()).containsExactly("s1", "s2");
    }

    @Test
    public void testDataTypes() {
        SheetGrid excel = SheetGrid.of(new File(""), new MockedFactory(Date.class::isAssignableFrom), GridReader.DEFAULT);
        assertThat(excel.isSupportedDataType(Date.class)).isTrue();
        assertThat(excel.isSupportedDataType(Number.class)).isFalse();
        assertThat(excel.isSupportedDataType(String.class)).isFalse();
        assertThat(excel.isSupportedDataType(LocalDateTime.class)).isTrue();
        assertThat(excel.isSupportedDataType(Object.class)).isFalse();

        SheetGrid html = SheetGrid.of(new File(""), new MockedFactory(Number.class::isAssignableFrom), GridReader.DEFAULT);
        assertThat(html.isSupportedDataType(Date.class)).isFalse();
        assertThat(html.isSupportedDataType(Number.class)).isTrue();
        assertThat(html.isSupportedDataType(String.class)).isFalse();
        assertThat(html.isSupportedDataType(LocalDateTime.class)).isFalse();
        assertThat(html.isSupportedDataType(Object.class)).isFalse();
    }

    @lombok.AllArgsConstructor
    private static final class MockedFactory extends Book.Factory {

        private final Predicate<Class<?>> supportedDataType;

        @Override
        public boolean isSupportedDataType(@NonNull Class<?> type) {
            return supportedDataType.test(type);
        }

        @Override
        public @NonNull String getName() {
            throw new RuntimeException();
        }

        @Override
        public @NonNull Book load(@NonNull InputStream stream) throws IOException {
            throw new RuntimeException();
        }

        @Override
        public void store(@NonNull OutputStream stream, @NonNull Book book) throws IOException {
            throw new RuntimeException();
        }

        @Override
        public boolean accept(File pathname) {
            throw new RuntimeException();
        }
    }
}
