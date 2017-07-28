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
package demetra.maths.polynomials;

import java.util.Arrays;
import java.util.Formatter;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntToDoubleFunction;
import demetra.design.Development;
import demetra.design.Immutable;
import demetra.maths.Complex;
import demetra.maths.ComplexBuilder;
import demetra.maths.ComplexMath;
import demetra.maths.Simplifying;
import demetra.utilities.Arrays2;
import lombok.NonNull;
import demetra.data.DoubleReader;
import demetra.data.DoubleSequence;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable
public final class Polynomial implements DoubleSequence {

    public static final Polynomial ZERO = new Polynomial(Polynomial.Coefficients.zero());
    public static final Polynomial ONE = new Polynomial(Polynomial.Coefficients.one());

    private final double[] coeff;
    private final AtomicReference<Complex[]> defRoots = new AtomicReference<>(); // caching the roots
    private static final double EPSILON = 1e-9;

    //<editor-fold defaultstate="collapsed" desc="DoubleSequence interface">
    @Override
    public void copyTo(double[] buffer, int start) {
        System.arraycopy(coeff, 0, buffer, start, coeff.length);
    }

    @Override
    public int length() {
        return coeff.length;
    }

    @Override
    public DoubleSequence extract(int start, int length) {
        return DoubleSequence.ofInternal(coeff, start, length);
    }

    @Override
    public DoubleReader reader() {
        return DoubleReader.of(coeff);
    }

    //</editor-fold>
    /**
     * cut-off value for zero. 1e-9.
     *
     * @return
     */
    public static double getEpsilon() {
        return EPSILON;
    }

    //<editor-fold defaultstate="collapsed" desc="Static factories">
    /**
     * Create a new Polynomial by using the specified coefficients. The
     * polynomial will be ofFunction getDegree
     * <code>coefficients.length-1</code>
     * <br> Note that the array of doubles is used directly. If you need
     * defensive copy, use {@link Polynomial#of(double[])} instead.
     *
     * @param coefficients
     * @return
     */
    public static Polynomial ofInternal(@NonNull double[] coefficients) {
        return new Polynomial(Coefficients.ofInternal(coefficients));
    }

    /**
     * Creates a new polynomial form given coefficients
     *
     * @param p0 The constant term
     * @param p The coefficients of the powers of x. Can be null
     * @return
     */
    public static Polynomial valueOf(double p0, double... p) {
        if (Arrays2.isNullOrEmpty(p)) {
            if (p0 == 0) {
                return ZERO;
            } else if (p0 == 1) {
                return ONE;
            } else {
                return new Polynomial(new double[]{p0});
            }
        } else {
            int dp = Coefficients.getUsedCoefficients(p, 0);
            if (dp == 0) {
                return new Polynomial(new double[]{p0});
            } else {
                double[] np = new double[dp + 1];
                np[0] = p0;
                System.arraycopy(p, 0, np, 1, dp);
                return new Polynomial(np);
            }
        }
    }

    /**
     * Create a new Polynomial by using the specified coefficients. The
     * polynomial will be of degree <code>coefficients.length-1</code>
     * <br> Note that the array of doubles is copied. If you don't need
     * defensive copy, use {@link Polynomial#ofInternal(double[])} instead.
     *
     * @param coefficients
     * @return a non-null Polynomial
     */
    public static Polynomial of(@NonNull double[] coefficients) {
        return new Polynomial(Coefficients.of(coefficients));
    }

    /**
     * Create a new Polynomial by using the specified coefficients, from reader.
     * The polynomial will be of degree <code>n-1</code> <br>, except if there
     * are 0 values at the end
     *
     * @param coefficients
     * @param start First position in the array
     * @param end Last position in the array (excluded)
     * @return a non-null Polynomial
     */
    public static Polynomial of(@NonNull double[] coefficients, int start, int end) {
        double[] copy = Arrays.copyOfRange(coefficients, start, end);
        return new Polynomial(Coefficients.ofInternal(copy));
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
            return new Polynomial(new double[]{c});
        }

        double[] pcoeff = new double[roots.length + 1];

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
            pcoeff[i] = p[i].getRe();
        }

        Polynomial pol = new Polynomial(pcoeff);
        pol.defRoots.set(roots.clone());
        return pol;
    }

    /**
     * The polynomial is equal to 1 - c*x^n
     *
     * @param c The coefficient. Should be strictly higher than 0
     * @param n The getDegree ofFunction the polynomial
     * @return The new polynomial. The roots ofFunction the polynomial are also
     * computed
     */
    public static Polynomial factor(double c, int n) {
        if (c == 0) {
            return ONE;
        }
        double[] p = new double[n + 1];
        p[0] = 1;
        p[n] = -c;
        Polynomial F = Polynomial.ofInternal(p);
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

    //</editor-fold>
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

    /**
     * The constructor takes an array ofFunction doubles. The array represents
     * the coefficients of the polynomial.
     *
     * @param coefficients
     */
    private Polynomial(final double[] coefficients) {
        // Keep this contructor alone and private; use factory methods instead
        this.coeff = coefficients;
    }

    public int getDegree() {
        return coeff.length - 1;
    }

    /**
     * This read-only property will return a polynomial representing the first
     * derivate ofFunction the polynomial.
     *
     * @return
     */
    public Polynomial derivate() {
        if (coeff.length == 1) {
            return Polynomial.ZERO;
        }
        int d = coeff.length - 1;
        double[] result = new double[d];
        for (int i = 1; i <= d; ++i) {
            result[i - 1] = i * coeff[i];
        }
        return new Polynomial(result);
    }

    /**
     * The static method creates a new polynomial as the division ofFunction p
     * and a constant value d. cnew[0] = cold[0]-d.
     *
     * @param d
     * @return
     */
    public Polynomial divide(final double d) {
        return times(1 / d);
    }

    /**
     * The operator divides two polynomials creating a new polynomial as a
     * result. The roots ofFunction the resulting polynomial are only calculated
     * when the roots ofFunction l and r have been calculated before. This
     * method does not return the remainder ofFunction the division
     *
     * @param r A polynomial ofFunction getDegree d'
     * @return The quotient ofFunction l and r
     */
    public Polynomial divide(final Polynomial r) {
        // if (l == null)
        // throw new ArgumentNullException("l");
        //return divide(this, r).getQuotient();
        return Polynomial.ofInternal(divide(coeff, r.coeff));
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
        if (coeff.length != other.coeff.length) {
            return false;
        }
        for (int i = 0; i <= coeff.length; ++i) {
            if (!DoubleSequence.equals(get(i), other.get(i), epsilon)) {
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
        int i = coeff.length - 1;
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
        int i = coeff.length - 1;
        double f = get(i--);
        for (; i >= 0; --i) {
            f = coeff[i] + (f * x);
        }
        return f;
    }

    /**
     * Evaluates a polynomial defined by given coefficients at a given point.
     * The coefficients are stored in reverse order (the first coefficient
     * corresponds to the highest power and the last one to the constant)
     *
     * @param c The coefficients. Should contain at least one element (not
     * checked)
     * @param x The evaluation point;
     * @return the value ofFunction p(x)
     */
    public static double reverseEvaluate(final double[] c, final double x) {
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
     * The coefficients are stored in normal order (the first coefficient
     * corresponds to the constant and the last one to the highest power)
     *
     * @param c The coefficients. Should contain at least one element (not
     * checked)
     * @param x The evaluation point;
     * @return the value ofFunction p(x)
     */
    public static double evaluate(final double[] c, final double x) {
        int p = c.length - 1;
        double y = c[p--];
        for (; p >= 0; --p) {
            y = c[p] + (y * x);
        }
        return y;
    }

    /**
     * Evaluates a polynomial with coefficients defined by a give function at a
     * given point.
     *
     * @param degree The getDegree ofFunction the polynomial
     * @param fn The function defining the coefficients. fn(i) is the
     * coefficient corresponding to the power i
     * @param x The evaluation point;
     * @return the value ofFunction p(x)
     */
    public static double evaluate(final int degree, IntToDoubleFunction fn, final double x) {
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
        for (int i = 1; i < coeff.length; ++i) {
            f.add(Complex.polar(coeff[i], w * i));
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
        return coeff[idx];
    }

    /**
     * Gets a copy ofFunction the coefficients of the polynomial.
     *
     * @return
     */
    public DoubleSequence coefficients() {
        return DoubleSequence.ofInternal(coeff);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(coeff);
    }

    /**
     * The read-only property returns whether the polynomial is a zero
     * polynomial. A zero polynomial is a polynomial ofFunction getDegree 0 and
     * a single coefficient with value zero.
     *
     * @return
     */
    public boolean isZero() {
        return coeff.length == 1 && coeff[0] == 0;
    }

    /**
     * The read-only property returns whether the polynomial is an identity
     * polynomial. An identity polynomial is a polynomial ofFunction getDegree 0
     * and a single coefficient with value 1.
     *
     * @return
     */
    public boolean isIdentity() {
        return coeff.length == 1 && coeff[0] == 1;
    }

    /**
     * The static method creates a new polynomial as the difference ofFunction p
     * and a constant value d. cnew[0] = cold[0]-d. The roots ofFunction the
     * result must be recomputed.
     *
     * @param d
     * @return
     */
    public Polynomial minus(double d) {
        return plus(-d);
    }

    /**
     * The operator subtracts two polynomials creating a new polynomial as a
     * result. The algorithm checks the getDegree (equal highest power
     * coefficients will annihilate this power). The roots ofFunction the
     * resulting polynomial are only calculated when the roots ofFunction l and
     * r have been calculated before.
     *
     * @param r A polynomial ofFunction getDegree d'
     * @return The difference ofFunction l and r
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
        if (coeff.length == 1) {
            return this;
        }
        double[] result = new double[coeff.length];
        for (int i = 0, j = coeff.length - 1; i < coeff.length; ++i, --j) {
            result[i] = coeff[j];
        }
        return new Polynomial(Coefficients.ofInternal(result));
    }

    /**
     * The operator changes the sign ofFunction the coefficients ofFunction the
     * polynomial. A new polynomial is returned as a result.
     *
     * @return
     */
    public Polynomial negate() {
        return times(-1);
    }

    /**
     * Creates a new polynomial as the sum ofFunction this and a constant value
     * d. cnew[0] = d+cold[0]. The roots ofFunction the result are not computed.
     *
     * @param d
     * @return
     */
    public Polynomial plus(final double d) {
        if (d == 0d) {
            return this;
        }
        double[] result = coefficients().toArray();
        result[0] += d;
        return new Polynomial(result);
    }

    /**
     * The operator adds a polynomial creating a new polynomial as a result. The
     * algorithm checks the getDegree (equal highest power coefficients with
     * inverse sign will annihilate this power). The roots ofFunction the
     * resulting polynomial are only calculated when the roots ofFunction this
     * and r have been calculated before.
     *
     * @param r A polynomial ofFunction getDegree d'
     * @return The sum ofFunction this and r
     */
    public Polynomial plus(final Polynomial r) {
        if (r.isZero()) {
            return this;
        }
        // swap l and r if l.Degree < r.Degree
        if (coeff.length < r.coeff.length) {
            return r.plus(this);
        }
        double[] result = coeff.clone();
        for (int i = 0; i < r.coeff.length; ++i) {
            result[i] += r.get(i);
        }
        return new Polynomial(Coefficients.ofInternal(result));
    }

    /**
     * The property get returns the roots ofFunction the polynomial. To do so it
     * will use a statically defined RootsSearcher algorithm. By default this is
     * the Muller algorithm for finding roots ofFunction polynomials. You can
     * change the algorithm by setting DefRootsSearcher to another RootsSearcher
     * object. Setting this property re-initializes the internal state
     * ofFunction the polynomial.
     *
     * @return
     */
    public Complex[] roots() {
        Complex[] result = defRoots.get();
        if (result == null) {
            result = roots(IRootsSolver.fastSolver());
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
     * @param solver
     * @return
     */
    public Complex[] roots(IRootsSolver solver) {
        if (coeff.length == 1) {
            return new Complex[0];
        }
        if (solver == null) {
            solver = IRootsSolver.fastSolver();
        }
        if (solver.factorize(this)) {
            Complex[] roots = solver.roots();
            solver.clear();
            return roots;
        } else {
            return null;
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
        double[] result = coefficients().toArray();
        for (int i = 0; i < result.length; ++i) {
            double c = Math.round(result[i]);
            if (DoubleSequence.equals(c, result[i], EPSILON)) {
                result[i] = c;
            }
        }
        return new Polynomial(Coefficients.ofInternal(result));
    }

    /**
     * The static method creates a new polynomial as the product ofFunction p
     * and a constant value d. cnew[i] = d*cold[i]. The roots are invariant to
     * this change in scale.
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
        double[] c = new double[coeff.length];
        for (int i = 0; i < c.length; ++i) {
            c[i] = d * coeff[i];
        }
        Polynomial result = new Polynomial(c);
        result.defRoots.set(defRoots.get());
        return result;
    }

    /**
     * The operator multiplies two polynomials creating a new polynomial as a
     * result. The roots ofFunction the resulting polynomial are only calculated
     * when the roots ofFunction l and r have been calculated before.
     *
     * @param r A polynomial ofFunction getDegree d'
     * @return The product ofFunction l and r
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
        int d = getDegree() + r.getDegree();
        double[] result = new double[d + 1];
        for (int u = 0; u < coeff.length; ++u) {
            if (coeff[u] != 0) {
                for (int v = 0; v < r.coeff.length; ++v) {
                    if (r.coeff[v] != 0) {
                        result[u + v] += coeff[u] * r.coeff[v];
                    }
                }
            }
        }
        Polynomial prod = new Polynomial(result);
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
     * @return The string representation ofFunction the polynomial
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
     * @return A string representation ofFunction the polynomial
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

    static final class Coefficients {

        private static final double[] C_ZERO = {0}, C_ONE = {1},
                C_POSINF = {Double.POSITIVE_INFINITY},
                C_NEGINF = {Double.NEGATIVE_INFINITY},
                C_NAN = {Double.NaN};

        private Coefficients() {
            // static class
        }

        /**
         * Return the coefficients of a polynomial of getDegree n as an array of
         * doubles. All coefficients are set to zero, except the highest which
         * is set to 1.
         *
         * @param degree
         * @return
         */
        static double[] fromDegree(int degree) {
            // all coefficuents are set to 0, except the highest.
            double[] c = new double[degree + 1];
            c[degree] = 1;
            return c;
        }

        static double[] ofInternal(double[] c) {
            int nd = getUsedCoefficients(c, EPSILON);
            if (nd == c.length) {
                return c;
            } else if (nd == 0) {
                return C_ZERO;
            } else {
                return Arrays.copyOf(c, nd);
            }
        }

        static double[] of(double[] c) {
            int nd = getUsedCoefficients(c, EPSILON);
            if (nd == c.length) {
                return c.clone();
            } else if (nd == 0) {
                return C_ZERO;
            } else {
                return Arrays.copyOf(c, nd);
            }
        }

        static double[] zero() {
            return C_ZERO;
        }

        static double[] one() {
            return C_ONE;
        }

        static double[] positiveInfinity() {
            return C_POSINF;
        }

        static double[] negativeInfinity() {
            return C_NEGINF;
        }

        static double[] nan() {
            return C_NAN;
        }

        static int getUsedCoefficients(double[] coefficients, double eps) {
            int n = coefficients.length ;
            while ((n > 0) && (Math.abs(coefficients[n-1]) <= eps)) {
                --n;
            }
            return n;
        }
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
         * @return The quotient ofFunction the division
         */
        public Polynomial getQuotient() {
            return m_q;
        }

        /**
         * @return The remainder ofFunction the division
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
            common = Polynomial.ONE;
            if (left.coeff.length >= right.coeff.length) {
                simplify(left, right, null);
            } else {
                simplify(right, left, null);
                Polynomial tmp = simplifiedLeft;
                simplifiedLeft = simplifiedRight;
                simplifiedRight = tmp;
            }

            return common.coeff.length > 1;
        }

        private boolean simplify(Polynomial left, final Polynomial right, Complex[] roots) // simplifiedLeft.Degree >= right.Degree)
        {
            if (right.coeff.length == 1) {
                return false;
            }
            // try first exact division
            if (simplifyExact(left, right)) {
                return true;
            }
            if (roots == null) {
                roots = right.roots();
            }
            double[] rtmp = Coefficients.fromDegree(1);
            double[] ctmp = Coefficients.fromDegree(2);
            for (Complex element : roots) {
                if (left.evaluateAt(element).abs() < 1E-12) {
                    double a = element.getRe(), b = element.getIm();
                    if (b == 0) // real root
                    {
                        /*
                         * rtmp.pcoeff[0] = -a; Division division = new Division();
                         * division.divide(simplifiedLeft, rtmp); Polynomial div =
                         * division.getQuotient(); rem =
                         * division.getRemainder(); rem.smooth(); if
                         * (rem.isNull()) { simplifiedLeft = div; common =
                         * common.times(rtmp); }
                         */
                        // if element is a root, remove it
                        rtmp[0] = -a;
                        // FIXME: find a way to avoid creating xxx
                        Polynomial xxx = Polynomial.ofInternal(rtmp);
                        left = left.divide(xxx);
                        common = common.times(xxx);
                    } else if (b > 0) {
                        // (x-(a+bi))*(x-(a-bi) = x^2-2ax+a^2+b^2
                        ctmp[0] = a * a + b * b;
                        ctmp[1] = -2 * a;
                        /*
                         * Division division = new Division();
                         * division.divide(simplifiedLeft, ctmp); Polynomial div =
                         * division.getQuotient(); rem =
                         * division.getRemainder(); rem.smooth(); if
                         * (rem.isNull()) { simplifiedLeft = div;
                         */
                        // FIXME: find a way to avoid creating xxx
                        Polynomial xxx = Polynomial.ofInternal(ctmp);
                        left = left.divide(xxx);
                        common = common.times(xxx);
                    }
                }
            }
            if (common.getDegree() > 0) {
                simplifiedLeft = left;
                simplifiedRight = right.divide(common);
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
            common = Polynomial.ONE;
            return simplify(left, right.toPolynomial(), right.roots());
        }

        private boolean simplifyExact(Polynomial left, Polynomial right) {
            LeastSquaresDivision div = new LeastSquaresDivision();
            if (!div.divide(left, right) || !div.isExact()) {
                return false;
            }
            simplifiedLeft = div.getQuotient();
            simplifiedRight = Polynomial.ONE;
            common = right;
//            Polynomial.Division division = Polynomial.divide(simplifiedLeft, right);
//            if (!division.isExact()) {
//                return false;
//            }
//            simplifiedLeft = division.getQuotient();
//            simplifiedRight = Polynomial.ONE;
//            common = right;
            return true;
        }
    }

    /**
     * This method divides the polynomial by a second polynomial. The quotient
     * is returned as a new polynomial. The remainder ofFunction the division is
     * returned in an out parameter. Roots ofFunction the result are only
     * calculated when the roots ofFunction the instance and ofFunction the
     * polynomial p have already been calculated.
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
        Polynomial m_q = new Polynomial(Arrays.copyOf(q, n - nv + 1));
        Polynomial m_r = nv > 0 ? new Polynomial(Coefficients.ofInternal(Arrays.copyOf(r, nv))) : Polynomial.ZERO;
        return new Division(m_r, m_q);
    }

    static double[] divide(final double[] num, final double[] denom) {
        int n = num.length - 1, nv = denom.length - 1;
        while (n >= 0) {
            if (Math.abs(num[n]) > EPSILON) {
                break;
            } else {
                --n;
            }
        }
        if (n < 0) {
            return Coefficients.zero();
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
                return Coefficients.positiveInfinity();
            } else if (num[n] < 0) {
                return Coefficients.negativeInfinity();
            } else {
                return Coefficients.nan();
            }
        }

        if (nv > n) {
            return Polynomial.Coefficients.zero();
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
                                PolynomialException.CONJUGATE);
                    }
                    rootsc[j++] = c;
                    rootsc[j++] = conj;
                }
            }
        }
        return rootsc;
    }

}
