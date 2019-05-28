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

import jdplus.maths.matrices.MatrixException;
import jdplus.data.DataBlock;
import demetra.design.Development;
import demetra.design.Immutable;
import demetra.maths.Complex;
import demetra.maths.linearfilters.internal.SymmetricFilterAlgorithms;
import jdplus.maths.polynomials.Polynomial;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntToDoubleFunction;
import javax.annotation.Nonnull;
import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import demetra.maths.PolynomialType;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable
public final class SymmetricFilter implements IFiniteFilter {

    /**
     *
     */
    public static final SymmetricFilter ZERO = new SymmetricFilter(Polynomial.ZERO);
    /**
     *
     */
    public static final SymmetricFilter ONE = new SymmetricFilter(Polynomial.ONE);

    /**
     *
     * @param d
     * @param f
     * @return
     */
    public static SymmetricFilter add(final double d, final SymmetricFilter f) {
        return f.plus(d);
    }

    /**
     * Computes the symmetric filter defined by f*f.mirror()
     *
     * @param f The initial filter
     * @return f * f.mirror()
     */
    public static SymmetricFilter fromFilter(IFiniteFilter f) {
        return fromFilter(f, 1);
    }

    /**
     * Computes the symmetric filter defined by f*f.mirror()
     *
     * @param f The initial filter
     * @param scaling
     * @return f * f.mirror()
     */
    public static SymmetricFilter fromFilter(IFiniteFilter f, final double scaling) {
        double[] w = f.weightsToArray();
        double[] c = new double[w.length];
        for (int i = 0; i < w.length; ++i) {
            for (int j = i; j < w.length; ++j) {
                c[j - i] += w[i] * w[j];
            }
        }
        if (scaling != 1) {
            for (int i = 0; i < w.length; ++i) {
                c[i] *= scaling;
            }
        }
        return SymmetricFilter.ofInternal(c);

    }

    /**
     * Computes a*f(B)f(F)
     * @param f 
     * @param a The scaling factor
     * @return 
     */
    public static SymmetricFilter convolutionOf(PolynomialType f, final double a) {
        double[] w = f.toArray();
        double[] c = new double[w.length];
        for (int i = 0; i < w.length; ++i) {
            for (int j = i; j < w.length; ++j) {
                c[j - i] += w[i] * w[j];
            }
        }
        if (a != 1) {
            for (int i = 0; i < w.length; ++i) {
                c[i] *= a;
            }
        }
        return SymmetricFilter.ofInternal(c);

    }
    
    /**
     * Creates a symmetric filter using the given weights
     *
     * @param w The full weights ofFunction the filter. The number ofFunction
     * weights should be odd. moreover, they should be symmetric (w[i] ==
     * w[w.getDegree()-i]).
     * @return The corresponding
     */
    public static SymmetricFilter of(final DoubleSeq w) {
        int d = w.length() - 1;
        if (d % 2 != 0) {
            throw new LinearFilterException(
                    LinearFilterException.SFILTER);
        }
        int n = d / 2;
        double[] wc = new double[n + 1];
        for (int i = 0; i <= n; ++i) {
            double x = w.get(n + i);
            if (Math.abs(x - w.get(n - i)) > Polynomial.getEpsilon()) {
                throw new LinearFilterException(
                        LinearFilterException.SFILTER);
            }
            wc[i] = x;
        }
        return SymmetricFilter.ofInternal(wc);
    }

    /**
     *
     * @param d
     * @param f
     * @return
     */
    public static SymmetricFilter multiply(final double d,
            final SymmetricFilter f) {
        return f.times(d);
    }

    /**
     *
     * @param d
     * @param f
     * @return
     */
    public static SymmetricFilter subtract(final double d,
            final SymmetricFilter f) {
        Polynomial tmp = f.polynomial.negate().plus(d);
        return new SymmetricFilter(tmp);
    }

    private final Polynomial polynomial;

    // allows us to reuse ONE and ZERO
    public static SymmetricFilter ofInternal(double[] c) {
        if (c.length == 1) {
            if (c[0] == 1.0) {
                return SymmetricFilter.ONE;
            } else if (c[0] == 0.0) {
                return SymmetricFilter.ZERO;
            }
        }
        return new SymmetricFilter(Polynomial.ofInternal(c));
    }

    /**
     *
     * @param p
     */
    public SymmetricFilter(final Polynomial p) {
        polynomial = p;
    }

    @FunctionalInterface
    public static interface Decomposer {

        BackFilter decompose(final SymmetricFilter filter, final BackFilter Q) throws MatrixException;
    }

    private static final AtomicReference<Decomposer> DEF_DECOMPOSER = new AtomicReference<>(SymmetricFilterAlgorithms.decomposer(null));

    public static void setDefaultDecomposer(@Nonnull Decomposer decomposer) {
        DEF_DECOMPOSER.set(decomposer);
    }

    /**
     * Returns G(B) such that S(F, B)= G(B)* Q(F) + G(F) * Q(B). Cfr
     * Burman/Wilson
     *
     * @param Q(B)
     * @return G(B)
     * @throws MatrixException
     */
    public BackFilter decompose(final BackFilter Q) throws MatrixException {
        return DEF_DECOMPOSER.get().decompose(this, Q);
    }

    @Override
    public void apply(DataBlock in, DataBlock out) {
        double[] pin = in.getStorage();
        int ub = getUpperBound();
        int istart = in.getStartPosition(), iinc = in.getIncrement();
        DoubleSeqCursor.OnMutable cursor = out.cursor();
        if (iinc == 1) {
            int imax = in.getEndPosition() - ub;
            for (int i = istart + ub; i < imax; ++i) {
                double s = pin[i] * polynomial.get(0);
                for (int k = 1; k <= ub; ++k) {
                    s += polynomial.get(k) * (pin[i - k] + pin[i + k]);
                }
                cursor.setAndNext(s);
            }
        } else {
            int imax = in.getEndPosition() - ub * iinc;
            for (int i = istart + ub * iinc; i != imax; i += iinc) {
                double s = pin[i] * polynomial.get(0);
                for (int k = 1, l = iinc; k <= ub; ++k, l += iinc) {
                    s += polynomial.get(k) * (pin[i - l] + pin[i + l]);
                }
                cursor.setAndNext(s);
            }
        }

    }

    // IFilter interface
    /**
     *
     * @param freq
     * @return
     */
    @Override
    public Complex frequencyResponse(final double freq) {
        return Complex.cart(realFrequencyResponse(freq));
    }

    public double realFrequencyResponse(final double freq) {
        // computed by the iteration procedure : cos (i+1)freq + cos (i-1)freq=
        // 2*cos iw *cos freq
        int idx = 0;
        double r = polynomial.get(idx++);
        if (idx >= polynomial.degree() + 1) {
            return r;
        }

        double cos0 = 1, cos1 = Math.cos(freq), cos = cos1;
        do {
            // r+=2*System.Math.Cos((d2-idx)*freq)*m_w[idx--];
            r += 2 * cos1 * polynomial.get(idx++);
            if (idx < polynomial.degree() + 1) {
                double tmp = 2 * cos * cos1 - cos0;
                cos0 = cos1;
                cos1 = tmp;
            } else {
                break;
            }
        } while (true);

        return r;
    }
    /**
     * Returns the coefficients of the symmetric filter, in the form of a
     * polynomial The polynomial corresponds to the weights of the filter, from
     * 0 to n
     *
     * @return
     */
    public Polynomial coefficientsAsPolynomial() {
        return polynomial;
    }

    /**
     *
     * @return
     */
    @Override
    public int getLowerBound() {
        return -polynomial.degree();
    }

    /**
     *
     * @return
     */
    @Override
    public int getUpperBound() {
        return polynomial.degree();
    }

    /**
     *
     * @return
     */
    @Override
    public IntToDoubleFunction weights() {
        return pos -> pos < 0 ? polynomial.get(-pos) : polynomial.get(pos);
    }

    @Override
    public SymmetricFilter mirror() {
        return this;
    }

    /**
     *
     * @return
     */
    public boolean isNull() {
        return polynomial.isZero();
    }

    /**
     *
     * @param d
     * @return
     */
    public SymmetricFilter minus(final double d) {
        return new SymmetricFilter(polynomial.minus(d));
    }

    /**
     *
     * @param r
     * @return
     */
    public SymmetricFilter minus(final SymmetricFilter r) {
        return new SymmetricFilter(polynomial.minus(r.polynomial));
    }

    /**
     *
     * @return
     */
    public SymmetricFilter normalize() {
        double s = polynomial.get(0);
        for (int i = 1; i <= polynomial.degree(); ++i) {
            s += 2 * polynomial.get(i);
        }
        if (s != 0 && s != 1) {
            return new SymmetricFilter(polynomial.times(1 / s));
        } else {
            return this;
        }
    }

    /**
     *
     * @param d
     * @return
     */
    public SymmetricFilter plus(final double d) {
        return new SymmetricFilter(polynomial.plus(d));
    }

    /**
     *
     * @param r
     * @return
     */
    public SymmetricFilter plus(final SymmetricFilter r) {
        return new SymmetricFilter(polynomial.plus(r.polynomial));
    }

    /**
     *
     * @param d
     * @return
     */
    public SymmetricFilter times(final double d) {
        return new SymmetricFilter(polynomial.times(d));
    }

    /**
     *
     * @param r
     * @return
     */
    public SymmetricFilter times(final SymmetricFilter r) {
        int ll = polynomial.degree();
        int lr = r.polynomial.degree();
        double[] o = new double[ll + lr + 1];

        if (r.polynomial.get(0) != 0) {
            for (int u = 0; u <= ll; ++u) {
                o[u] += polynomial.get(u) * r.polynomial.get(0);
            }
        }
        if (polynomial.get(0) != 0) {
            for (int v = 1; v <= lr; ++v) // ne pas compter 2 * l.polynomial[0], r.polynomial[0] !!!
            {
                o[v] += r.polynomial.get(v) * polynomial.get(0);
            }
        }
        for (int u = 1; u <= ll; ++u) {
            if (polynomial.get(u) != 0) {
                for (int v = 1; v <= lr; ++v) {
                    if (r.polynomial.get(v) != 0) {
                        double x = polynomial.get(u) * r.polynomial.get(v);
                        o[u + v] += x;
                        if (u > v) {
                            o[u - v] += x;
                        } else if (u < v) {
                            o[v - u] += x;
                        } else {
                            o[0] += 2 * x;
                        }
                    }
                }
            }
        }
        return SymmetricFilter.ofInternal(o);
    }

    public static class Factorization {

        public Factorization(final BackFilter factor, final double scaling) {
            this.factor = factor;
            this.scaling = scaling;
        }

        public final BackFilter factor;
        public final double scaling;
    }

    /**
     * Generic interface that describe the following factorization problem:
     * Given a symmetric filter S(B, F), find D(B), D(F), v such that S(B, F) =
     * v * D(B) * D(F), D(0) = 1
     *
     * @author Jean Palate
     */
    @Development(status = Development.Status.Release)
    @FunctionalInterface
    public interface Factorizer {

        Factorization factorize(SymmetricFilter sf);
    }

    private static final AtomicReference<Factorizer> DEF_FACTORIZER = new AtomicReference<>(SymmetricFilterAlgorithms.factorizer());

    public static void setDefaultFactorizer(@Nonnull Factorizer factorizer) {
        DEF_FACTORIZER.set(factorizer);
    }

    public Factorization factorize() {
        if (polynomial.degree() == 0) {
            return new Factorization(BackFilter.ONE, polynomial.get(0));
        } else {
            return DEF_FACTORIZER.get().factorize(this);
        }
    }

}
