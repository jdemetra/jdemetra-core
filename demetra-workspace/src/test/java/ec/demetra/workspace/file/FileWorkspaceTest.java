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
package ec.demetra.workspace.file;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import ec.demetra.workspace.Workspace;
import ec.demetra.workspace.WorkspaceItem;
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
import static internal.test.TestResources.GENERIC_INDEX;
import static internal.test.TestResources.GENERIC_ITEMS;
import static internal.test.TestResources.GENERIC_MOD_DOC_REGARIMA;
import static internal.test.TestResources.GENERIC_MOD_DOC_TRAMO;
import static internal.test.TestResources.GENERIC_MOD_SPEC_REGARIMA;
import static internal.test.TestResources.GENERIC_MOD_SPEC_TRAMO;
import static internal.test.TestResources.GENERIC_SA_DOC_TRAMOSEATS;
import static internal.test.TestResources.GENERIC_SA_DOC_X13;
import static internal.test.TestResources.GENERIC_SA_MULTI;
import static internal.test.TestResources.GENERIC_SA_SPEC_TRAMOSEATS;
import static internal.test.TestResources.GENERIC_SA_SPEC_X13;
import static internal.test.TestResources.GENERIC_UTIL_CAL;
import static internal.test.TestResources.GENERIC_UTIL_VAR;
import static internal.test.TestResources.LEGACY_INDEX;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 *
 * @author Philippe Charles
 */
public class FileWorkspaceTest {

    @Test
    @SuppressWarnings("null")
    public void testProbeFormat() throws IOException {
        assertThat(FileWorkspace.probeFormat(Files.createTempFile("ws", ".xml"))).isEmpty();
        assertThat(FileWorkspace.probeFormat(GENERIC_INDEX)).hasValue(FileFormat.GENERIC);
        assertThat(FileWorkspace.probeFormat(LEGACY_INDEX)).hasValue(FileFormat.LEGACY);
        assertThatThrownBy(() -> FileWorkspace.probeFormat(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testOpenWithProbe() throws IOException {
        try (FileWorkspace ws = FileWorkspace.open(GENERIC_INDEX)) {
            assertThat(ws.getFileFormat()).isEqualTo(FileFormat.GENERIC);
            assertThat(ws.getFile()).isEqualTo(GENERIC_INDEX);
        }
        try (FileWorkspace ws = FileWorkspace.open(LEGACY_INDEX)) {
            assertThat(ws.getFileFormat()).isEqualTo(FileFormat.LEGACY);
            assertThat(ws.getFile()).isEqualTo(LEGACY_INDEX);
        }
        assertThatThrownBy(() -> FileWorkspace.open(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testOpenWithFormat() throws IOException {
        try (FileWorkspace ws = FileWorkspace.open(GENERIC_INDEX, FileFormat.GENERIC)) {
            assertThat(ws.getFileFormat()).isEqualTo(FileFormat.GENERIC);
            assertThat(ws.getFile()).isEqualTo(GENERIC_INDEX);
        }
        try (FileWorkspace ws = FileWorkspace.open(LEGACY_INDEX, FileFormat.LEGACY)) {
            assertThat(ws.getFileFormat()).isEqualTo(FileFormat.LEGACY);
            assertThat(ws.getFile()).isEqualTo(LEGACY_INDEX);
        }
        assertThatThrownBy(() -> FileWorkspace.open(null, FileFormat.LEGACY)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> FileWorkspace.open(GENERIC_INDEX, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> FileWorkspace.open(GENERIC_INDEX, FileFormat.LEGACY)).isInstanceOf(IOException.class);
        assertThatThrownBy(() -> FileWorkspace.open(LEGACY_INDEX, FileFormat.GENERIC)).isInstanceOf(IOException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testCreate() throws IOException {
        assertThatThrownBy(() -> FileWorkspace.create(null, FileFormat.GENERIC)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> FileWorkspace.create(JIM_FS.getPath("/"), null)).isInstanceOf(NullPointerException.class);
        {
            Path generic = Files.createTempFile(JIM_FS.getPath("/"), "ws_", "xml");

            assertThatThrownBy(() -> FileWorkspace.create(generic, FileFormat.GENERIC)).isInstanceOf(IOException.class);

            Files.delete(generic);
            try (FileWorkspace ws = FileWorkspace.create(generic, FileFormat.GENERIC)) {
                assertThat(ws.getItems()).isEmpty();
            }
            try (FileWorkspace ws = FileWorkspace.open(generic, FileFormat.GENERIC)) {
                assertThat(ws.getItems()).isEmpty();
            }
        }
        {
            Path legacy = Files.createTempFile(JIM_FS.getPath("/"), "ws_", "xml");

            assertThatThrownBy(() -> FileWorkspace.create(legacy, FileFormat.LEGACY)).isInstanceOf(IOException.class);

            Files.delete(legacy);
            try (FileWorkspace ws = FileWorkspace.create(legacy, FileFormat.LEGACY)) {
                assertThat(ws.getItems()).isEmpty();
            }
            try (FileWorkspace ws = FileWorkspace.open(legacy, FileFormat.LEGACY)) {
                assertThat(ws.getItems()).isEmpty();
            }
        }
    }

    @Test
    public void testCopyTo() throws IOException {
        Path newFile = JIM_FS.getPath("/copied.xml");
        try (Workspace source = FileWorkspace.open(GENERIC_INDEX); Workspace target = FileWorkspace.create(newFile, FileFormat.GENERIC)) {
            source.copyTo(target);
        }
        try (FileWorkspace ws = FileWorkspace.open(newFile)) {
            assertThat(ws.getName()).isEqualTo("my_workspace");
            assertThat(ws.getFileFormat()).isEqualTo(FileFormat.GENERIC);
            assertThat(ws.getFile()).isEqualTo(newFile);
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

    private static FileSystem JIM_FS;

    @BeforeClass
    public static void beforeClass() {
        JIM_FS = Jimfs.newFileSystem(Configuration.unix());
    }

    @AfterClass
    public static void afterClass() throws IOException {
        JIM_FS.close();
    }
}
