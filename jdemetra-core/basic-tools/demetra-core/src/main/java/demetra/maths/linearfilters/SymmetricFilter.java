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
import demetra.data.Doubles;
import demetra.design.Development;
import demetra.design.Immutable;
import demetra.maths.Complex;
import demetra.maths.linearfilters.internal.FilterAlgorithms;
import demetra.maths.matrices.*;
import demetra.maths.matrices.internal.CroutDoolittle;
import demetra.maths.polynomials.Polynomial;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntToDoubleFunction;
import javax.annotation.Nonnull;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable
public class SymmetricFilter extends AbstractFiniteFilter {

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
    public static SymmetricFilter convolution(IFiniteFilter f) {
        double[] w = f.toArray();
        double[] c = new double[w.length];
        for (int i = 0; i < w.length; ++i) {
            for (int j = i; j < w.length; ++j) {
                c[j - i] += w[i] * w[j];
            }
        }
        return SymmetricFilter.of(c);
        
    }

    /**
     * Creates a symmetric filter using the given weights
     *
     * @param w The full weights ofFunction the filter. The number ofFunction
     * weights should be odd. moreover, they should be symmetric (w[i] ==
     * w[w.getDegree()-i]).
     * @return The corresponding
     */
    public static SymmetricFilter createFromWeights(final Polynomial w) {
        int d = w.getDegree();
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
        return SymmetricFilter.of(wc);
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
        Polynomial tmp = f.m_p.negate().plus(d);
        return new SymmetricFilter(tmp);
    }
    private final Polynomial m_p;

    // allows us to reuse ONE and ZERO
    public static SymmetricFilter of(double[] c) {
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
        m_p = p;
    }
    
    /**
     * 
     * @param Q
     * @param decomposer May be null. In that case, the default robust linear solver will be used in the computation
     * @return
     * @throws MatrixException 
     */
    public BackFilter decompose(final BackFilter Q, Decomposer decomposer) throws MatrixException {
        return decomposer == null ? DECOMPOSER.get().decompose(this, Q) : decomposer.decompose(this, Q);
    }

    /**
     *
     * @param in
     * @param out
     * @param lb
     * @param ub
     */
    @Override
    protected void defaultFilter(DataBlock in, DataBlock out, int lb, int ub) {
        double[] pin = in.getStorage(), pout = out.getStorage();
        int istart = in.getStartPosition(), iinc = in.getIncrement();
        int ostart = out.getStartPosition(), oend = out.getEndPosition(), oinc = out.getIncrement();
        if (iinc == 1 && oinc == 1) {
            for (int i = istart + ub, j = ostart; j < oend; ++i, ++j) {
                double s = pin[i] * m_p.get(0);
                for (int k = 1; k <= ub; ++k) {
                    s += m_p.get(k) * (pin[i - k] + pin[i + k]);
                }
                pout[j] = s;
            }
        } else {
            for (int i = istart + ub * iinc, j = ostart; j != oend; i += iinc, j += oinc) {
                double s = pin[i] * m_p.get(0);
                for (int k = 1, l = iinc; k <= ub; ++k, l += iinc) {
                    s += m_p.get(k) * (pin[i - l] + pin[i + l]);
                }
                pout[j] = s;
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
        // computed by the iteration procedure : cos (i+1)freq + cos (i-1)freq=
        // 2*cos iw *cos freq
        int idx = 0;
        double r = m_p.get(idx++);
        if (idx >= m_p.getDegree() + 1) {
            return Complex.cart(r);
        }
        
        double cos0 = 1, cos1 = Math.cos(freq), cos = cos1;
        do {
            // r+=2*System.Math.Cos((d2-idx)*freq)*m_w[idx--];
            r += 2 * cos1 * m_p.get(idx++);
            if (idx < m_p.getDegree() + 1) {
                double tmp = 2 * cos * cos1 - cos0;
                cos0 = cos1;
                cos1 = tmp;
            } else {
                break;
            }
        } while (true);
        
        return Complex.cart(r);
    }

    /**
     *
     * @return
     */
    public Doubles coefficients() {
        return m_p.coefficients();
    }
    
    @Override
    public Polynomial asPolynomial() {
        return m_p;
    }

    /**
     *
     * @return
     */
    public int getDegree() {
        return m_p.getDegree();
    }

    /**
     *
     * @return
     */
    @Override
    public int getLowerBound() {
        return -m_p.getDegree();
    }

    /**
     *
     * @return
     */
    @Override
    public int getUpperBound() {
        return m_p.getDegree();
    }

    /**
     *
     * @return
     */
    @Override
    public IntToDoubleFunction weights() {
        return pos -> pos < 0 ? m_p.get(-pos) : m_p.get(pos);
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
        return m_p.isZero();
    }

    /**
     *
     * @param d
     * @return
     */
    public SymmetricFilter minus(final double d) {
        return new SymmetricFilter(m_p.minus(d));
    }

    /**
     *
     * @param r
     * @return
     */
    public SymmetricFilter minus(final SymmetricFilter r) {
        return new SymmetricFilter(m_p.minus(r.m_p));
    }

    /**
     *
     * @return
     */
    public SymmetricFilter normalize() {
        double s = m_p.get(0);
        for (int i = 1; i <= m_p.getDegree(); ++i) {
            s += 2 * m_p.get(i);
        }
        if (s != 0 && s != 1) {
            return new SymmetricFilter(m_p.times(1 / s));
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
        return new SymmetricFilter(m_p.plus(d));
    }

    /**
     *
     * @param r
     * @return
     */
    public SymmetricFilter plus(final SymmetricFilter r) {
        return new SymmetricFilter(m_p.plus(r.m_p));
    }

    /**
     *
     * @param d
     * @return
     */
    public SymmetricFilter times(final double d) {
        return new SymmetricFilter(m_p.times(d));
    }

    /**
     *
     * @param r
     * @return
     */
    public SymmetricFilter times(final SymmetricFilter r) {
        int ll = m_p.getDegree();
        int lr = r.m_p.getDegree();
        double[] o = new double[ll + lr + 1];
        
        if (r.m_p.get(0) != 0) {
            for (int u = 0; u <= ll; ++u) {
                o[u] += m_p.get(u) * r.m_p.get(0);
            }
        }
        if (m_p.get(0) != 0) {
            for (int v = 1; v <= lr; ++v) // ne pas compter 2 * l.m_p[0], r.m_p[0] !!!
            {
                o[v] += r.m_p.get(v) * m_p.get(0);
            }
        }
        for (int u = 1; u <= ll; ++u) {
            if (m_p.get(u) != 0) {
                for (int v = 1; v <= lr; ++v) {
                    if (r.m_p.get(v) != 0) {
                        double x = m_p.get(u) * r.m_p.get(v);
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
        return SymmetricFilter.of(o);
    }

//    public static class Decomposer {
//
//        public BackFilter factorize(SymmetricFilter filter) {
//            nur_ = 0;
//            cur_ = filter.m_p;
//            try {
//                DataBlock C = new DataBlock(filter.getCoefficients());
//                int n = C.getLength();
//                double w = 0;
//                do {
//                    cur_ = fullPolynomial(C);
//                    w = zevaluate(C);
//                    if (Math.abs(w) < EPS) {
//                        if (!simplifyUnitRoot()) {
//                            return null;
//                        }
//                        --n;
//                        C = new DataBlock(cur_.rextract(n - 1, n));
//                    } else if (w < 0) {
//                        return null;
//                    } else {
//                        break;
//                    }
//                } while (n > 0);
//                DataBlock Ce = C.deepClone();
//
//                // initialisation
//                int iter = 0;
//                double[] q = new double[n + nur_];
//                DataBlock Q = new DataBlock(q, 0, n, 1);
//                Matrix T1 = new Matrix(n, n), T2 = new Matrix(n, n);
//                C.mul(1 / w);
//                Ce.mul(1 / w);
//                q[0] = 1;
//                do {
//                    T1.clear();
//                    T2.clear();
//                    for (int i = 0; i < n; i++) {
//                        double r = q[i];
//                        if (r != 0) {
//                            T1.skewDiagonal(i).set(r);
//                            T2.subDiagonal(i).set(r);
//                        }
//                    }
//                    Ce.product(T2.rows(), Q);
//                    if (Ce.distance(C) / n < EPS) {
//                        break;
//                    }
//                    T1.add(T2);
//                    Ce.add(C);
//                    Gauss gauss = new Gauss();
//                    gauss.decompose(T1);
//                    if (!gauss.isFullRank()) {
//                        return null;
//                    }
//                    gauss.solve(Ce, Q);
//                } while (++iter < MAXITER);
//
//                Q.mul(Math.sqrt(w));
//                for (int i = 0; i < nur_; ++i) {
//                    for (int j = n + i; j > 0; --j) {
//                        q[j] -= q[j - 1];
//                    }
//                }
//
//                return BackFilter.ofInternal(q);
//
//
//            } catch (Exception e) {
//                return null;
//            }
//        }
//        private static final int MAXITER = 50;
//        private static final double EPS = 1e-9;
//        private int nur_;
//        private Polynomial cur_;
//        private static Polynomial D = Polynomial.ofInternal(new double[]{-1, 2, -1});
//
//        private Polynomial fullPolynomial(DataBlock d) {
//            int m = d.getLength() - 1;
//            double[] c = new double[2 * m + 1];
//            d.copyTo(c, m);
//            for (int i = 1; i <= m; ++i) {
//                c[m - i] = c[m + i];
//            }
//            return Polynomial.ofInternal(c);
//        }
//
//        private double zevaluate(DataBlock c) {
//            double z = 2 * c.sum();
//            z -= c.get(0);
//            return z;
//        }
//
//        private boolean simplifyUnitRoot() {
//            Polynomial.Division v = Polynomial.divide(cur_, D);
//            if (!v.isExact()) {
//                return false;
//            }
//            ++nur_;
//            cur_ = v.getQuotient();
//            return true;
//        }
//    }
    
    // Algorithms
    
    @FunctionalInterface
    public static interface Decomposer {

        /**
         * Returns G(B) such that S(F, B)= G(B)* Q(F) + G(F) * Q(B). Cfr
         * Burman/Wilson
         *
         * @param Q(B)
         * @return G(B)
         * @throws MatrixException
         */
        BackFilter decompose(final SymmetricFilter filter, final BackFilter Q) throws MatrixException;
    }
    
    private static final AtomicReference<Decomposer> DECOMPOSER = new AtomicReference<>(FilterAlgorithms.symmetricFilterDecomposer(null));
    
    public static void setDefaultDecomposer(@Nonnull Decomposer decomposer){
        DECOMPOSER.set(decomposer);
    }
    
    public static Decomposer getDefaultDecomposer(){
        return DECOMPOSER.get();
    }
    

}
