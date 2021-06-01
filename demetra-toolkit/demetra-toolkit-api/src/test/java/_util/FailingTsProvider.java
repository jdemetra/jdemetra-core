package _util;

import demetra.timeseries.*;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;

public class FailingTsProvider implements TsProvider {

    public static final String NAME = "Failing";

    @Override
    public void clearCache() {
    }

    @Override
    public void close() {
    }

    @Override
    public @NonNull TsCollection getTsCollection(@NonNull TsMoniker moniker, @NonNull TsInformationType type) throws IllegalArgumentException, IOException {
        throw new IOException("Boom");
    }

    @Override
    public @NonNull Ts getTs(@NonNull TsMoniker moniker, @NonNull TsInformationType type) throws IllegalArgumentException, IOException {
        throw new IOException("Boom");
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
