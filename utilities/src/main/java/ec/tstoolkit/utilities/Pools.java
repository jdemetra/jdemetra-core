/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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

package ec.tstoolkit.utilities;

import com.google.common.base.Throwables;
import java.util.Deque;
import java.util.LinkedList;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
public final class Pools {

    private Pools() {
        // static class
    }

    @Nonnull
    public static <X> IPool<X> on(@Nonnull Class<? extends X> clazz, int maxPoolSize) {
        return on(new BasicFactory<>(clazz), maxPoolSize);
    }

    @Nonnull
    public static <X> IPool<X> on(@Nonnull IPool.Factory<X> factory, int maxPoolSize) {
        return new BasicPool<>(factory, maxPoolSize);
    }

    static class BasicPool<T> implements IPool<T> {

        final Factory<T> factory;
        final int maxPoolSize;
        final Deque<T> data;
        int wildCount;

        BasicPool(@Nonnull Factory<T> factory, int maxPoolSize) {
            this.factory = factory;
            this.maxPoolSize = maxPoolSize;
            this.data = new LinkedList<>();
            wildCount = 0;
        }

        @Override
        public T getOrCreate() {
            wildCount++;
            T result = data.pollLast();
            return result != null ? result : factory.create();
        }

        @Override
        public void recycle(T o) {
            if (data.size() < maxPoolSize) {
                factory.reset(o);
                data.offerLast(o);
            } else {
                factory.destroy(o);
            }
            wildCount--;
        }

        @Override
        public void clear() {
            for (T o : data) {
                factory.destroy(o);
            }
            data.clear();
        }
    }

    static class BasicFactory<T> implements IPool.Factory<T> {

        final Class<? extends T> clazz;

        BasicFactory(@Nonnull Class<? extends T> clazz) {
            this.clazz = clazz;
        }

        @Override
        public T create() {
            try {
                return clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                throw Throwables.propagate(ex);
            }
        }

        @Override
        public void reset(T o) {
            // do nothing
        }

        @Override
        public void destroy(T o) {
            // do nothing
        }
    }
}
