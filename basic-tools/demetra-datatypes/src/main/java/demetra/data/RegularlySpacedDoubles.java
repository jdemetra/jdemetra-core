/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.data;

import demetra.design.Immutable;

/**
 *
 * @author Jean Palate
 */
@Immutable
final class RegularlySpacedDoubles implements DoubleSequence {

    private final double[] data;
    private final int beg, len, inc;

    RegularlySpacedDoubles(final double[] data, int start, int len, int inc) {
        this.data = data;
        this.beg = start;
        this.len = len;
        this.inc = inc;
    }

    @Override
    public DoubleReader reader() {
        return new Cell();
    }

    @Override
    public double get(int idx) {
        return data[beg + idx * inc];
    }

    @Override
    public int length() {
        return len;
    }

    @Override
    public double[] toArray() {
        double[] ndata = new double[len];
        for (int i = 0, j = beg; i < len; ++i, j += inc) {
            ndata[i] = data[j];
        }
        return ndata;
    }

    @Override
    public DoubleSequence extract(int start, int length) {
        return new RegularlySpacedDoubles(data, this.beg + start * inc, length, inc);
    }

    @Override
    public String toString() {
        return DoubleSequence.toString(this);
    }

    class Cell implements DoubleReader {

        private int pos;

        Cell() {
            pos = beg;
        }

        @Override
        public double next() {
            double val=data[pos];
            pos += inc;
            return val;
        }

        @Override
        public void setPosition(int npos) {
            pos = beg + npos*inc;
        }
    }
}
