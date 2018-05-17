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
import demetra.workspace.WorkspaceItem;
import demetra.workspace.io.IoUtil;
import demetra.workspace.util.Paths;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author Philippe Charles
 */
public final class FileWorkspaceImpl implements FileWorkspace {

    @Nonnull
    public static Optional<FileFormat> probeFormat(@Nonnull Path file) throws IOException {
        if (GenericIndexer.isValid(file)) {
            return Optional.of(FileFormat.GENERIC);
        }
        if (LegacyIndexer.isValid(file)) {
            return Optional.of(FileFormat.LEGACY);
        }
        return Optional.empty();
    }

    @Nonnull
    public static FileWorkspaceImpl create(@Nonnull Path file, @Nonnull FileFormat format, @Nonnull Supplier<Iterable<FamilyHandler>> handlers) throws IOException {
        Objects.requireNonNull(file, "file");
        Objects.requireNonNull(format, "format");
        Objects.requireNonNull(handlers, "handler");
//        if (Files.exists(file)) {
//            throw new FileAlreadyExistsException(file.toString());
//        }

        Path rootFolder = getRootFolder(file);
        Indexer indexer = getIndexer(format, file, rootFolder).memoize();
        indexer.storeIndex(Index.builder().name("").build());

        return of(file, format, rootFolder, indexer, handlers);
    }

    @Nonnull
    public static FileWorkspaceImpl open(@Nonnull Path file, @Nonnull FileFormat format, @Nonnull Supplier<Iterable<FamilyHandler>> handlers) throws IOException {
        Objects.requireNonNull(file, "file");
        Objects.requireNonNull(format, "format");
        Objects.requireNonNull(handlers, "handler");
        if (!Files.exists(file)) {
            throw new NoSuchFileException(file.toString());
        }

        Path rootFolder = getRootFolder(file);
        Indexer indexer = getIndexer(format, file, rootFolder).memoize();
        indexer.loadIndex();

        return of(file, format, rootFolder, indexer, handlers);
    }

    private static FileWorkspaceImpl of(Path indexFile, FileFormat format, Path rootFolder, Indexer indexer, Supplier<Iterable<FamilyHandler>> handlers) throws IOException {
        try {
            return new FileWorkspaceImpl(indexFile, format, rootFolder, indexer, SafeHandler.create(handlers, format));
        } catch (IOException ex) {
            throw IoUtil.ensureClosed(ex, indexer);
        }
    }

    private final Path indexFile;
    private final FileFormat fileFormat;
    private final Path rootFolder;
    private final Indexer indexer;
    private final SafeHandler handlers;

    private FileWorkspaceImpl(Path indexFile, FileFormat fileFormat, Path rootFolder, Indexer indexer, SafeHandler handlers) {
        this.indexFile = indexFile;
        this.fileFormat = fileFormat;
        this.rootFolder = rootFolder;
        this.indexer = indexer;
        this.handlers = handlers;
    }

    @Override
    public String getName() throws IOException {
        return indexer.loadIndex().getName();
    }

    @Override
    public void setName(String name) throws IOException {
        indexer.storeIndex(indexer.loadIndex().withName(name));
    }

    @Override
    public Collection<WorkspaceFamily> getSupportedFamilies() throws IOException {
        return handlers.getSupportedFamilies();
    }

    @Override
    public Collection<WorkspaceItem> getItems() throws IOException {
        Collection<WorkspaceItem> result = new ArrayList<>();
        WorkspaceItem.Builder b = WorkspaceItem.builder();
        indexer.loadIndex().getItems().forEach((k, v) -> result.add(toItem(b, k, v)));
        return result;
    }

    @Override
    public Object load(WorkspaceItem item) throws IOException {
        Index.Key key = toKey(item);

        return handlers.loadValue(key.getFamily(), rootFolder, key.getId());
    }

    @Override
    public void store(WorkspaceItem item, Object value) throws IOException {
        Objects.requireNonNull(value, "value");

        Index.Key key = toKey(item);
        indexer.checkId(key);

        handlers.storeValue(key.getFamily(), rootFolder, key.getId(), value);
        indexer.storeIndex(indexer.loadIndex().withItem(key, toValue(item)));
    }

    @Override
    public void delete(WorkspaceItem item) throws IOException {
        Index.Key key = toKey(item);

        handlers.deleteValue(key.getFamily(), rootFolder, key.getId());
        indexer.storeIndex(indexer.loadIndex().withoutItem(key));
    }

    @Override
    public void close() throws IOException {
        indexer.close();
    }

    @Override
    public FileFormat getFileFormat() throws IOException {
        return fileFormat;
    }

    @Override
    public Path getFile() throws IOException {
        return indexFile;
    }

    @Override
    public Path getRootFolder() throws IOException {
        return rootFolder;
    }

    @Override
    public Path getFile(WorkspaceItem item) throws IOException {
        Index.Key key = toKey(item);

        return handlers.resolveFile(key.getFamily(), rootFolder, key.getId());
    }

    static WorkspaceItem toItem(WorkspaceItem.Builder b, Index.Key k, Index.Value v) {
        return b
                .family(k.getFamily())
                .id(k.getId())
                .label(v.getLabel())
                .readOnly(v.isReadOnly())
                .comments(v.getComments())
                .build();
    }

    static Index.Key toKey(WorkspaceItem item) {
        return new Index.Key(item.getFamily(), item.getId());
    }

    static Index.Value toValue(WorkspaceItem item) {
        return new Index.Value(item.getLabel(), item.isReadOnly(), item.getComments());
    }

    static Path getRootFolder(Path indexFile) throws IOException {
        Path parent = indexFile.toAbsolutePath().getParent();
        if (parent == null) {
            throw new IOException();
        }
        return parent.resolve(Paths.changeExtension(indexFile.getFileName().toString(), null));
    }

    private static Indexer getIndexer(FileFormat format, Path file, Path rootFolder) {
        switch (format) {
            case GENERIC:
                return new GenericIndexer(file, rootFolder);
            case LEGACY:
                return new LegacyIndexer(file);
            default:
                throw new RuntimeException();
        }
    }
}
