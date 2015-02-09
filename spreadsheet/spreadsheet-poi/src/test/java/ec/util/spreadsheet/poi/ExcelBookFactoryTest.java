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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Objects;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class ExcelBookFactoryTest {

    private static URL TOP5;

    private static URL BAD_URL;
    private static File BAD_FILE;
    private static File MISSING_FILE;
    private static URL MISSING_URL;

    private static ExcelBookFactory FAST_FACTORY;
    private static ExcelBookFactory NORMAL_FACTORY;

    @BeforeClass
    public static void beforeClass() throws IOException {
        TOP5 = ExcelBookFactoryTest.class.getResource("/Top5Browsers.xlsx");
        Objects.requireNonNull(TOP5);

        BAD_URL = ExcelBookFactoryTest.class.getResource("/InvalidZipFile.xlsx");
        Objects.requireNonNull(BAD_URL);

        BAD_FILE = Files.createTempFile("Empty", ".xlsx").toFile();
        BAD_FILE.deleteOnExit();

        MISSING_FILE = Files.createTempFile("Missing", ".xlsx").toFile();
        MISSING_FILE.delete();

        MISSING_URL = MISSING_FILE.toURI().toURL();

        FAST_FACTORY = new ExcelBookFactory();
        FAST_FACTORY.setFast(true);

        NORMAL_FACTORY = new ExcelBookFactory();
        NORMAL_FACTORY.setFast(false);
    }

    @Test
    public void testLoadStore() throws IOException {
        SpreadsheetAssert.assertLoadStore(FAST_FACTORY, TOP5);
        SpreadsheetAssert.assertLoadStore(NORMAL_FACTORY, TOP5);
    }

    @Test(expected = IOException.class)
    public void testFastLoadBadUrl() throws IOException {
        FAST_FACTORY.load(BAD_URL);
    }

    @Test(expected = IOException.class)
    public void testLoadBadUrl() throws IOException {
        NORMAL_FACTORY.load(BAD_URL);
    }

    @Test(expected = IOException.class)
    public void testFastLoadBadFile() throws IOException {
        FAST_FACTORY.load(BAD_FILE);
    }

    @Test(expected = IOException.class)
    public void testLoadBadFile() throws IOException {
        NORMAL_FACTORY.load(BAD_FILE);
    }

    @Test(expected = IOException.class)
    public void testFastLoadMissingFile() throws IOException {
        FAST_FACTORY.load(MISSING_FILE);
    }

    @Test(expected = IOException.class)
    public void testLoadMissingFile() throws IOException {
        NORMAL_FACTORY.load(MISSING_FILE);
    }

    @Test(expected = IOException.class)
    public void testFastLoadMissingUrl() throws IOException {
        FAST_FACTORY.load(MISSING_URL);
    }

    @Test(expected = IOException.class)
    public void testLoadMissingUrl() throws IOException {
        NORMAL_FACTORY.load(MISSING_URL);
    }
}
