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

import nbbrd.design.Development;
import nbbrd.design.Immutable;
import nbbrd.design.Internal;
import java.util.function.IntToDoubleFunction;
import java.util.stream.DoubleStream;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import lombok.AccessLevel;

/**
 * An immutable sequence of doubles.
 *
 * @author Philippe Charles
 */
@Immutable
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
@lombok.EqualsAndHashCode
@Development(status = Development.Status.Release)
public final class Doubles implements DoubleSeq {

    public static final Doubles EMPTY = new Doubles(new double[0]);

    @Deprecated
    public static Doubles of(Doubles values) {
        // Refactoring trick; will be removed later on
        return values;
    }

    @NonNull
    public static Doubles of(@NonNegative int length, @NonNull IntToDoubleFunction generator) {
        double[] values = new double[length];
        for (int i = 0; i < values.length; i++) {
            values[i] = generator.applyAsDouble(i);
        }
        return new Doubles(values);
    }

    @NonNull
    public static Doubles of(@NonNull DoubleStream stream) {
        return new Doubles(stream.toArray());
    }

    @NonNull
    public static Doubles of(@NonNull DoubleSeq seq) {
        return seq instanceof Doubles
                ? (Doubles) seq
                : new Doubles(seq.toArray());
    }

    @NonNull
    public static Doubles of(@NonNull double value) {
        return new Doubles(new double[]{value});
    }

    @NonNull
    public static Doubles of(@NonNull double[] values) {
        return new Doubles(values.clone());
    }

    @Internal
    @NonNull
    public static Doubles ofInternal(@NonNull double[] safeArray) {
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
    
    @Override
    public String toString(){
        return DoubleSeq.format(this);
    }
}
