package demetra.util;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@lombok.experimental.UtilityClass
public class Collections2 {

    public static <T> @NonNull Stream<T> streamOf(@NonNull Iterable<T> iterable) {
        return iterable instanceof Collection
                ? ((Collection) iterable).stream()
                : StreamSupport.stream(iterable.spliterator(), false);
    }
}
