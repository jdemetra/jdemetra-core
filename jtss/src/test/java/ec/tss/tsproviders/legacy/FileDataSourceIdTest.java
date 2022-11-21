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

package ec.tss.tsproviders.legacy;

import ec.tss.tsproviders.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class FileDataSourceIdTest {

    @Test
    public void testDemetraUri() {
        String input = DataSource.uriFormatter().formatAsString(DataSource.of("p", "123"));
        assertThat(FileDataSourceId.parse(input)).isNull();
    }

    @Test
    public void testFromFile(@TempDir Path temp) throws IOException {
        File file = Files.createTempFile(temp, "123", "456").toFile();

        FileDataSourceId sourceId = FileDataSourceId.from(file);
        assertThat(sourceId).isNotNull();
        assertThat(file).isEqualTo(new File(sourceId.getFile()));
    }

    @Test
    public void testParseString(@TempDir Path temp) throws IOException {
        File file = Files.createTempFile(temp, "123", "456").toFile();

        FileDataSourceId sourceId = FileDataSourceId.parse(file.getPath());
        assertThat(sourceId).isNotNull();
        assertThat(file).isEqualTo(new File(sourceId.getFile()));
    }

    @Test
    public void testParseCharSequence(@TempDir Path temp) throws IOException {
        File file = Files.createTempFile(temp, "123", "456").toFile();

        FileDataSourceId sourceId = FileDataSourceId.parse((CharSequence) file.getPath());
        assertThat(sourceId).isNotNull();
        assertThat(file).isEqualTo(new File(sourceId.getFile()));
    }

    @Test
    public void testEquals(@TempDir Path temp) throws IOException {
        File file = Files.createTempFile(temp, "123", "456").toFile();
        File other = Files.createTempFile(temp, "aaa", "bbb").toFile();

        FileDataSourceId sourceId = FileDataSourceId.from(file);

        assertThat(sourceId)
                .isEqualTo(FileDataSourceId.from(file))
                .isNotSameAs(FileDataSourceId.from(file))
                .isNotEqualTo(FileDataSourceId.from(other))
                .isEqualTo(FileDataSourceId.parse(file.getPath()))
                .isNotSameAs(FileDataSourceId.parse(file.getPath()))
                .isNotEqualTo(FileDataSourceId.parse(other.getPath()))
                .isEqualTo(FileDataSourceId.parse((CharSequence) sourceId))
                .isSameAs(FileDataSourceId.parse((CharSequence) sourceId));
    }
}
