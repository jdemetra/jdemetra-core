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

import demetra.design.Immutable;
import demetra.design.Internal;
import java.util.stream.DoubleStream;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
@Immutable
@lombok.EqualsAndHashCode
@lombok.ToString
public final class DoubleValues implements Sequence.OfDouble {

    public static final DoubleValues EMPTY = new DoubleValues(new double[0]);

    /**
     * Creates a new value using an array of doubles. Internal use only since it
     * can break immutability.
     *
     * @param data
     * @return
     */
    @Internal
    @Nonnull
    public static DoubleValues ofInternal(@Nonnull double... data) {
        return data.length > 0 ? new DoubleValues(data) : EMPTY;
    }

    @Nonnull
    public static DoubleValues of(@Nonnull double... data) {
        return ofInternal(data.clone());
    }

    @Nonnull
    public static DoubleValues of(@Nonnull DoubleStream stream) {
        return ofInternal(stream.toArray());
    }

    @Nonnull
    public static DoubleValues of(@Nonnull Sequence.OfDouble seq) {
        return seq instanceof DoubleValues ? (DoubleValues) seq : ofInternal(seq.toArray());
    }

    private final double[] values;

    private DoubleValues(double[] values) {
        this.values = values;
    }

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
}
