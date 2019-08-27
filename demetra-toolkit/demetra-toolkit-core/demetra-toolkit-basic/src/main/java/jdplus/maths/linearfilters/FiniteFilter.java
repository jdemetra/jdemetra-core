/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.maths.linearfilters;

import demetra.design.Development;
import java.text.NumberFormat;

import jdplus.maths.polynomials.Polynomial;
import java.util.Arrays;
import java.util.function.IntToDoubleFunction;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class FiniteFilter implements IFiniteFilter, Cloneable {

    /**
     *
     * @param l
     * @param d
     * @return
     */
    public static FiniteFilter add(final IFiniteFilter l, final double d) {
        int llb = l.getLowerBound(), lub = l.getUpperBound();
        int lb = llb < 0 ? llb : 0;
        int ub = lub < 0 ? 0 : lub;
        double[] p = new double[ub - lb + 1];
        // p[0] corresponds to x^LB
        IntToDoubleFunction weights = l.weights();
        for (int i = llb; i <= lub; ++i) {
            p[i - lb] = weights.applyAsDouble(i);
        }
        // p[-lb] corresponds to x^0
        p[-lb] += d;
        return FiniteFilter.ofInternal(p, lb);
    }

    /**
     *
     * @param l
     * @param r
     * @return
     */
    public static FiniteFilter add(final IFiniteFilter l, final IFiniteFilter r) {
        // bounds ?
        int llb = l.getLowerBound(), lub = l.getUpperBound(), rlb = r
                .getLowerBound(), rub = r.getUpperBound();
        int lb = llb < rlb ? llb : rlb;
        int ub = lub < rub ? rub : lub;
        double[] p = new double[ub - lb + 1];
        // p[0] corresponds to x^LB
        IntToDoubleFunction lweights = l.weights();
        IntToDoubleFunction rweights = r.weights();
        for (int i = llb; i <= lub; ++i) {
            p[i - lb] = lweights.applyAsDouble(i);
        }
        for (int i = rlb; i <= rub; ++i) {
            p[i - lb] += rweights.applyAsDouble(i);
        }
        return FiniteFilter.ofInternal(p, lb);
    }

    /**
     *
     * @param l
     * @param d
     * @return
     */
    public static FiniteFilter multiply(final IFiniteFilter l, final double d) {
        int lb = l.getLowerBound();
        double[] p = l.weightsToArray();
        for (int i = 0; i < p.length; ++i) {
            p[i] *= d;
        }
        return FiniteFilter.ofInternal(p, lb);
    }

    /**
     *
     * @param l
     * @param r
     * @return
     */
    public static FiniteFilter multiply(final IFiniteFilter l,
            final IFiniteFilter r) {
        int llb = l.getLowerBound(), rlb = r.getLowerBound();
        Polynomial lp = Polynomial.ofInternal(l.weightsToArray()), rp = Polynomial.ofInternal(r.weightsToArray());
        Polynomial w = lp.times(rp);
        return new FiniteFilter(w, llb + rlb);
    }

    /**
     *
     * @param l
     * @return
     */
    public static FiniteFilter negate(final IFiniteFilter l) {
        int lb = l.getLowerBound();
        double[] p = l.weightsToArray();
        for (int i = 0; i < p.length; ++i) {
            p[i] = -p[i];
        }
        return FiniteFilter.ofInternal(p, lb);
    }

    /**
     *
     * @param c
     * @param lb
     * @return
     */
    public static FiniteFilter ofInternal(final double[] c, final int lb) {
        return new FiniteFilter(Polynomial.ofInternal(c), lb);
    }

    /**
     *
     * @param l
     * @param d
     * @return
     */
    public static FiniteFilter subtract(final IFiniteFilter l, double d) {
        return add(l, -d);
    }

    /**
     *
     * @param l
     * @param r
     * @return
     */
    public static FiniteFilter subtract(final IFiniteFilter l,
            final IFiniteFilter r) {
        // bounds ?
        int llb = l.getLowerBound(), lub = l.getUpperBound(), rlb = r
                .getLowerBound(), rub = r.getUpperBound();
        int lb = llb < rlb ? llb : rlb;
        int ub = lub < rub ? rub : lub;
        double[] p = new double[ub - lb + 1];
        // p[0] corresponds to x^LB
        IntToDoubleFunction lweights = l.weights();
        IntToDoubleFunction rweights = r.weights();
        for (int i = llb; i <= lub; ++i) {
            p[i - lb] = lweights.applyAsDouble(i);
        }
        for (int i = rlb; i <= rub; ++i) {
            p[i - lb] -= rweights.applyAsDouble(i);
        }
        return FiniteFilter.ofInternal(p, lb);
    }

    private final int lb;
    private final Polynomial w;

    private static final double EPS = 1e-4;

    // private static final double g_epsilon2 = g_epsilon * g_epsilon;
    /**
     *
     * @param c
     * @param lb
     */
    public FiniteFilter(final double[] c, final int lb) {
        this.lb = lb;
        w = Polynomial.of(c);
    }

    /**
     *
     * @param c
     * @param lb
     */
    public FiniteFilter(final Polynomial c, final int lb) {
        this.lb = lb;
        w = c;
    }

    /**
     *
     * @param f
     */
    public FiniteFilter(final IFiniteFilter f) {
        double[] w = f.weightsToArray();
        this.w = Polynomial.ofInternal(w);
        lb = f.getLowerBound();
    }

    /**
     *
     * @param n
     */
    public FiniteFilter(final int n) {
        double[] weights = new double[n];
        Arrays.fill(weights, 1);
        w = Polynomial.ofInternal(weights);
        lb = 0;
    }

    /**
     *
     * @return
     */
    @Override
    public int length() {
        return w.degree() + 1;
    }

    /**
     *
     * @return
     */
    @Override
    public int getLowerBound() {
        return lb;
    }

    @Override
    public int getUpperBound() {
        return lb + w.degree();
    }

    /**
     *
     * @return
     */
    @Override
    public IntToDoubleFunction weights() {
        return i -> w.get(i - lb);
    }

    /**
     *
     * @return
     */
    public boolean isIdentity() {
        return w.isIdentity();
    }

    /**
     *
     * @return
     */
    public boolean isSymmetric() {
        int d = w.degree();
        if (d % 2 != 0 || d != -lb) {
            return false;
        }
        for (int i = 0; i < d / 2; ++i) {
            if (Math.abs(w.get(i) - w.get(d - i)) > EPS) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @return
     */
    @Override
    public FiniteFilter mirror() {
        Polynomial mw = w.mirror();
        int mlb = -lb - w.degree();
        return new FiniteFilter(mw, mlb);
    }

    @Override
    public String toString() {
        Polynomial p = w.smooth();
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(4);
        format.setMinimumFractionDigits(4);
        // info.NumberDecimalSeparator = "";

        StringBuilder buffer = new StringBuilder(512);
        int curp = lb;
        int n = p.degree();
        for (int i = 0; i <= n; ++i, ++curp) {
            double v = Math.abs(p.get(i));
            if (v >= 1e-6) {
                if (v > p.get(i)) {
                    buffer.append(" - ");
                } else if (i > 0) {
                    buffer.append(" + ");
                }
                if (v != 1 || curp == 0) {
                    buffer.append(format.format(v));
                }
                if (curp < 0) {
                    buffer.append(' ').append('B');
                    if (curp < -1) {
                        buffer.append('^').append(-curp);
                    }
                } else if (curp > 0) {
                    buffer.append(' ').append('F');
                    if (curp > 1) {
                        buffer.append('^').append(curp);
                    }
                }
            }
        }
        return buffer.toString();
    }
}
