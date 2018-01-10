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
package demetra.data;

import java.util.function.IntToDoubleFunction;

/**
 *
 * @author Philippe Charles
 */
final class IntToDoubleSequence implements DoubleSequence {

    private final int length;
    private final IntToDoubleFunction fn;

    IntToDoubleSequence(int length, IntToDoubleFunction fn) {
        this.length = length;
        this.fn = fn;
    }

    @Override
    public double get(int idx) {
        return fn.applyAsDouble(idx);
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public DoubleSequence extract(final int start, final int length) {
        return new IntToDoubleSequence(length, i -> fn.applyAsDouble(i + start));
    }

    @Override
    public DoubleReader reader() {
        return new Cell();
    }
    
    @Override
    public String toString() {
        return DoubleSequence.toString(this);
    }

    class Cell implements DoubleReader {

        private int pos;

        Cell() {
            pos = 0;
        }

        @Override
        public double next() {
            return fn.applyAsDouble(pos++);
        }

        @Override
        public void setPosition(int npos) {
            pos = npos;
        }
    }
}
