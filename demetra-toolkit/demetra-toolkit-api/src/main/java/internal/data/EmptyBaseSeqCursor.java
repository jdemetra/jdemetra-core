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
public abstract class EmptyBaseSeqCursor implements BaseSeqCursor {

    @Override
    public void moveTo(int index) {
    }

    @Override
    public void skip(int n) {
    }

    public static class EmptyDoubleSeqCursor extends EmptyBaseSeqCursor implements DoubleSeqCursor {

        public static final EmptyDoubleSeqCursor DOUBLE_SEQ = new EmptyDoubleSeqCursor();

        @Override
        public double getAndNext() throws IndexOutOfBoundsException {
            throw new IndexOutOfBoundsException("Empty");
        }
    }

    public static class EmptyDoubleVectorCursor extends EmptyDoubleSeqCursor implements DoubleVectorCursor {

        public static final EmptyDoubleVectorCursor DOUBLE_VECTOR = new EmptyDoubleVectorCursor();

        @Override
        public void setAndNext(double newValue) throws IndexOutOfBoundsException {
            throw new IndexOutOfBoundsException("Empty");
        }

        @Override
        public void applyAndNext(DoubleUnaryOperator fn) throws IndexOutOfBoundsException {
            throw new IndexOutOfBoundsException("Empty");
        }
    }
}
