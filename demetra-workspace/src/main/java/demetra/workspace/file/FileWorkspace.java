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

import demetra.DemetraVersion;
import demetra.workspace.WorkspaceDescriptor;
import demetra.workspace.WorkspaceItemDescriptor;
import demetra.workspace.file.spi.FamilyHandlerLoader;
import internal.workspace.file.FileWorkspaceImpl;
import java.io.IOException;
import java.nio.file.Path;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Defines a kind of workspace that uses files as storage.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
public interface FileWorkspace extends WorkspaceDescriptor {

    @NonNull
    DemetraVersion getVersion() throws IOException;

    @NonNull
    Path getFile() throws IOException;

    @NonNull
    Path getRootFolder() throws IOException;

    @NonNull
    Path getFile(@NonNull WorkspaceItemDescriptor item) throws IOException;

    @NonNull
    static FileWorkspace create(@NonNull Path file) throws IOException {
        return FileWorkspaceImpl.create(file, DemetraVersion.JD3, new FamilyHandlerLoader()::get);
    }

    @NonNull
    static FileWorkspace open(@NonNull Path file) throws IOException {
        return FileWorkspaceImpl.open(file, DemetraVersion.JD3, new FamilyHandlerLoader()::get);
    }

    @NonNull
    static FileWorkspace create(@NonNull Path file, @NonNull DemetraVersion version) throws IOException {
        return FileWorkspaceImpl.create(file, version, new FamilyHandlerLoader()::get);
    }

    @NonNull
    static FileWorkspace open(@NonNull Path file, @NonNull DemetraVersion version) throws IOException {
        return FileWorkspaceImpl.open(file, version, new FamilyHandlerLoader()::get);
    }

}
