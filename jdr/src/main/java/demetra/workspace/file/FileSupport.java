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

import demetra.workspace.WorkspaceFamily;
import java.io.IOException;
import java.nio.file.Path;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
public interface FileSupport {

    @Nonnull
    Path resolveFile(@Nonnull Path root, @Nonnull String fileName);

    @Nonnull
    Object read(@Nonnull Path root, @Nonnull String fileName) throws IOException;

    void write(@Nonnull Path root, @Nonnull String fileName, @Nonnull Object value) throws IOException;

    @Nonnull
    default FamilyHandler asHandler(@Nonnull WorkspaceFamily family, @Nonnull FileFormat format) {
        return new QuickHandler(family, format, this);
    }
}
