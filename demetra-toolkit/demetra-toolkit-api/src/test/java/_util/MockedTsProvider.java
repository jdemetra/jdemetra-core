package _util;

import demetra.timeseries.*;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

@lombok.Builder
public class MockedTsProvider implements TsProvider {

    public static final String NAME = "Mocked";

    @lombok.Singular
    final List<TsCollection> tsCollections;

    @lombok.Singular("ts")
    final List<Ts> tss;

    @Override
    public void clearCache() {
    }

    @Override
    public void close() {
    }

    @Override
    public @NonNull TsCollection getTsCollection(@NonNull TsMoniker moniker, @NonNull TsInformationType type) throws IllegalArgumentException {
        return tsCollections.stream()
                .filter(col -> col.getMoniker().equals(moniker))
                .map(col -> col.toBuilder().type(type).build())
                .findFirst()
                .orElseGet(() -> TsCollection.builder().moniker(moniker).type(type).build());
    }

    @Override
    public @NonNull Ts getTs(@NonNull TsMoniker moniker, @NonNull TsInformationType type) throws IllegalArgumentException {
        return tss.stream()
                .filter(ts -> ts.getMoniker().equals(moniker))
                .map(ts -> {
                    Ts.Builder result = ts.toBuilder().type(type);
                    if (!type.hasData()) result.data(TsData.empty("Not requested"));
                    return result.build();
                })
                .findFirst()
                .orElseGet(() -> Ts.builder().moniker(moniker).type(type).build());
    }

    @Override
    public @NonNull String getSource() {
        return NAME;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
