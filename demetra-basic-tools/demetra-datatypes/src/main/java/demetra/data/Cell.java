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

import demetra.data.DoubleReader;
import java.util.function.DoubleUnaryOperator;

/**
 *
 * @author Jean Palate
 */
public interface Cell extends DoubleReader {

    public static Cell of(double[] data, int pos, int inc) {
        switch (inc) {
            case 1:
                return new CellP(data, pos);
            case -1:
                return new CellM(data, pos);
            default:
                return new DefaultCell(data, pos, inc);
        }
    }

    public static Cell of(double value) {
        return new SingleCell(value);
    }

    /**
     * Sets the given value at the current position and advance the iterator
     *
     * @param newvalue
     */
    void setAndNext(double newvalue);

    void applyAndNext(DoubleUnaryOperator fn);

}

class DefaultCell implements Cell {

    final double[] data;
    final int inc;
    int pos;

    DefaultCell(double[] data, int leftPos, int inc) {
        this.data = data;
        this.pos = leftPos;
        this.inc = inc;
    }

    @Override
    public double next() {
        double val = data[pos];
        pos = pos + inc;
        return val;
    }

    @Override
    public void setPosition(int npos) {
        pos = npos;
    }

    @Override
    public void setAndNext(double value) {
        data[pos] = value;
        pos = pos + inc;
    }

    @Override
    public void applyAndNext(DoubleUnaryOperator fn) {
        data[pos] = fn.applyAsDouble(data[pos]);
        pos = pos + inc;
    }
}

class CellP implements Cell {

    final double[] data;
    int pos;

    CellP(double[] data, int leftPos) {
        this.data = data;
        this.pos = leftPos;
    }

     @Override
    public double next() {
        return data[pos++];
    }

    @Override
    public void setPosition(int npos) {
        pos = npos;
    }

    @Override
    public void setAndNext(double value) {
        data[pos++] = value;
    }

    @Override
    public void applyAndNext(DoubleUnaryOperator fn) {
        data[pos] = fn.applyAsDouble(data[pos++]);
    }
}

class CellM implements Cell {

    final double[] data;
    int pos;

    CellM(double[] data, int leftPos) {
        this.data = data;
        this.pos = leftPos;
    }

    @Override
    public double next() {
        return data[pos--];
    }

    @Override
    public void setPosition(int npos) {
        pos = npos;
    }

    @Override
    public void setAndNext(double value) {
        data[pos--] = value;
    }

    @Override
    public void applyAndNext(DoubleUnaryOperator fn) {
        data[pos] = fn.applyAsDouble(data[pos--]);
    }
}

class SingleCell implements Cell {

    double value;

    SingleCell(final double value) {
        this.value = value;
    }

    @Override
    public void setAndNext(double newvalue) {
        this.value = newvalue;
    }

    @Override
    public double next() {
        return value;
    }

    @Override
    public void setPosition(int npos) {
    }

    @Override
    public void applyAndNext(DoubleUnaryOperator fn) {
        value = fn.applyAsDouble(value);
    }

}
