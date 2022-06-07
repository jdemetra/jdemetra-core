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
package demetra.tsprovider.legacy;

import demetra.tsprovider.DataSource;
import org.junit.AfterClass;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class LegacyFileIdTest {

    static File FILE, OTHER;

    @BeforeAll
    public static void beforeClass() throws IOException {
        FILE = File.createTempFile("123", "456");
        OTHER = File.createTempFile("aaa", "bbb");
    }

    @AfterAll
    public static void afterClass() {
        FILE.delete();
        OTHER.delete();
    }

    @Test
    public void testDemetraUri() {
        String input = DataSource.of("p", "123").toString();
        assertThat(LegacyFileId.parse(input)).isNull();
    }

    @Test
    public void testFromFile() {
        LegacyFileId sourceId = LegacyFileId.of(FILE);
        Assertions.assertNotNull(sourceId);
        Assertions.assertEquals(FILE, new File(sourceId.getFile()));
    }

    @Test
    public void testParseString() {
        LegacyFileId sourceId = LegacyFileId.parse(FILE.getPath());
        Assertions.assertNotNull(sourceId);
        Assertions.assertEquals(FILE, new File(sourceId.getFile()));
    }

    @Test
    public void testParseCharSequence() {
        LegacyFileId sourceId = LegacyFileId.parse((CharSequence) FILE.getPath());
        Assertions.assertNotNull(sourceId);
        Assertions.assertEquals(FILE, new File(sourceId.getFile()));
    }

    @Test
    public void testEquals() throws IOException {
        LegacyFileId sourceId = LegacyFileId.of(FILE);

        Assertions.assertEquals(sourceId, LegacyFileId.of(FILE));
        Assertions.assertNotSame(sourceId, LegacyFileId.of(FILE));
        Assertions.assertFalse(sourceId.equals(LegacyFileId.of(OTHER)));

        Assertions.assertEquals(sourceId, LegacyFileId.parse(FILE.getPath()));
        Assertions.assertNotSame(sourceId, LegacyFileId.parse(FILE.getPath()));
        Assertions.assertFalse(sourceId.equals(LegacyFileId.parse(OTHER.getPath())));

        Assertions.assertEquals(sourceId, LegacyFileId.parse((CharSequence) sourceId));
        Assertions.assertSame(sourceId, LegacyFileId.parse((CharSequence) sourceId));
    }
}
