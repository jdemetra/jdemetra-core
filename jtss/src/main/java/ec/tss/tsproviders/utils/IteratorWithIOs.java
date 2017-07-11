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

import ec.tstoolkit.utilities.Closeables;
import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;

/**
 *
 * @author Philippe Charles
 */
final class IteratorWithIOs {

    private IteratorWithIOs() {
        // static class
    }

    private static abstract class ForwardingIterator<E> implements IteratorWithIO<E> {

        protected final IteratorWithIO<E> delegate;

        private ForwardingIterator(IteratorWithIO<E> delegate) {
            this.delegate = Objects.requireNonNull(delegate);
        }

        @Override
        public boolean hasNext() throws IOException {
            return delegate.hasNext();
        }

        @Override
        public E next() throws IOException, NoSuchElementException {
            return delegate.next();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }

    static final class TransformingIterator<E, Z> extends ForwardingIterator<Z> {

        private final Function<? super E, ? extends Z> function;

        TransformingIterator(IteratorWithIO<E> delegate, Function<? super E, ? extends Z> function) {
            super((IteratorWithIO<Z>) delegate);
            this.function = Objects.requireNonNull(function);
        }

        @Override
        public Z next() throws IOException, NoSuchElementException {
            return function.apply(((IteratorWithIO<E>) delegate).next());
        }
    }

    static final class OnCloseIterator<E> extends ForwardingIterator<E> {

        private final Closeable closeHandler;

        OnCloseIterator(IteratorWithIO<E> delegate, Closeable closeHandler) {
            super(delegate);
            this.closeHandler = Objects.requireNonNull(closeHandler);
        }

        @Override
        public void close() throws IOException {
            Closeables.closeBoth(delegate, closeHandler);
        }
    }

    static final class EmptyIterator<E> implements IteratorWithIO<E> {

        @Override
        public boolean hasNext() throws IOException {
            return false;
        }

        @Override
        public E next() throws IOException, NoSuchElementException {
            throw new NoSuchElementException();
        }

        @Override
        public void close() throws IOException {
            // do nothing
        }
    }

    static final class Adapter<E> implements IteratorWithIO<E> {

        private final Iterator<E> delegate;

        Adapter(Iterator<E> delegate) {
            this.delegate = Objects.requireNonNull(delegate);
        }

        @Override
        public boolean hasNext() throws IOException {
            return delegate.hasNext();
        }

        @Override
        public E next() throws IOException, NoSuchElementException {
            return delegate.next();
        }

        @Override
        public void close() throws IOException {
            // do nothing
        }
    }
}
