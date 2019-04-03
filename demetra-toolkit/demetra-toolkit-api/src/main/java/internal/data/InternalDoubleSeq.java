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

import demetra.data.DoubleSeqCursor;
import demetra.util.function.BiDoublePredicate;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoublePredicate;
import java.util.function.IntToDoubleFunction;
import java.util.stream.DoubleStream;
import java.util.stream.StreamSupport;
import demetra.data.DoubleSeq;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class InternalDoubleSeq {

    @lombok.RequiredArgsConstructor
    public static final class DoubleIterator implements PrimitiveIterator.OfDouble {

        private final DoubleSeq seq;
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

    public void forEach(DoubleSeq seq, DoubleConsumer action) {
        for (int i = 0; i < seq.length(); i++) {
            action.accept(seq.get(i));
        }
    }

    public Spliterator.OfDouble spliterator(DoubleSeq seq) {
        return Spliterators.spliterator(new DoubleIterator(seq), seq.length(), Spliterator.ORDERED);
    }

    public void copyTo(DoubleSeq seq, double[] buffer, int offset) {
        int n = seq.length();
        DoubleSeqCursor reader = seq.cursor();
        for (int i = 0; i < n; ++i) {
            buffer[offset + i] = reader.getAndNext();
        }
    }

    public double[] toArray(DoubleSeq seq) {
        double[] result = new double[seq.length()];
        DoubleSeqCursor reader = seq.cursor();
        for (int i = 0; i < result.length; ++i) {
            result[i] = reader.getAndNext();
        }
        return result;
    }

    public DoubleStream stream(DoubleSeq seq) {
        return StreamSupport.doubleStream(spliterator(seq), false);
    }

    public boolean allMatch(DoubleSeq seq, DoublePredicate pred) {
        int n = seq.length();
        DoubleSeqCursor reader = seq.cursor();
        for (int i = 0; i < n; ++i) {
            if (!pred.test(reader.getAndNext())) {
                return false;
            }
        }
        return true;
    }

    public boolean allMatch(DoubleSeq seq1, DoubleSeq seq2, BiDoublePredicate pred) {
        int n = seq1.length();
        DoubleSeqCursor reader1 = seq1.cursor();
        DoubleSeqCursor reader2 = seq2.cursor();
        for (int i = 0; i < n; ++i) {
            if (!pred.test(reader1.getAndNext(), reader2.getAndNext())) {
                return false;
            }
        }
        return true;
    }

    public boolean anyMatch(DoubleSeq seq, DoublePredicate pred) {
        int n = seq.length();
        DoubleSeqCursor reader = seq.cursor();
        for (int i = 0; i < n; ++i) {
            if (pred.test(reader.getAndNext())) {
                return true;
            }
        }
        return false;
    }

    public int firstIndexOf(DoubleSeq seq, DoublePredicate pred) {
        int n = seq.length();
        DoubleSeqCursor reader = seq.cursor();
        for (int i = 0; i < n; ++i) {
            if (pred.test(reader.getAndNext())) {
                return i;
            }
        }
        return n;
    }

    public int lastIndexOf(DoubleSeq seq, DoublePredicate pred) {
        int n = seq.length();
        for (int i = n - 1; i >= 0; --i) {
            if (pred.test(seq.get(i))) {
                return i;
            }
        }
        return -1;
    }

    public double reduce(DoubleSeq seq, double initial, DoubleBinaryOperator fn) {
        double cur = initial;
        int n = seq.length();
        DoubleSeqCursor reader = seq.cursor();
        for (int i = 0; i < n; ++i) {
            cur = fn.applyAsDouble(cur, reader.getAndNext());
        }
        return cur;
    }

    public int count(DoubleSeq seq, DoublePredicate pred) {
        int n = seq.length();
        int c = 0;
        DoubleSeqCursor reader = seq.cursor();
        for (int i = n - 1; i >= 0; --i) {
            if (pred.test(reader.getAndNext())) {
                ++c;
            }
        }
        return c;
    }

    @lombok.AllArgsConstructor
    public static final class PartialDoubleArray implements DoubleSeq {

        private final double[] data;
        private final int beg, len;

        @Override
        public DoubleSeqCursor cursor() {
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
        public DoubleSeq extract(int start, int length) {
            return new PartialDoubleArray(data, this.beg + start, length);
        }

        @Override
        public String toString() {
            return DoubleSeq.format(this);
        }

        private final class Cell implements DoubleSeqCursor {

            private int pos = beg;

            @Override
            public double getAndNext() {
                return data[pos++];
            }

            @Override
            public void skip(int n) {
                pos += n;
            }

            @Override
            public void moveTo(int npos) {
                pos = beg + npos;
            }
        }
    }

    @lombok.AllArgsConstructor
    public static final class IntToDoubleSequence implements DoubleSeq {

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
        public DoubleSeq extract(final int start, final int length) {
            return new IntToDoubleSequence(length, i -> fn.applyAsDouble(i + start));
        }

        @Override
        public DoubleSeqCursor cursor() {
            return new Cell();
        }

        @Override
        public String toString() {
            return DoubleSeq.format(this);
        }

        private final class Cell implements DoubleSeqCursor {

            private int pos = 0;

            @Override
            public double getAndNext() {
                return fn.applyAsDouble(pos++);
            }

            @Override
            public void skip(int n) {
                pos += n;
            }

            @Override
            public void moveTo(int npos) {
                pos = npos;
            }
        }
    }

    @lombok.AllArgsConstructor
    public static final class RegularlySpacedDoubles implements DoubleSeq {

        private final double[] data;
        private final int beg, len, inc;

        @Override
        public DoubleSeqCursor cursor() {
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
        public DoubleSeq extract(int start, int length) {
            return new RegularlySpacedDoubles(data, this.beg + start * inc, length, inc);
        }

        @Override
        public String toString() {
            return DoubleSeq.format(this);
        }

        private final class Cell implements DoubleSeqCursor {

            private int pos = beg;

            @Override
            public double getAndNext() {
                double val = data[pos];
                pos += inc;
                return val;
            }

            @Override
            public void skip(int n) {
                pos += n * inc;
            }

            @Override
            public void moveTo(int npos) {
                pos = beg + npos * inc;
            }
        }
    }
}
