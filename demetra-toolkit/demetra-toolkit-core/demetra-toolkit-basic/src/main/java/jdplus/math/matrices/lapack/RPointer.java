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
final class RPointer extends DataPointer {

    final int inc;

    RPointer(final double[] storage, final int pos, final int inc) {
        super(storage, pos);
        this.inc = inc;
    }

    @Override
    public int inc() {
        return inc;
    }

    @Override
    public double value(int n) {
        return p[pos + n * inc];
    }

    @Override
    public double dot(int n, DataPointer x) {
        int xinc = x.inc();
        double s = 0;
        int imax = pos + n;
        for (int i = pos, j = x.pos; i != imax; i += inc, j += xinc) {
            s += p[i] * x.p[j];
        }
        return s;
    }

    @Override
    public void addAX(int n, double a, DataPointer x) {
        int xinc = x.inc();
        int imax = pos + n * inc;
        for (int i = pos, j = x.pos; i != imax; i += inc, j += xinc) {
            p[i] += a * x.p[j];
        }
    }

    double dot(int n, RPointer x) {
        int imax = pos + n * inc;
        double s = 0;
        for (int i = pos, j = x.pos; i != imax; i += inc, j += x.inc) {
            s += p[i] * x.p[j];
        }
        return s;
    }

    @Override
    public boolean test(int n, DoublePredicate pred) {
        int imax = pos + n * inc;
        for (int i = pos; i != imax; i += inc) {
            if (!pred.test(p[i]))
                return false;
        }
        return true;
    }

    void add(int n, RPointer x) {
        int imax = pos + n * inc;
        for (int i = pos, j = x.pos; i != imax; i += inc, j += x.inc) {
            p[i] += x.p[j];
        }
    }

    void sub(int n, RPointer x) {
        int imax = pos + n * inc;
        for (int i = pos, j = x.pos; i != imax; i += inc, j += x.inc) {
            p[i] -= x.p[j];
        }
    }

    void copy(int n, RPointer x) {
        int imax = pos + n * inc;
        for (int i = pos, j = x.pos; i != imax; i += inc, j += x.inc) {
            p[i] = x.p[j];
        }
    }

    @Override
    public void mul(int n, double a) {
        int imax = pos + n * inc;
        for (int i = pos; i != imax; i += inc) {
            p[i] *= a;
        }
    }

    @Override
    public void div(int n, double a) {
        int imax = pos + n * inc;
        for (int i = pos; i != imax; i += inc) {
            p[i] /= a;
        }
    }

    @Override
    public void add(int n, double a) {
        int imax = pos + n * inc;
        for (int i = pos; i != imax; i += inc) {
            p[i] += a;
        }
    }

    @Override
    public void set(int n, double a) {
        int imax = pos + n * inc;
        for (int i = pos; i != imax; i += inc) {
            p[i] = a;
        }
    }

    @Override
    public void chs(int n) {
        int imax = pos + n * inc;
        for (int i = pos; i != imax; i += inc) {
            p[i] = -p[i];
        }
    }

    @Override
    public double asum(int n) {
        double rslt = 0;
        int jmax = pos + n * inc;
        for (int j = pos; j != jmax; j += inc) {
            rslt += Math.abs(p[j]);
        }
        return rslt;
    }

    @Override
    public double sum(int n) {
        double rslt = 0;
        int jmax = pos + n * inc;
        for (int j = pos; j != jmax; j += inc) {
            rslt += p[j];
        }
        return rslt;
    }

    @Override
    public double ssq(int n) {
        double d = 0;
        int imax = pos + n * inc;
        for (int i = pos; i != imax; i += inc) {
            double cur = p[i];
            d += cur * cur;
        }
        return d;
    }

    void swap(int n, RPointer x) {
        int imax = pos + n * inc;
        for (int i = pos, j = x.pos; i < imax; i += inc, j += x.inc) {
            double tmp = p[i];
            p[i] = x.p[j];
            p[j] = tmp;
        }
    }
}
