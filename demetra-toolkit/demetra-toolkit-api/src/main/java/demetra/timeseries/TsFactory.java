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

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.util.List;
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

    @NonNull
    public static TsFactory ofServiceLoader() {
        return TsFactory
                .builder()
                .providers(new TsProviderLoader()::get)
                .onIOException(TsFactory::logError)
                .build();
    }

    @lombok.NonNull
    private final Supplier<List<TsProvider>> providers;

    @lombok.NonNull
    private final BiConsumer<? super String, ? super IOException> onIOException;

    public Ts makeTs(TsMoniker moniker, TsInformationType info) {
        Optional<TsProvider> provider = getProvider(moniker);
        if (provider.isPresent()) {
            try {
                return provider.get().getTs(moniker, info);
            } catch (IOException ex) {
                onIOException.accept("", ex);
                return Ts.builder().moniker(moniker).data(TsData.empty(ex.getMessage())).build();
            }
        }
        return Ts.builder().moniker(moniker).data(TsData.empty("Provider not found")).build();
    }

    public TsCollection makeTsCollection(TsMoniker moniker, TsInformationType info) {
        Optional<TsProvider> provider = getProvider(moniker);
        if (provider.isPresent()) {
            try {
                return provider.get().getTsCollection(moniker, info);
            } catch (IOException ex) {
                onIOException.accept("", ex);
            }
        }
        return TsCollection.builder().moniker(moniker).build();
    }

    private Optional<TsProvider> getProvider(TsMoniker moniker) {
        return providers.get()
                .stream()
                .filter(provider -> provider.getSource().equals(moniker.getSource()))
                .findFirst();
    }

    private static void logError(String msg, IOException ex) {
        Logger.getLogger(TsFactory.class.getName()).log(Level.SEVERE, msg, ex);
    }
}
