package internal.demetra.tsp.text;

import demetra.timeseries.Ts;
import demetra.timeseries.TsCollection;
import demetra.timeseries.TsData;
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
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@lombok.AllArgsConstructor(staticName = "of")
public class XmlSupport implements HasDataHierarchy, HasTsStream {

    @ThreadSafe
    public interface Resource {

        @NonNull List<TsCollection> getData(@NonNull DataSource dataSource) throws IOException;

        DataSet.@NonNull Converter<Integer> getCollectionParam(@NonNull DataSource dataSource);

        DataSet.@NonNull Converter<Integer> getSeriesParam(@NonNull DataSource dataSource);
    }

    @lombok.NonNull
    private final String providerName;

    @lombok.NonNull
    private final XmlSupport.Resource resource;

    @Override
    public @NonNull List<DataSet> children(@NonNull DataSource dataSource) throws IllegalArgumentException, IOException {
        DataSourcePreconditions.checkProvider(providerName, dataSource);

        DataSet.Converter<Integer> collectionParam = resource.getCollectionParam(dataSource);

        List<TsCollection> data = resource.getData(dataSource);

        DataSet.Builder builder = DataSet.builder(dataSource, DataSet.Kind.COLLECTION);

        return IntStream.range(0, data.size())
                .mapToObj(index -> {
                    collectionParam.set(builder, index);
                    return builder.build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public @NonNull List<DataSet> children(@NonNull DataSet parent) throws IllegalArgumentException, IOException {
        DataSourcePreconditions.checkProvider(providerName, parent);

        DataSet.Converter<Integer> collectionParam = resource.getCollectionParam(parent.getDataSource());
        DataSet.Converter<Integer> seriesParam = resource.getSeriesParam(parent.getDataSource());

        int collection = collectionParam.get(parent);
        TsCollection data = resource.getData(parent.getDataSource()).get(collection);

        DataSet.Builder builder = DataSet.builder(parent.getDataSource(), DataSet.Kind.SERIES);
        collectionParam.set(builder, collection);

        return IntStream.range(0, data.size())
                .mapToObj(series -> {
                    seriesParam.set(builder, series);
                    return builder.build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public @NonNull Stream<DataSetTs> getData(@NonNull DataSource dataSource, @NonNull TsInformationType type) throws IllegalArgumentException, IOException {
        DataSourcePreconditions.checkProvider(providerName, dataSource);

        DataSet.Converter<Integer> collectionParam = resource.getCollectionParam(dataSource);
        DataSet.Converter<Integer> seriesParam = resource.getSeriesParam(dataSource);

        List<TsCollection> data = resource.getData(dataSource);

        Stream<XmlSeries> result = IntStream.range(0, data.size())
                .mapToObj(Integer::valueOf)
                .flatMap(collection -> IntStream.range(0, data.get(collection).size()).mapToObj(series -> XmlSeries.of(collection, series, data)));

        return result.map(getMapper(dataSource, collectionParam, seriesParam));
    }

    @Override
    public @NonNull Stream<DataSetTs> getData(@NonNull DataSet dataSet, @NonNull TsInformationType type) throws IllegalArgumentException, IOException {
        DataSourcePreconditions.checkProvider(providerName, dataSet.getDataSource());

        DataSet.Converter<Integer> collectionParam = resource.getCollectionParam(dataSet.getDataSource());
        DataSet.Converter<Integer> seriesParam = resource.getSeriesParam(dataSet.getDataSource());

        int collection = collectionParam.get(dataSet);
        List<TsCollection> data = resource.getData(dataSet.getDataSource());

        Stream<XmlSeries> result = IntStream.range(0, data.get(collection).size())
                .filter(getFilter(dataSet, seriesParam))
                .mapToObj(series -> XmlSeries.of(collection, series, data));

        return result.map(getMapper(dataSet.getDataSource(), collectionParam, seriesParam));
    }

    private IntPredicate getFilter(DataSet dataSet, DataSet.Converter<Integer> seriesParam) {
        switch (dataSet.getKind()) {
            case COLLECTION:
                return index -> true;
            case SERIES:
                int series = seriesParam.get(dataSet);
                return index -> index == series;
            default:
                return index -> false;
        }
    }

    private Function<XmlSeries, DataSetTs> getMapper(DataSource dataSource, DataSet.Converter<Integer> collectionParam, DataSet.Converter<Integer> seriesParam) {
        DataSet.Builder builder = DataSet.builder(dataSource, DataSet.Kind.SERIES);
        return xmlSeries -> {
            collectionParam.set(builder, xmlSeries.getCollection());
            seriesParam.set(builder, xmlSeries.getSeries());
            return new DataSetTs(
                    builder.build(),
                    xmlSeries.getName(), xmlSeries.getMeta(), xmlSeries.getData());
        };
    }

    @lombok.Value
    private final static class XmlSeries {

        int collection;
        int series;
        String name;
        Map<String, String> meta;
        TsData data;

        static XmlSeries of(int collectionIndex, int seriesIndex, List<TsCollection> data) {
            TsCollection col = data.get(collectionIndex);
            Ts ts = col.get(seriesIndex);
            return new XmlSeries(collectionIndex, seriesIndex, col.getName() + " - " + ts.getName(), ts.getMeta(), ts.getData());
        }
    }
}
