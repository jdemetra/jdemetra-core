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
import ec.tstoolkit.maths.Complex;
import ec.tstoolkit.utilities.Arrays2;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class Laguerre2 implements IRootsSolver {

    private final double m_AbsoluteAccuracy = 1E-6;

    private final double m_RelativeAccuracy = 1E-14;

    private final double m_FunctionValueAccuracy = 1E-15;

    private Complex[] m_roots;

    private Polynomial m_remainder;

    private int m_niterations;
    ;
    private final int m_maximalIterationCount = 100;

    /**
     * Default constructor
     */
    public Laguerre2() {
    }

    @Override
    public void clear() {
        m_roots = null;
        m_remainder = null;
    }

    @Override
    public boolean factorize(final Polynomial p) {
        try {
            m_niterations = 0;
            int d = p.getDegree();
            m_roots = new Complex[d];
            Complex[] a = new Complex[d + 1];
            for (int u = 0; u <= d; ++u) {
                a[u] = Complex.cart(p.get(u), 0);
            }
            zroots(a, m_roots);
            m_remainder = Polynomial.valueOf(p.get(p.getDegree()));
            return true;
        } catch (PolynomialException e) {
            m_remainder = p;
            m_roots = null;
            return false;
        }
    }

    @Override
    public Polynomial remainder() {
        return m_remainder;
    }

    @Override
    public Complex[] roots() {
        return m_roots;
    }

    @Override
    public Laguerre2 exemplar() {
        Laguerre2 solver = new Laguerre2();
        return solver;
    }

    /**
     *
     * @param coefficients
     * @param initial
     * @return
     * @throws PolynomialException
     */
    public Complex solve(Complex coefficients[], Complex initial)
            throws PolynomialException {
        int n = coefficients.length - 1;
        Complex N = Complex.cart(n, 0.0);
        Complex N1 = Complex.cart(n - 1, 0.0);

        int i = 1;
        Complex pv = null;
        Complex dv = null;
        Complex d2v = null;
        Complex G = null;
        Complex G2 = null;
        Complex H = null;
        Complex delta = null;
        Complex denominator = null;
        Complex z = initial;
        Complex oldz = Complex.cart(Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY);
        while (i <= m_maximalIterationCount) {
	    // Compute pv (polynomial value), dv (derivative value), and
            // d2v (second derivative value) simultaneously.
            pv = coefficients[n];
            dv = Complex.ZERO;
            d2v = Complex.ZERO;
            for (int j = n - 1; j >= 0; j--) {
                d2v = dv.plus(z.times(d2v));
                dv = pv.plus(z.times(dv));
                pv = coefficients[j].plus(z.times(pv));
            }
            d2v = d2v.times(Complex.TWO);

            // check for convergence
            double tolerance = Math.max(this.m_RelativeAccuracy * z.abs(),
                    this.m_AbsoluteAccuracy);
            if (z.minus(oldz).abs() <= tolerance) {
                m_niterations = i;
                return z;
            }
            if (pv.abs() <= this.m_FunctionValueAccuracy) {
                m_niterations = i;
                return z;
            }

            // now pv != 0, calculate the new approximation
            G = dv.div(pv);
            G2 = G.times(G);
            H = G2.minus(d2v.div(pv));
            delta = N1.times((N.times(H)).minus(G2));
            // choose a denominator larger in magnitude
            Complex deltaSqrt = delta.sqrt();
            Complex dplus = G.plus(deltaSqrt);
            Complex dminus = G.minus(deltaSqrt);
            denominator = dplus.abs() > dminus.abs() ? dplus : dminus;
	    // Perturb z if denominator is zero, for instance,
            // p(x) = x^3 + 1, z = 0.
            if (denominator.equals(Complex.ZERO)) {
                z = z.plus(Complex.cart(this.m_AbsoluteAccuracy,
                        this.m_AbsoluteAccuracy));
                oldz = Complex.cart(Double.POSITIVE_INFINITY,
                        Double.POSITIVE_INFINITY);
            } else {
                oldz = z;
                z = z.minus(N.div(denominator));
            }
            i++;
        }
        throw new PolynomialException("Laguerre failed");
    }

    void zroots(final Complex[] coefficients, final Complex[] roots) {
        int n = coefficients.length - 1;
        int literationCount = 0;
        if (n < 1) {
            return;
        }

        Complex[] c = Arrays2.copyOf(coefficients);

        // solve individual root successively
        Complex[] root = new Complex[n];
        for (int i = 0; i < n; i++) {
            Complex[] subarray = new Complex[n - i + 1];
            System.arraycopy(c, 0, subarray, 0, subarray.length);
            root[i] = solve(subarray, Complex.ZERO);
            // polynomial deflation using synthetic division
            Complex newc = c[n - i];
            Complex oldc = null;
            for (int j = n - i - 1; j >= 0; j--) {
                oldc = c[j];
                c[j] = newc;
                newc = oldc.plus(newc.times(root[i]));
            }
            literationCount += this.m_niterations;
        }

        m_roots = root;
        m_niterations = literationCount;
    }
}
