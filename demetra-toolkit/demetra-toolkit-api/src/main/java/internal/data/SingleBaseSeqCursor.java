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
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;

/**
 *
 * @author Philippe Charles
 */
public abstract class SingleBaseSeqCursor implements BaseSeqCursor {

    protected int cursor;

    @Override
    public void moveTo(int index) {
        cursor = index;
    }

    @Override
    public void skip(int n) {
        cursor += n;
    }

    public static class SingleDoubleSeqCursor extends SingleBaseSeqCursor implements DoubleSeqCursor {

        protected DoubleSupplier getter;

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

    public static class SingleDoubleVectorCursor extends SingleDoubleSeqCursor implements DoubleVectorCursor {

        protected DoubleConsumer setter;

        public SingleDoubleVectorCursor(DoubleSupplier getter, DoubleConsumer setter) {
            super(getter);
            this.setter = setter;
        }

        @Override
        public void setAndNext(double newValue) throws IndexOutOfBoundsException {
            if (cursor != 0) {
                throw new IndexOutOfBoundsException(String.valueOf(cursor));
            }
            cursor++;
            setter.accept(newValue);
        }

        @Override
        public void applyAndNext(DoubleUnaryOperator fn) throws IndexOutOfBoundsException {
            if (cursor != 0) {
                throw new IndexOutOfBoundsException(String.valueOf(cursor));
            }
            cursor++;
            setter.accept(fn.applyAsDouble(getter.getAsDouble()));
        }
    }
}
