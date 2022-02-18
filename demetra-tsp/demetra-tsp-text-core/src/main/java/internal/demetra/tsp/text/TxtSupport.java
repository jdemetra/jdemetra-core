package internal.demetra.tsp.text;

import demetra.timeseries.TsCollection;
import demetra.timeseries.TsInformationType;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.HasDataHierarchy;
import demetra.tsprovider.stream.DataSetTs;
import demetra.tsprovider.stream.HasTsStream;
import demetra.tsprovider.util.ImmutableValueFactory;
import demetra.tsprovider.util.DataSourcePreconditions;
import demetra.tsprovider.util.DataSetConversion;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@lombok.AllArgsConstructor(staticName = "of")
public final class TxtSupport implements HasDataHierarchy, HasTsStream {

    @lombok.NonNull
    private final String providerName;

    @lombok.NonNull
    private final ImmutableValueFactory<TsCollection> txt;

    @lombok.NonNull
    private final DataSetConversion<TsCollection, Integer> seriesIndex;

    @Override
    public @NonNull List<DataSet> children(@NonNull DataSource dataSource) throws IllegalArgumentException, IOException {
        DataSourcePreconditions.checkProvider(providerName, dataSource);

        TsCollection data = txt.load(dataSource);
        DataSet.Converter<Integer> seriesParam = seriesIndex.getConverter(data);

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

        TsCollection data = txt.load(dataSource);
        DataSet.Converter<Integer> seriesParam = seriesIndex.getConverter(data);

        return IntStream.range(0, data.length())
                .mapToObj(getMapper(dataSource, data, seriesParam));
    }

    @Override
    public @NonNull Stream<DataSetTs> getData(@NonNull DataSet dataSet, @NonNull TsInformationType type) throws IllegalArgumentException, IOException {
        DataSourcePreconditions.checkProvider(providerName, dataSet.getDataSource());

        TsCollection data = txt.load(dataSet.getDataSource());
        DataSet.Converter<Integer> seriesParam = seriesIndex.getConverter(data);

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
