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

import internal.timeseries.DefaultTsFactory;
import nbbrd.design.StaticFactoryMethod;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author PALATEJ
 */
public interface TsFactory {

    String
            DESCRIPTION = "@description", OWNER = "@owner",
            SOURCE = "@source", ID = "@id", DATE = "@timestamp",
            DOCUMENT = "@document", SUMMARY = "@summary",
            NOTE = "@note", TODO = "@todo",
            ALGORITHM = "@algorithm",
            QUALITY = "@quality";

    @StaticFactoryMethod
    static @NonNull TsFactory ofServiceLoader() {
        return of(TsProviderLoader.load());
    }

    @StaticFactoryMethod
    static @NonNull TsFactory of(@NonNull Iterable<? extends TsProvider> providers) {
        DefaultTsFactory.Builder result = DefaultTsFactory.builder();
        providers.forEach(result::provider);
        return result.build();
    }

    @NonNull Optional<TsProvider> getProvider(@NonNull String name);

    @NonNull Stream<TsProvider> getProviders();

    default @NonNull Ts makeTs(@NonNull TsMoniker moniker, @NonNull TsInformationType info) {
        Optional<TsProvider> provider = getProvider(moniker.getSource());
        if (provider.isPresent()) {
            try {
                return provider.get().getTs(moniker, info);
            } catch (IOException ex) {
                return makeTs(moniker, ex);
            }
        }
        Objects.requireNonNull(info);
        return DefaultTsFactory.fallbackTs(moniker, TsData.empty(PROVIDER_NOT_FOUND));
    }

    default @NonNull Ts makeTs(@NonNull TsMoniker moniker, @NonNull IOException ex) {
        return DefaultTsFactory.fallbackTs(moniker, TsData.empty(ex.getMessage()));
    }

    default @NonNull TsCollection makeTsCollection(@NonNull TsMoniker moniker, @NonNull TsInformationType info) {
        Optional<TsProvider> provider = getProvider(moniker.getSource());
        if (provider.isPresent()) {
            try {
                return provider.get().getTsCollection(moniker, info);
            } catch (IOException ex) {
                return makeTsCollection(moniker, ex);
            }
        }
        Objects.requireNonNull(info);
        return DefaultTsFactory.fallbackTsCollection(moniker, PROVIDER_NOT_FOUND);
    }

    default @NonNull TsCollection makeTsCollection(@NonNull TsMoniker moniker, @NonNull IOException ex) {
        return DefaultTsFactory.fallbackTsCollection(moniker, ex.getMessage());
    }

    String PROVIDER_NOT_FOUND = "Provider not found";
}
