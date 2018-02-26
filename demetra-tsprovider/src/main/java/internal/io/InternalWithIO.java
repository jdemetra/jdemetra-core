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
package internal.io;

import demetra.io.IteratorWithIO;
import ioutil.IO;
import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class InternalWithIO {

    public static final class MappingIterator<E, Z> implements IteratorWithIO<Z> {

        private final IteratorWithIO<E> delegate;
        private final IO.Function<? super E, ? extends Z> function;

        public MappingIterator(IteratorWithIO<E> delegate, IO.Function<? super E, ? extends Z> function) {
            this.delegate = Objects.requireNonNull(delegate);
            this.function = Objects.requireNonNull(function);
        }

        @Override
        public boolean hasNext() throws IOException {
            return delegate.hasNext();
        }

        @Override
        public Z next() throws IOException, NoSuchElementException {
            return function.applyWithIO(delegate.next());
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }

    public static final class OnCloseIterator<E> implements IteratorWithIO<E> {

        private final IteratorWithIO<E> delegate;
        private final Closeable closeHandler;

        public OnCloseIterator(IteratorWithIO<E> delegate, Closeable closeHandler) {
            this.delegate = Objects.requireNonNull(delegate);
            this.closeHandler = Objects.requireNonNull(closeHandler);
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
            IO.closeBoth(delegate, closeHandler);
        }
    }

    public static final class EmptyIterator<E> implements IteratorWithIO<E> {

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

    public static final class SingletonIterator<E> implements IteratorWithIO<E> {

        private final E element;
        private boolean first;

        public SingletonIterator(E element) {
            this.element = Objects.requireNonNull(element);
            this.first = false;
        }

        @Override
        public boolean hasNext() throws IOException {
            return first;
        }

        @Override
        public E next() throws IOException, NoSuchElementException {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            first = false;
            return element;
        }

        @Override
        public void close() throws IOException {
            // do nothing
        }
    }

    public static final class CheckedIterator<E> implements IteratorWithIO<E> {

        private final Iterator<E> delegate;

        public CheckedIterator(Iterator<E> delegate) {
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

    public static final class UncheckedIterator<E> implements Iterator<E> {

        private final IteratorWithIO<E> delegate;

        public UncheckedIterator(IteratorWithIO<E> delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean hasNext() {
            try {
                return delegate.hasNext();
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }

        @Override
        public E next() {
            try {
                E result = delegate.next();
                if (!hasNext()) {
                    delegate.close();
                }
                return result;
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }
}
