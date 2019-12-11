/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.lapack;

import java.util.function.DoublePredicate;

/**
 *
 * @author palatej
 */
@lombok.ToString
final class CPointer extends DataPointer {

    CPointer(final double[] storage, final int pos) {
        super(storage, pos);
    }

    @Override
    public int inc() {
        return 1;
    }

    @Override
    public double value(int n) {
        return p[pos + n];
    }

    @Override
    public double dot(int n, DataPointer x) {
        int imax = pos + n;
        int xinc = x.inc();
        double s = 0;
        for (int i = pos, j = x.pos; i < imax; ++i, j += xinc) {
            s += p[i] * x.p[j];
        }
        return s;
    }

    @Override
    public void addAX(int n, double a, DataPointer x) {
        int imax = pos + n;
        int xinc = x.inc();
        for (int i = pos, j = x.pos; i < imax; ++i, j += xinc) {
            p[i] += a * x.p[j];
        }
    }

    public void addAX(int n, double a, CPointer x) {
        int imax = pos + n;
        for (int i = pos, j = x.pos; i < imax; ++i, ++j) {
            p[i] += a * x.p[j];
        }
    }

    @Override
    public boolean test(int n, DoublePredicate pred) {
        int imax = pos + n;
        for (int i = pos; i < imax; ++i) {
            if (!pred.test(p[i]))
                return false;
        }
        return true;
    }
    
    double dot(int n, CPointer x) {
        int imax = pos + n;
        double s = 0;
        for (int i = pos, j = x.pos; i < imax; ++i, ++j) {
            s += p[i] * x.p[j];
        }
        return s;
    }

    void add(int n, CPointer x) {
        int imax = pos + n;
        for (int i = pos, j = x.pos; i < imax; ++i, ++j) {
            p[i] += x.p[j];
        }
    }

    void sub(int n, CPointer x) {
        int imax = pos + n;
        for (int i = pos, j = x.pos; i < imax; ++i, ++j) {
            p[i] -= x.p[j];
        }
    }

    void copy(int n, CPointer x) {
        int imax = pos + n;
        for (int i = pos, j = x.pos; i < imax; ++i, ++j) {
            p[i] = x.p[j];
        }
    }

    @Override
    public void mul(int n, double a) {
        int imax = pos + n;
        for (int i = pos; i < imax; ++i) {
            p[i] *= a;
        }
    }

    @Override
    public void div(int n, double a) {
        int imax = pos + n;
        for (int i = pos; i < imax; ++i) {
            p[i] /= a;
        }
    }

    @Override
    public void add(int n, double a) {
        int imax = pos + n;
        for (int i = pos; i < imax; ++i) {
            p[i] += a;
        }
    }

    @Override
    public void set(int n, double a) {
        int imax = pos + n;
        for (int i = pos; i < imax; ++i) {
            p[i] = a;
        }
    }

    @Override
    public void chs(int n) {
        int imax = pos + n;
        for (int i = pos; i < imax; ++i) {
            p[i] = -p[i];
        }
    }

    void swap(int n, CPointer other) {
        int imax = pos + n;
        for (int i = pos, j = other.pos; i < imax; ++i, ++j) {
            double tmp = p[i];
            p[i] = other.p[j];
            p[j] = tmp;
        }
    }

    @Override
    public double asum(int n) {
        double rslt = 0;
        int jmax = pos + n;
        for (int j = pos; j < jmax; ++j) {
            rslt += Math.abs(p[j]);
        }
        return rslt;
    }

    @Override
    public double sum(int n) {
        double rslt = 0;
        int jmax = pos + n;
        for (int j = pos; j < jmax; ++j) {
            rslt += p[j];
        }
        return rslt;
    }

    @Override
    public double ssq(int n) {
        double d = 0;
        int imax = pos + n;
        for (int i = pos; i < imax; ++i) {
            double cur = p[i];
            d += cur * cur;
        }
        return d;
    }
    
    public double fastNorm(int n){
        return Math.sqrt(ssq(n));
    }

}
