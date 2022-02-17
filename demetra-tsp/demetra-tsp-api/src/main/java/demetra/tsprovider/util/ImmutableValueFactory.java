package demetra.tsprovider.util;

import demetra.tsprovider.DataSource;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;

@FunctionalInterface
public interface ImmutableValueFactory<VALUE> {

    @NonNull VALUE load(@NonNull DataSource dataSource) throws IOException;
}
