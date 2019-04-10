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
package internal.data;

import demetra.data.Seq;
import demetra.data.SeqCursor;
import demetra.data.Vector;
import demetra.data.VectorCursor;
import java.util.function.UnaryOperator;

/**
 * Support class of SeqCursor.
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class InternalSeqCursor {

    public static class DefaultSeqCursor<E, S extends Seq<E>> extends InternalBaseSeqCursor.DefaultBaseSeqCursor<S> implements SeqCursor<E> {

        public DefaultSeqCursor(S data) {
            super(data);
        }

        @Override
        public E getAndNext() {
            return data.get(cursor++);
        }
    }

    public static class DefaultVectorCursor<E, V extends Vector<E>> extends DefaultSeqCursor<E, V> implements VectorCursor<E> {

        public DefaultVectorCursor(V data) {
            super(data);
        }

        @Override
        public void setAndNext(E newValue) throws IndexOutOfBoundsException {
            data.set(cursor++, newValue);
        }

        @Override
        public void applyAndNext(UnaryOperator<E> fn) throws IndexOutOfBoundsException {
            data.apply(cursor++, fn);
        }
    }
}
