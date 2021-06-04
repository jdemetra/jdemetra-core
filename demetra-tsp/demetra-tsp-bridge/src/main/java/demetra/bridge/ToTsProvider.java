package demetra.bridge;

import demetra.timeseries.*;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;

public class ToTsProvider implements TsProvider {

    public static @NonNull TsProvider toTsProvider(ec.tss.@NonNull ITsProvider delegate) {
        return delegate instanceof FromTsProvider
                ? ((FromTsProvider) delegate).getDelegate()
                : new ToTsProvider(delegate);
    }

    @lombok.Getter
    @lombok.NonNull
    private final ec.tss.ITsProvider delegate;

    protected ToTsProvider(ec.tss.ITsProvider delegate) {
        this.delegate = delegate;
    }

    @Override
    public void clearCache() {
        getDelegate().clearCache();
    }

    @Override
    public void close() {
        getDelegate().close();
    }

    @Override
    public @NonNull TsCollection getTsCollection(@NonNull TsMoniker moniker, @NonNull TsInformationType type) throws IOException, IllegalArgumentException {
        TsCollectionInformation info = new TsCollectionInformation();
        info.moniker = TsConverter.fromTsMoniker(moniker);
        info.type = TsConverter.fromType(type);
        if (getDelegate().get(info)) {
            return TsConverter.toTsCollectionBuilder(info).build();
        }
        throw new IOException(info.invalidDataCause);
    }

    @Override
    public @NonNull Ts getTs(@NonNull TsMoniker moniker, @NonNull TsInformationType type) throws IOException, IllegalArgumentException {
        TsInformation info = new TsInformation();
        info.moniker = TsConverter.fromTsMoniker(moniker);
        info.type = TsConverter.fromType(type);
        if (getDelegate().get(info)) {
            return TsConverter.toTsBuilder(info).build();
        }
        throw new IOException(info.invalidDataCause);
    }

    @Override
    public @NonNull String getSource() {
        return getDelegate().getSource();
    }

    @Override
    public boolean isAvailable() {
        return getDelegate().isAvailable();
    }
}
