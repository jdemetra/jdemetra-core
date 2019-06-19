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
package ec.demetra.workspace.file.spi;

import ec.demetra.workspace.WorkspaceFamily;
import ec.demetra.workspace.file.FileFormat;
import ec.tstoolkit.design.ServiceDefinition;
import java.io.IOException;
import java.nio.file.Path;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Defines an extension point for FileWorkspace that allows it to deal with new
 * kind of data.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@ServiceDefinition
public interface FamilyHandler {

    @NonNull
    WorkspaceFamily getFamily();

    @NonNull
    FileFormat getFormat();

    @NonNull
    Path resolveFile(@NonNull Path root, @NonNull String fileName);

    @NonNull
    Object read(@NonNull Path root, @NonNull String fileName) throws IOException;

    void write(@NonNull Path root, @NonNull String fileName, @NonNull Object value) throws IOException;
}
