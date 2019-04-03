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
package internal.data;

import demetra.data.BaseSeqCursor;
import demetra.data.DoubleSeqCursor;
import demetra.data.DoubleVectorCursor;
import java.util.function.DoubleUnaryOperator;

/**
 *
 * @author Philippe Charles
 */
public abstract class ArrayBaseSeqCursor implements BaseSeqCursor {

    protected int cursor = 0;

    @Override
    public void moveTo(int index) {
        cursor = index;
    }

    @Override
    public void skip(int n) {
        cursor += n;
    }

    public static class ArrayDoubleSeqCursor extends ArrayBaseSeqCursor implements DoubleSeqCursor {

        protected final double[] values;

        public ArrayDoubleSeqCursor(double[] values) {
            this.values = values;
        }

        @Override
        public double getAndNext() throws IndexOutOfBoundsException {
            return values[cursor++];
        }
    }

    public static class ArrayDoubleVectorCursor extends ArrayDoubleSeqCursor implements DoubleVectorCursor {

        public ArrayDoubleVectorCursor(double[] values) {
            super(values);
        }

        @Override
        public void setAndNext(double newValue) throws IndexOutOfBoundsException {
            values[cursor++] = newValue;
        }

        @Override
        public void applyAndNext(DoubleUnaryOperator fn) throws IndexOutOfBoundsException {
            values[cursor] = fn.applyAsDouble(values[cursor]);
            cursor++;
        }
    }
}
