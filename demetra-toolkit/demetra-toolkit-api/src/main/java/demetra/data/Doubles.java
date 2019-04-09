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

import demetra.design.Immutable;
import demetra.design.Internal;
import java.util.function.IntToDoubleFunction;
import java.util.stream.DoubleStream;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import lombok.AccessLevel;

/**
 * An immutable sequence of doubles.
 *
 * @author Philippe Charles
 */
@Immutable
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
@lombok.EqualsAndHashCode
public final class Doubles implements DoubleSeq {

    public static final Doubles EMPTY = new Doubles(new double[0]);

    @Deprecated
    public static Doubles of(Doubles values) {
        // Refactoring trick; will be removed later on
        return values;
    }

    @Nonnull
    public static Doubles of(@Nonnegative int length, @Nonnull IntToDoubleFunction generator) {
        double[] values = new double[length];
        for (int i = 0; i < values.length; i++) {
            values[i] = generator.applyAsDouble(i);
        }
        return new Doubles(values);
    }

    @Nonnull
    public static Doubles of(@Nonnull DoubleStream stream) {
        return new Doubles(stream.toArray());
    }

    @Nonnull
    public static Doubles of(@Nonnull DoubleSeq seq) {
        return seq instanceof Doubles
                ? (Doubles) seq
                : new Doubles(seq.toArray());
    }

    @Nonnull
    public static Doubles of(@Nonnull double value) {
        return new Doubles(new double[]{value});
    }

    @Nonnull
    public static Doubles of(@Nonnull double[] values) {
        return new Doubles(values.clone());
    }

    @Internal
    @Nonnull
    public static Doubles ofInternal(@Nonnull double[] safeArray) {
        return new Doubles(safeArray);
    }

    private final double[] values;

    @Override
    public double get(int index) throws IndexOutOfBoundsException {
        return values[index];
    }

    @Override
    public int length() {
        return values.length;
    }
}
