package demetra.tsprovider.util;

import demetra.tsprovider.DataSet;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;

@FunctionalInterface
public interface DataSetConversion<T, ID> {

    DataSet.@NonNull Converter<ID> getConverter(@NonNull T obj) throws IOException;
}
