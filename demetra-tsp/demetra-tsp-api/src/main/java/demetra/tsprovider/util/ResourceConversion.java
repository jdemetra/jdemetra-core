package demetra.tsprovider.util;

import demetra.tsprovider.DataSet;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;

@FunctionalInterface
public interface ResourceConversion<RESOURCE, ID> {

    DataSet.@NonNull Converter<ID> getConverter(@NonNull RESOURCE resource) throws IOException;
}
