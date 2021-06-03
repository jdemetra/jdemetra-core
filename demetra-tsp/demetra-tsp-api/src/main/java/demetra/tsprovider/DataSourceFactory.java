package demetra.tsprovider;

import demetra.io.Files2;
import demetra.timeseries.*;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public interface DataSourceFactory extends TsFactory {

    @NonNull
    default <T extends DataSourceProvider> Optional<T> getProvider(@NonNull Class<T> type, @NonNull String name) {
        return getProvider(name)
                .filter(type::isInstance)
                .map(type::cast);
    }

    @NonNull
    default <T extends DataSourceProvider> Optional<T> getProvider(@NonNull Class<T> type, @NonNull DataSource dataSource) {
        return getProvider(type, dataSource.getProviderName());
    }

    @NonNull
    default <T extends DataSourceProvider> Optional<T> getProvider(@NonNull Class<T> type, @NonNull DataSet dataSet) {
        return getProvider(type, dataSet.getDataSource());
    }

    @NonNull
    default <T extends DataSourceProvider> Optional<T> getProvider(@NonNull Class<T> type, @NonNull TsMoniker moniker) {
        return getProvider(type, moniker.getSource());
    }

    @NonNull
    default Optional<File> getFile(@NonNull DataSource dataSource) {
        return getProvider(FileLoader.class, dataSource)
                .map(loader -> {
                    File file = loader.decodeBean(dataSource).getFile();
                    return Files2.getAbsoluteFile(loader.getPaths(), file);
                });
    }

    @NonNull
    default Optional<TsCollection> getTsCollection(@NonNull DataSource dataSource, @NonNull TsInformationType info) {
        return getProvider(DataSourceProvider.class, dataSource)
                .map(provider -> {
                    TsMoniker moniker = provider.toMoniker(dataSource);
                    try {
                        return provider.getTsCollection(moniker, info);
                    } catch (IOException ex) {
                        return makeTsCollection(moniker, ex);
                    }
                });
    }

    @NonNull
    default Optional<TsCollection> getTsCollection(@NonNull DataSet dataSet, @NonNull TsInformationType info) {
        return getProvider(DataSourceProvider.class, dataSet)
                .map(provider -> {
                    switch (dataSet.getKind()) {
                        case COLLECTION: {
                            TsMoniker moniker = provider.toMoniker(dataSet);
                            try {
                                return provider.getTsCollection(moniker, info);
                            } catch (IOException ex) {
                                return makeTsCollection(moniker, ex);
                            }
                        }
                        case SERIES: {
                            TsMoniker moniker = provider.toMoniker(dataSet);
                            try {
                                return TsCollection.of(provider.getTs(moniker, info));
                            } catch (IOException ex) {
                                return TsCollection.of(makeTs(moniker, ex));
                            }
                        }
                        case DUMMY:
                            return TsCollection.builder().name(provider.getDisplayName(dataSet)).build();
                        default:
                            throw new RuntimeException("Not implemented");
                    }
                });
    }

    @NonNull
    default Optional<Ts> getTs(@NonNull DataSet dataSet, @NonNull TsInformationType info) {
        return getProvider(DataSourceProvider.class, dataSet)
                .map(provider -> {
                    switch (dataSet.getKind()) {
                        case SERIES: {
                            TsMoniker moniker = provider.toMoniker(dataSet);
                            try {
                                return provider.getTs(moniker, info);
                            } catch (IOException ex) {
                                return makeTs(moniker, ex);
                            }
                        }
                        default:
                            throw new RuntimeException("Not implemented");
                    }
                });
    }
}
