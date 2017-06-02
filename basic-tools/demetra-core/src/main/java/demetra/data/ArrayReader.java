/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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

import demetra.design.Immutable;
import java.util.function.IntToDoubleFunction;

/**
 *
 * @author Jean Palate
 */
@Immutable
class ArrayReader implements Doubles {

    static final ArrayReader EMPTY = new ArrayReader(new double[0]);

    private final double[] data;

    ArrayReader(final double[] data) {
        this.data = data;
    }

    @Override
    public CellReader reader() {
        return new Cell();
    }

    @Override
    public double get(int idx) {
        return data[idx];
    }

    @Override
    public int length() {
        return data.length;
    }

    @Override
    public Doubles extract(int start, int length) {
        return new PartialArrayReader(data, start, length);
    }

    @Override
    public String toString() {
        return Doubles.toString(this);
    }

    class Cell implements CellReader {

        private int pos;

        Cell() {
            pos = -1;
        }

        @Override
        public double next() {
            return data[++pos];
        }

        @Override
        public void setPosition(int npos) {
            pos = npos;
        }
    }
}

@Immutable
class PartialArrayReader implements Doubles {

    private final double[] data;
    private final int beg, len;

    PartialArrayReader(final double[] data, int start, int len) {
        this.data = data;
        this.beg = start;
        this.len = len;
    }

    @Override
    public CellReader reader() {
        return new Cell();
    }

    @Override
    public double get(int idx) {
        return data[beg + idx];
    }

    @Override
    public int length() {
        return len;
    }

    @Override
    public Doubles extract(int start, int length) {
        return new PartialArrayReader(data, this.beg + start, length);
    }

    @Override
    public String toString() {
        return Doubles.toString(this);
    }

    class Cell implements CellReader {

        private int pos;

        Cell() {
            pos = -1;
        }

        @Override
        public double next() {
            return data[beg + (++pos)];
        }

        @Override
        public void setPosition(int npos) {
            pos = npos;
        }
    }
}

@Immutable
class FnReader implements Doubles {

    private final IntToDoubleFunction fn;
    private final int n;

    FnReader(final int n, IntToDoubleFunction fn) {
        this.fn = fn;
        this.n = n;
    }

    @Override
    public CellReader reader() {
        return new Cell();
    }

    @Override
    public double get(int idx) {
        return fn.applyAsDouble(idx);
    }

    @Override
    public int length() {
        return n;
    }

    @Override
    public Doubles extract(final int start, final int length) {
        return new FnReader(length, i -> fn.applyAsDouble(i + start));
    }

    class Cell implements CellReader {

        private int pos;

        Cell() {
            pos = -1;
        }

        @Override
        public double next() {
            return fn.applyAsDouble(++pos);
        }

        @Override
        public void setPosition(int npos) {
            pos = npos;
        }
    }
}
