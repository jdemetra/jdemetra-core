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
package internal.data;

import demetra.data.DoubleReader;
import demetra.data.DoubleSequence;
import demetra.util.function.BiDoublePredicate;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoublePredicate;
import java.util.function.IntToDoubleFunction;
import java.util.stream.DoubleStream;
import java.util.stream.StreamSupport;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class InternalDoubleSeq {

    @lombok.RequiredArgsConstructor
    public static final class DoubleIterator implements PrimitiveIterator.OfDouble {

        private final DoubleSequence seq;
        private int cur = 0;

        @Override
        public boolean hasNext() {
            return cur < seq.length();
        }

        @Override
        public double nextDouble() {
            if (hasNext()) {
                return seq.get(cur++);
            } else {
                throw new NoSuchElementException();
            }
        }

        @Override
        public void forEachRemaining(DoubleConsumer block) {
            for (; cur < seq.length(); cur++) {
                block.accept(seq.get(cur));
            }
        }
    }

    public void forEach(DoubleSequence seq, DoubleConsumer action) {
        for (int i = 0; i < seq.length(); i++) {
            action.accept(seq.get(i));
        }
    }

    public Spliterator.OfDouble spliterator(DoubleSequence seq) {
        return Spliterators.spliterator(new DoubleIterator(seq), seq.length(), Spliterator.ORDERED);
    }

    public void copyTo(DoubleSequence seq, double[] buffer, int offset) {
        int n = seq.length();
        DoubleReader reader = seq.reader();
        for (int i = 0; i < n; ++i) {
            buffer[offset + i] = reader.next();
        }
    }

    public double[] toArray(DoubleSequence seq) {
        double[] result = new double[seq.length()];
        DoubleReader reader = seq.reader();
        for (int i = 0; i < result.length; ++i) {
            result[i] = reader.next();
        }
        return result;
    }

    public DoubleStream stream(DoubleSequence seq) {
        return StreamSupport.doubleStream(spliterator(seq), false);
    }

    public boolean allMatch(DoubleSequence seq, DoublePredicate pred) {
        int n = seq.length();
        DoubleReader reader = seq.reader();
        for (int i = 0; i < n; ++i) {
            if (!pred.test(reader.next())) {
                return false;
            }
        }
        return true;
    }

    public boolean allMatch(DoubleSequence seq1, DoubleSequence seq2, BiDoublePredicate pred) {
        int n = seq1.length();
        DoubleReader reader1 = seq1.reader();
        DoubleReader reader2 = seq2.reader();
        for (int i = 0; i < n; ++i) {
            if (!pred.test(reader1.next(), reader2.next())) {
                return false;
            }
        }
        return true;
    }

    public boolean anyMatch(DoubleSequence seq, DoublePredicate pred) {
        int n = seq.length();
        DoubleReader reader = seq.reader();
        for (int i = 0; i < n; ++i) {
            if (pred.test(reader.next())) {
                return true;
            }
        }
        return false;
    }

    public int firstIndexOf(DoubleSequence seq, DoublePredicate pred) {
        int n = seq.length();
        DoubleReader reader = seq.reader();
        for (int i = 0; i < n; ++i) {
            if (pred.test(reader.next())) {
                return i;
            }
        }
        return n;
    }

    public int lastIndexOf(DoubleSequence seq, DoublePredicate pred) {
        int n = seq.length();
        for (int i = n - 1; i >= 0; --i) {
            if (pred.test(seq.get(i))) {
                return i;
            }
        }
        return -1;
    }

    public double reduce(DoubleSequence seq, double initial, DoubleBinaryOperator fn) {
        double cur = initial;
        int n = seq.length();
        DoubleReader reader = seq.reader();
        for (int i = 0; i < n; ++i) {
            cur = fn.applyAsDouble(cur, reader.next());
        }
        return cur;
    }

    public int count(DoubleSequence seq, DoublePredicate pred) {
        int n = seq.length();
        int c = 0;
        DoubleReader reader = seq.reader();
        for (int i = n - 1; i >= 0; --i) {
            if (pred.test(reader.next())) {
                ++c;
            }
        }
        return c;
    }

    public enum DoubleSeq0 implements DoubleSequence {
        INSTANCE;

        @Override
        public int length() {
            return 0;
        }

        @Override
        public double get(int index) throws IndexOutOfBoundsException {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }

        @Override
        public double[] toArray() {
            return new double[0];
        }

        @Override
        public void copyTo(double[] buffer, int offset) {
            Objects.requireNonNull(buffer);
        }
    }

    @lombok.AllArgsConstructor
    public static final class DoubleSeq1 implements DoubleSequence {

        private final double value;

        @Override
        public int length() {
            return 1;
        }

        @Override
        public double get(int index) throws IndexOutOfBoundsException {
            if (index == 0) {
                return value;
            }
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }

        @Override
        public double[] toArray() {
            return new double[]{value};
        }

        @Override
        public void copyTo(double[] buffer, int offset) {
            buffer[offset] = value;
        }
    }

    @lombok.AllArgsConstructor
    @lombok.EqualsAndHashCode
    public static final class DoubleSeqN implements DoubleSequence {

        @lombok.NonNull
        private final double[] values;

        @Override
        public int length() {
            return values.length;
        }

        @Override
        public double get(int index) {
            return values[index];
        }

        @Override
        public double[] toArray() {
            return values.clone();
        }

        @Override
        public void copyTo(double[] buffer, int offset) {
            System.arraycopy(values, 0, buffer, offset, values.length);
        }

        @Override
        public DoubleReader reader() {
            return new Cell();
        }

        @Override
        public DoubleSequence extract(int start, int length) {
            return new PartialDoubleArray(values, start, length);
        }

        @Override
        public String toString() {
            return DoubleSequence.format(this);
        }

        private final class Cell implements DoubleReader {

            private int pos = 0;

            @Override
            public double next() {
                return values[pos++];
            }

            @Override
            public void setPosition(int npos) {
                pos = npos;
            }
        }
    }

    @lombok.AllArgsConstructor
    public static final class PartialDoubleArray implements DoubleSequence {

        private final double[] data;
        private final int beg, len;

        @Override
        public DoubleReader reader() {
            return new Cell();
        }

        @Override
        public double get(int idx) {
            return data[beg + idx];
        }

        @Override
        public int length() {
            return len;
        }

        @Override
        public double[] toArray() {
            double[] ndata = new double[len];
            System.arraycopy(data, beg, ndata, 0, len);
            return ndata;
        }

        @Override
        public DoubleSequence extract(int start, int length) {
            return new PartialDoubleArray(data, this.beg + start, length);
        }

        @Override
        public String toString() {
            return DoubleSequence.format(this);
        }

        private final class Cell implements DoubleReader {

            private int pos = beg;

            @Override
            public double next() {
                return data[pos++];
            }

            @Override
            public void setPosition(int npos) {
                pos = beg + npos;
            }
        }
    }

    @lombok.AllArgsConstructor
    public static final class IntToDoubleSequence implements DoubleSequence {

        private final int length;
        private final IntToDoubleFunction fn;

        @Override
        public double get(int idx) {
            return fn.applyAsDouble(idx);
        }

        @Override
        public int length() {
            return length;
        }

        @Override
        public DoubleSequence extract(final int start, final int length) {
            return new IntToDoubleSequence(length, i -> fn.applyAsDouble(i + start));
        }

        @Override
        public DoubleReader reader() {
            return new Cell();
        }

        @Override
        public String toString() {
            return DoubleSequence.format(this);
        }

        private final class Cell implements DoubleReader {

            private int pos = 0;

            @Override
            public double next() {
                return fn.applyAsDouble(pos++);
            }

            @Override
            public void setPosition(int npos) {
                pos = npos;
            }
        }
    }

    @lombok.AllArgsConstructor
    public static final class RegularlySpacedDoubles implements DoubleSequence {

        private final double[] data;
        private final int beg, len, inc;

        @Override
        public DoubleReader reader() {
            return new Cell();
        }

        @Override
        public double get(int idx) {
            return data[beg + idx * inc];
        }

        @Override
        public int length() {
            return len;
        }

        @Override
        public double[] toArray() {
            double[] ndata = new double[len];
            for (int i = 0, j = beg; i < len; ++i, j += inc) {
                ndata[i] = data[j];
            }
            return ndata;
        }

        @Override
        public DoubleSequence extract(int start, int length) {
            return new RegularlySpacedDoubles(data, this.beg + start * inc, length, inc);
        }

        @Override
        public String toString() {
            return DoubleSequence.format(this);
        }

        private final class Cell implements DoubleReader {

            private int pos = beg;

            @Override
            public double next() {
                double val = data[pos];
                pos += inc;
                return val;
            }

            @Override
            public void setPosition(int npos) {
                pos = beg + npos * inc;
            }
        }
    }
}
