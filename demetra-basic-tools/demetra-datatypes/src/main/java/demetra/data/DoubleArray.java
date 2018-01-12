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

import demetra.design.Immutable;

/**
 *
 * @author Philippe Charles
 */
@Immutable
@lombok.EqualsAndHashCode
final class DoubleArray implements DoubleSequence {


    private final double[] values;

    DoubleArray(double[] values) {
        this.values = values;
    }

    @Override
    public int length() {
        return values.length;
    }

    @Override
    public double get(int index) {
        return values[index];
    }

    @Override
    public double[] toArray() {
        return values.clone();
    }

    @Override
    public void copyTo(double[] buffer, int offset) {
        System.arraycopy(values, 0, buffer, offset, values.length);
    }

    @Override
    public DoubleReader reader() {
        return new Cell();
    }

    @Override
    public DoubleSequence extract(int start, int length) {
        return new PartialDoubleArray(values, start, length);
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
            return values[pos++];
        }

        @Override
        public void setPosition(int npos) {
            pos = npos;
        }
    }
}
