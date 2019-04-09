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
import java.util.Objects;

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

    public void copyToByCursor(DoubleSeq seq, double[] buffer, int offset) {
        int n = seq.length();
        DoubleSeqCursor cursor = seq.cursor();
        for (int i = 0; i < n; ++i) {
            buffer[offset + i] = cursor.getAndNext();
        }
    }

    public double[] toArrayByCursor(DoubleSeq seq) {
        double[] result = new double[seq.length()];
        DoubleSeqCursor cursor = seq.cursor();
        for (int i = 0; i < result.length; ++i) {
            result[i] = cursor.getAndNext();
        }
        return result;
    }

    public DoubleStream stream(DoubleSeq seq) {
        return StreamSupport.doubleStream(spliterator(seq), false);
    }

    public boolean allMatchByCursor(DoubleSeq seq, DoublePredicate pred) {
        int n = seq.length();
        DoubleSeqCursor cursor = seq.cursor();
        for (int i = 0; i < n; ++i) {
            if (!pred.test(cursor.getAndNext())) {
                return false;
            }
        }
        return true;
    }

    public boolean allMatchByCursor(DoubleSeq seq1, DoubleSeq seq2, BiDoublePredicate pred) {
        int n = seq1.length();
        DoubleSeqCursor cursor1 = seq1.cursor();
        DoubleSeqCursor cursor2 = seq2.cursor();
        for (int i = 0; i < n; ++i) {
            if (!pred.test(cursor1.getAndNext(), cursor2.getAndNext())) {
                return false;
            }
        }
        return true;
    }

    public boolean anyMatchByCursor(DoubleSeq seq, DoublePredicate pred) {
        int n = seq.length();
        DoubleSeqCursor cursor = seq.cursor();
        for (int i = 0; i < n; ++i) {
            if (pred.test(cursor.getAndNext())) {
                return true;
            }
        }
        return false;
    }

    public int firstIndexOfByCursor(DoubleSeq seq, DoublePredicate pred) {
        int n = seq.length();
        DoubleSeqCursor cursor = seq.cursor();
        for (int i = 0; i < n; ++i) {
            if (pred.test(cursor.getAndNext())) {
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

    public double reduceByCursor(DoubleSeq seq, double initial, DoubleBinaryOperator fn) {
        double result = initial;
        int n = seq.length();
        DoubleSeqCursor cursor = seq.cursor();
        for (int i = 0; i < n; ++i) {
            result = fn.applyAsDouble(result, cursor.getAndNext());
        }
        return result;
    }

    public int countByCursor(DoubleSeq seq, DoublePredicate pred) {
        int n = seq.length();
        int result = 0;
        DoubleSeqCursor cursor = seq.cursor();
        for (int i = n - 1; i >= 0; --i) {
            if (pred.test(cursor.getAndNext())) {
                ++result;
            }
        }
        return result;
    }

    public static class EmptyDoubleSeq extends InternalBaseSeq.EmptyBaseSeq implements DoubleSeq {

        public static final EmptyDoubleSeq DOUBLE_SEQ = new EmptyDoubleSeq();

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

        @Override
        public DoubleSeqCursor cursor() {
            return InternalDoubleSeqCursor.EmptyDoubleSeqCursor.DOUBLE_SEQ;
        }
    }

    @lombok.AllArgsConstructor
    public static class SingleDoubleSeq extends InternalBaseSeq.SingleBaseSeq implements DoubleSeq {

        protected double value;

        public double getValue() {
            return value;
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

        @Override
        public DoubleSeqCursor cursor() {
            return new InternalDoubleSeqCursor.SingleDoubleSeqCursor(this::getValue);
        }
    }

    @lombok.AllArgsConstructor
    @lombok.EqualsAndHashCode(callSuper = false)
    public static class MultiDoubleSeq extends InternalBaseSeq.MultiBaseSeq implements DoubleSeq {

        @lombok.NonNull
        protected final double[] values;

        @Override
        public int length() {
            return values.length;
        }

        @Override
        public double get(int index) throws IndexOutOfBoundsException {
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
        public DoubleSeqCursor cursor() {
            return new InternalDoubleSeqCursor.MultiDoubleSeqCursor(values);
        }

        @Override
        public String toString() {
            return DoubleSeq.format(this);
        }
    }

    public static class SubDoubleSeq extends InternalBaseSeq.SubBaseSeq implements DoubleSeq {

        protected final double[] values;

        public SubDoubleSeq(double[] data, int begin, int length) {
            super(begin, length);
            this.values = data;
        }

        @Override
        public DoubleSeqCursor cursor() {
            return new InternalDoubleSeqCursor.SubDoubleSeqCursor(values, begin);
        }

        @Override
        public double get(int idx) {
            return values[begin + idx];
        }

        @Override
        public double[] toArray() {
            double[] ndata = new double[length];
            System.arraycopy(values, begin, ndata, 0, length);
            return ndata;
        }

        @Override
        public String toString() {
            return DoubleSeq.format(this);
        }
    }

    public static class MappingDoubleSeq extends InternalBaseSeq.MappingBaseSeq implements DoubleSeq {

        protected final IntToDoubleFunction getter;

        public MappingDoubleSeq(int length, IntToDoubleFunction getter) {
            super(length);
            this.getter = getter;
        }

        @Override
        public double get(int idx) {
            return getter.applyAsDouble(idx);
        }

        @Override
        public DoubleSeqCursor cursor() {
            return new InternalDoubleSeqCursor.DefaultDoubleSeqCursor(this);
        }

        @Override
        public String toString() {
            return DoubleSeq.format(this);
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
