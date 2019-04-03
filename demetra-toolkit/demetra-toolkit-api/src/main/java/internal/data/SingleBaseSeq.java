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
public abstract class SingleBaseSeq implements BaseSeq {

    @Override
    public int length() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @lombok.AllArgsConstructor
    public static class SingleDoubleSeq extends SingleBaseSeq implements DoubleSeq {

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
            return new SingleBaseSeqCursor.SingleDoubleSeqCursor(this::getValue);
        }
    }

    public static class SingleDoubleVector extends SingleDoubleSeq implements DoubleVector {

        public SingleDoubleVector(double value) {
            super(value);
        }

        public void setValue(double value) {
            this.value = value;
        }

        @Override
        public void set(int index, double value) throws IndexOutOfBoundsException {
            if (index == 0) {
                this.value = value;
            }
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }

        @Override
        public DoubleVectorCursor cursor() {
            return new SingleBaseSeqCursor.SingleDoubleVectorCursor(this::getValue, this::setValue);
        }
    }
}
