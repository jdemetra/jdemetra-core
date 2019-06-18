/*
 * Copyright 2016 National Bank of Belgium
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
package demetra.io;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
public final class ResourceWatcher {

    private final List<Id> resources;
    private int cpt;

    public ResourceWatcher() {
        this.resources = new ArrayList<>();
        cpt = 0;
    }

    public boolean isLeaking() {
        return !resources.isEmpty();
    }

    @NonNull
    public Id watch(@NonNull String name) {
        Objects.requireNonNull(name);
        Id result = new Id(name, cpt++);
        resources.add(result);
        return result;
    }

    public void unwatch(@NonNull Id id) {
        Objects.requireNonNull(id);
        if (!resources.remove(id)) {
            throw new IllegalArgumentException(id.toString());
        }
    }

    @NonNull
    public Closeable watchAsCloseable(@NonNull String name) {
        Id id = watch(name);
        return () -> unwatch(id);
    }

    @lombok.Value
    public static class Id {

        String name;
        int cpt;
    }
}
