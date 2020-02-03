/*
 * Copyright 2017 National Bank of Belgium
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

import internal.io.InternalWithIO;
import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import nbbrd.io.function.IOConsumer;
import nbbrd.io.function.IOFunction;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Philippe Charles
 * @param <E>
 */
public interface IteratorWithIO<E> extends Closeable {

    boolean hasNext() throws IOException;

    @Nullable
    E next() throws IOException, NoSuchElementException;

    @Override
    void close() throws IOException;

    /**
     * Performs the given action for each remaining element until all elements
     * have been processed or the action throws an exception. Actions are
     * performed in the order of iteration, if that order is specified.
     * Exceptions thrown by the action are relayed to the caller.
     *
     * @implSpec
     * <p>
     * The default implementation behaves as if:      <pre>{@code
     *     while (hasNext())
     *         action.accept(next());
     * }</pre>
     *
     * @param action The action to be performed for each element
     * @throws NullPointerException if the specified action is null
     * @throws java.io.IOException
     */
    default void forEachRemaining(IOConsumer<? super E> action) throws IOException {
        Objects.requireNonNull(action);
        while (hasNext()) {
            action.acceptWithIO(next());
        }
    }

    @NonNull
    default <Z> IteratorWithIO<Z> map(@NonNull IOFunction<? super E, ? extends Z> function) {
        return new InternalWithIO.MappingIterator<>(this, function);
    }

    @NonNull
    default IteratorWithIO<E> onClose(@NonNull Closeable closeHandler) {
        return new InternalWithIO.OnCloseIterator<>(this, closeHandler);
    }

    //<editor-fold defaultstate="collapsed" desc="Factories">
    @NonNull
    static <E> IteratorWithIO<E> empty() {
        return new InternalWithIO.EmptyIterator<>();
    }

    @NonNull
    static <E> IteratorWithIO<E> singleton(@NonNull E element) {
        return new InternalWithIO.SingletonIterator<>(element);
    }

    @NonNull
    static <E> IteratorWithIO<E> checked(@NonNull Iterator<E> iterator) {
        return new InternalWithIO.CheckedIterator<>(iterator);
    }

    @NonNull
    static <E> Iterator<E> unchecked(@NonNull IteratorWithIO<E> iterator) {
        return new InternalWithIO.UncheckedIterator<>(iterator);
    }
    //</editor-fold>
}
