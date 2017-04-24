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
package demetra.data;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoublePredicate;
import java.util.function.IntFunction;
import java.util.stream.DoubleStream;
import java.util.stream.StreamSupport;

/**
 * Support class of Sequence.
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
class Sequences {

    static final class SequenceIterator<E> implements Iterator<E> {

        private final Sequence<E> seq;
        private int cur = 0;

        SequenceIterator(Sequence<E> seq) {
            this.seq = seq;
        }

        @Override
        public boolean hasNext() {
            return cur < seq.length();
        }

        @Override
        public E next() {
            if (hasNext()) {
                return seq.get(cur++);
            } else {
                throw new NoSuchElementException();
            }
        }
    }

    <E> void forEach(Sequence<E> seq, Consumer<? super E> action) {
        for (int i = 0; i < seq.length(); i++) {
            action.accept(seq.get(i));
        }
    }

    <E> E[] toArray(Sequence<E> seq, IntFunction<E[]> generator) {
        E[] result = generator.apply(seq.length());
        for (int i = 0; i < result.length; i++) {
            result[i] = seq.get(i);
        }
        return result;
    }

    static final class DoubleIterator implements PrimitiveIterator.OfDouble {

        private final Sequence.OfDouble seq;
        private int cur = 0;

        DoubleIterator(Sequence.OfDouble seq) {
            this.seq = seq;
        }

        @Override
        public boolean hasNext() {
            return cur < seq.length();
        }

        @Override
        public double nextDouble() {
            if (hasNext()) {
                return seq.getDouble(cur++);
            } else {
                throw new NoSuchElementException();
            }
        }

        @Override
        public void forEachRemaining(DoubleConsumer block) {
            for (; cur < seq.length(); cur++) {
                block.accept(seq.getDouble(cur));
            }
        }
    }

    void forEach(Sequence.OfDouble seq, DoubleConsumer action) {
        for (int i = 0; i < seq.length(); i++) {
            action.accept(seq.getDouble(i));
        }
    }

    Spliterator.OfDouble spliterator(Sequence.OfDouble seq) {
        return Spliterators.spliterator(seq.iterator(), seq.length(), Spliterator.ORDERED);
    }

    DoubleStream stream(Sequence.OfDouble seq) {
        return StreamSupport.doubleStream(seq::spliterator,
                Spliterator.SUBSIZED | Spliterator.SIZED | Spliterator.ORDERED,
                false);
    }

    void copyTo(Sequence.OfDouble seq, double[] buffer, int offset) {
        int n = seq.length();
        for (int i = 0; i < n; ++i) {
            buffer[offset + i] = seq.getDouble(i);
        }
    }

    double[] toArray(Sequence.OfDouble seq) {
        double[] result = new double[seq.length()];
        for (int i = 0; i < result.length; ++i) {
            result[i] = seq.getDouble(i);
        }
        return result;
    }

    boolean allMatch(Sequence.OfDouble seq, DoublePredicate pred) {
        int n = seq.length();
        for (int i = 0; i < n; ++i) {
            if (!pred.test(seq.getDouble(i))) {
                return false;
            }
        }
        return true;
    }

    int firstIndexOf(Sequence.OfDouble seq, DoublePredicate pred) {
        int n = seq.length();
        for (int i = 0; i < n; ++i) {
            if (pred.test(seq.getDouble(i))) {
                return i;
            }
        }
        return n;
    }

    int lastIndexOf(Sequence.OfDouble seq, DoublePredicate pred) {
        int n = seq.length();
        for (int i = n - 1; i >= 0; --i) {
            if (pred.test(seq.getDouble(i))) {
                return i;
            }
        }
        return -1;
    }

    double reduce(Sequence.OfDouble seq, double initial, DoubleBinaryOperator fn) {
        double cur = initial;
        int n = seq.length();
        for (int i = 0; i < n; ++i) {
            cur = fn.applyAsDouble(cur, seq.getDouble(i));
        }
        return cur;
    }
}
