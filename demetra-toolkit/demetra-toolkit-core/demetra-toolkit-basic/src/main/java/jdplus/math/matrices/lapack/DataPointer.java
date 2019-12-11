/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.lapack;

import java.util.function.DoublePredicate;
import jdplus.data.DataBlock;

/**
 * Light-weight class for passing parameters
 * The content of the class should not be modified by the called methods.
 * @author palatej
 */
public abstract class DataPointer {

    final double[] p;
    int pos;

    public static DataPointer of(final double[] storage, final int pos) {
        return new CPointer(storage, pos);
    }

    public static DataPointer of(final double[] storage, final int pos, int inc) {
        if (inc == 1) {
            return new CPointer(storage, pos);
        } else {
            return new RPointer(storage, pos, inc);
        }
    }

    public static DataPointer of(DataBlock z) {
        return of(z.getStorage(), z.getStartPosition(), z.getIncrement());
    }

    protected DataPointer(final double[] storage, final int pos) {
        this.p = storage;
        this.pos = pos;
    }

    public abstract int inc();

    public final double[] p() {
        return p;
    }

    public final int pos() {
        return pos;
    }

    public final void pos(int npos) {
        pos = npos;
    }

    public final void move(int del) {
        pos += del;
    }

    public double value() {
        return p[pos];
    }

    public void value(double nval) {
        p[pos]=nval;
    }

    public abstract double value(int n);
    
    public double fastNorm2(int n){
        return Math.sqrt(ssq(n));
    }

    public double norm2(int n) {
        int inc = inc();
        if (n < 1) {
            return 0;
        } else if (n == 1) {
            return Math.abs(p[pos]);
        } else {
            int imax = pos + n * inc;
            double scale = 0;
            double ssq = 1;
            for (int i = pos; i < imax; i += inc) {
                double xcur = p[i];
                if (xcur != 0) {
                    double absxi = Math.abs(xcur);
                    if (scale < absxi) {
                        double tmp = scale / absxi;
                        ssq = 1 + ssq * tmp * tmp;
                        scale = absxi;
                    } else {
                        double tmp = absxi / scale;
                        ssq += tmp * tmp;
                    }
                }
            }
            return scale * Math.sqrt(ssq);
        }
    }
    
    public abstract boolean test(int n, DoublePredicate pred);

    public abstract void mul(int n, double a);

    public abstract void div(int n, double a);

    public abstract void add(int n, double a);

    public abstract void set(int n, double a);

    public abstract void chs(int n);

    public abstract double sum(int n);

    public abstract double asum(int n);

    public abstract double ssq(int n);

    public abstract double dot(int n, DataPointer x);

    public abstract void addAX(int n, double a, DataPointer x);
}
