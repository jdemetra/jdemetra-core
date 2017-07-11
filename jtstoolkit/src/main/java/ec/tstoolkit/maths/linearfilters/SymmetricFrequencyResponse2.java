/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
package ec.tstoolkit.maths.linearfilters;

import ec.tstoolkit.data.TableOfLong;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.design.Immutable;
import ec.tstoolkit.maths.Complex;
import ec.tstoolkit.maths.Simplifying;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.MatrixException;
import ec.tstoolkit.maths.matrices.UpperTriangularMatrix;
import ec.tstoolkit.maths.polynomials.IRootsSolver;
import ec.tstoolkit.maths.polynomials.Polynomial;

/**
 * Considering the symmetric filter P(B)*P(F), where B is the backward operator
 * and F is the forward operator, and its Fourier transform
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable
public class SymmetricFrequencyResponse2 {

    /**
     *
     */
    public static final double TwoPi = Math.PI * 2;
    private static Matrix g_u;

    private static synchronized Matrix _transform(final int r) {
        if (g_u == null || g_u.getRowsCount() < r) {
            g_u = transform(r);
        }
        return g_u;
    }

    /**
     *
     * @param f
     * @return
     */
    public static SymmetricFrequencyResponse2 createFromFilter(final IFiniteFilter f) {
        return new SymmetricFrequencyResponse2(SymmetricFilter.createFromFilter(f));
    }

    /**
     * @param c On entry, coefficients of a symmetric filter (Fourier
     * transform). On exit, the transformed coefficients, corresponding to the
     * polynomial in 2*cos(x)
     */
    private static void D2SFR(final double[] c) {
        int q = c.length;
        // first, transform the coefficients so they correspond to a funcion in
        // cos nt.
        for (int i = 1; i < q; ++i) {
            c[i] *= 2;
        }
        // then compute the transformation to (cos t)^n.
        if (q > 2) {
            Matrix u = _transform(q);
            UpperTriangularMatrix.rmul(u, c);
        }
    }

    /**
     * Reciprocal function of D2SFR
     *
     * @param c On entry, the transformed coefficients, corresponding to the
     * polynomial in cos(x). On exit, coefficients of a symmetric filter
     * (Fourier transform)
     */
    private static void SFR2D(final double[] c) throws MatrixException {
        int q = c.length;
        if (q > 2) {
            Matrix u = _transform(q);
            UpperTriangularMatrix.rsolve(u, c);
        }
        for (int i = 1; i < q; ++i) {
            c[i] /= 2;
        }
    }

    // SFR for a unit root
    private static Polynomial sfrur(final Complex ur) {
        double a = ur.getRe();
        if (ur.getIm() == 0) {
            return Polynomial.valueOf(2, -2 / a);
        } else {
            double[] s = new double[]{2 + 4 * a * a, -4 * a, 1};
            D2SFR(s);
            return Polynomial.of(s);
        }
    }

    static Matrix transform(final int rank) {
        // we want to express sum(a(i)cos(iw)) as sum(b(i)(0.5 cos(w))^i
        // use the usual recurrence : cos (k+1)w + cos (k-1)w= 2*cos kw *cos w
        // cos kw = -cos (k-2)w + 2* cos (k-1)w * cos w
        // set u(i) = (0.5 cos(w))^i
        
        // C[k] = [- C[k-2], 0 ] + 2 * [ 0, C[k-1]]
        // Initial values: C[0] = 1, C[1] = 1

        Matrix U = new Matrix(rank, rank);
        U.set(0, 0, 1);
        U.set(1, 1, 0.5);
        for (int c = 2; c < rank; ++c) {
            U.set(0, c, -U.get(0, c - 2));
            for (int r = 1; r < c - 1; ++r) {
                U.set(r, c, -U.get(r, c - 2) + U.get(r - 1, c - 1));
            }
            U.set(c - 1, c, U.get(c - 2, c - 1));
            U.set(c, c, U.get(c - 1, c - 1));
        }
        return U;
    }

    private final Polynomial m_p;

    /**
     *
     * @param p
     */
    public SymmetricFrequencyResponse2(final Polynomial p) {
        m_p = p;
    }

    /**
     *
     * @param sf
     */
    public SymmetricFrequencyResponse2(final SymmetricFilter sf) {
        double[] n = sf.getCoefficients();
        D2SFR(n);
        m_p = Polynomial.of(n).adjustDegree();
    }

    /**
     *
     * @param r
     * @return
     */
    public SymmetricFrequencyResponse2 divide(final SymmetricFrequencyResponse2 r) {
        Polynomial p = m_p.divide(r.m_p).adjustDegree();
        return new SymmetricFrequencyResponse2(p);
    }

    /**
     *
     * @param freq
     * @return
     */
    public double evaluateAt(final double freq) {
        return evaluateAt2Cos(2*Math.cos(freq));
    }

    /**
     *
     * @param cos
     * @return
     */
    public double evaluateAt2Cos(final double twocos) {
        return m_p.evaluateAt(twocos);
    }

    /**
     *
     * @param idx
     * @return
     */
    public double get(final int idx) {
        return m_p.get(idx);
    }

    public Polynomial getPolynomial() {
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
    public double getIntegral() {
        // (1/pi)* integral((2cos x)^n) = (1/pi)*integral((cos
        // x)^(n-2))*4n/(n-1),
        // with (1/pi)*integral((cos x)^0)=1 and (1/pi)*integral((cos
        // x)^1)=0
        // the integrals of the powers of cos x give the suite
        // 1, 0, 4*1/2, 0, 4*4*1/2*3/4, 0, 4*4*4*1/2*3/4*5/6, 0, ...

        double var = m_p.get(0);
        double icos = 1;
        for (int i = 2; i <= m_p.getDegree(); i += 2) {
            icos *= 4*(i - 1);
            icos /= i;
            var += m_p.get(i) * icos;
        }
        return var;
    }

    /**
     *
     * @param d
     * @return
     */
    public SymmetricFrequencyResponse2 minus(final double d) {
        Polynomial p = m_p.minus(d);
        return new SymmetricFrequencyResponse2(p);
    }

    /**
     *
     * @param r
     * @return
     */
    public SymmetricFrequencyResponse2 minus(final SymmetricFrequencyResponse2 r) {
        Polynomial p = m_p.minus(r.m_p).adjustDegree();
        return new SymmetricFrequencyResponse2(p);
    }

    /**
     *
     * @return
     */
    public SymmetricFrequencyResponse2 negate() {
        Polynomial p = m_p.negate();
        return new SymmetricFrequencyResponse2(p);
    }

    /**
     *
     * @param d
     * @return
     */
    public SymmetricFrequencyResponse2 plus(final double d) {
        Polynomial p = m_p.plus(d);
        return new SymmetricFrequencyResponse2(p);
    }

    /**
     *
     * @param r
     * @return
     */
    public SymmetricFrequencyResponse2 plus(final SymmetricFrequencyResponse2 r) {
        Polynomial p = m_p.plus(r.m_p).adjustDegree();
        return new SymmetricFrequencyResponse2(p);
    }

    /**
     *
     * @return
     */
    public Complex[] roots() {
        return m_p.roots();
    }

    /**
     *
     * @param searcher
     * @return
     */
    public Complex[] roots(final IRootsSolver searcher) {
        return m_p.roots(searcher);
    }

    /**
     *
     * @param d
     * @return
     */
    public SymmetricFrequencyResponse2 times(final double d) {
        Polynomial p = m_p.times(d);
        return new SymmetricFrequencyResponse2(p);
    }

    /**
     *
     * @param r
     * @return
     */
    public SymmetricFrequencyResponse2 times(final SymmetricFrequencyResponse2 r) {
        Polynomial p = m_p.times(r.m_p).adjustDegree();
        return new SymmetricFrequencyResponse2(p);
    }

    /**
     *
     * @return @throws MatrixException
     */
    public SymmetricFilter toSymmetricFilter() throws MatrixException {
        double[] c = m_p.getCoefficients();
        SFR2D(c);
        return SymmetricFilter.of(c);
    }

    /**
     *
     */
    public static class SimplifyingTool extends Simplifying<SymmetricFrequencyResponse2> {

        /**
         *
         */
        public SimplifyingTool() {
        }

        /**
         *
         * @param left
         * @param urb
         * @return
         */
        public boolean simplify(final SymmetricFrequencyResponse2 left, final BackFilter urb) {
            clear();
            if (left.m_p.getDegree() == 0) {
                return false;
            }
            Complex[] roots = urb.roots();
            if (roots == null) {
                return false;
            }
            Polynomial P = left.m_p;
            Polynomial Q = null;
            Polynomial R = null;
            for (int i = 0; i < roots.length; ++i) {
                if (roots[i].getIm() >= 0) {
                    Polynomial D = sfrur(roots[i]);
                    Polynomial.Division div = Polynomial.divide(P, D);
                    if (div.isExact()) {
                        P = div.getQuotient();
                        if (Q == null) {
                            Q = D;
                        } else {
                            Q = Q.times(D);
                        }
                    } else if (R == null) {
                        R = D;
                    } else {
                        R = R.times(D);
                    }
                }
            }
            if (Q == null) {
                return false;
            } else {
                m_left = new SymmetricFrequencyResponse2(P);
                if (R == null) {
                    m_right = new SymmetricFrequencyResponse2(Polynomial.ONE);
                } else {
                    m_right = new SymmetricFrequencyResponse2(R);
                }
                m_common = new SymmetricFrequencyResponse2(Q);
                return true;
            }
        }

        @Override
        public boolean simplify(final SymmetricFrequencyResponse2 left, final SymmetricFrequencyResponse2 right) {
            clear();
            if (left.m_p.getDegree() == 0 || right.m_p.getDegree() == 0) {
                return false;
            }
            Polynomial lp = left.m_p, rp = right.m_p, p;
            Polynomial.SimplifyingTool psimp = new Polynomial.SimplifyingTool();
            if (psimp.simplify(lp, rp)) {
                lp = psimp.getLeft();
                rp = psimp.getRight();
                p = psimp.getCommon();

                m_common = new SymmetricFrequencyResponse2(p);
                m_left = new SymmetricFrequencyResponse2(lp);
                m_right = new SymmetricFrequencyResponse2(rp);
                return true;
            } else {
                return false;
            }
        }
    }
}
