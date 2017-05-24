/*
* Copyright 2013 National Bank ofInternal Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions ofInternal the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy ofInternal the Licence at:
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

import demetra.data.Doubles;
import demetra.design.Development;
import java.text.NumberFormat;

import demetra.maths.polynomials.Polynomial;
import java.util.Arrays;
import java.util.function.IntToDoubleFunction;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class FiniteFilter extends AbstractFiniteFilter implements Cloneable {

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
	for (int i = llb; i <= lub; ++i)
	    p[i - lb] = weights.applyAsDouble(i);
	// p[-lb] corresponds to x^0
	p[-lb] += d;
	return FiniteFilter.promote(p, lb);
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
	for (int i = llb; i <= lub; ++i)
	    p[i - lb] = lweights.applyAsDouble(i);
	for (int i = rlb; i <= rub; ++i)
	    p[i - lb] += rweights.applyAsDouble(i);
	return FiniteFilter.promote(p, lb);
    }

    /**
     * 
     * @param l
     * @param d
     * @return
     */
    public static FiniteFilter multiply(final IFiniteFilter l, final double d) {
	int lb = l.getLowerBound();
	double[] p = l.toArray();
	for (int i = 0; i < p.length; ++i)
	    p[i] *= d;
	return FiniteFilter.promote(p, lb);
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
	Polynomial lp = l.asPolynomial(), rp = r.asPolynomial();
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
	double[] p = l.toArray();
	for (int i = 0; i < p.length; ++i)
	    p[i] = -p[i];
	return FiniteFilter.promote(p, lb);
    }

    /**
     * 
     * @param c
     * @param lb
     * @return
     */
    public static FiniteFilter promote(final double[] c, final int lb) {
        return new FiniteFilter(Polynomial.ofInternal(c), lb);
    }

    /**
     * 
     * @param l
     * @param d
     * @return
     */
    public static FiniteFilter subtract(final IFiniteFilter l, double d)
    {
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
	for (int i = llb; i <= lub; ++i)
	    p[i - lb] = lweights.applyAsDouble(i);
	for (int i = rlb; i <= rub; ++i)
	    p[i - lb] -= rweights.applyAsDouble(i);
	return FiniteFilter.promote(p, lb);
    }

    private int m_lb;

    private Polynomial m_w;

    private static final double g_epsilon = 1e-4;

    // private static final double g_epsilon2 = g_epsilon * g_epsilon;

    /**
     * 
     * @param c
     * @param lb
     */
    public FiniteFilter(final double[] c, final int lb) {
	m_lb = lb;
	m_w = Polynomial.of(c);
    }

        /**
     * 
     * @param c
     * @param lb
     */
    public FiniteFilter(final Polynomial c, final int lb) {
	m_lb = lb;
	m_w = c;
    }

    /**
     * 
     * @param f
     */
    public FiniteFilter(final IFiniteFilter f) {
	double[] w = f.toArray();
	m_w = Polynomial.ofInternal(w);
	m_lb = f.getLowerBound();
    }

    /**
     * 
     * @param n
     */
    public FiniteFilter(final int n) {
	double[] w = new double[n];
        Arrays.fill(w, 1);
	m_w = Polynomial.ofInternal(w);
    }

    /**
     * 
     * @param npast
     * @param nfuture
     */
    public FiniteFilter(int npast, int nfuture)
    {
	m_lb = -npast;
	double[] w = new double[npast + nfuture + 1];
	Polynomial.ofInternal(w); // FIXME: nothing usefull done here
    }

    /**
     * 
     * @param n
     */
    public void backShift(final int n) {
	m_lb -= n;
    }

    /**
     * 
     * @return
     */
    public boolean center() {
	int d = m_w.getDegree();
	if (d % 2 != 0)
	    return false;
	m_lb = -d / 2;
	return true;
    }

    @Override
    public FiniteFilter clone() {
	try {
	    FiniteFilter f = (FiniteFilter) super.clone();
            f.m_w = m_w;
            return f;
	} catch (CloneNotSupportedException err) {
            throw new AssertionError();
	}
    }

    /**
     * 
     * @return
     */
    @Override
    public int length() {
	return m_w.getDegree() + 1;
    }

    /**
     * 
     * @return
     */
    @Override
    public int getLowerBound() {
	return m_lb;
    }

    @Override
    public int getUpperBound() {
	return m_lb + m_w.getDegree();
    }

    /**
     * 
     * @return
     */
    @Override
    public IntToDoubleFunction weights() {
	return i->m_w.get(i-m_lb);
    }

    /**
     * 
     * @return
     */
    public boolean isIdentity()
    {
	return m_w.isIdentity();
    }

    /**
     * 
     * @return
     */
    public boolean isSymmetric() {
	int d = m_w.getDegree();
	if (d % 2 != 0 || d != -m_lb)
	    return false;
	for (int i = 0; i < d / 2; ++i)
	    if (Math.abs(m_w.get(i) - m_w.get(d - i)) > g_epsilon)
		return false;
	return true;
    }

    /**
     * 
     * @return
     */
    public FiniteFilter mirror() {
        Polynomial w = m_w.mirror();
	int lb = 1 - m_lb - (w.getDegree() + 1);
	return new FiniteFilter(w, lb);
    }

    /**
     * 
     * @param d
     */
    public void mul(final double d) {
	m_w = m_w.times(d);
    }

    /**
     * 
     * @param center
     * @return
     */
    public boolean normalize(final boolean center) {
	double s = 0;
	for (int i = 0; i <= m_w.getDegree(); ++i)
	    s += m_w.get(i);
	if (s == 0)
	    return false;
	if (s != 1)
	    m_w = m_w.divide(s);

	if (center)
	    return center();
	return true;
    }


    @Override
    public String toString() {
	Polynomial p = m_w.smooth();
	NumberFormat format = NumberFormat.getNumberInstance();
	format.setMaximumFractionDigits(4);
	format.setMinimumFractionDigits(4);
	// info.NumberDecimalSeparator = "";

	StringBuilder buffer = new StringBuilder(512);
	int curp = m_lb;
	int n = p.getDegree();
	for (int i = 0; i <= n; ++i, ++curp) {
	    double v = Math.abs(p.get(i));
	    if (v >= 1e-6) {
		if (v > p.get(i))
		    buffer.append(" - ");
		else if (i > 0)
		    buffer.append(" + ");
		if (v != 1 || curp == 0)
		    buffer.append(format.format(v));
		if (curp < 0) {
		    buffer.append(' ').append('B');
		    if (curp < -1)
			buffer.append('^').append(-curp);
		} else if (curp > 0) {
		    buffer.append(' ').append('F');
		    if (curp > 1)
			buffer.append('^').append(curp);
		}
	    }
	}
	return buffer.toString();
    }
}
