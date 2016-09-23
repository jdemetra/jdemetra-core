/*
 * Copyright 2016 National Bank of Belgium
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
package ec.tss.tsproviders;

import ec.tstoolkit.utilities.Files2;
import java.io.File;
import java.io.FileNotFoundException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Defines the ability to resolve a relative path by using a list of folders.
 * Note that the implementations must be thread-safe.
 *
 * @author Philippe Charles
 * @since 2.0.0
 */
@ThreadSafe
public interface HasFilePaths {

    /**
     * Sets the list of folders where a relative file could be found. Note that
     * order matters and null list is transformed to empty list.
     *
     * @param paths a nullable list of files
     */
    void setPaths(@Nullable File[] paths);

    /**
     * Gets the list of folders where a relative file could be found.
     *
     * @return a non-null list of files
     */
    @Nonnull
    File[] getPaths();

    /**
     * Resolves the absolute path of a file from a relative one.
     *
     * @param file a non-null file
     * @return a non-null file
     * @throws FileNotFoundException if the path cannot be resolved
     */
    @Nonnull
    default File resolveFilePath(@Nonnull File file) throws FileNotFoundException {
        File result = Files2.getAbsoluteFile(getPaths(), file);
        if (result == null) {
            throw new FileNotFoundException("Relative file '" + file.getPath() + "' outside paths");
        }
        if (!result.exists()) {
            throw new FileNotFoundException(result.getPath());
        }
        return result;
    }
}
