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
import demetra.workspace.file.FileFormat;
import java.util.Objects;

/**
 *
 * @author Philippe Charles
 */
public final class QuickHandler implements FamilyHandler {

    private final WorkspaceFamily family;
    private final FileFormat format;

    @lombok.experimental.Delegate
    private final FileSupport fileSupport;

    public QuickHandler(WorkspaceFamily family, FileFormat format, FileSupport fileSupport) {
        this.family = Objects.requireNonNull(family, "family");
        this.format = Objects.requireNonNull(format, "format");
        this.fileSupport = Objects.requireNonNull(fileSupport, "fileSupport");
    }

    @Override
    public WorkspaceFamily getFamily() {
        return family;
    }

    @Override
    public FileFormat getFormat() {
        return format;
    }
}
