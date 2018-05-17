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

import demetra.workspace.Workspace;
import demetra.workspace.WorkspaceItem;
import demetra.workspace.io.IoUtil;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import javax.annotation.Nonnull;

/**
 * Defines a kind of workspace that uses files as storage.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
public interface FileWorkspace extends Workspace {

    @Nonnull
    FileFormat getFileFormat() throws IOException;

    @Nonnull
    Path getFile() throws IOException;

    @Nonnull
    Path getRootFolder() throws IOException;

    @Nonnull
    Path getFile(@Nonnull WorkspaceItem item) throws IOException;

    @Nonnull
    static FileWorkspace create(@Nonnull Path file, @Nonnull FileFormat format) throws IOException {
        return FileWorkspaceImpl.create(file, format, IoUtil.supplierOfServiceLoader(FamilyHandler.class));
    }

    @Nonnull
    static FileWorkspace open(@Nonnull Path file) throws IOException {
        return open(file, probeFormat(file).orElseThrow(() -> new IOException("Cannot probe workspace file format of '" + file + "'")));
    }

    @Nonnull
    static FileWorkspace open(@Nonnull Path file, @Nonnull FileFormat format) throws IOException {
        return FileWorkspaceImpl.open(file, format, IoUtil.supplierOfServiceLoader(FamilyHandler.class));
    }

    @Nonnull
    static Optional<FileFormat> probeFormat(@Nonnull Path file) throws IOException {
        return FileWorkspaceImpl.probeFormat(file);
    }
}
