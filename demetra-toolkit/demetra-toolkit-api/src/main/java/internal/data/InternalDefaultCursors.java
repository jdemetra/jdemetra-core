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
import demetra.data.BaseSeqCursor;
import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import demetra.data.DoubleVector;
import demetra.data.DoubleVectorCursor;
import java.util.function.DoubleUnaryOperator;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class InternalDefaultCursors {

    private static class DefaultBaseSeqCursor<T extends BaseSeq> implements BaseSeqCursor {

        protected final T data;
        protected int cursor = 0;

        public DefaultBaseSeqCursor(T data) {
            this.data = data;
        }

        @Override
        public void moveTo(int index) {
            cursor = index;
        }

        @Override
        public void skip(int n) {
            cursor += n;
        }
    }

    public static class DefaultDoubleSeqCursor<T extends DoubleSeq> extends DefaultBaseSeqCursor<T> implements DoubleSeqCursor {

        public DefaultDoubleSeqCursor(T data) {
            super(data);
        }

        @Override
        public double getAndNext() {
            return data.get(cursor++);
        }
    }

    public static class DefaultDoubleVectorCursor<T extends DoubleVector> extends DefaultDoubleSeqCursor<T> implements DoubleVectorCursor {

        public DefaultDoubleVectorCursor(T data) {
            super(data);
        }

        @Override
        public void setAndNext(double newValue) {
            data.set(cursor++, newValue);
        }

        @Override
        public void applyAndNext(DoubleUnaryOperator fn) {
            data.apply(cursor++, fn);
        }
    }
}
