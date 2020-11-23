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
package demetra.tsprovider.util;

import demetra.tsprovider.DataSource;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import nbbrd.io.function.IOFunction;

/**
 *
 * @author Philippe Charles
 * @param <T>
 */
@lombok.extern.java.Log
public final class ResourceMap<T> {

    public static <T> ResourceMap<T> newInstance() {
        return new ResourceMap<>();
    }

    private final Map<DataSource, T> map = new HashMap<>();

    public synchronized void clear() {
        map.forEach((k, v) -> closeSilently(v));
        map.clear();
    }

    public synchronized void remove(DataSource source) {
        T accessor = map.remove(source);
        if (accessor != null) {
            closeSilently(accessor);
        }
    }

    public synchronized T computeIfAbsent(DataSource source, IOFunction<DataSource, T> loader) throws IOException {
        try {
            return map.computeIfAbsent(source, loader.asUnchecked());
        } catch (UncheckedIOException ex) {
            throw ex.getCause();
        }
    }

    private void closeSilently(T o) {
        if (o instanceof AutoCloseable) {
            try {
                ((AutoCloseable) o).close();
            } catch (Exception ex) {
                log.log(Level.SEVERE, null, ex);
            }
        }
    }
}
