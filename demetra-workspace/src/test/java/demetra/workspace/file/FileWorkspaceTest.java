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
package demetra.workspace.file;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import demetra.regarima.RegArimaSpec;
import demetra.sa.SaItems;
import demetra.timeseries.TsDocument;
import demetra.timeseries.calendars.CalendarManager;
import demetra.timeseries.regression.TsDataSuppliers;
import demetra.tramo.TramoSpec;
import demetra.tramoseats.TramoSeatsSpec;
import demetra.workspace.Workspace;
import demetra.workspace.WorkspaceItem;
import demetra.x13.X13Spec;
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
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        assertThatThrownBy(() -> FileWorkspace.probeFormat(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testOpenWithProbe() throws IOException {
        try (FileWorkspace ws = FileWorkspace.open(GENERIC_INDEX)) {
            assertThat(ws.getFileFormat()).isEqualTo(FileFormat.GENERIC);
            assertThat(ws.getFile()).isEqualTo(GENERIC_INDEX);
            assertThat(ws.getRootFolder()).isEqualTo(GENERIC_ROOT);
        }
        assertThatThrownBy(() -> FileWorkspace.open(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testOpenWithFormat() throws IOException {
        try (FileWorkspace ws = FileWorkspace.open(GENERIC_INDEX, FileFormat.GENERIC)) {
            assertThat(ws.getFileFormat()).isEqualTo(FileFormat.GENERIC);
            assertThat(ws.getFile()).isEqualTo(GENERIC_INDEX);
            assertThat(ws.getRootFolder()).isEqualTo(GENERIC_ROOT);
            ws.getItems().forEach(v -> {
                try {
                    ws.load(v);
                } catch (IOException ex) {
                    Logger.getLogger(FileWorkspaceTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }
        assertThatThrownBy(() -> FileWorkspace.open(null, FileFormat.LEGACY)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> FileWorkspace.open(GENERIC_INDEX, null)).isInstanceOf(NullPointerException.class);
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

            assertThat(ws.load(GENERIC_SA_MULTI)).isInstanceOf(SaItems.class);
            assertThat(ws.load(GENERIC_SA_DOC_X13)).isInstanceOf(TsDocument.class);
            assertThat(ws.load(GENERIC_SA_DOC_TRAMOSEATS)).isInstanceOf(TsDocument.class);
            assertThat(ws.load(GENERIC_SA_SPEC_X13)).isInstanceOf(X13Spec.class);
            assertThat(ws.load(GENERIC_SA_SPEC_TRAMOSEATS)).isInstanceOf(TramoSeatsSpec.class);

            assertThat(ws.load(GENERIC_MOD_DOC_REGARIMA)).isInstanceOf(TsDocument.class);
            assertThat(ws.load(GENERIC_MOD_DOC_TRAMO)).isInstanceOf(TsDocument.class);
            assertThat(ws.load(GENERIC_MOD_SPEC_REGARIMA)).isInstanceOf(RegArimaSpec.class);
            assertThat(ws.load(GENERIC_MOD_SPEC_TRAMO)).isInstanceOf(TramoSpec.class);

            assertThat(ws.load(GENERIC_UTIL_CAL)).isInstanceOf(CalendarManager.class);
            assertThat(ws.load(GENERIC_UTIL_VAR)).isInstanceOf(TsDataSuppliers.class);
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

    public static void main(String[] arg) throws IOException {
        Path path=FileSystems.getDefault().getPath("c:\\sarepository", "test.xml");
        FileWorkspace ws = FileWorkspace.open(path);
        for (WorkspaceItem item : ws.getItems()) {
            assertThat(ws.getFile(item)).exists();
            Object rslt = ws.load(item);
            assertThat(rslt != null);
            System.out.println(item.getLabel());
        }
    }
}
