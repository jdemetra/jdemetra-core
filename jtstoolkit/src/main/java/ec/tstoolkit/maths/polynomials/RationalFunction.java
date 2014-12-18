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

import ec.tstoolkit.design.Development;
import ec.tstoolkit.design.Immutable;
import ec.tstoolkit.maths.Complex;

/**
 *
 * @author Jean Palate
 */
@Immutable
@Development(status = Development.Status.Alpha)
public class RationalFunction {

    private double[] m_c;
    private final Polynomial m_pn;
    private final Polynomial m_pd;
    public static final double EPSILON = 1.0e-12;
    private final static int g_atom = 32;

    /**
     * The default constructor declares a rational function with value 0.
     */
    public RationalFunction() {
        m_pn = Polynomial.ZERO;
        m_pd = Polynomial.ONE;
    }

    /**
     * The constructor creates a new rational function p1/p2 based on the
     * polynomials passed in as parameters.
     *
     * @param pn A polynomial of degree d
     * @param pd A polynomial of degree d'
     */
    public RationalFunction(final Polynomial pn, final Polynomial pd) {
        /*
         * if (pn == null) throw new ArgumentNullException("pn"); if (pd ==
         * null) throw new ArgumentNullException("pd");
         */
        m_pn = pn;
        m_pd = pd;
        // Simplify();
    }

    /**
     * The method returns the coefficients of the first n terms in the power
     * series expansion.
     *
     * @param n Indicates the number of coefficients requested
     * @return An array of double representing the first n coefficients
     */
    public double[] coefficients(final int n) {
        prepare(n - 1);
        double[] rslt = new double[n];
        int nmin = n > m_c.length ? m_c.length : n;
        for (int i = 0; i < nmin; ++i) {
            rslt[i] = m_c[i];
        }
        return rslt;
    }

    /**
     * The method divides two rational functions and returns the result as a new
     * one. Internally r1/r2 is calculated as (r1.p1*r2.p2)/(r1.p1*r2.p2). A new
     * rational function object is created with p1 as the numerator of the above
     * expression and p2 as the denominator of the above expression. The
     * resulting rational function is simplified prior to returning.
     *
     * @param r2 A rational expression
     * @return The sum of the rational expressions
     */
    public RationalFunction divide(final RationalFunction r2) {
        return new RationalFunction(m_pn.times(r2.m_pd), m_pd.times(r2.m_pn));
    }

    /**
     * The method computes the rational function that has the same expansion of
     * this one, once the n first weights have been dropped.
     * 
     * If this=num(x)/denom(x)=sum(p(i)x^i), we can see that 
     * [sum(p(i)x^i), i>n] has the form m(x)/denom(x) (same denominator); 
     * degree(m(x)) will be equal to the maximum of degree(denom(x)) and 
     * degree((num(x))-(n-1).
     * The coefficients of m(x) are easily computed by using the relationship
     * m(x) = denom(x)*p(i+n)x^i)
     *
     * @param n The number of weights to be dropped
     * @return
     */
    public RationalFunction drop(final int n) {
        Polynomial cn = m_pn, cd = m_pd;

        // size of the numerator:
        int p = cd.getDegree() + 1;
        int q = p;// -1;
        if (cn.getDegree() + 1 - n > q) {
            q = cn.getDegree() + 1 - n;
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
            if (Math.abs(s) < EPSILON) {
                s = 0;
            }
            ntmp[i] = s;
        }
        // simplifiy ntmp
        int qc = q;
        while ((qc > 0) && (ntmp[qc - 1] == 0)) {
            --qc;
        }
        if (qc == 0)
            return new RationalFunction();
        if (qc != q) {
            double[] ntmp2 = new double[qc];
            for (int i = 0; i < qc; ++i) {
                ntmp2[i] = ntmp[i];
            }
            ntmp = ntmp2;
        }
        return new RationalFunction(Polynomial.of(ntmp), cd);

    }

    /**
     * The method evaluates the rational function for a given complex value x.
     *
     * @param c A complex value
     * @return The value of p1(x)/p2(x) as a complex
     * @throws PolynomialException Thrown when p2 evaluates to zero at value x
     */
    public Complex evaluateAt(final Complex c) {
        final Complex nx = m_pn.evaluateAt(c);
        final Complex dx = m_pd.evaluateAt(c);
        if (dx.minus(Complex.cart(0.0)).abs() <= EPSILON) {
            throw new PolynomialException(PolynomialException.PoleError);
        }
        return nx.div(dx);
    }

    /**
     * The method evaluates the rational function for a given double value x.
     *
     * @param x A double value
     * @return The value of p1(x)/p2(x) as a double
     * @throws PolynomialException Thrown when p2 evaluates to zero at value x
     */
    public double evaluateAt(final double x) {
        double nx = m_pn.evaluateAt(x);
        double dx = m_pd.evaluateAt(x);
        if (Math.abs(dx - 0.0) <= EPSILON) {
            throw new PolynomialException(PolynomialException.PoleError);
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
        return k >= m_c.length ? 0 : m_c[k];
    }

    /**
     * The property gets/sets the denominator of the rational function. Setting
     * the denominator implies simplification of the rational function.
     *
     * @return
     */
    public Polynomial getDenominator() {
        return m_pd;
    }

    /**
     * The property gets/sets the numerator of the rational function. Setting
     * the numerator implies simplification of the rational function.
     *
     * @return
     */
    public Polynomial getNumerator() {
        return m_pn;
    }

    /**
     * This read-only property indicates whether the power series is Finite. It
     * is when the denominator is a constant value.
     *
     * @return
     */
    public boolean isFinite() {
        return m_pd.getDegree() == 0;
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
     * @return The difference of the rational expressions
     */
    public RationalFunction minus(final RationalFunction r2) {
        /*
         * if (r1 == null) throw new ArgumentNullException("r1"); if (r2 ==
         * null) throw new ArgumentNullException("r2");
         */
        Polynomial pn = m_pn.times(r2.m_pd).minus(m_pd.times(r2.m_pn));
        Polynomial pd = m_pd.times(r2.m_pd);
        return new RationalFunction(pn, pd);
    }

    /**
     * The method adds two rational functions and returns the result as a new
     * one. Internally r1+r2 is calculated as
     * (r1.p1*r2.p2+r2.p1*r1.p2)/(r1.p2*r2.p2). A new rational function object
     * is created with p1 as the numerator of the above expression and p2 as the
     * denominator of the above expression. The resulting rational function is
     * simplified prior to returning.
     *
     * @param r2 A rational expression
     * @return The sum of the rational expressions
     */
    public RationalFunction plus(final RationalFunction r2) {
        /*
         * if (r1 == null) throw new ArgumentNullException("r1"); if (r2 ==
         * null) throw new ArgumentNullException("r2");
         */
        Polynomial pn = m_pn.times(r2.m_pd).plus(m_pd.times(r2.m_pn));
        Polynomial pd = m_pd.times(r2.m_pd);
        return new RationalFunction(pn, pd);
    }

    /**
     *
     * @return
     */
    public Complex[] poles() {
        return m_pd.roots();
    }

    /**
     * The read-only returns the poles of the rational function. These are the
     * values that evaluate to zero in the denominator. The poles are the roots
     * of the denominator polynomial.
     *
     * @param searcher
     * @return
     */
    public Complex[] poles(final IRootsSolver searcher) {
        return m_pd.roots(searcher);
    }

    /**
     * The method computes the weights of the power series expansion
     *
     * @param degree
     */
    public void prepare(int degree) {
        if ((m_c != null) && (m_c.length > degree)) {
            return;
        }
        Polynomial pd = m_pd;
        Polynomial pn = m_pn;

        int k0 = 1;
        int p = pd.getDegree();
        int q = pn.getDegree();
        double d = pd.get(0);

        // trivial case
        if (p == 0) {
            m_c = pn.getCoefficients();
            if (d != 1) {
                for (int i = 0; i < m_c.length; ++i) {
                    m_c[i] /= d;
                }
            }
            return;
        }

        // compute at least the first max(p, q) weights, otherwise, expand by
        // blocks
        int r = p < q ? q : p;
        if (degree < r) {
            degree = r;
        } else {
            degree = ((degree - 1) / g_atom + 1) * g_atom;
        }

        if (m_c == null) {
            m_c = new double[degree + 1];
            m_c[0] = pn.get(0) / d;

            // until p...
            for (int k = k0; k <= p; ++k) {
                double s = 0;
                for (int w = 1; w <= k; ++w) {
                    s += pd.get(w) * m_c[k - w];
                }
                if (k <= q) {
                    m_c[k] = (pn.get(k) - s) / d;
                } else {
                    m_c[k] = -s / d;
                }
            }

            // until r...
            if (q > p) {
                for (int k = p + 1; k <= q; ++k) {
                    double s = 0;
                    for (int w = 1; w <= p; ++w) {
                        s += pd.get(w) * m_c[k - w];
                    }
                    m_c[k] = (pn.get(k) - s) / pd.get(0);
                }
            }

            k0 = r + 1;
        } else {
            double[] tmp = new double[degree + 1];
            k0 = m_c.length;
            for (int u = 0; u < k0; ++u) {
                tmp[u] = m_c[u];
            }
            m_c = tmp;
        }

        for (int k = k0; k <= degree; ++k) {
            double s = 0;
            for (int w = 1; w <= p; ++w) {
                s += pd.get(w) * m_c[k - w];
            }
            m_c[k] = -s / d;
        }
    }

    /**
     * The read-only property returns the roots of the rational function. These
     * are the roots of the numerator polynomial.
     *
     * @return
     */
    public Complex[] roots() {
        return m_pn.roots();
    }

    /**
     *
     * @param searcher
     * @return
     */
    public Complex[] roots(final IRootsSolver searcher) {
        return m_pn.roots(searcher);
    }

    /**
     * The method multiplies two rational functions and returns the result as a
     * new one. Internally r1*r2 is calculated as (r1.p1*r2.p1)/(r1.p2*r2.p2). A
     * new rational function object is created with p1 as the numerator of the
     * above expression and p2 as the denominator of the above expression. The
     * resulting rational function is simplified prior to returning.
     *
     * @param r2 A rational expression
     * @return The product of the rational expressions
     */
    public RationalFunction times(final RationalFunction r2) {
        return new RationalFunction(m_pn.times(r2.m_pn), m_pd.times(r2.m_pd));
    }

    /**
     * The method writes the rational function as a string. It is written as
     * [p1]/[p2]. where p1 and p2 are polynomials.
     *
     * @return The string representation of a rational function
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(1024);
        sb.append("[");
        sb.append(m_pn.toString());
        sb.append("]/[");
        sb.append(m_pd.toString());
        sb.append("]");
        return sb.toString();
    }
}
