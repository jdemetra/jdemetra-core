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
package ec.tss.tsproviders.utils;

import ec.tss.tsproviders.HasFilePaths;
import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Supporting class for {@link HasFilePaths}.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@ThreadSafe
public final class FilePathSupport implements HasFilePaths {

    /**
     * Creates a new instance of this class.
     *
     * @return a non-null instance
     */
    @Nonnull
    public static FilePathSupport of() {
        return new FilePathSupport(() -> {
            // do nothing
        });
    }

    /**
     * Creates a new instance of this class with a callback.
     *
     * @param onPathsChange a non-null callback to be notified of paths change
     * @return a non-null instance
     */
    @Nonnull
    public static FilePathSupport of(@Nonnull Runnable onPathsChange) {
        Objects.requireNonNull(onPathsChange);
        return new FilePathSupport(onPathsChange);
    }

    private static final File[] EMPTY = new File[0];

    private final Runnable onPathsChange;
    private final AtomicReference<File[]> paths;

    private FilePathSupport(Runnable onPathsChange) {
        this.onPathsChange = Objects.requireNonNull(onPathsChange);
        this.paths = new AtomicReference<>(EMPTY);
    }

    //<editor-fold defaultstate="collapsed" desc="HasFilePaths">
    @Override
    public void setPaths(@Nullable File[] paths) {
        File[] newValue = paths != null ? paths.clone() : EMPTY;
        if (!Arrays.equals(this.paths.getAndSet(newValue), newValue)) {
            onPathsChange.run();
        }
    }

    @Nonnull
    @Override
    public File[] getPaths() {
        return paths.get().clone();
    }
    //</editor-fold>
}
