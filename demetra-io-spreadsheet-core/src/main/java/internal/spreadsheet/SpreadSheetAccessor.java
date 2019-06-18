/*
 * Copyright 2018 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package internal.spreadsheet;

import demetra.tsprovider.TsCollection;
import ioutil.IO;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;
import javax.cache.Cache;

/**
 *
 * @author Philippe Charles
 */
public interface SpreadSheetAccessor extends Closeable {

    @NonNull
    Optional<TsCollection> getSheetByName(@NonNull String name) throws IOException;

    @NonNull
    List<String> getSheetNames() throws IOException;

    @NonNull
    List<TsCollection> getSheets() throws IOException;

    @NonNull
    default SpreadSheetAccessor withCache(@NonNull Cache cache) {
        SpreadSheetAccessor delegate = this;
        return new SpreadSheetAccessor() {
            @Override
            public Optional<TsCollection> getSheetByName(String name) throws IOException {
                Objects.requireNonNull(name);

                List<TsCollection> all = peek("getSheets");
                if (all != null) {
                    return all.stream().filter(o -> o.getName().equals(name)).findFirst();
                }

                return load("getSheetByName/" + name, () -> delegate.getSheetByName(name));
            }

            @Override
            public List<String> getSheetNames() throws IOException {
                List<TsCollection> all = peek("getSheets");
                if (all != null) {
                    return all.stream().map(TsCollection::getName).collect(Collectors.toList());
                }

                return load("getSheetNames", delegate::getSheetNames);
            }

            @Override
            public List<TsCollection> getSheets() throws IOException {
                return load("getSheets", delegate::getSheets);
            }

            @Override
            public void close() throws IOException {
                IO.closeBoth(cache, delegate);
            }

            private <T> T peek(String key) {
                return (T) cache.get(key);
            }

            private <T> T load(String key, IO.Supplier<T> loader) throws IOException {
                T result = (T) cache.get(key);
                if (result == null) {
                    result = loader.getWithIO();
                    cache.put(key, result);
                }
                return result;
            }
        };
    }
}
