/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.data;

import java.util.function.DoubleUnaryOperator;

/**
 *
 * @author Jean Palate
 */
public final class FastArray implements DoubleVector {

    private class FastCursor implements DoubleVectorCursor {

        private int pos;

        @Override
        public void setAndNext(double newValue) throws IndexOutOfBoundsException {
            data[pos++] = newValue;
        }

        @Override
        public void applyAndNext(DoubleUnaryOperator fn) throws IndexOutOfBoundsException {
            double x = data[pos];
            data[pos++] = fn.applyAsDouble(x);
        }

        @Override
        public double getAndNext() throws IndexOutOfBoundsException {
            return data[pos++];
        }

        @Override
        public void moveTo(int index) {
            pos = beg + index;
        }

        @Override
        public void skip(int n) {
            pos += n;
        }

    }

    private final double[] data;
    private int beg, end;

    public FastArray(double[] data) {
        this.data = data;
        beg = 0;
        end = data.length;
    }

    public FastArray(double[] data, int beg, int end) {
        this.data = data;
        this.beg = beg;
        this.end = end;
    }

    @Override
    public void set(int index, double value) throws IndexOutOfBoundsException {
        data[beg + index] = value;
    }

    @Override
    public double get(int index) throws IndexOutOfBoundsException {
        return data[beg + index];
    }

    @Override
    public int length() {
        return end - beg;
    }

    @Override
    public DoubleVectorCursor cursor() {
        return new FastCursor();
    }

    /**
     * @return the data
     */
    public double[] getData() {
        return data;
    }

    /**
     * @return the beg
     */
    public int getBegin() {
        return beg;
    }

    /**
     * @return the end
     */
    public int getEnd() {
        return end;
    }

    @Override
    public double sum() {
        double s = 0;
        for (int i = beg; i < end; ++i) {
            double x = data[i];
            if (Double.isFinite(x)) {
                s += x;
            }
        }
        return s;
    }

    public void set(double value) {
        for (int i = beg; i < end; ++i) {
            data[i] = value;
        }
    }

    public void set(FastArray y) {
        for (int i = beg, j = y.beg; i < end; ++i, ++j) {
            data[i] = y.data[j];
        }
    }

    public void setAY(double a, FastArray y) {
        if (a == 0) {
            set(0);
        } else if (a == 1) {
            set(y);
        } else {
            for (int i = beg, j = y.beg; i < end; ++i, ++j) {
                data[i] = a * y.data[j];
            }
        }
    }

    @Override
    public void add(double a) {
        if (a != 0) {
            for (int i = beg; i < end; ++i) {
                data[i] += a;
            }
        }
    }

    public void add(FastArray y) {
        for (int i = beg, j = y.beg; i < end; ++i, ++j) {
            data[i] += y.data[j];
        }
    }

    public void addAY(double a, FastArray y) {
        if (a == 1) {
            add(y);
        } else if (a == -1) {
            sub(y);
        } else if (a != 0) {
            for (int i = beg, j = y.beg; i < end; ++i, ++j) {
                data[i] += a * y.data[j];
            }
        }
    }

    @Override
    public void sub(double a) {
        if (a != 0) {
            for (int i = beg; i < end; ++i) {
                data[i] -= a;
            }
        }
    }

    public void chs() {
        for (int i = beg; i < end; ++i) {
            data[i] = -data[i];
        }
    }

    public void sub(FastArray y) {
        for (int i = beg, j = y.beg; i < end; ++i, ++j) {
            data[i] -= y.data[j];
        }
    }

    public void mul(double a) {
        if (a == 0) {
            set(0);
        } else if (a == -1) {
            chs();
        } else if (a != 1) {
            for (int i = beg; i < end; ++i) {
                data[i] *= a;
            }
        }
    }

    public void mul(FastArray y) {
        for (int i = beg, j = y.beg; i < end; ++i, ++j) {
            data[i] *= y.data[j];
        }
    }

    public void slide(int n) {
        beg += n;
        end += n;
    }

}
