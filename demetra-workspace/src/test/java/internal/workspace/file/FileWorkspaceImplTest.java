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
import internal.workspace.file.spi.FamilyHandlerLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;

import static internal.test.TestResources.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Philippe Charles
 */
public class FileWorkspaceImplTest {

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
        try (FileWorkspace ws = FileWorkspaceImpl.open(LEGACY_INDEX, FileFormat.LEGACY, new FamilyHandlerLoader()::get)) {
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
    public void testRename(@TempDir Path temp) throws IOException {
        Path copyOfGeneric = newGenericSample(temp);

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
    public void testDeleteItem(@TempDir Path temp) throws IOException {
        Path copyOfGeneric = newGenericSample(temp);

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
    public void testStoreItem(@TempDir Path temp) throws IOException {
        Path copyOfGeneric = newGenericSample(temp);

        try (FileWorkspace ws = openGenericUsingServiceLoader(copyOfGeneric)) {
            assertThatThrownBy(() -> ws.store(GENERIC_SA_MULTI, "hello"))
                    .isInstanceOf(IOException.class)
                    .hasCauseInstanceOf(ClassCastException.class);
            assertThatThrownBy(() -> ws.store(null, "hello")).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> ws.store(GENERIC_SA_MULTI, null)).isInstanceOf(NullPointerException.class);
        }
    }

    @Test
    public void testAddItem(@TempDir Path temp) throws IOException {
        Path copyOfGeneric = newGenericSample(temp);

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
    public void testReplaceItem(@TempDir Path temp) throws IOException {
        Path copyOfGeneric = newGenericSample(temp);

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
    public void testItemWithoutFile(@TempDir Path temp) throws IOException {
        Path copyOfGeneric = newGenericSample(temp);

        Files.delete(FileWorkspaceImpl.getRootFolder(copyOfGeneric).resolve("SAProcessing").resolve("SAProcessing-1.xml"));
        try (FileWorkspace ws = openGenericUsingServiceLoader(copyOfGeneric)) {
            assertThat(ws.getFile(GENERIC_SA_MULTI)).doesNotExist();
            assertThat(ws.getItems()).contains(GENERIC_SA_MULTI);
        }
    }

    @Test
    public void testFileWithoutItem(@TempDir Path temp) throws IOException {
        Path copyOfGeneric = newGenericSample(temp);

        try (Indexer indexer = new GenericIndexer(copyOfGeneric, FileWorkspaceImpl.getRootFolder(copyOfGeneric))) {
            indexer.storeIndex(indexer.loadIndex().withoutItem(FileWorkspaceImpl.toKey(GENERIC_SA_MULTI)));
        }
        try (FileWorkspace ws = openGenericUsingServiceLoader(copyOfGeneric)) {
            assertThat(ws.getFile(GENERIC_SA_MULTI)).exists();
            assertThat(ws.getItems()).doesNotContain(GENERIC_SA_MULTI);
        }
    }

    @Test
    public void testGetRootFolder(@TempDir Path temp) throws IOException {
        assertThat(FileWorkspaceImpl.getRootFolder(temp.resolve("workspace.xml")))
                .isAbsolute()
                .hasFileName("workspace");

        assertThatThrownBy(() -> FileWorkspaceImpl.getRootFolder(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> FileWorkspaceImpl.getRootFolder(temp.getRoot())).isInstanceOf(IOException.class);
    }

    private static FileWorkspaceImpl openGenericUsingServiceLoader(Path file) throws IOException {
        return FileWorkspaceImpl.open(file, FileFormat.GENERIC, new FamilyHandlerLoader()::get);
    }

    private static Path newGenericSample(Path temp) throws IOException {
        Path result = Files.createTempFile(temp, "ws_", ".xml");
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
