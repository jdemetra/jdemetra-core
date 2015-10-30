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
package ec.tstoolkit.utilities;

import com.google.common.base.Joiner;
import com.google.common.io.Files;
import java.io.File;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Provides utility methods for the {@link File} class;
 *
 * @author Jean Palate
 * @author Philippe Charles
 */
public final class Files2 {

    private Files2() {
        // static class
    }

    public static File fromPath(String parent, String... path) {
        return new File(parent, Joiner.on(File.separatorChar).join(path));
    }

    public static boolean isValidPath(File file) {
        try {
            file.getCanonicalPath();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Nullable
    public static File getAbsoluteFile(@Nonnull File[] paths, @Nonnull File file) {
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
    public static File getRelativeFile(@Nonnull File[] paths, @Nonnull File file) {
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

    public static boolean acceptByLowerCaseExtension(File pathname, String... lowerCaseExtensions) {
        String ext = Files.getFileExtension(pathname.getName()).toLowerCase(Locale.ENGLISH);
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
    public static File extractFile(@Nonnull String path) {
        File file = new File(path);
        if (file.isFile()) {
            return file;
        }
        while ((file = file.getParentFile()) != null && !file.exists()) {
        }
        return file;
    }
}
