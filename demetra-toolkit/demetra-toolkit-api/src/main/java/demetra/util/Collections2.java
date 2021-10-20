package demetra.util;

import demetra.data.Seq;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@lombok.experimental.UtilityClass
public class Collections2 {

    public <T> @NonNull Stream<T> streamOf(@NonNull Iterable<T> iterable) {
        if (iterable instanceof Collection) return ((Collection) iterable).stream();
        if (iterable instanceof Seq) return ((Seq) iterable).stream();
        return StreamSupport.stream(iterable.spliterator(), false);
    }
    
    public boolean isNullOrEmpty(Collection coll){
        return coll == null || coll.isEmpty();
    }
}
