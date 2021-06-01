/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package demetra.timeseries;

import nbbrd.design.StaticFactoryMethod;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author PALATEJ
 */
@lombok.Builder
public class TsFactory {

    public static final String
            DESCRIPTION = "@description", OWNER = "@owner",
            SOURCE = "@source", ID = "@id", DATE = "@timestamp",
            DOCUMENT = "@document", SUMMARY = "@summary",
            NOTE = "@note", TODO = "@todo",
            ALGORITHM = "@algorithm",
            QUALITY = "@quality";

    @lombok.NonNull
    private final Supplier<List<TsProvider>> providers;

    @lombok.NonNull
    private final BiConsumer<? super String, ? super IOException> onIOException;

    @StaticFactoryMethod
    public static @NonNull TsFactory ofServiceLoader() {
        return builder()
                .providers(new TsProviderLoader()::get)
                .build();
    }

    public static @NonNull Builder builder() {
        return new Builder()
                .providers(Collections::emptyList)
                .onIOException(TsFactory::logError);
    }

    public @NonNull Ts makeTs(@NonNull TsMoniker moniker, @NonNull TsInformationType info) {
        Optional<TsProvider> provider = getProvider(moniker);
        if (provider.isPresent()) {
            try {
                return provider.get().getTs(moniker, info);
            } catch (IOException ex) {
                onIOException.accept("", ex);
                return fallbackTs(moniker, TsData.empty(ex.getMessage()));
            }
        }
        Objects.requireNonNull(info);
        return fallbackTs(moniker, TS_DATA_NOT_FOUND);
    }

    public @NonNull TsCollection makeTsCollection(@NonNull TsMoniker moniker, @NonNull TsInformationType info) {
        Optional<TsProvider> provider = getProvider(moniker);
        if (provider.isPresent()) {
            try {
                return provider.get().getTsCollection(moniker, info);
            } catch (IOException ex) {
                onIOException.accept("", ex);
                return fallbackTsCollection(moniker, ex.getMessage());
            }
        }
        Objects.requireNonNull(info);
        return fallbackTsCollection(moniker, PROVIDER_NOT_FOUND);
    }

    private Optional<TsProvider> getProvider(TsMoniker moniker) {
        return providers.get()
                .stream()
                .filter(provider -> provider.getSource().equals(moniker.getSource()))
                .findFirst();
    }

    private Ts fallbackTs(TsMoniker moniker, TsData data) {
        return Ts.builder().moniker(moniker).type(TsInformationType.None).data(data).build();
    }

    private TsCollection fallbackTsCollection(TsMoniker moniker, String emptyMessage) {
        return TsCollection.builder().moniker(moniker).type(TsInformationType.None).emptyCause(emptyMessage).build();
    }

    private static final String PROVIDER_NOT_FOUND = "Provider not found";
    private static final TsData TS_DATA_NOT_FOUND = TsData.empty(PROVIDER_NOT_FOUND);

    private static void logError(String msg, IOException ex) {
        Logger.getLogger(TsFactory.class.getName()).log(Level.SEVERE, msg, ex);
    }
}
