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

import demetra.data.BaseSeq;
import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import demetra.data.DoubleVector;
import demetra.data.DoubleVectorCursor;

/**
 *
 * @author Philippe Charles
 */
public abstract class ArrayBaseSeq implements BaseSeq {

    @lombok.AllArgsConstructor
    @lombok.EqualsAndHashCode(callSuper = false)
    public static class ArrayDoubleSeq extends ArrayBaseSeq implements DoubleSeq {

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
            return new ArrayBaseSeqCursor.ArrayDoubleSeqCursor(values);
        }

        @Override
        public DoubleSeq extract(int start, int length) {
            return new InternalDoubleSeq.PartialDoubleArray(values, start, length);
        }

        @Override
        public String toString() {
            return DoubleSeq.format(this);
        }
    }

    public static class ArrayDoubleVector extends ArrayDoubleSeq implements DoubleVector {

        public ArrayDoubleVector(double[] values) {
            super(values);
        }

        @Override
        public void set(int index, double value) throws IndexOutOfBoundsException {
            values[index] = value;
        }

        @Override
        public DoubleVectorCursor cursor() {
            return new ArrayBaseSeqCursor.ArrayDoubleVectorCursor(values);
        }
    }
}
