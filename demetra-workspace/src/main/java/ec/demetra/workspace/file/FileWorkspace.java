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

import internal.workspace.file.FileWorkspaceImpl;
import ec.demetra.workspace.Workspace;
import ec.demetra.workspace.WorkspaceItem;
import internal.workspace.file.spi.FamilyHandlerLoader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Defines a kind of workspace that uses files as storage.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
public interface FileWorkspace extends Workspace {

    @NonNull
    FileFormat getFileFormat() throws IOException;

    @NonNull
    Path getFile() throws IOException;

    @NonNull
    Path getRootFolder() throws IOException;

    @NonNull
    Path getFile(@NonNull WorkspaceItem item) throws IOException;

    @NonNull
    static FileWorkspace create(@NonNull Path file, @NonNull FileFormat format) throws IOException {
        return FileWorkspaceImpl.create(file, format, new FamilyHandlerLoader()::get);
    }

    @NonNull
    static FileWorkspace open(@NonNull Path file) throws IOException {
        return open(file, probeFormat(file).orElseThrow(() -> new IOException("Cannot probe workspace file format of '" + file + "'")));
    }

    @NonNull
    static FileWorkspace open(@NonNull Path file, @NonNull FileFormat format) throws IOException {
        return FileWorkspaceImpl.open(file, format, new FamilyHandlerLoader()::get);
    }

    @NonNull
    static Optional<FileFormat> probeFormat(@NonNull Path file) throws IOException {
        return FileWorkspaceImpl.probeFormat(file);
    }
}
