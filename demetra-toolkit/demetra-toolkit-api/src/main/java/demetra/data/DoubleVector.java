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
import internal.data.InternalDefaultCursors;
import java.util.function.DoubleUnaryOperator;
import javax.annotation.Nonnegative;

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

    default void apply(@Nonnegative int index, DoubleUnaryOperator fn) throws IndexOutOfBoundsException {
        set(index, fn.applyAsDouble(get(index)));
    }

    @Override
    default DoubleVectorCursor cursor() {
        return new InternalDefaultCursors.DefaultDoubleVectorCursor(this);
    }
}
