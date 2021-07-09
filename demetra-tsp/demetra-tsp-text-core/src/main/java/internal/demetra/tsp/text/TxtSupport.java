package internal.demetra.tsp.text;

import demetra.timeseries.TsCollection;
import demetra.timeseries.TsInformationType;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.HasDataHierarchy;
import demetra.tsprovider.stream.DataSetTs;
import demetra.tsprovider.stream.HasTsStream;
import demetra.tsprovider.util.DataSourcePreconditions;
import nbbrd.design.ThreadSafe;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@lombok.AllArgsConstructor(staticName = "of")
public class TxtSupport implements HasDataHierarchy, HasTsStream {

    @ThreadSafe
    public interface Resource {

        @NonNull TsCollection getData(@NonNull DataSource dataSource) throws IOException;

        DataSet.@NonNull Converter<Integer> getSeriesParam(@NonNull DataSource dataSource);
    }

    @lombok.NonNull
    private final String providerName;

    @lombok.NonNull
    private final Resource resource;

    @Override
    public @NonNull List<DataSet> children(@NonNull DataSource dataSource) throws IllegalArgumentException, IOException {
        DataSourcePreconditions.checkProvider(providerName, dataSource);

        TsCollection data = resource.getData(dataSource);
        DataSet.Converter<Integer> seriesParam = resource.getSeriesParam(dataSource);

        DataSet.Builder builder = DataSet.builder(dataSource, DataSet.Kind.SERIES);

        return IntStream.range(0, data.length())
                .mapToObj(index -> {
                    seriesParam.set(builder, index);
                    return builder.build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public @NonNull List<DataSet> children(@NonNull DataSet parent) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, parent);
        throw new IllegalArgumentException("Not supported yet.");
    }

    @Override
    public @NonNull Stream<DataSetTs> getData(@NonNull DataSource dataSource, @NonNull TsInformationType type) throws IllegalArgumentException, IOException {
        DataSourcePreconditions.checkProvider(providerName, dataSource);

        TsCollection data = resource.getData(dataSource);
        DataSet.Converter<Integer> seriesParam = resource.getSeriesParam(dataSource);

        return IntStream.range(0, data.length())
                .mapToObj(getMapper(dataSource, data, seriesParam));
    }

    @Override
    public @NonNull Stream<DataSetTs> getData(@NonNull DataSet dataSet, @NonNull TsInformationType type) throws IllegalArgumentException, IOException {
        DataSourcePreconditions.checkProvider(providerName, dataSet.getDataSource());

        TsCollection data = resource.getData(dataSet.getDataSource());
        DataSet.Converter<Integer> seriesParam = resource.getSeriesParam(dataSet.getDataSource());

        return IntStream.range(0, data.length())
                .filter(seriesParam.get(dataSet)::equals)
                .mapToObj(getMapper(dataSet.getDataSource(), data, seriesParam));
    }

    private IntFunction<DataSetTs> getMapper(DataSource dataSource, TsCollection data, DataSet.Converter<Integer> seriesParam) {
        DataSet.Builder builder = DataSet.builder(dataSource, DataSet.Kind.SERIES);
        return index -> {
            seriesParam.set(builder, index);
            return new DataSetTs(builder.build(), data.get(index).getName(), data.get(index).getMeta(), data.get(index).getData());
        };
    }
}
