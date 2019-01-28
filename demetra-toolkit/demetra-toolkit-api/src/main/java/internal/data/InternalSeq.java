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

import demetra.data.Sequence;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.IntFunction;

/**
 * Support class of Sequence.
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class InternalSeq {

    @lombok.RequiredArgsConstructor
    public static final class SequenceIterator<E> implements Iterator<E> {

        private final Sequence<E> seq;
        private int cur = 0;

        @Override
        public boolean hasNext() {
            return cur < seq.length();
        }

        @Override
        public E next() {
            if (hasNext()) {
                return seq.get(cur++);
            } else {
                throw new NoSuchElementException();
            }
        }
    }

    public <E> void forEach(Sequence<E> seq, Consumer<? super E> action) {
        for (int i = 0; i < seq.length(); i++) {
            action.accept(seq.get(i));
        }
    }

    public <E> E[] toArray(Sequence<E> seq, IntFunction<E[]> generator) {
        E[] result = generator.apply(seq.length());
        for (int i = 0; i < result.length; i++) {
            result[i] = seq.get(i);
        }
        return result;
    }
}
