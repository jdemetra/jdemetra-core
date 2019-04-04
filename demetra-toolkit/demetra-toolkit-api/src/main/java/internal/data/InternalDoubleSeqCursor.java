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

import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import java.util.function.DoubleSupplier;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class InternalDoubleSeqCursor {

    public static class DefaultDoubleSeqCursor<T extends DoubleSeq> extends InternalBaseSeqCursor.DefaultBaseSeqCursor<T> implements DoubleSeqCursor {

        public DefaultDoubleSeqCursor(T data) {
            super(data);
        }

        @Override
        public double getAndNext() {
            return data.get(cursor++);
        }
    }

    public static class EmptyDoubleSeqCursor extends InternalBaseSeqCursor.EmptyBaseSeqCursor implements DoubleSeqCursor {

        public static final EmptyDoubleSeqCursor DOUBLE_SEQ = new EmptyDoubleSeqCursor();

        @Override
        public double getAndNext() throws IndexOutOfBoundsException {
            throw new IndexOutOfBoundsException("Empty");
        }
    }

    public static class SingleDoubleSeqCursor extends InternalBaseSeqCursor.SingleBaseSeqCursor implements DoubleSeqCursor {

        protected final DoubleSupplier getter;

        public SingleDoubleSeqCursor(DoubleSupplier getter) {
            this.getter = getter;
        }

        @Override
        public double getAndNext() throws IndexOutOfBoundsException {
            if (cursor != 0) {
                throw new IndexOutOfBoundsException(String.valueOf(cursor));
            }
            cursor++;
            return getter.getAsDouble();
        }
    }

    public static class MultiDoubleSeqCursor extends InternalBaseSeqCursor.MultiBaseSeqCursor implements DoubleSeqCursor {

        protected final double[] values;

        public MultiDoubleSeqCursor(double[] values) {
            this.values = values;
        }

        @Override
        public double getAndNext() throws IndexOutOfBoundsException {
            return values[cursor++];
        }
    }

    public static class SubDoubleSeqCursor extends InternalBaseSeqCursor.SubBaseSeqCursor implements DoubleSeqCursor {

        protected final double[] values;

        public SubDoubleSeqCursor(double[] values, int begin) {
            super(begin);
            this.values = values;
        }

        @Override
        public double getAndNext() throws IndexOutOfBoundsException {
            return values[cursor++];
        }
    }
}
