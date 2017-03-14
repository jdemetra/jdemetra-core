/*
 * Copyright 2017 National Bank of Belgium
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
package internal.workspace.file;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import static ec.demetra.workspace.WorkspaceFamily.MOD_DOC_REGARIMA;
import static ec.demetra.workspace.WorkspaceFamily.MOD_DOC_TRAMO;
import static ec.demetra.workspace.WorkspaceFamily.MOD_SPEC_REGARIMA;
import static ec.demetra.workspace.WorkspaceFamily.MOD_SPEC_TRAMO;
import static ec.demetra.workspace.WorkspaceFamily.SA_DOC_TRAMOSEATS;
import static ec.demetra.workspace.WorkspaceFamily.SA_DOC_X13;
import static ec.demetra.workspace.WorkspaceFamily.SA_MULTI;
import static ec.demetra.workspace.WorkspaceFamily.SA_SPEC_TRAMOSEATS;
import static ec.demetra.workspace.WorkspaceFamily.SA_SPEC_X13;
import static ec.demetra.workspace.WorkspaceFamily.UTIL_CAL;
import static ec.demetra.workspace.WorkspaceFamily.UTIL_VAR;
import static internal.test.TestResources.GENERIC_INDEX;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.assertj.core.util.Strings;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Philippe Charles
 */
public class GenericIndexerTest {

    @Test
    public void testLoad() throws IOException {
        Path sampleFile = newGenericSample();
        try (Indexer indexer = new GenericIndexer(sampleFile, FileWorkspaceImpl.getRootFolder(sampleFile))) {
            assertThat(indexer.loadIndex()).isEqualTo(sampleIndex);
        }
    }

    @Test
    @SuppressWarnings("null")
    public void testStore() throws IOException {
        Path sampleFile = newGenericSample();
        try (Indexer indexer = new GenericIndexer(sampleFile, FileWorkspaceImpl.getRootFolder(sampleFile))) {
            assertThatThrownBy(() -> indexer.storeIndex(null)).isInstanceOf(NullPointerException.class);

            Index newIndex = sampleIndex.withItem(new Index.Key(SA_MULTI, "hello"), new Index.Value("hello", false, null));
            indexer.storeIndex(newIndex);
            assertThat(indexer.loadIndex()).isEqualTo(newIndex);
        }
    }

    @Test
    public void testSameContentAfterLoadStore() throws IOException {
        Path sampleFile = newGenericSample();
        try (Indexer indexer = new GenericIndexer(sampleFile, FileWorkspaceImpl.getRootFolder(sampleFile))) {
            FileTime lastModified = Files.getLastModifiedTime(sampleFile);
            indexer.storeIndex(indexer.loadIndex());
            assertThat(Files.getLastModifiedTime(sampleFile)).isGreaterThanOrEqualTo(lastModified);
            assertThat(Strings.join(Files.readAllLines(sampleFile)).with("")).isXmlEqualToContentOf(GENERIC_INDEX.toFile());
        }
    }

    @Test
    public void testUniqueItems() throws IOException {
        Path sampleFile = newGenericSample();
        try (Indexer indexer = new GenericIndexer(sampleFile, FileWorkspaceImpl.getRootFolder(sampleFile))) {
            assertThatThrownBy(() -> indexer.checkId(new Index.Key(UTIL_CAL, "Calendars2"))).isInstanceOf(IOException.class);
        }
    }

    private static FileSystem JIM_FS;

    @BeforeClass
    public static void beforeClass() {
        JIM_FS = Jimfs.newFileSystem(Configuration.unix());
    }

    @AfterClass
    public static void afterClass() throws IOException {
        JIM_FS.close();
    }

    private final Index sampleIndex = Index.builder()
            .name("my_workspace")
            .item(new Index.Key(SA_MULTI, "SAProcessing-1"), new Index.Value("SAProcessing-1", false, "hello world"))
            .item(new Index.Key(MOD_DOC_TRAMO, "TramoDoc-1"), new Index.Value("TramoDoc-1", false, null))
            .item(new Index.Key(SA_DOC_X13, "X13Doc-1"), new Index.Value("X13Doc-1", false, null))
            .item(new Index.Key(UTIL_VAR, "Vars-1"), new Index.Value("Vars-1", false, null))
            .item(new Index.Key(SA_SPEC_TRAMOSEATS, "TramoSeatsSpec-1"), new Index.Value("TramoSeatsSpec-1", false, null))
            .item(new Index.Key(SA_SPEC_X13, "X13Spec-1"), new Index.Value("X13Spec-1", false, null))
            .item(new Index.Key(MOD_SPEC_REGARIMA, "RegArimaSpec-1"), new Index.Value("RegArimaSpec-1", false, null))
            .item(new Index.Key(MOD_SPEC_TRAMO, "TramoSpec-1"), new Index.Value("TramoSpec-1", false, null))
            .item(new Index.Key(SA_DOC_TRAMOSEATS, "TramoSeatsDoc-1"), new Index.Value("TramoSeatsDoc-1", false, null))
            .item(new Index.Key(MOD_DOC_REGARIMA, "RegArimaDoc-1"), new Index.Value("RegArimaDoc-1", false, null))
            .item(new Index.Key(UTIL_CAL, "Calendars"), new Index.Value("Calendars", false, null))
            .build();

    private static Path newGenericSample() throws IOException {
        Path result = Files.createTempFile(JIM_FS.getPath("/"), "ws_", ".xml");
        Files.copy(GENERIC_INDEX, result, StandardCopyOption.REPLACE_EXISTING);
        Files.createDirectories(FileWorkspaceImpl.getRootFolder(result).resolve("Calendars").resolve("Calendars.xml"));
        Files.copy(FileWorkspaceImpl.getRootFolder(GENERIC_INDEX).resolve("Calendars").resolve("Calendars.xml"),
                FileWorkspaceImpl.getRootFolder(result).resolve("Calendars").resolve("Calendars.xml"),
                StandardCopyOption.REPLACE_EXISTING);
        return result;
    }
}
