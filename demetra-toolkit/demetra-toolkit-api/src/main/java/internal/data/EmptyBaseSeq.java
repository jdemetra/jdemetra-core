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
import java.util.Objects;

/**
 *
 * @author Philippe Charles
 */
public abstract class EmptyBaseSeq implements BaseSeq {

    @Override
    public int length() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    public static class EmptyDoubleSeq extends EmptyBaseSeq implements DoubleSeq {

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
            return EmptyBaseSeqCursor.EmptyDoubleSeqCursor.DOUBLE_SEQ;
        }
    }

    public static class EmptyDoubleVector extends EmptyDoubleSeq implements DoubleVector {

        public static final EmptyDoubleVector DOUBLE_VECTOR = new EmptyDoubleVector();

        @Override
        public void set(int index, double value) throws IndexOutOfBoundsException {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }

        @Override
        public DoubleVectorCursor cursor() {
            return EmptyBaseSeqCursor.EmptyDoubleVectorCursor.DOUBLE_VECTOR;
        }
    }
}
