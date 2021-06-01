package internal.timeseries;

import demetra.timeseries.*;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Optional;

@lombok.Builder
public class DefaultTsFactory implements TsFactory {

    @lombok.Singular
    private final List<TsProvider> providers;

    @Override
    public @NonNull Optional<TsProvider> getProvider(@NonNull String name) {
        return providers
                .stream()
                .filter(provider -> provider.getSource().equals(name))
                .findFirst();
    }

    public static Ts fallbackTs(TsMoniker moniker, TsData data) {
        return Ts.builder().moniker(moniker).type(TsInformationType.None).data(data).build();
    }

    public static TsCollection fallbackTsCollection(TsMoniker moniker, String emptyMessage) {
        return TsCollection.builder().moniker(moniker).type(TsInformationType.None).emptyCause(emptyMessage).build();
    }
}
