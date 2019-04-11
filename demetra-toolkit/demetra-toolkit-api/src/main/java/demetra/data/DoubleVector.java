/*
 * Copyright 2019 National Bank of Belgium
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

import demetra.design.Development;
import demetra.util.function.IntDoubleConsumer;
import internal.data.InternalDoubleVector;
import internal.data.InternalDoubleVectorCursor;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntUnaryOperator;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Describes a writable sequence of doubles.
 *
 * @author Philippe Charles
 */
@Development(status = Development.Status.Release)
public interface DoubleVector extends DoubleSeq {

    /**
     * Sets <code>double</code> value at the specified index.
     *
     *
     * @param index the index of the <code>double</code> value to be modified
     * @param value the specified <code>double</code> value
     */
    void set(@Nonnegative int index, double value) throws IndexOutOfBoundsException;

    default void set(double value) {
        DoubleVectorCursor cursor = cursor();
        int n = length();
        for (int i = 0; i < n; ++i) {
            cursor.setAndNext(value);
        }
    }

    @Override
    default DoubleVectorCursor cursor() {
        return new InternalDoubleVectorCursor.DefaultDoubleVectorCursor(this);
    }

    @Override
    default DoubleVector map(int length, IntUnaryOperator indexMapper) {
        return onMapping(length, i -> get(indexMapper.applyAsInt(i)), (i, v) -> set(indexMapper.applyAsInt(i), v));
    }

    @Override
    default DoubleVector extract(int start, int length) {
        return map(length, i -> start + i);
    }

    @Override
    default DoubleVector extract(int start, int length, int increment) {
        return map(length, i -> start + i * increment);
    }

    @Override
    default DoubleVector drop(int beg, int end) {
        return extract(beg, length() - beg - end);
    }

    @Override
    default DoubleVector range(int beg, int end) {
        return end <= beg ? map(0, i -> -1) : extract(beg, end - beg);
    }

    @Override
    default DoubleVector reverse() {
        final int n = length();
        return map(n, i -> n - 1 - i);
    }

    @Nonnull
    static DoubleVector of(@Nonnull double[] values) {
        return new InternalDoubleVector.MultiDoubleVector(values);
    }

    @Nonnull
    static DoubleVector onMapping(@Nonnegative int length, @Nonnull IntToDoubleFunction getter, @Nonnull IntDoubleConsumer setter) {
        return new InternalDoubleVector.MappingDoubleVector(length, getter, setter);
    }

    //<editor-fold defaultstate="collapsed" desc="Lambda expressions">
    default void apply(@Nonnegative int index, DoubleUnaryOperator fn) throws IndexOutOfBoundsException {
        set(index, fn.applyAsDouble(get(index)));
    }

    default void set(DoubleSupplier fn) throws IndexOutOfBoundsException {
        DoubleVectorCursor cur = cursor();
        int n = length();
        for (int i = 0; i < n; ++i) {
            cur.setAndNext(fn.getAsDouble());
        }
    }

    default void set(IntToDoubleFunction fn) throws IndexOutOfBoundsException {
        DoubleVectorCursor cur = cursor();
        int n = length();
        for (int i = 0; i < n; ++i) {
            cur.setAndNext(fn.applyAsDouble(i));
        }
    }

    default void set(DoubleSeq z) throws IndexOutOfBoundsException {
        DoubleVectorCursor cur = cursor();
        DoubleSeqCursor zcur = z.cursor();
        int n = length();
        for (int i = 0; i < n; ++i) {
            cur.setAndNext(zcur.getAndNext());
        }
    }

    default void set(DoubleSeq z, DoubleUnaryOperator fn) throws IndexOutOfBoundsException {
        DoubleVectorCursor cur = cursor();
        DoubleSeqCursor zcur = z.cursor();
        int n = length();
        for (int i = 0; i < n; ++i) {
            cur.setAndNext(fn.applyAsDouble(zcur.getAndNext()));
        }
    }

    default void apply(DoubleUnaryOperator fn) {
        DoubleVectorCursor cursor = cursor();
        int n = length();
        for (int i = 0; i < n; ++i) {
            cursor.applyAndNext(fn);
        }
    }

    default void apply(DoubleSeq z, DoubleBinaryOperator fn) throws IndexOutOfBoundsException {
        DoubleVectorCursor cur = cursor();
        DoubleSeqCursor zcur = z.cursor();
        int n = length();
        for (int i = 0; i < n; ++i) {
            cur.applyAndNext(x -> fn.applyAsDouble(x, zcur.getAndNext()));
        }
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Common operations">
    default void add(double a) {
        if (a != 0) {
            apply(x -> x + a);
        }
    }

    default void sub(double a) {
        if (a != 0) {
            apply(x -> x - a);
        }
    }

    default void chs() {
        apply(x -> -x);
    }

    default void mul(double a) {
        if (a == 0) {
            set(0);
        } else if (a == -1) {
            chs();
        } else {
            apply(x -> x * a);
        }
    }

    default void div(double a) {
        if (a == 0) {
            set(Double.NaN);
        } else if (a == -1) {
            chs();
        } else {
            apply(x -> x * a);
        }
    }

    default void add(DoubleSeq y) throws IndexOutOfBoundsException {
        apply(y, (a, b) -> a + b);
    }

    default void addAY(double a, DoubleSeq y) throws IndexOutOfBoundsException {
        if (a == 1)
            add(y);
        else if (a == -1)
            sub(y);
        else if (a != 0)
        apply(y, (a, b) -> a + b);
    }

    default void sub(DoubleSeq y) throws IndexOutOfBoundsException {
        apply(y, (a, b) -> a - b);
    }

    default void mul(DoubleSeq y) throws IndexOutOfBoundsException {
        apply(y, (a, b) -> a * b);
    }

    default void div(DoubleSeq y) throws IndexOutOfBoundsException {
        apply(y, (a, b) -> a / b);
    }

    //</editor-fold>
}
