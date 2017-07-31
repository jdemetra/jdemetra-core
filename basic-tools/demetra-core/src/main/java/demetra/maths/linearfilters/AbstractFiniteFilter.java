/*
* Copyright 2013 National Bank ofFunction Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions ofFunction the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy ofFunction the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
 */
package demetra.maths.linearfilters;

import demetra.data.DataBlock;
import demetra.data.DataWindow;
import demetra.design.Development;
import demetra.maths.Complex;
import java.util.Formatter;
import java.util.function.IntToDoubleFunction;
import demetra.data.DoubleSequence;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public abstract class AbstractFiniteFilter implements IFiniteFilter {

    /**
     *
     * @param in
     * @param out
     * @param lb
     * @param ub
     */
    protected void defaultFilter(DataBlock in, DataBlock out, int lb, int ub) {
        int nw = ub - lb + 1;
        DataWindow cur = in.left();
        int len = in.length() - nw + 1;
        IntToDoubleFunction weights = weights();
        double w = weights.applyAsDouble(lb);
        out.setAY(w, cur.next(len));
        for (int j = 1; j < nw; ++j) {
            w = weights.applyAsDouble(lb + j);
            out.addAY(w, cur.move(1));
        }
    }

    /**
     *
     * @param in
     * @param out
     * @param lb
     * @param ub
     */
    protected void exFilter(final DataBlock in, final DataBlock out,
            final int lb, final int ub) {
        if (lb > 0 || ub < 0) {
            throw new LinearFilterException(
                    LinearFilterException.SFILTER);
        }
        defaultFilter(in, out.drop(-lb, ub), lb, ub);
    }

    /**
     *
     * @param in
     * @param out
     */
    public void extendedFilter(final DataBlock in, final DataBlock out) {
        int lb = getLowerBound(), ub = getUpperBound();
        int nw = ub - lb + 1;
        IntToDoubleFunction weights = weights();
        out.setAY(weights.applyAsDouble(ub--), in);
        DataWindow wout = out.window(), win = in.window();
        for (int j = 1; j < nw; ++j) {
            wout.bshrink().addAY(weights.applyAsDouble(ub--), win.eshrink());
        }
    }

    /**
     *
     * @param in
     * @param out
     * @return
     */
    @Override
    public void apply(DataBlock in, DataBlock out) {
        int lb = getLowerBound(), ub = getUpperBound();
        int nw = ub - lb + 1;
        int nin = in.length();
        int nout = out.length();
        if (nin == nout) {
            extendedFilter(in, out);
        } else {
            if (nin < nw || out.length() != nin - nw + 1) {
                throw new LinearFilterException(LinearFilterException.LENGTH);
            }
            defaultFilter(in, out, lb, ub);
        }
    }

    @Override
    public void apply(IntToDoubleFunction in, IFilterOutput out) {
        IntToDoubleFunction weights = weights();
        int lb = getLowerBound(), ub = getUpperBound();
        for (int i = out.getStart(); i < out.getEnd(); ++i) {
            double s = 0;
            for (int j = lb; j <= ub; ++j) {
                s += weights.applyAsDouble(j) * in.applyAsDouble(i + j);
            }
            out.set(i, s);
        }
    }

    public void apply2(IntToDoubleFunction in, IFilterOutput out) {
        IntToDoubleFunction weights = weights();
        int lb = getLowerBound(), ub = getUpperBound();
        double w = weights.applyAsDouble(lb);
        for (int i = out.getStart(); i < out.getEnd(); ++i) {
            out.set(i, w * in.applyAsDouble(i + lb));
        }
        for (int j = lb + 1; j <= ub; ++j) {
            w = weights.applyAsDouble(j);
            if (w != 0) {
                for (int i = out.getStart(); i < out.getEnd(); ++i) {
                    out.add(i, w * in.applyAsDouble(i + j));
                }
            }
        }
    }

    /**
     *
     * @param freq
     * @return
     */
    @Override
    public Complex frequencyResponse(final double freq) {
        return Utilities.frequencyResponse(weights(), getLowerBound(), getUpperBound(), freq);
    }

    /**
     *
     * @return
     */
    @Override
    public int length() {
        return getUpperBound() - getLowerBound() + 1;
    } // UB-LB+1

    /**
     *
     * @return
     */
    @Override
    public abstract int getLowerBound();

    @Override
    public abstract int getUpperBound();

    /**
     *
     * @return
     */
    @Override
    public boolean hasLowerBound() {
        return true;
    }

    @Override
    public boolean hasUpperBound() {
        return true;
    }

    /**
     * Solves recursively the relationship: F * out = in, considering that the
     * initial values are 0.
     *
     * @param in
     * @param out
     */
    public void solve(final double[] in, final double[] out) {
        int n = in.length;

        double[] w = weightsToArray();
        int u = w.length - 1;

        // initial iterations
        int nmax = Math.min(w.length, n);
        double a = w[u];
        for (int i = 0; i < nmax; ++i) {
            double z = in[i];
            for (int j = 1; j <= i; ++j) {
                z -= out[i - j] * w[u - j];
            }
            out[i] = z / a;
        }
        for (int i = w.length; i < n; ++i) {
            double z = in[i];
            for (int j = 1; j <= u; ++j) {
                z -= out[i - j] * w[u - j];
            }
            out[i] = z / a;
        }

    }

    public void solve(final DataBlock in, final DataBlock out) {
        int n = in.length();

        double[] w = weightsToArray();
        int u = w.length - 1;

        // initial iterations
        int nmax = Math.min(w.length, n);
        double a = w[u];
        for (int i = 0; i < nmax; ++i) {
            double z = in.get(i);
            for (int j = 1; j <= i; ++j) {
                z -= out.get(i - j) * w[u - j];
            }
            out.set(i, z / a);
        }
        for (int i = w.length; i < n; ++i) {
            double z = in.get(i);
            for (int j = 1; j <= u; ++j) {
                z -= out.get(i - j) * w[u - j];
            }
            out.set(i, z / a);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String fmt = "%6g";
        boolean sign = false;
        IntToDoubleFunction weights = this.weights();
        for (int i = getLowerBound(); i <= getUpperBound(); ++i) {
            double v = weights.applyAsDouble(i);
            double av = Math.abs(v);
            if (av >= 1e-6) {
                if (av > v) {
                    sb.append(" - ");
                } else if (sign) {
                    sb.append(" + ");
                }
                if ((av != 1) || (i == 0)) {
                    sb.append(new Formatter().format(fmt, av).toString());
                }
                sign = true;
                if (i < 0) {
                    sb.append("(t-").append(-i).append(')');
                } else if (i > 0) {
                    sb.append("(t+").append(i).append(')');
                } else {
                    sb.append("(t)");
                }
            }
        }
        return sb.toString();

    }
}
