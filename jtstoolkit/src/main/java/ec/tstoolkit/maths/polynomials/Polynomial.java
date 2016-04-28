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
package ec.tstoolkit.maths.polynomials;

import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.design.Immutable;
import ec.tstoolkit.maths.Complex;
import ec.tstoolkit.maths.ComplexBuilder;
import ec.tstoolkit.maths.ComplexMath;
import ec.tstoolkit.maths.Simplifying;
import ec.tstoolkit.utilities.Arrays2;
import java.util.Arrays;
import java.util.Formatter;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntToDoubleFunction;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable
public final class Polynomial implements IReadDataBlock {

    public static final Polynomial ZERO = new Polynomial(Polynomial.Doubles.zero(), 0);
    public static final Polynomial ONE = new Polynomial(Polynomial.Doubles.one(), 0);

    /**
     * This method divides the polynomial by a second polynomial. The quotient
     * is returned as a new polynomial. The remainder of the division is
     * returned in an out parameter. Roots of the result are only calculated
     * when the roots of the instance and of the polynomial p have already been
     * calculated.
     *
     * @param num The numerator polynomial
     * @param denom The denominator polynomial
     * @return
     */
    public static Division divide(final Polynomial num, final Polynomial denom) {
        int n = num.getDegree(), nv = denom.getDegree();
        if (nv > n) {
            return new Division(num, Polynomial.ZERO);
        }
        double[] r = new double[n + 1];
        double[] q = new double[n + 1];

        for (int j = 0; j <= n; ++j) {
            r[j] = num.get(j);
            q[j] = 0.0;
        }
        for (int k = n - nv; k >= 0; --k) {
            q[k] = r[nv + k] / denom.get(nv);
            for (int j = nv + k - 1; j >= k; j--) {
                r[j] -= q[k] * denom.get(j - k);
            }
        }
        Polynomial m_q = Polynomial.of(Arrays.copyOf(q, n - nv + 1));
        Polynomial m_r = nv > 0 ? Polynomial.of(Arrays.copyOf(r, nv)).adjustDegree() : Polynomial.ZERO;
        return new Division(m_r, m_q);
    }

    static double[] divide(final double[] num, int numdegree, final double[] denom, int denumdegree) {
        int n = numdegree, nv = denumdegree;
        while (n >= 0) {
            if (Math.abs(num[n]) > EPSILON) {
                break;
            } else {
                --n;
            }
        }
        if (n < 0) {
            return Doubles.zero();
        }
        while (nv >= 0) {
            if (Math.abs(denom[nv]) > EPSILON) {
                break;
            } else {
                --nv;
            }
        }
        if (nv < 0) {
            if (num[n] > 0) {
                return Doubles.positiveInfinity();
            } else if (num[n] < 0) {
                return Doubles.negativeInfinity();
            } else {
                return Doubles.nan();
            }
        }

        if (nv > n) {
            return Polynomial.Doubles.zero();
        }
        double[] r = num.clone();
        double[] q = new double[n + 1];

        for (int k = n - nv; k >= 0; --k) {
            q[k] = r[nv + k] / denom[nv];
            for (int j = nv + k - 1; j >= k; j--) {
                r[j] -= q[k] * denom[j - k];
            }
        }
        return Arrays.copyOf(q, n - nv + 1);
    }

    @Override
    public void copyTo(double[] buffer, int start) {
        System.arraycopy(m_c, 0, buffer, start, degree + 1);
    }

    @Override
    public int getLength() {
        return degree + 1;
    }

    @Override
    public IReadDataBlock rextract(int start, int length) {
        return new ReadDataBlock(m_c, start, length);
    }

    /**
     *
     */
    public final static class Division {

        private final Polynomial m_r, m_q;

        private Division(Polynomial remainder, Polynomial quotient) {
            this.m_r = remainder;
            this.m_q = quotient;
        }

        /**
         * @return The quotient of the division
         */
        public Polynomial getQuotient() {
            return m_q;
        }

        /**
         * @return The remainder of the division
         */
        public Polynomial getRemainder() {
            return m_r;
        }

        /**
         *
         * @return
         */
        public boolean isExact() {
            return m_r.isZero();
        }
    }

    /**
     * The static method tries to simplify polynomials by finding a common
     * polynomial. The common polynomial is returned in o. Both l and r are
     * "divided" by the common polynomial
     */
    public static class SimplifyingTool extends Simplifying<Polynomial> {

        @Override
        public boolean simplify(final Polynomial left, final Polynomial right) {
            clear();
            //
            m_common = Polynomial.ONE;
            if (left.getDegree() >= right.getDegree()) {
                simplify(left, right, null);
            } else {
                simplify(right, left, null);
                Polynomial tmp = m_left;
                m_left = m_right;
                m_right = tmp;
            }

            return m_common.getDegree() > 0;
        }

        private boolean simplify(Polynomial left, final Polynomial right, Complex[] roots) // left.Degree >= right.Degree)
        {
            if (right.getDegree() <= 0) {
                return false;
            }
            // try first exact division
            if (simplifyExact(left, right)) {
                return true;
            }
            if (roots == null) {
                roots = right.roots();
            }
            double[] rtmp = Doubles.fromDegree(1);
            double[] ctmp = Doubles.fromDegree(2);
            for (Complex element : roots) {
                if (left.evaluateAt(element).abs() < 1E-12) {
                    double a = element.getRe(), b = element.getIm();
                    if (b == 0) // real root
                    {
                        /*
                         * rtmp.m_c[0] = -a; Division division = new Division();
                         * division.divide(left, rtmp); Polynomial div =
                         * division.getQuotient(); rem =
                         * division.getRemainder(); rem.smooth(); if
                         * (rem.isNull()) { left = div; m_common =
                         * m_common.times(rtmp); }
                         */
                        // if element is a root, remove it
                        rtmp[0] = -a;
                        // FIXME: find a way to avoid creating xxx
                        Polynomial xxx = Polynomial.of(rtmp);
                        left = left.divide(xxx);
                        m_common = m_common.times(xxx);
                    } else if (b > 0) {
                        // (x-(a+bi))*(x-(a-bi) = x^2-2ax+a^2+b^2
                        ctmp[0] = a * a + b * b;
                        ctmp[1] = -2 * a;
                        /*
                         * Division division = new Division();
                         * division.divide(left, ctmp); Polynomial div =
                         * division.getQuotient(); rem =
                         * division.getRemainder(); rem.smooth(); if
                         * (rem.isNull()) { left = div;
                         */
                        // FIXME: find a way to avoid creating xxx
                        Polynomial xxx = Polynomial.of(ctmp);
                        left = left.divide(xxx);
                        m_common = m_common.times(xxx);
                    }
                }
            }
            if (m_common.getDegree() > 0) {
                m_left = left;
                m_right = right.divide(m_common);
                return true;
            } else {
                return false;
            }
        }

        /**
         *
         * @param left
         * @param right
         * @return
         */
        public boolean simplify(final Polynomial left, final UnitRoots right) {
            clear();
            m_common = Polynomial.ONE;
            return simplify(left, right.toPolynomial(), right.roots());
        }

        private boolean simplifyExact(Polynomial left, Polynomial right) {
            LeastSquaresDivision div = new LeastSquaresDivision();
            if (!div.divide(left, right) || !div.isExact()) {
                return false;
            }
            m_left = div.getQuotient();
            m_right = Polynomial.ONE;
            m_common = right;
//            Polynomial.Division division = Polynomial.divide(left, right);
//            if (!division.isExact()) {
//                return false;
//            }
//            m_left = division.getQuotient();
//            m_right = Polynomial.ONE;
//            m_common = right;
            return true;
        }
    }

    /**
     * The method checks an array of complex roots to see whether conjugates are
     * present
     *
     * @param roots
     * @return
     * @throws PolynomialException
     */
    public static Complex[] checkRoots(final Complex[] roots)
            throws PolynomialException {
        int nroots = roots.length;
        final Complex[] rootsc = new Complex[nroots];
        // copy roots for dual representation
        boolean[] used = new boolean[nroots];

        for (int i = 0; i < nroots; ++i) {
            roots[i] = smooth(roots[i]);
        }
        for (int i = 0, j = 0; i < nroots; ++i) {
            if (!used[i]) {
                Complex c = roots[i];
                if (c.getIm() == 0) {
                    rootsc[j++] = c;
                    used[i] = true;
                } else if (c.getIm() > 0) {
                    used[i] = true;
                    // rootsc[j++]=c;
                    // search for the conjugate;
                    Complex conj = c.conj();
                    int k = 0;
                    for (; k < nroots; ++k) {
                        if (!used[k] && (roots[k].getIm() < 0)) {
                            if (roots[k].equals(conj, EPSILON)) {
                                used[k] = true;
                                c = Complex.cart(
                                        (c.getRe() + roots[k].getRe()) / 2,
                                        (c.getIm() - roots[k].getIm()) / 2);

                                conj = c.conj();
                                // rootsc[j++]=conj;
                                break;
                            }
                        }
                    }
                    if (k == nroots) {
                        throw new PolynomialException(
                                PolynomialException.Conjugate);
                    }
                    rootsc[j++] = c;
                    rootsc[j++] = conj;
                }
            }
        }
        return rootsc;
    }

    /**
     * The property gets/sets the cut-off value for zero. By default it is set
     * to 1e-6.
     *
     * @return
     */
    public static double getEpsilon() {
        return EPSILON;
    }

    // Factory methods >>
    /**
     * Create a new Polynomial by using the specified coefficients. The
     * polynomial will be of degree <code>coefficients.length-1</code> <br> Note
     * that the array of doubles is used directly. If you need defensive copy,
     * use {@link Polynomial#copyOf(double[])} instead.
     *
     * @param coefficients
     * @return
     * @throws IllegalArgumentException if {@code coefficients} is null or empty
     */
    public static Polynomial of(double[] coefficients) {
        if (Arrays2.isNullOrEmpty(coefficients)) {
            throw new IllegalArgumentException("Coefficients cannot be null or empty");
        }
        int usedDegree = Doubles.getUsedDegree(coefficients);
        Polynomial result = Cache.find(coefficients, usedDegree);
        return result != null ? result : new Polynomial(coefficients, usedDegree);
    }

    /**
     * Create a new Polynomial by using the specified coefficients. The
     * polynomial will be of degree <code>coefficients.length-1</code> <br> Note
     * that the array of doubles is copied. If you don't need defensive copy,
     * use {@link Polynomial#of(double[])} instead.
     *
     * @param coefficients
     * @return a non-null Polynomial
     * @throws IllegalArgumentException if {@code coefficients} is null or empty
     */
    public static Polynomial copyOf(double[] coefficients) {
        if (Arrays2.isNullOrEmpty(coefficients)) {
            throw new IllegalArgumentException("Coefficients cannot be null or empty");
        }
        int usedDegree = Doubles.getUsedDegree(coefficients);
        Polynomial result = Cache.find(coefficients, usedDegree);
        return result != null ? result : new Polynomial(Arrays.copyOf(coefficients, usedDegree + 1), usedDegree);
    }

    /**
     * Create a new Polynomial by using the specified coefficients, from start.
     * The polynomial will be of degree <code>n-1</code> <br>
     *
     * @param coefficients
     * @param start First position in the array
     * @param end Las position in the array (excluded)
     * @return a non-null Polynomial
     * @throws IllegalArgumentException if {@code coefficients} is null or empty
     */
    public static Polynomial copyOf(double[] coefficients, int start, int end) {
        if (Arrays2.isNullOrEmpty(coefficients) || coefficients.length < end) {
            throw new IllegalArgumentException("Coefficients cannot be null or empty");
        }
        double[] copy = Arrays.copyOfRange(coefficients, start, end);
        return Polynomial.of(copy);
    }

    /**
     * Shortcut for <code>Polynomial.of(new double[] {...})</code>
     *
     * @param firstCoefficient
     * @param nextCoefficients
     * @return
     */
    public static Polynomial valueOf(double firstCoefficient, double... nextCoefficients) {
        if (Arrays2.isNullOrEmpty(nextCoefficients)) {
            if (firstCoefficient == 0.0) {
                return Polynomial.ZERO;
            }
            if (firstCoefficient == 1.0) {
                return Polynomial.ONE;
            }
            return new Polynomial(new double[]{firstCoefficient}, 0);
        }
        double[] result = Doubles.fromDegree(nextCoefficients.length);
        result[0] = firstCoefficient;
        System.arraycopy(nextCoefficients, 0, result, 1, nextCoefficients.length);
        return new Polynomial(result, Doubles.getUsedDegree(result));
    }

    /**
     * Constructor: the polynomial is initialized with an array of complex
     * roots. The coefficient of the highest power = 1.0
     *
     * @param roots
     * @return a non-null Polynomial
     */
    public static Polynomial fromComplexRoots(Complex[] roots) {
        return fromComplexRoots(roots, 1.0);
    }

    /**
     * The polynomial is equal to cnt*(x-r0)*(x-r1)...
     *
     * @param roots
     * @param c
     * @return a non-null Polynomial
     */
    public static Polynomial fromComplexRoots(final Complex[] roots, final double c) {
        if (Arrays2.isNullOrEmpty(roots)) {
            return new Polynomial(new double[]{c}, 0);
        }

        double[] m_c = new double[roots.length + 1];

        final Complex[] p = new Complex[roots.length + 1];
        p[0] = Complex.cart(c, 0);
        for (int i = 0; i < roots.length; ++i) {
            // multiply by (x-rc[i])
            p[i + 1] = p[i];
            for (int j = i; j >= 1; --j) {
                p[j] = p[j - 1].minus(p[j].times(roots[i]));
            }
            p[0] = p[0].times(roots[i].negate());
        }
        for (int i = 0; i < p.length; ++i) {
            m_c[i] = p[i].getRe();
        }

        Polynomial pol = new Polynomial(m_c, Doubles.getUsedDegree(m_c));
        pol.defRoots.set(roots.clone());
        return pol;
    }

    public static Polynomial fromData(IReadDataBlock data) {
        double[] d = new double[data.getLength()];
        data.copyTo(d, 0);
        return new Polynomial(d, Doubles.getUsedDegree(d));
    }

    /**
     * The polynomial is equal to 1 - c*x^n
     *
     * @param c The coefficient. Should be strictly higher than 0
     * @param n The degree of the polynomial
     * @return The new polynomial. The roots of the polynomial are also computed
     */
    public static Polynomial factor(double c, int n) {
        if (c == 0) {
            return ONE;
        }
        double[] p = new double[n + 1];
        p[0] = 1;
        p[n] = -c;
        Polynomial F = Polynomial.of(p);
        Complex[] ur = Complex.unitRoots(n);
        if (c > 0 || n % 2 == 1) {
            double rc;
            if (c > 0) {
                rc = Math.pow(1 / c, 1.0 / n);
            } else {
                rc = -Math.pow(-1 / c, 1.0 / n);
            }
            for (int i = 0; i < ur.length; ++i) {
                ur[i] = ur[i].times(rc);
            }
        } else {
            Complex rc = ComplexMath.pow(Complex.cart(1 / c, 0), 1.0 / n);
            for (int i = 0; i < ur.length; ++i) {
                ur[i] = ur[i].times(rc);
            }
        }
        F.setRoots(ur);
        return F;
    }

    // << Factory methods
    /**
     *
     * @param value
     */
    public static void setEpsilon(final double value) {
        EPSILON = value;
    }

    private static Complex smooth(final Complex c) {
        double re = c.getRe();
        double im = c.getIm();
        if (Math.abs(im) <= EPSILON) {
            im = 0;
        }
        if (Math.abs(re) <= EPSILON) {
            re = 0;
        }
        if (Math.abs(re - 1) <= EPSILON) {
            re = 1;
        }
        if (Math.abs(re + 1) <= EPSILON) {
            re = -1;
        }
        return Complex.cart(re, im);
    }
    private final double[] m_c;
    private final int degree;
    private final AtomicReference<Complex[]> defRoots = new AtomicReference<>(); // caching the roots
    private static double EPSILON = 1e-9;
    /**
     * The static member defines the Root finding algorithm used to find the
     * roots of the polynomial. By default this is the Muller algorithm. It can
     * be changed to whatever algorithm that supports the RootsSearcher
     * interface.
     *
     */
    //private static IRootsSolver g_defRootsSolver = new GrantHitchinsSolver();
    private static IRootsSolver g_defRootsSolver = new MullerNewtonSolver();

    /**
     *
     * @return
     */
    public static IRootsSolver getDefRootsSearcher() {
        return Polynomial.g_defRootsSolver;
    }

    /**
     *
     * @param value
     */
    public static void setDefRootsSearcher(final IRootsSolver value) {
        Polynomial.g_defRootsSolver = value;
    }

    /**
     * The constructor takes an array of doubles. The array represents the
     * coefficients of the polynomial. The polynomial will be of degree
     * specified by the second parameter.
     *
     * @param coefficients
     * @param degree the degree; <code>degree <= coefficients.length -1</code>
     */
    private Polynomial(final double[] coefficients, final int degree) {
        // Keep this contructor alone and private; use factory methods instead
        this.m_c = coefficients;
        this.degree = degree;
    }

    /**
     * Create a new Polynomial by decreasing the degree of the specified
     * polynomial by one until the highest non-zero coefficient is reached.
     *
     * @return a non-null Polynomial
     */
    public Polynomial adjustDegree() {
        int n = degree;
        while ((n >= 0) && Doubles.equals(0, get(n), EPSILON)) {
            --n;
        }
        if (n < 0) {
            return Polynomial.ZERO;
        }
        if (n == degree) {
            return this;
        }
        return new Polynomial(m_c, n);
    }

    /**
     * This read-only property will return a polynomial representing the first
     * derivate of the polynomial.
     *
     * @return
     */
    public Polynomial derivate() {
        if (degree == 0) {
            return Polynomial.ZERO;
        }
        double[] result = Doubles.fromDegree(degree - 1);
        for (int i = 1; i <= degree; ++i) {
            result[i - 1] = i * get(i);
        }
        return new Polynomial(result, degree - 1);
    }

    /**
     * The static method creates a new polynomial as the division of p and a
     * constant value d. cnew[0] = cold[0]-d.
     *
     * @param d
     * @return
     */
    public Polynomial divide(final double d) {
        return times(1 / d);
    }

    /**
     * The operator divides two polynomials creating a new polynomial as a
     * result. The roots of the resulting polynomial are only calculated when
     * the roots of l and r have been calculated before. This method does not
     * return the remainder of the division
     *
     * @param r A polynomial of degree d'
     * @return The quotient of l and r
     */
    public Polynomial divide(final Polynomial r) {
        // if (l == null)
        // throw new ArgumentNullException("l");
        //return divide(this, r).getQuotient();
        return Polynomial.of(divide(m_c, degree, r.m_c, r.degree));
    }

    /**
     * The method checks whether two polynomials are the same. Sameness is
     * approximate. when this[i]-p[i] LE epsilon both coefficients are
     * considered equal.
     *
     * @return
     */
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof Polynomial ? equals((Polynomial) obj, EPSILON) : false;
    }

    public boolean equals(Polynomial other, double epsilon) {
        if (this == other) {
            return true;
        }
        if (degree != other.degree) {
            return false;
        }
        for (int i = 0; i <= degree; ++i) {
            if (!Doubles.equals(get(i), other.get(i), epsilon)) {
                return false;
            }
        }
        return true;
    }

    /**
     * The method evaluates the polynomial for a given complex value x.
     *
     * @param x
     * @return
     */
    public Complex evaluateAt(final Complex x) {
        int i = getDegree();
        double xr = x.getRe(), xi = x.getIm();
        double fr = get(i--), fi = 0;
        for (; i >= 0; --i) {
            double tr = fr * xr - fi * xi + get(i);
            double ti = fr * xi + fi * xr;
            fr = tr;
            fi = ti;
        }
        return Complex.cart(fr, fi);
    }

    /**
     * The method evaluates the polynomial for a given double value x.
     *
     * @param x
     * @return
     */
    public double evaluateAt(final double x) {
        int i = getDegree();
        double f = get(i--);
        for (; i >= 0; --i) {
            f = m_c[i] + (f * x);
        }
        return f;
    }
    
    /**
     * Evaluates a polynomial defined by given coefficients at a given point.
     * The coefficients are stored in reverse order (the first coefficient corresponds
     * to the highest power and the last one to the constant)
     * @param c The coefficients. Should contain at least one element (not checked)
     * @param x The evaluation point;
     * @return the value of p(x)
     */
    public static double revaluate(final double[] c, final double x){
        int d = c.length;
        int p = 0;
        double y = c[p++];
        for (; p < d; ++p) {
            y = c[p] + y * x;
        }
        return y;
    }

    /**
     * Evaluates a polynomial defined by given coefficients at a given point.
     * The coefficients are stored in normal order (the first coefficient corresponds
     * to the constant and the last one to the highest power)
     * @param c The coefficients. Should contain at least one element (not checked)
     * @param x The evaluation point;
     * @return the value of p(x)
     */
    public static double evaluate(final double[] c, final double x){
        int p = c.length-1;
        double y = c[p--];
        for (; p >= 0; --p) {
            y = c[p] + (y * x);
        }
        return y;
    }

    /**
     * Evaluates a polynomial with coefficients defined by a give function at a given point.
     * @param degree The degree of the polynomial
     * @param fn The function defining the coefficients. fn(i) is the coefficient corresponding to the power i
     * @param x The evaluation point;
     * @return the value of p(x)
     */
    public static double evaluate(final int degree, IntToDoubleFunction fn, final double x){
        int p = degree;
        double y = fn.applyAsDouble(p--);
        for (; p >= 0; --p) {
            y = fn.applyAsDouble(p) + (y * x);
        }
        return y;
    }

    /**
     * The method evaluates the polynomial for the complex number (cos x, sin x)
     * value x.
     *
     * @param w
     * @return
     */
    public Complex evaluateAtFrequency(final double w) {
        ComplexBuilder f = new ComplexBuilder(get(0));
        for (int i = 1; i <= degree; ++i) {
            f.add(Complex.polar(m_c[i], w * i));
        }
        return f.build();
    }

    /**
     * Gets the coefficient at the index.
     *
     * @param idx
     * @return
     */
    @Override
    public double get(final int idx) {
        return m_c[idx];
    }

    /**
     * Gets a copy of the coefficients of the polynomial.
     *
     * @return
     */
    public double[] getCoefficients() {
        return Arrays.copyOf(m_c, degree + 1);
    }

    /**
     * The property gets/sets the degree of the polynomial. Changing the degree
     * resets the coefficients and the roots to zero values
     *
     * @return
     */
    public int getDegree() {
        return degree;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(m_c);
    }

    /**
     * The read-only property returns whether the polynomial is a zero
     * polynomial. A zero polynomial is a polynomial of degree 0 and a single
     * coefficient with value zero.
     *
     * @return
     */
    public boolean isZero() {
        return degree == 0 && get(0) == 0d;
    }

    /**
     * The read-only property returns whether the polynomial is an identity
     * polynomial. An identity polynomial is a polynomial of degree 0 and a
     * single coefficient with value 1.
     *
     * @return
     */
    public boolean isIdentity() {
        return degree == 0 && get(0) == 1d;
    }

    /**
     * The static method creates a new polynomial as the difference of p and a
     * constant value d. cnew[0] = cold[0]-d. The roots of the result must be
     * recomputed.
     *
     * @param d
     * @return
     */
    public Polynomial minus(double d) {
        return plus(-d);
    }

    /**
     * The operator subtracts two polynomials creating a new polynomial as a
     * result. The algorithm checks the degree (equal highest power coefficients
     * will annihilate this power). The roots of the resulting polynomial are
     * only calculated when the roots of l and r have been calculated before.
     *
     * @param r A polynomial of degree d'
     * @return The difference of l and r
     */
    public Polynomial minus(final Polynomial r) {
        return plus(r.negate());
    }

    /**
     * This read-only property will create a new polynomial whose coefficients
     * are in reverse order of the coefficients of the original polynomial.
     *
     * @return
     */
    public Polynomial mirror() {
        if (degree == 0) {
            return this;
        }
        double[] result = Doubles.fromDegree(degree);
        for (int i = 0; i <= degree; ++i) {
            result[i] = get(degree - i);
        }
        return new Polynomial(result, Doubles.getUsedDegree(result));
    }

    /**
     * The operator changes the sign of the coefficients of the polynomial. A
     * new polynomial is returned as a result.
     *
     * @return
     */
    public Polynomial negate() {
        return times(-1);
    }

    /**
     * Creates a new polynomial as the sum of this and a constant value d.
     * cnew[0] = d+cold[0]. The roots of the result are not computed.
     *
     * @param d
     * @return
     */
    public Polynomial plus(final double d) {
        if (d == 0d) {
            return this;
        }
        double[] result = getCoefficients();
        result[0] += d;
        return new Polynomial(result, degree);
    }

    /**
     * The operator adds a polynomial creating a new polynomial as a result. The
     * algorithm checks the degree (equal highest power coefficients with
     * inverse sign will annihilate this power). The roots of the resulting
     * polynomial are only calculated when the roots of this and r have been
     * calculated before.
     *
     * @param r A polynomial of degree d'
     * @return The sum of this and r
     */
    public Polynomial plus(final Polynomial r) {
        if (r.isZero()) {
            return this;
        }
        // swap l and r if l.Degree < r.Degree
        if (this.degree < r.degree) {
            return r.plus(this);
        }
        double[] result = getCoefficients();
        for (int i = 0; i <= r.degree; ++i) {
            result[i] += r.get(i);
        }
        int tmp = this.degree == r.degree ? Doubles.getUsedDegree(result) : this.degree;
        return new Polynomial(result, tmp);
    }

    /**
     * The property get returns the roots of the polynomial. To do so it will
     * use a statically defined RootsSearcher algorithm. By default this is the
     * Muller algorithm for finding roots of polynomials. You can change the
     * algorithm by setting DefRootsSearcher to another RootsSearcher object.
     * Setting this property re-initializes the internal state of the
     * polynomial.
     *
     * @return
     */
    public Complex[] roots() {
        Complex[] result = defRoots.get();
        if (result == null) {
            result = roots(g_defRootsSolver);
            defRoots.set(result);
        }
        return result;
    }

    /**
     * To be used with caution. Be sure that the roots correspond to the current
     * polynomial. No verification is done.
     *
     * @param roots
     */
    void setRoots(Complex[] roots) {
        this.defRoots.set(roots);
    }

    /**
     *
     * @param searcher
     * @return
     */
    public Complex[] roots(IRootsSolver searcher) {
        if (getDegree() == 0) {
            return new Complex[0];
        }
        if (searcher == null) {
            searcher = g_defRootsSolver;
        }
        synchronized (searcher) {
            final Polynomial tmp = this.adjustDegree();
            if (tmp.getDegree() == 0) {
                return new Complex[0];
            }
            if (searcher.factorize(tmp)) {
                Complex[] roots = searcher.roots();
                searcher.clear();
                return roots;
            } else {
                return null;
            }
        }
    }

    /**
     * The method sets small coefficient values to zero. Small is defined as the
     * absolute value being smaller than some predefined value epsilon. This
     * method will create a new Polynomial.
     *
     * @return
     */
    public Polynomial smooth() {
        double[] result = getCoefficients();
        for (int i = 0; i < result.length; ++i) {
            double c = Math.round(result[i]);
            if (Doubles.equals(c, result[i], EPSILON)) {
                result[i] = c;
            }
        }
        return new Polynomial(result, Doubles.getUsedDegree(result));
    }

    /**
     * The static method creates a new polynomial as the product of p and a
     * constant value d. cnew[i] = d*cold[i]. The roots are invariant to this
     * change in scale.
     *
     * @param d
     * @return
     */
    public Polynomial times(final double d) {
        if (d == 0d || this.isZero()) {
            return Polynomial.ZERO;
        }
        if (d == 1d) {
            return this;
        }
        double[] coefficients = Doubles.fromDegree(degree);
        for (int i = 0; i <= degree; ++i) {
            coefficients[i] = get(i) * d;
        }
        Polynomial result = new Polynomial(coefficients, degree);
        result.defRoots.set(defRoots.get());
        return result;
    }

    /**
     * The operator multiplies two polynomials creating a new polynomial as a
     * result. The roots of the resulting polynomial are only calculated when
     * the roots of l and r have been calculated before.
     *
     * @param r A polynomial of degree d'
     * @return The product of l and r
     */
    public Polynomial times(final Polynomial r) {
        return times(r, false);
    }

    public Polynomial times(final Polynomial r, boolean computeroots) {
        if (r.isZero() || this.isZero()) {
            return Polynomial.ZERO;
        }
        if (r.isIdentity()) {
            return this;
        }
        if (this.isIdentity()) {
            return r;
        }
        double[] result = Doubles.fromDegree(this.degree + r.degree);
        result[this.degree + r.degree] = 0;
        for (int u = 0; u <= this.degree; ++u) {
            if (this.get(u) != 0) {
                for (int v = 0; v <= r.degree; ++v) {
                    if (r.get(v) != 0) {
                        result[u + v] += this.get(u) * r.get(v);
                    }
                }
            }
        }
        Polynomial prod = Polynomial.of(result);
        {
            Complex[] lRoots = defRoots.get();
            Complex[] rRoots = r.defRoots.get();
            if (lRoots != null && rRoots != null) {
                prod.defRoots.set(Arrays2.concat(lRoots, rRoots));
            } else if (computeroots) {
                prod.defRoots.set(Arrays2.concat(roots(), r.roots()));
            }
        }
        return prod;
    }

    /**
     * The method represents the polynomial in convential notation ax+bx^2+....
     * Internally it calls the overload ToString(var, bSmooth) where var == 'X'
     * and bSmooth == true
     *
     * @return The string representation of the polynomial
     */
    @Override
    public String toString() {
        return toString('X', true);
    }

    /**
     * The method represents the polynomial as a string : [+ | -] c0 {[+ | -] ci
     * C^i}
     *
     * @param var Indicates the character that represents the variable in the
     * polynomial (x, X, y, ...)
     * @param bSmooth Indicates whether smoothing should be applied prior to
     * output.
     * @return A string representation of the polynomial
     */
    public String toString(final char var, final boolean bSmooth) {
        return toString("%6g", var, bSmooth);
    }

    /**
     *
     * @param fmt
     * @param var
     * @param bSmooth
     * @return
     */
    public String toString(final String fmt, final char var,
            final boolean bSmooth) {
        // TODO: fmt

        Polynomial p = bSmooth ? this.smooth() : this;
        StringBuilder sb = new StringBuilder(512);
        // System.Globalization.NumberFormatInfo info= new
        // System.Globalization.NumberFormatInfo();
        // info.NumberGroupSeparator="";
        // info.NumberDecimalDigits=4;
        boolean sign = false;
        int n = p.getDegree();
        if (n == 0) {
            sb.append(new Formatter().format(fmt, p.get(0)));
        } else {
            for (int i = 0; i <= n; ++i) {
                double v = Math.abs(p.get(i));
                if (v >= 1e-6) {
                    if (v > p.get(i)) {
                        sb.append(" - ");
                    } else if (sign) {
                        sb.append(" + ");
                    }
                    if ((v != 1) || (i == 0)) {
                        sb.append(new Formatter().format(fmt, v).toString());
                    }
                    sign = true;
                    if (i > 0) {
                        sb.append(' ').append(var);
                    }
                    if (i > 1) {
                        sb.append('^').append(i);
                    }
                }
            }
        }
        return sb.toString();
    }

    public static final class Doubles {

        private Doubles() {
            // static class
        }

        /**
         * Return the coefficients of a polynomial of degree n as an array of
         * doubles. All coefficients are set to zero, except the highest which
         * is set to 1.
         *
         * @param degree
         * @return
         */
        public static double[] fromDegree(int degree) {
            // all coefficuents are set to 0, except the highest.
            double[] c = new double[degree + 1];
            c[degree] = 1;
            return c;
        }

        public static double[] zero() {
            return new double[]{0};
        }

        public static double[] one() {
            return new double[]{1};
        }

        public static double[] positiveInfinity() {
            return new double[]{Double.POSITIVE_INFINITY};
        }

        public static double[] negativeInfinity() {
            return new double[]{Double.NEGATIVE_INFINITY};
        }

        public static double[] nan() {
            return new double[]{Double.NaN};
        }

        public static int getUsedDegree(double[] coefficients) {
            int n = coefficients.length - 1;
            while ((n > 0) && (coefficients[n] == 0.0)) {
                --n;
            }
            return n;
        }

        public static boolean equals(double a, double b, double epsilon) {
//            return Math.abs(a - b) <= epsilon;            
            return a > b ? (a - epsilon <= b) : (b - epsilon <= a);
        }
    }

    /**
     * Some values are requested often; this class makes them memory
     * efficient<br> Note that if might be interesting to check call stack to
     * prevent theses values to be created.
     */
    private static final class Cache {

        static final Polynomial p13 = new Polynomial(new double[]{1.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0, 1.0}, 13);
        static final Polynomial p12 = new Polynomial(new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0}, 12);
        static final Polynomial p11 = new Polynomial(new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0}, 11);
        static final Polynomial p11_2 = new Polynomial(new double[]{12.0, 11.0, 10.0, 9.0, 8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0}, 11);
        static final Polynomial p4 = new Polynomial(new double[]{1.0, 0.0, 0.0, 0.0, -1.0}, 4);
        static final Polynomial p3 = new Polynomial(new double[]{1.0, 1.0, 1.0, 1.0}, 3);
        static final Polynomial p1 = new Polynomial(new double[]{1.0, -1.0}, 1);

        static Polynomial find(double[] coefficients, int degree) {
            switch (degree) {
                case 0:
                    if (coefficients[0] == 0d) {
                        return Polynomial.ZERO;
                    }
                    if (coefficients[0] == 1d) {
                        return Polynomial.ONE;
                    }
                    return null;
                case 1:
                    if (intEquals(coefficients, p1)) {
                        return p1;
                    }
                    return null;
                case 3:
                    if (intEquals(coefficients, p3)) {
                        return p3;
                    }
                    return null;
                case 4:
                    if (intEquals(coefficients, p4)) {
                        return p4;
                    }
                    return null;
                case 11:
                    if (intEquals(coefficients, p11)) {
                        return p11;
                    }
                    if (intEquals(coefficients, p11_2)) {
                        return p11_2;
                    }
                    return null;
                case 12:
                    if (intEquals(coefficients, p12)) {
                        return p12;
                    }
                    return null;
                case 13:
                    if (intEquals(coefficients, p13)) {
                        return p13;
                    }
                    return null;
            }
            return null;
        }

        static boolean intEquals(double[] c, Polynomial p) {
            for (int i = 0; i <= p.getDegree(); i++) {
                if (c[i] != p.get(i)) {
                    return false;
                }
            }
            return true;
        }
    }
}
