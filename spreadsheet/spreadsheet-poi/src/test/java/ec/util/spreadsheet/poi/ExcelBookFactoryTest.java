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

import static ec.util.spreadsheet.Assertions.assertThat;
import ec.util.spreadsheet.Book;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.assertj.core.util.DateUtil;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class ExcelBookFactoryTest {

    private static final URL TOP5 = ExcelBookFactoryTest.class.getResource("/Top5Browsers.xlsx");

    private static File VALID_FILE;
    private static File EMPTY_FILE;
    private static File MISSING_FILE;

    private static ExcelBookFactory FAST_FACTORY;
    private static ExcelBookFactory NORMAL_FACTORY;

    @BeforeClass
    public static void beforeClass() throws IOException {
        Path top5 = Files.createTempFile("top5", ".xlsx");
        try (InputStream stream = TOP5.openStream()) {
            Files.copy(stream, top5, StandardCopyOption.REPLACE_EXISTING);
        }

        VALID_FILE = top5.toFile();
        VALID_FILE.deleteOnExit();

        EMPTY_FILE = Files.createTempFile("Empty", ".xlsx").toFile();
        EMPTY_FILE.deleteOnExit();

        MISSING_FILE = Files.createTempFile("Missing", ".xlsx").toFile();
        MISSING_FILE.delete();

        FAST_FACTORY = new ExcelBookFactory();
        FAST_FACTORY.setFast(true);

        NORMAL_FACTORY = new ExcelBookFactory();
        NORMAL_FACTORY.setFast(false);
    }

    @Test
    public void testCompliance() throws IOException {
        assertThat(FAST_FACTORY).isCompliant(VALID_FILE, EMPTY_FILE);
        assertThat(NORMAL_FACTORY).isCompliant(VALID_FILE, EMPTY_FILE);
    }

    @Test
    public void testLoadEmptyAndMissing() {
        assertThatThrownBy(() -> FAST_FACTORY.load(EMPTY_FILE)).isInstanceOf(IOException.class);
        assertThatThrownBy(() -> NORMAL_FACTORY.load(EMPTY_FILE)).isInstanceOf(IOException.class);
        assertThatThrownBy(() -> FAST_FACTORY.load(MISSING_FILE)).isInstanceOf(IOException.class);
        assertThatThrownBy(() -> NORMAL_FACTORY.load(MISSING_FILE)).isInstanceOf(IOException.class);
    }

    @Test
    public void testGetSheetCount() throws Exception {
        try (InputStream stream = TOP5.openStream()) {
            try (Book book = FAST_FACTORY.load(stream)) {
                assertEquals(3, book.getSheetCount());
            }
        }
    }

    @Test
    public void testGetSheet() throws Exception {
        try (InputStream stream = TOP5.openStream()) {
            try (Book book = FAST_FACTORY.load(stream)) {
                assertThat(book.getSheet(0))
                        .hasCellValue(0, 0, null)
                        .hasCellValue(0, 1, "IE")
                        .hasCellValue(1, 0, DateUtil.parse("2008-07-01"))
                        .hasCellValue(1, 1, 68.57);
            }
        }
    }
}
