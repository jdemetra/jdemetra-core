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
import ec.demetra.workspace.WorkspaceItem;
import ec.demetra.workspace.file.FileFormat;
import ec.demetra.workspace.file.FileWorkspace;
import ec.demetra.workspace.file.spi.FamilyHandler;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.satoolkit.x13.X13Specification;
import ec.tss.modelling.documents.RegArimaDocument;
import ec.tss.modelling.documents.TramoDocument;
import ec.tss.sa.SaProcessing;
import ec.tss.sa.documents.TramoSeatsDocument;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.timeseries.calendars.GregorianCalendarManager;
import ec.tstoolkit.timeseries.regression.TsVariables;
import internal.io.IoUtil;
import static internal.test.TestResources.GENERIC_INDEX;
import static internal.test.TestResources.GENERIC_ITEMS;
import static internal.test.TestResources.GENERIC_MOD_DOC_REGARIMA;
import static internal.test.TestResources.GENERIC_MOD_DOC_TRAMO;
import static internal.test.TestResources.GENERIC_MOD_SPEC_REGARIMA;
import static internal.test.TestResources.GENERIC_MOD_SPEC_TRAMO;
import static internal.test.TestResources.GENERIC_ROOT;
import static internal.test.TestResources.GENERIC_SA_DOC_TRAMOSEATS;
import static internal.test.TestResources.GENERIC_SA_DOC_X13;
import static internal.test.TestResources.GENERIC_SA_MULTI;
import static internal.test.TestResources.GENERIC_SA_SPEC_TRAMOSEATS;
import static internal.test.TestResources.GENERIC_SA_SPEC_X13;
import static internal.test.TestResources.GENERIC_UTIL_CAL;
import static internal.test.TestResources.GENERIC_UTIL_VAR;
import static internal.test.TestResources.LEGACY_INDEX;
import static internal.test.TestResources.LEGACY_ITEMS;
import static internal.test.TestResources.LEGACY_ROOT;
import static internal.test.TestResources.LEGACY_SA_DOC_TRAMOSEATS;
import static internal.test.TestResources.LEGACY_SA_DOC_X13;
import static internal.test.TestResources.LEGACY_SA_MULTI;
import static internal.test.TestResources.LEGACY_SA_SPEC_TRAMOSEATS;
import static internal.test.TestResources.LEGACY_SA_SPEC_X13;
import static internal.test.TestResources.LEGACY_UTIL_CAL;
import static internal.test.TestResources.LEGACY_UTIL_VAR;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 *
 * @author Philippe Charles
 */
public class FileWorkspaceImplTest {

    private static FileSystem JIM_FS;

    @BeforeClass
    public static void beforeClass() {
        JIM_FS = Jimfs.newFileSystem(Configuration.unix());
    }

    @AfterClass
    public static void afterClass() throws IOException {
        JIM_FS.close();
    }

    @Test
    public void testLoadGeneric() throws IOException {
        try (FileWorkspace ws = openGenericUsingServiceLoader(GENERIC_INDEX)) {
            assertThat(ws.getName()).isEqualTo("my_workspace");
            assertThat(ws.getFileFormat()).isEqualTo(FileFormat.GENERIC);
            assertThat(ws.getFile()).isEqualTo(GENERIC_INDEX);
            assertThat(ws.getRootFolder()).isEqualTo(GENERIC_ROOT);
            assertThat(ws.getSupportedFamilies()).isNotEmpty();

            assertThat(ws.getItems()).containsExactlyInAnyOrder(GENERIC_ITEMS.toArray(new WorkspaceItem[0]));

            for (WorkspaceItem item : ws.getItems()) {
                assertThat(ws.getFile(item)).exists();
            }

            assertThat(ws.load(GENERIC_SA_MULTI)).isInstanceOf(SaProcessing.class);
            assertThat(ws.load(GENERIC_SA_DOC_X13)).isInstanceOf(X13Document.class);
            assertThat(ws.load(GENERIC_SA_DOC_TRAMOSEATS)).isInstanceOf(TramoSeatsDocument.class);
            assertThat(ws.load(GENERIC_SA_SPEC_X13)).isInstanceOf(X13Specification.class);
            assertThat(ws.load(GENERIC_SA_SPEC_TRAMOSEATS)).isInstanceOf(TramoSeatsSpecification.class);

            assertThat(ws.load(GENERIC_MOD_DOC_REGARIMA)).isInstanceOf(RegArimaDocument.class);
            assertThat(ws.load(GENERIC_MOD_DOC_TRAMO)).isInstanceOf(TramoDocument.class);
            assertThat(ws.load(GENERIC_MOD_SPEC_REGARIMA)).isInstanceOf(RegArimaSpecification.class);
            assertThat(ws.load(GENERIC_MOD_SPEC_TRAMO)).isInstanceOf(TramoSpecification.class);

            assertThat(ws.load(GENERIC_UTIL_CAL)).isInstanceOf(GregorianCalendarManager.class);
            assertThat(ws.load(GENERIC_UTIL_VAR)).isInstanceOf(TsVariables.class);
        }
    }

    @Test
    public void testLoadLegacy() throws IOException {
        try (FileWorkspace ws = FileWorkspaceImpl.open(LEGACY_INDEX, FileFormat.LEGACY, IoUtil.supplierOfServiceLoader(FamilyHandler.class))) {
            assertThat(ws.getFileFormat()).isEqualTo(FileFormat.LEGACY);
            assertThat(ws.getFile()).isEqualTo(LEGACY_INDEX);
            assertThat(ws.getRootFolder()).isEqualTo(LEGACY_ROOT);
            assertThat(ws.getSupportedFamilies()).isNotEmpty();

            assertThat(ws.getItems()).containsExactlyInAnyOrder(LEGACY_ITEMS.toArray(new WorkspaceItem[0]));

            for (WorkspaceItem item : ws.getItems()) {
                assertThat(ws.getFile(item)).exists();
            }

            assertThat(ws.load(LEGACY_SA_MULTI)).isInstanceOf(SaProcessing.class);
            assertThat(ws.load(LEGACY_SA_DOC_X13)).isInstanceOf(X13Document.class);
            assertThat(ws.load(LEGACY_SA_DOC_TRAMOSEATS)).isInstanceOf(TramoSeatsDocument.class);
            assertThat(ws.load(LEGACY_SA_SPEC_X13)).isInstanceOf(X13Specification.class);
            assertThat(ws.load(LEGACY_SA_SPEC_TRAMOSEATS)).isInstanceOf(TramoSeatsSpecification.class);

            assertThat(ws.load(LEGACY_UTIL_CAL)).isInstanceOf(GregorianCalendarManager.class);
            assertThat(ws.load(LEGACY_UTIL_VAR)).isInstanceOf(TsVariables.class);
        }
    }

    @Test
    public void testNoHandlers() throws IOException {
        try (FileWorkspace ws = FileWorkspaceImpl.open(GENERIC_INDEX, FileFormat.GENERIC, Collections::emptyList)) {
            assertThat(ws.getName()).isEqualTo("my_workspace");
            assertThat(ws.getFileFormat()).isEqualTo(FileFormat.GENERIC);
            assertThat(ws.getFile()).isEqualTo(GENERIC_INDEX);
            assertThat(ws.getRootFolder()).isEqualTo(GENERIC_ROOT);
            assertThat(ws.getSupportedFamilies()).isEmpty();

            assertThat(ws.getItems()).containsExactlyInAnyOrder(GENERIC_ITEMS.toArray(new WorkspaceItem[0]));

            GENERIC_ITEMS.forEach(o -> {
                assertThatThrownBy(() -> ws.getFile(o)).isInstanceOf(IOException.class);
                assertThatThrownBy(() -> ws.load(o)).isInstanceOf(IOException.class);
            });
        }
    }

    @Test
    public void testUnexpected() throws IOException {
        assertThatThrownBy(() -> FileWorkspaceImpl.open(GENERIC_INDEX, FileFormat.GENERIC, FileWorkspaceImplTest::getHandlersButThrowUnexpected))
                .isInstanceOf(IOException.class)
                .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    public void testRename() throws IOException {
        Path copyOfGeneric = newGenericSample();

        try (FileWorkspace ws = openGenericUsingServiceLoader(copyOfGeneric)) {
            assertThat(ws.getName()).isEqualTo("my_workspace");
            ws.setName("hello");
            assertThat(ws.getName()).isEqualTo("hello");
        }
        try (FileWorkspace ws = openGenericUsingServiceLoader(copyOfGeneric)) {
            assertThat(ws.getName()).isEqualTo("hello");
        }
    }

    @Test
    public void testDeleteItem() throws IOException {
        Path copyOfGeneric = newGenericSample();

        try (FileWorkspace ws = openGenericUsingServiceLoader(copyOfGeneric)) {
            assertThat(ws.getFile(GENERIC_SA_MULTI)).exists();
            assertThat(ws.getItems()).contains(GENERIC_SA_MULTI);
            ws.delete(GENERIC_SA_MULTI);
            assertThat(ws.getFile(GENERIC_SA_MULTI)).doesNotExist();
            assertThat(ws.getItems()).doesNotContain(GENERIC_SA_MULTI);
        }
        try (FileWorkspace ws = openGenericUsingServiceLoader(copyOfGeneric)) {
            assertThat(ws.getFile(GENERIC_SA_MULTI)).doesNotExist();
            assertThat(ws.getItems()).doesNotContain(GENERIC_SA_MULTI);
        }
    }

    @Test
    @SuppressWarnings("null")
    public void testStoreItem() throws IOException {
        Path copyOfGeneric = newGenericSample();

        try (FileWorkspace ws = openGenericUsingServiceLoader(copyOfGeneric)) {
            assertThatThrownBy(() -> ws.store(GENERIC_SA_MULTI, "hello"))
                    .isInstanceOf(IOException.class)
                    .hasCauseInstanceOf(ClassCastException.class);
            assertThatThrownBy(() -> ws.store(null, "hello")).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> ws.store(GENERIC_SA_MULTI, null)).isInstanceOf(NullPointerException.class);
        }
    }

    @Test
    public void testAddItem() throws IOException {
        Path copyOfGeneric = newGenericSample();

        WorkspaceItem newItem = GENERIC_SA_MULTI.toBuilder().id("other").build();
        try (FileWorkspace ws = openGenericUsingServiceLoader(copyOfGeneric)) {
            assertThat(ws.getFile(newItem)).doesNotExist();
            ws.store(newItem, ws.load(GENERIC_SA_MULTI));
            assertThat(ws.getFile(newItem)).exists();
        }
        try (FileWorkspace ws = openGenericUsingServiceLoader(copyOfGeneric)) {
            assertThat(ws.getFile(newItem)).exists();
        }
    }

    @Test
    public void testReplaceItem() throws IOException {
        Path copyOfGeneric = newGenericSample();

        try (FileWorkspace ws = openGenericUsingServiceLoader(copyOfGeneric)) {
            SaProcessing processing = (SaProcessing) ws.load(GENERIC_SA_MULTI);
            assertThat(processing.getMetaData()).doesNotContainEntry("key", "value");
            processing.getMetaData().put("key", "value");
            ws.store(GENERIC_SA_MULTI, processing);
            assertThat(((SaProcessing) ws.load(GENERIC_SA_MULTI)).getMetaData()).containsEntry("key", "value");
        }
        try (FileWorkspace ws = openGenericUsingServiceLoader(copyOfGeneric)) {
            assertThat(((SaProcessing) ws.load(GENERIC_SA_MULTI)).getMetaData()).containsEntry("key", "value");
        }
    }

    @Test
    public void testItemWithoutFile() throws IOException {
        Path copyOfGeneric = newGenericSample();

        Files.delete(FileWorkspaceImpl.getRootFolder(copyOfGeneric).resolve("SAProcessing").resolve("SAProcessing-1.xml"));
        try (FileWorkspace ws = openGenericUsingServiceLoader(copyOfGeneric)) {
            assertThat(ws.getFile(GENERIC_SA_MULTI)).doesNotExist();
            assertThat(ws.getItems()).contains(GENERIC_SA_MULTI);
        }
    }

    @Test
    public void testFileWithoutItem() throws IOException {
        Path copyOfGeneric = newGenericSample();

        try (Indexer indexer = new GenericIndexer(copyOfGeneric, FileWorkspaceImpl.getRootFolder(copyOfGeneric))) {
            indexer.storeIndex(indexer.loadIndex().withoutItem(FileWorkspaceImpl.toKey(GENERIC_SA_MULTI)));
        }
        try (FileWorkspace ws = openGenericUsingServiceLoader(copyOfGeneric)) {
            assertThat(ws.getFile(GENERIC_SA_MULTI)).exists();
            assertThat(ws.getItems()).doesNotContain(GENERIC_SA_MULTI);
        }
    }

    @Test
    public void testGetRootFolder() throws IOException {
        assertThat(FileWorkspaceImpl.getRootFolder(JIM_FS.getPath("/workspace.xml")))
                .isAbsolute()
                .hasFileName("workspace")
                .matches(o -> o.getFileSystem().equals(JIM_FS));

        assertThatThrownBy(() -> FileWorkspaceImpl.getRootFolder(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> FileWorkspaceImpl.getRootFolder(JIM_FS.getPath("/"))).isInstanceOf(IOException.class);
    }

    private static FileWorkspaceImpl openGenericUsingServiceLoader(Path file) throws IOException {
        return FileWorkspaceImpl.open(file, FileFormat.GENERIC, IoUtil.supplierOfServiceLoader(FamilyHandler.class));
    }

    private static Path newGenericSample() throws IOException {
        Path result = Files.createTempFile(JIM_FS.getPath("/"), "ws_", ".xml");
        Files.copy(GENERIC_INDEX, result, StandardCopyOption.REPLACE_EXISTING);
        Files.createDirectories(FileWorkspaceImpl.getRootFolder(result).resolve("SAProcessing"));
        Files.copy(
                FileWorkspaceImpl.getRootFolder(GENERIC_INDEX).resolve("SAProcessing").resolve("SAProcessing-1.xml"),
                FileWorkspaceImpl.getRootFolder(result).resolve("SAProcessing").resolve("SAProcessing-1.xml"));
        return result;
    }

    private static Iterable<FamilyHandler> getHandlersButThrowUnexpected() {
        throw new RuntimeException("boom");
    }
}
