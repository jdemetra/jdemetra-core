/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
package demetra.io;

import java.io.File;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Provides utility methods for the {@link File} class;
 *
 * @author Jean Palate
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class Files2 {

    public File fromPath(String parent, String... path) {
        return new File(parent, Stream.of(path).collect(Collectors.joining(File.separator)));
    }

    @Nullable
    public File getAbsoluteFile(@Nonnull File[] paths, @Nonnull File file) {
        if (file.isAbsolute()) {
            return file;
        }
        for (File parent : paths) {
            File result = new File(parent, file.getPath());
            if (result.exists()) {
                return result;
            }
        }
        // relative file outside paths
        return null;
    }

    @Nullable
    public File getRelativeFile(@Nonnull File[] paths, @Nonnull File file) {
        if (!file.isAbsolute()) {
            return file;
        }
        String path = file.getAbsolutePath();
        for (File parent : paths) {
            String parentPath = parent.getAbsolutePath() + File.separator;
            if (path.startsWith(parentPath)) {
                return new File(path.substring(parentPath.length()));
            }
        }
        // absolute file outside paths
        return null;
    }

    public String getFileExtension(File file) {
        Objects.requireNonNull(file);
        String name = file.getName();
        int index = name.lastIndexOf('.');
        return index != -1 ? name.substring(index + 1) : "";
    }

    public boolean acceptByLowerCaseExtension(File pathname, String... lowerCaseExtensions) {
        String ext = getFileExtension(pathname).toLowerCase(Locale.ENGLISH);
        for (String o : lowerCaseExtensions) {
            if (ext.equals(o)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tries to get an existing file from a path
     *
     * @param path
     * @return
     */
    @Nullable
    public File extractFile(@Nonnull String path) {
        File file = new File(path);
        if (file.isFile()) {
            return file;
        }
        while ((file = file.getParentFile()) != null && !file.exists()) {
        }
        return file;
    }
}
