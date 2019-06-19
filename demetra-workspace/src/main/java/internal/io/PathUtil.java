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
package internal.io;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@lombok.experimental.UtilityClass
public class PathUtil {

    @NonNull
    public Path get(URL url) {
        try {
            return Paths.get(url.toURI());
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void copyDirectory(@NonNull Path sourcePath, @NonNull Path targetPath, @NonNull CopyOption... options) throws IOException {
        Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
            final Function<Path, Path> resolver = getResolver(sourcePath, targetPath);

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Files.createDirectories(resolver.apply(sourcePath.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, resolver.apply(sourcePath.relativize(file)), options);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private Function<Path, Path> getResolver(Path sourcePath, Path targetPath) {
        return isSameFileSystem(sourcePath, targetPath)
                ? (o -> targetPath.resolve(o))
                : (o -> resolvePathOnDifferentFileSystem(targetPath, o));
    }

    private boolean isSameFileSystem(Path l, Path r) {
        return l.getFileSystem().equals(r.getFileSystem());
    }

    private Path resolvePathOnDifferentFileSystem(Path folder, Path relative) {
        Path result = folder;
        for (Path nameElement : relative) {
            result = result.resolve(nameElement.toString());
        }
        return result;
    }
}
