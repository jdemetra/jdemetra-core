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
package jdplus.maths.polynomials;

import demetra.design.Development;
import demetra.design.Immutable;
import demetra.design.SkipProcessing;
import demetra.math.Complex;
import demetra.math.Constants;

/**
 * Rational function expansion, defined by N(x)/D(x)
 *
 * @author Jean Palate
 */
@Immutable(lazy=true)
@Development(status = Development.Status.Alpha)
  public final class RationalFunction {

    private static final RationalFunction ZERO = new RationalFunction(), ONE = RationalFunction.of(Polynomial.ONE, Polynomial.ONE);

    private volatile double[] coeff;
    private final Polynomial num;
    private final Polynomial denom;
    private final static int ATOM = 32;

    public static RationalFunction of(Polynomial num, Polynomial denom) {
        return new RationalFunction(num, denom, false);
    }

     public static RationalFunction zero() {
        return ZERO;
    }

    public static RationalFunction one() {
        return ONE;
    }

    private RationalFunction() {
        num = Polynomial.ZERO;
        denom = Polynomial.ONE;
    }

    /**
     * The constructor creates a new rational function pn(x)/pd(x) based on the
     * polynomials passed in as parameters.
     *
     * @param pn Numerator
     * @param pd Denominator
     * @param simplify Indicates that the polynomials must be simplified. If
     * not, we suppose that the two polynomials are prime (not checked)
     */
    public RationalFunction(final Polynomial pn, final Polynomial pd, boolean simplify) {
        if (simplify) {
            Polynomial.SimplifyingTool smp = new Polynomial.SimplifyingTool();
            if (smp.simplify(pn, pd)) {
                num = smp.getLeft();
                denom = smp.getRight();
            } else {
                num = pn;
                denom = pd;
            }
        } else {
            num = pn;
            denom = pd;
        }
    }

    /**
     * The method returns the coefficients of the first n terms in the power
     * series expansion.
     *
     * @param n Indicates the number ofInternal coefficients requested
     * @return An array ofInternal double representing the first n coefficients
     */
    public double[] coefficients(final int n) {
        prepare(n - 1);
        double[] rslt = new double[n];
        int nmin = n > coeff.length ? coeff.length : n;
        for (int i = 0; i < nmin; ++i) {
            rslt[i] = coeff[i];
        }
        return rslt;
    }

    /**
     * The method divides two rational functions and returns the result as a new
     * one. Internally n/d is calculated as (n1*d2)/(d1*n2). A new rational
     * function object is created with p1 as the numerator of the above
     * expression and p2 as the denominator of the above expression. The
     * resulting rational function is simplified prior to returning.
     *
     * @param r2 A rational expression
     * @return The sum of the rational expressions
     */
    public RationalFunction divide(final RationalFunction r2) {
        return new RationalFunction(num.times(r2.denom), denom.times(r2.num), true);
    }

    /**
     * The method computes the rational function that has the same expansion of
     * this one, once the n first weights have been dropped.
     *
     * If this=num(x)/denom(x)=sum(p(i)x^i), we can see that [sum(p(i)x^i), i>n]
     * has the form m(x)/denom(x) (same denominator); getDegree(m(x)) will be
     * equal to the maximum of getDegree(denom(x)) and getDegree((num(x))-(n-1).
     * The coefficients of m(x) are easily computed by using the relationship
     * m(x) = denom(x)*p(i+n)x^i)
     *
     * @param n The number of weights to be dropped
     * @return
     */
    public RationalFunction drop(final int n) {
        Polynomial cn = num, cd = denom;

        // size of the numerator:
        int p = cd.degree() + 1;
        int q = p;// -1;
        if (cn.degree() + 1 - n > q) {
            q = cn.degree() + 1 - n;
        }
        if (q == 0) {
            return new RationalFunction();
        }
        double[] phi = new double[q];
        double[] ntmp = new double[q];
        for (int i = 0; i < q; ++i) {
            phi[i] = get(i + n);
        }

        ntmp[0] = phi[0] * cd.get(0);
        for (int i = 1; i < q; ++i) {
            double s = 0;
            int imax = i < q ? i : q - 1;
            for (int j = 0; j <= imax; ++j) {
                int k = i - j;
                if (k < p) {
                    s += cd.get(k) * phi[j];
                }
            }
            if (Math.abs(s) < Constants.getEpsilon()) {
                s = 0;
            }
            ntmp[i] = s;
        }
        // simplifiy ntmp
        int qc = q;
        while ((qc > 0) && (ntmp[qc - 1] == 0)) {
            --qc;
        }
        if (qc == 0) {
            return new RationalFunction();
        }
        if (qc != q) {
            double[] ntmp2 = new double[qc];
            for (int i = 0; i < qc; ++i) {
                ntmp2[i] = ntmp[i];
            }
            ntmp = ntmp2;
        }
        return new RationalFunction(Polynomial.ofInternal(ntmp), cd, false);

    }

    /**
     * The method evaluates the rational function for a given complex value x.
     *
     * @param c A complex value
     * @return The value of p1(x)/p2(x) as a complex
     * @throws PolynomialException Thrown when p2 evaluates to zero at value x
     */
    public Complex evaluateAt(final Complex c) {
        final Complex nx = num.evaluateAt(c);
        final Complex dx = denom.evaluateAt(c);
        if (dx.abs() <= Constants.getEpsilon()) {
            throw new PolynomialException(PolynomialException.POLE);
        }
        return nx.div(dx);
    }

    /**
     * The method evaluates the rational function for a given double value x.
     *
     * @param x A double value
     * @return The value ofInternal p1(x)/p2(x) as a double
     * @throws PolynomialException Thrown when p2 evaluates to zero at value x
     */
    public double evaluateAt(final double x) {
        double nx = num.evaluateAt(x);
        double dx = denom.evaluateAt(x);
        if (Math.abs(dx - 0.0) <= Constants.getEpsilon()) {
            throw new PolynomialException(PolynomialException.POLE);
        }
        return nx / dx;
    }

    /**
     * This read-only indexer returns the coefficient for the k-th term in the
     * power series expansion. The property's value is zero for k larger than
     * the highest power term.
     *
     * @param k
     * @return
     */
    public double get(final int k) {
        prepare(k);
        return k >= coeff.length ? 0 : coeff[k];
    }

    /**
     * The property gets/sets the denominator ofInternal the rational function.
     * Setting the denominator implies simplification ofInternal the rational
     * function.
     *
     * @return
     */
    public Polynomial getDenominator() {
        return denom;
    }

    /**
     * The property gets/sets the numerator ofInternal the rational function.
     * Setting the numerator implies simplification ofInternal the rational
     * function.
     *
     * @return
     */
    public Polynomial getNumerator() {
        return num;
    }

    /**
     * This read-only property indicates whether the power series is Finite. It
     * is when the denominator is a constant value.
     *
     * @return
     */
    public boolean isFinite() {
        return denom.degree() == 0;
    }

    /**
     * The method subtracts two rational functions and returns the result as a
     * new one. Internally r1-r2 is calculated as
     * (r1.p1*r2.p2+-2.p1*r1.p2)/(r1.p2*r2.p2). A new rational function object
     * is created with p1 as the numerator of the above expression and p2 as the
     * denominator of the above expression. The resulting rational function is
     * simplified prior to returning.
     *
     * @param r2 A rational expression
     * @return The difference ofInternal the rational expressions
     */
    public RationalFunction minus(final RationalFunction r2) {
        /*
         * if (r1 == null) throw new ArgumentNullException("r1"); if (r2 ==
         * null) throw new ArgumentNullException("r2");
         */
        Polynomial pn = num.times(r2.denom).minus(denom.times(r2.num));
        Polynomial pd = denom.times(r2.denom);
        return new RationalFunction(pn, pd, true);
    }

    /**
     * The method adds two rational functions and returns the result as a new
     * one. Internally r1+r2 is calculated as
     * (r1.p1*r2.p2+r2.p1*r1.p2)/(r1.p2*r2.p2). A new rational function object
     * is created with p1 as the numerator ofInternal the above expression and
     * p2 as the denominator ofInternal the above expression. The resulting
     * rational function is simplified prior to returning.
     *
     * @param r2 A rational expression
     * @return The sum ofInternal the rational expressions
     */
    public RationalFunction plus(final RationalFunction r2) {
        /*
         * if (r1 == null) throw new ArgumentNullException("r1"); if (r2 ==
         * null) throw new ArgumentNullException("r2");
         */
        Polynomial pn = num.times(r2.denom).plus(denom.times(r2.num));
        Polynomial pd = denom.times(r2.denom);
        return new RationalFunction(pn, pd, true);
    }

    /**
     *
     * @return
     */
    public Complex[] poles() {
        return denom.roots();
    }

    /**
     * The read-only returns the poles ofInternal the rational function. These
     * are the values that evaluate to zero in the denominator. The poles are
     * the roots ofInternal the denominator polynomial.
     *
     * @param searcher
     * @return
     */
    public Complex[] poles(final RootsSolver searcher) {
        return denom.roots(searcher);
    }

    /**
     * The method computes the weights ofInternal the power series expansion
     *
     * @param degree
     */
    public void prepare(int degree) {
        double[] c = coeff;
        if (c == null || c.length <= degree) {
            synchronized (this) {
                Polynomial pd = denom;
                Polynomial pn = num;

                int k0 = 1;
                int p = pd.degree();
                int q = pn.degree();
                double d = pd.get(0);

                // trivial case
                if (p == 0) {
                    c = pn.coefficients().toArray();
                    if (d != 1) {
                        for (int i = 0; i < c.length; ++i) {
                            c[i] /= d;
                        }
                    }
                    coeff = c;
                    return;
                }

                // compute at least the first max(p, q) weights, otherwise, expand by
                // blocks
                int r = p < q ? q : p;
                if (degree < r) {
                    degree = r;
                } else {
                    degree = ((degree - 1) / ATOM + 1) * ATOM;
                }

                if (c == null) {
                    c = new double[degree + 1];
                    c[0] = pn.get(0) / d;

                    // until p...
                    for (int k = k0; k <= p; ++k) {
                        double s = 0;
                        for (int w = 1; w <= k; ++w) {
                            s += pd.get(w) * c[k - w];
                        }
                        if (k <= q) {
                            c[k] = (pn.get(k) - s) / d;
                        } else {
                            c[k] = -s / d;
                        }
                    }

                    // until r...
                    if (q > p) {
                        for (int k = p + 1; k <= q; ++k) {
                            double s = 0;
                            for (int w = 1; w <= p; ++w) {
                                s += pd.get(w) * c[k - w];
                            }
                            c[k] = (pn.get(k) - s) / pd.get(0);
                        }
                    }

                    k0 = r + 1;
                } else {
                    double[] tmp = new double[degree + 1];
                    k0 = c.length;
                    for (int u = 0; u < k0; ++u) {
                        tmp[u] = c[u];
                    }
                    c = tmp;
                }

                for (int k = k0; k <= degree; ++k) {
                    double s = 0;
                    for (int w = 1; w <= p; ++w) {
                        s += pd.get(w) * c[k - w];
                    }
                    c[k] = -s / d;
                }
                coeff = c;
            }
        }
    }

    /**
     * The read-only property returns the roots ofInternal the rational
     * function. These are the roots ofInternal the numerator polynomial.
     *
     * @return
     */
    public Complex[] roots() {
        return num.roots();
    }

    /**
     *
     * @param searcher
     * @return
     */
    public Complex[] roots(final RootsSolver searcher) {
        return num.roots(searcher);
    }

    /**
     * The method multiplies two rational functions and returns the result as a
     * new one. A new rational function object is created with p1 as the
     * numerator of the above expression and p2 as the denominator ofl the above
     * expression. The resulting rational function is simplified prior to
     * returning.
     *
     * @param r2 A rational expression
     * @return The product ofInternal the rational expressions
     */
    public RationalFunction times(final RationalFunction r2) {
        return new RationalFunction(num.times(r2.num), denom.times(r2.denom), true);
    }

    /**
     * The method writes the rational function as a string. It is written as
     * [p1]/[p2]. where p1 and p2 are polynomials.
     *
     * @return The string representation ofInternal a rational function
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(1024);
        sb.append("[");
        sb.append(num.toString());
        sb.append("]/[");
        sb.append(denom.toString());
        sb.append("]");
        return sb.toString();
    }
}
