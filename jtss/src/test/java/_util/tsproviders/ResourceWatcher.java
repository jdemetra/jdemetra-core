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
package _util.tsproviders;

import _util.MightBeMovedFromTestToMain;
import ec.tstoolkit.utilities.Id;
import ec.tstoolkit.utilities.LinearId;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 * @param <ID>
 */
@MightBeMovedFromTestToMain
public interface ResourceWatcher<ID> {

    boolean isLeakingResources();

    @Nonnull
    ID open(@Nonnull String name);

    void close(@Nonnull ID id);

    @Nonnull
    default Closeable watchAsCloseable(@Nonnull String name) {
        ID id = open(name);
        return () -> close(id);
    }

    @Nonnull
    static ResourceWatcher<?> noOp() {
        return NoOpResourceWatcher.INSTANCE;
    }

    @Nonnull
    static ResourceWatcher<Id> usingId() {
        return new IdResourceWatcher();
    }

    static final class NoOpResourceWatcher implements ResourceWatcher<Object> {

        static final NoOpResourceWatcher INSTANCE = new NoOpResourceWatcher();

        @Override
        public boolean isLeakingResources() {
            return false;
        }

        @Override
        public Object open(String name) {
            return new Object();
        }

        @Override
        public void close(Object id) {
        }
    }

    static final class IdResourceWatcher implements ResourceWatcher<Id> {

        private final List<Id> openedResources;
        private int cpt;

        public IdResourceWatcher() {
            this.openedResources = new ArrayList<>();
            cpt = 0;
        }

        @Override
        public boolean isLeakingResources() {
            return !openedResources.isEmpty();
        }

        @Override
        public Id open(String name) {
            Objects.requireNonNull(name);
            Id result = new LinearId(name, "_" + cpt++);
            openedResources.add(result);
            return result;
        }

        @Override
        public void close(Id id) {
            Objects.requireNonNull(id);
            if (!openedResources.remove(id)) {
                throw new IllegalArgumentException(id.toString());
            }
        }
    }
}
