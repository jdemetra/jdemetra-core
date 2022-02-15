package demetra.tsprovider.util;

import demetra.tsprovider.DataSource;
import nbbrd.design.ThreadSafe;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.logging.Level;

@ThreadSafe
@lombok.AllArgsConstructor
@lombok.extern.java.Log
public final class ResourcePool<RESOURCE extends Closeable> {

    @FunctionalInterface
    public interface Factory<T extends Closeable> {

        @NonNull
        T create(@NonNull DataSource dataSource) throws IOException;
    }

    @FunctionalInterface
    public interface Wrapper<T extends Closeable> {

        @NonNull
        T wrap(@NonNull T delegate, @NonNull Closeable onClose);
    }

    @NonNull
    public static <T extends Closeable> ResourcePool<T> of(Wrapper<T> wrapper) {
        return new ResourcePool<>(wrapper, new ConcurrentHashMap<>(), ResourcePool::log);
    }

    @lombok.NonNull
    private final Wrapper<RESOURCE> wrapper;

    @lombok.NonNull
    private final ConcurrentMap<DataSource, RESOURCE> resources;

    @lombok.NonNull
    private final Consumer<IOException> onCloseError;

    public @NonNull RESOURCE get(@NonNull DataSource dataSource, @NonNull Factory<RESOURCE> delegate) throws IOException {
        RESOURCE result = resources.remove(dataSource);
        if (result == null) {
            result = delegate.create(dataSource);
        }
        RESOURCE finalResult = result;
        return wrapper.wrap(finalResult, () -> recycle(dataSource, finalResult));
    }

    public void remove(DataSource dataSource) {
        RESOURCE connection = resources.remove(dataSource);
        if (connection != null) {
            closeSilently(connection);
        }
    }

    public void clear() {
        resources.values().forEach(this::closeSilently);
        resources.clear();
    }

    private void recycle(DataSource dataSource, RESOURCE resource) throws IOException {
        if (resources.putIfAbsent(dataSource, resource) != null) {
            resource.close();
        }
    }

    private void closeSilently(RESOURCE resource) {
        try {
            resource.close();
        } catch (IOException ex) {
            onCloseError.accept(ex);
        }
    }

    private static void log(IOException ex) {
        log.log(Level.SEVERE, null, ex);
    }
}
