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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 * @param <E>
 * @param <T>
 */
public abstract class CheckedIterator<E, T extends Throwable> {

    abstract public boolean hasNext() throws T;

    @Nullable
    abstract public E next() throws T, NoSuchElementException;

    //<editor-fold defaultstate="collapsed" desc="Exhausting methods">
    @Nullable
    public E next(@Nullable E defaultValue) throws T {
        return hasNext() ? next() : defaultValue;
    }

    public int count() throws T {
        int count = 0;
        while (hasNext()) {
            next();
            count++;
        }
        return count;
    }

    public boolean copyInto(@Nonnull Collection<? super E> collection) throws T {
        Objects.requireNonNull(collection);
        boolean wasModified = false;
        while (hasNext()) {
            wasModified |= collection.add(next());
        }
        return wasModified;
    }

    @Nonnull
    public List<E> toList() throws T {
        List<E> result = new ArrayList<>();
        while (hasNext()) {
            result.add(next());
        }
        return result;
    }

    @Nonnull
    public E[] toArray(@Nonnull Class<E> type) throws T {
        return Iterables.toArray(toList(), type);
    }

    @Nonnull
    public <V> Map<E, V> toMap(@Nonnull Function<? super E, V> valueFunc) throws T {
        return toMap(Functions.<E>identity(), valueFunc);
    }

    @Nonnull
    public <K, V> Map<K, V> toMap(@Nonnull Function<? super E, K> keyFunc, @Nonnull Function<? super E, V> valueFunc) throws T {
        requireNonNull(keyFunc, "keyFunc");
        requireNonNull(valueFunc, "valueFunc");
        Map<K, V> result = new LinkedHashMap<>();
        while (hasNext()) {
            E key = next();
            result.put(keyFunc.apply(key), valueFunc.apply(key));
        }
        return result;
    }

    @Nullable
    public E getLast() throws T, NoSuchElementException {
        while (true) {
            E current = next();
            if (!hasNext()) {
                return current;
            }
        }
    }

    @Nullable
    public E getLast(@Nullable E defaultValue) throws T {
        return hasNext() ? getLast() : defaultValue;
    }

    @VisibleForTesting
    int advance(int numberToAdvance) throws T, IllegalArgumentException {
        checkArgument(numberToAdvance >= 0, "numberToAdvance must be nonnegative");
        int i;
        for (i = 0; i < numberToAdvance && hasNext(); i++) {
            next();
        }
        return i;
    }

    @Nullable
    public E get(int position) throws T, IllegalArgumentException, NoSuchElementException {
        advance(position);
        return next();
    }

    @Nullable
    public E get(int position, @Nullable E defaultValue) throws T, IllegalArgumentException {
        advance(position);
        return next(defaultValue);
    }

    public boolean all(@Nonnull Predicate<? super E> predicate) throws T {
        requireNonNull(predicate);
        while (hasNext()) {
            E element = next();
            if (!predicate.apply(element)) {
                return false;
            }
        }
        return true;
    }

    public boolean any(@Nonnull Predicate<? super E> predicate) throws T {
        return indexOf(predicate) != -1;
    }

    public int indexOf(@Nonnull Predicate<? super E> predicate) throws T {
        requireNonNull(predicate, "predicate");
        for (int i = 0; hasNext(); i++) {
            E current = next();
            if (predicate.apply(current)) {
                return i;
            }
        }
        return -1;
    }

    public boolean contains(@Nullable E element) throws T {
        return any(Predicates.equalTo(element));
    }

    public boolean elementsEqual(@Nonnull CheckedIterator<E, T> that) throws T {
        while (this.hasNext()) {
            if (!that.hasNext()) {
                return false;
            }
            Object o1 = this.next();
            Object o2 = that.next();
            if (!com.google.common.base.Objects.equal(o1, o2)) {
                return false;
            }
        }
        return !that.hasNext();
    }

    @Nullable
    public E find(@Nonnull Predicate<? super E> predicate) throws T, NoSuchElementException {
        return filter(predicate).next();
    }

    @Nullable
    public E find(@Nonnull Predicate<? super E> predicate, @Nullable E defaultValue) throws T {
        return filter(predicate).next(defaultValue);
    }

    public int frequency(@Nullable E element) throws T {
        return filter(Predicates.equalTo(element)).count();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="View methods">
    @Nonnull
    public CheckedIterator<E, T> filter(@Nonnull final Predicate<? super E> predicate) {
        requireNonNull(predicate);
        final CheckedIterator<E, T> unfiltered = this;
        return new ACheckedIterator<E, T>() {
            @Override
            protected E computeNext() throws T {
                while (unfiltered.hasNext()) {
                    E element = unfiltered.next();
                    if (predicate.apply(element)) {
                        return element;
                    }
                }
                return endOfData();
            }
        };
    }

    @Nonnull
    public CheckedIterator<E, T> filter(@Nonnull Class<? extends E> type) {
        return filter(Predicates.instanceOf(type));
    }

    @Nonnull
    public CheckedIterator<E, T> skip(final int skipSize) throws IllegalArgumentException {
        checkArgument(skipSize >= 0, "skipSize is negative");
        final CheckedIterator<E, T> iterator = this;
        return new ACheckedIterator<E, T>() {
            private boolean first = true;

            @Override
            protected E computeNext() throws T {
                if (first) {
                    iterator.advance(skipSize);
                    first = false;
                }
                return iterator.hasNext() ? iterator.next() : endOfData();
            }
        };
    }

    @Nonnull
    public CheckedIterator<E, T> limit(final int limitSize) throws IllegalArgumentException {
        checkArgument(limitSize >= 0, "limit is negative");
        final CheckedIterator<E, T> iterator = this;
        return new CheckedIterator<E, T>() {
            private int count;

            @Override
            public boolean hasNext() throws T {
                return count < limitSize && iterator.hasNext();
            }

            @Override
            public E next() throws T {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                count++;
                return iterator.next();
            }
        };
    }

    @Nonnull
    public <NEW> CheckedIterator<NEW, T> transform(@Nonnull final Function<? super E, ? extends NEW> function) {
        requireNonNull(function);
        final CheckedIterator<E, T> iterator = this;
        return new CheckedIterator<NEW, T>() {
            @Override
            public boolean hasNext() throws T {
                return iterator.hasNext();
            }

            @Override
            public NEW next() throws T, NoSuchElementException {
                return function.apply(iterator.next());
            }
        };
    }

    @Nonnull
    public CheckedIterator<E, T> concat(@Nonnull CheckedIterator<E, T> input) {
        return concat(this, input);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Factories">
    @Nonnull
    public static <E, T extends Throwable> CheckedIterator<E, T> emptyIterator() {
        return new CheckedIterator<E, T>() {
            @Override
            public boolean hasNext() throws T {
                return false;
            }

            @Override
            public E next() throws T, NoSuchElementException {
                throw new NoSuchElementException();
            }
        };
    }

    @Nonnull
    private static <E, T extends Throwable> CheckedIterator<E, T> concat(@Nonnull CheckedIterator<E, T>... inputs) {
        return concat(ImmutableList.copyOf(inputs).iterator());
    }

    @Nonnull
    private static <E, T extends Throwable> CheckedIterator<E, T> concat(@Nonnull final Iterator<CheckedIterator<E, T>> inputs) {
        requireNonNull(inputs);
        return new CheckedIterator<E, T>() {
            private CheckedIterator<E, T> current = emptyIterator();

            @Override
            public boolean hasNext() throws T {
                boolean currentHasNext;
                while (!(currentHasNext = requireNonNull(current).hasNext())
                        && inputs.hasNext()) {
                    current = inputs.next();
                }
                return currentHasNext;
            }

            @Override
            public E next() throws T {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return current.next();
            }
        };
    }

    @Nonnull
    public static <X> CheckedIterator<X, RuntimeException> fromIterator(@Nonnull final Iterator<X> iterator) {
        return new CheckedIterator<X, RuntimeException>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public X next() {
                return iterator.next();
            }
        };
    }

    @Nonnull
    public static CheckedIterator<String, IOException> fromBufferedReader(@Nonnull final BufferedReader reader) {
        return new ACheckedIterator<String, IOException>() {
            @Override
            protected String computeNext() throws IOException {
                String result = reader.readLine();
                return result != null ? result : endOfData();
            }
        };
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    private static abstract class ACheckedIterator<E, T extends Throwable> extends CheckedIterator<E, T> {

        private State state = State.NOT_READY;

        private enum State {

            READY, NOT_READY, DONE, FAILED,
        }

        private E next;

        protected abstract E computeNext() throws T;

        protected final E endOfData() {
            state = State.DONE;
            return null;
        }

        @Override
        public final boolean hasNext() throws T {
            checkState(state != State.FAILED);
            switch (state) {
                case DONE:
                    return false;
                case READY:
                    return true;
                default:
            }
            return tryToComputeNext();
        }

        private boolean tryToComputeNext() throws T {
            state = State.FAILED;
            next = computeNext();
            if (state != State.DONE) {
                state = State.READY;
                return true;
            }
            return false;
        }

        @Override
        public final E next() throws T {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            state = State.NOT_READY;
            E result = next;
            next = null;
            return result;
        }
    }
    //</editor-fold>
}
