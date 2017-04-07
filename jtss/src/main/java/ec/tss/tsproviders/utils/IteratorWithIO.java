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
package ec.tss.tsproviders.utils;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 * @param <E>
 */
public interface IteratorWithIO<E> extends Closeable {

    boolean hasNext() throws IOException;

    @Nullable
    E next() throws IOException, NoSuchElementException;

    @Nonnull
    default <Z> IteratorWithIO<Z> transform(@Nonnull Function<? super E, ? extends Z> function) {
        return new IteratorWithIOs.TransformingIterator<>(this, function);
    }

    @Nonnull
    default IteratorWithIO<E> onClose(@Nonnull Closeable closeHandler) {
        return new IteratorWithIOs.OnCloseIterator<>(this, closeHandler);
    }

    @Nonnull
    static <E> IteratorWithIO<E> empty() {
        return new IteratorWithIOs.EmptyIterator<>();
    }

    @Nonnull
    static <E> IteratorWithIO<E> from(@Nonnull Iterator<E> iterator) {
        return new IteratorWithIOs.Adapter<>(iterator);
    }
}
