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
package internal.workspace.file;

import demetra.DemetraVersion;
import demetra.util.Paths;
import demetra.workspace.WorkspaceFamily;
import demetra.workspace.WorkspaceItemDescriptor;
import demetra.workspace.WorkspaceItemDescriptor.Key;
import demetra.workspace.file.FileWorkspace;
import demetra.workspace.file.spi.FamilyHandler;
import nbbrd.io.Resource;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 *
 * @author Philippe Charles
 */
public final class FileWorkspaceImpl implements FileWorkspace {

    @NonNull
    public static FileWorkspaceImpl create(@NonNull Path file, @NonNull DemetraVersion version, @NonNull Supplier<Iterable<FamilyHandler>> handlers) throws IOException {
        Objects.requireNonNull(file, "file");
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(handlers, "handler");
        return create(Logger.getLogger(FileWorkspaceImpl.class.getName()), file, version, handlers);
    }

    @NonNull
    public static FileWorkspaceImpl open(@NonNull Path file, @NonNull DemetraVersion version, @NonNull Supplier<Iterable<FamilyHandler>> handlers) throws IOException {
        Objects.requireNonNull(file, "file");
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(handlers, "handler");
        return open(Logger.getLogger(FileWorkspaceImpl.class.getName()), file, version, handlers);
    }

    static FileWorkspaceImpl create(Logger logger, Path file, @NonNull DemetraVersion version, Supplier<Iterable<FamilyHandler>> handlers) throws IOException {
        if (Files.exists(file)) {
            throw new FileAlreadyExistsException(file.toString());
        }

        Path rootFolder = getRootFolder(file);
        Indexer indexer = getIndexer(version, file, rootFolder).memoize();
        indexer.storeIndex(Index.builder().name("").build());

        return of(file, version, rootFolder, indexer, logger, handlers);
    }

    static FileWorkspaceImpl open(Logger logger, Path file, @NonNull DemetraVersion version, Supplier<Iterable<FamilyHandler>> handlers) throws IOException {
        if (!Files.exists(file)) {
            throw new NoSuchFileException(file.toString());
        }

        Path rootFolder = getRootFolder(file);
        Indexer indexer = getIndexer(version, file, rootFolder).memoize();
        indexer.loadIndex();

        return of(file, version, rootFolder, indexer, logger, handlers);
    }

    private static FileWorkspaceImpl of(Path indexFile, @NonNull DemetraVersion version, Path rootFolder, Indexer indexer, Logger logger, Supplier<Iterable<FamilyHandler>> handlers) throws IOException {
        try {
            return new FileWorkspaceImpl(indexFile, version, rootFolder, indexer, SafeHandler.create(logger, handlers, version));
        } catch (IOException ex) {
            Resource.ensureClosed(ex, indexer);
            throw ex;
        }
    }

    private final Path indexFile;
    private final DemetraVersion version;
    private final Path rootFolder;
    private final Indexer indexer;
    private final SafeHandler handlers;

    private FileWorkspaceImpl(Path indexFile, DemetraVersion version, Path rootFolder, Indexer indexer, SafeHandler handlers) {
        this.indexFile = indexFile;
        this.version = version;
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
    public Collection<WorkspaceItemDescriptor> getItems() throws IOException {
        Collection<WorkspaceItemDescriptor> result = new ArrayList<>();
        indexer.loadIndex().getItems().forEach((k, v) -> result.add(new WorkspaceItemDescriptor(k, v)));
        return result;
    }

    @Override
    public Object load(Key key) throws IOException {
        return handlers.loadValue(key.getFamily(), rootFolder, key.getId());
    }

    @Override
    public void store(WorkspaceItemDescriptor item, Object value) throws IOException {
        Objects.requireNonNull(value, "value");

        Key key = item.getKey();
        indexer.checkId(key);

        handlers.storeValue(key.getFamily(), rootFolder, key.getId(), value);
        indexer.storeIndex(indexer.loadIndex().withItem(key, item.getAttributes()));
    }

    @Override
    public void delete(Key key) throws IOException {
        handlers.deleteValue(key.getFamily(), rootFolder, key.getId());
        indexer.storeIndex(indexer.loadIndex().withoutItem(key));
    }

    @Override
    public void close() throws IOException {
        indexer.close();
    }

    @Override
    public DemetraVersion getVersion() throws IOException {
        return version;
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
    public Path getFile(WorkspaceItemDescriptor item) throws IOException {
        Key key = item.getKey();

        return handlers.resolveFile(key.getFamily(), rootFolder, key.getId());
    }

    static Path getRootFolder(Path indexFile) throws IOException {
        Path parent = indexFile.toAbsolutePath().getParent();
        if (parent == null) {
            throw new IOException();
        }
        return parent.resolve(Paths.changeExtension(indexFile.getFileName().toString(), null));
    }

    private static Indexer getIndexer(DemetraVersion version, Path file, Path rootFolder) {
        switch (version) {
            case JD2:
            case JD3:
                return new GenericIndexer(file, rootFolder);
            default:
                throw new RuntimeException();
        }
    }
}
