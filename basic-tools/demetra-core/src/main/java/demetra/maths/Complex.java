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
package demetra.maths;

import demetra.design.Development;
import demetra.design.Immutable;
import demetra.utilities.Arrays2;

/**
 * Complex number
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable
public final class Complex implements ComplexParts {

    /**
     * A constant representing i
     */
    public static final Complex I = new Complex(0, 1);
    /**
     *
     */
    public static final Complex TWO = new Complex(2, 0);
    /**
     *
     */
    public static final Complex ONE = new Complex(1, 0);
    /**
     *
     */
    public static final Complex ZERO = new Complex(0, 0);
    /**
     *
     */
    public static final Complex NEG_ONE = new Complex(-1, 0);
    /**
     *
     */
    public static final Complex NEG_TWO = new Complex(-2, 0);
    /**
     *
     */
    public static final Complex NEG_I = new Complex(0, -1);
    /**
     * A small double
     */
    public final static double EPS = 1e-9;
    /**
     *
     */
    public final static double TWOPI = 2.0 * Math.PI;

    /**
     * Returns a Complex from real and imaginary parts.
     *
     * @param re the real part
     * @param im the imaginary part
     * @return a non-null complex
     */
    public static Complex cart(final double re, final double im) {
        // most used complexes
        if (re == 0.0) {
            if (im == 1.0) {
                return Complex.I;
            }
            if (im == 0.0) {
                return Complex.ZERO;
            }
            if (im == -1.0) {
                return Complex.NEG_I;
            }
        } else if (im == 0.0) {
            if (re == 1.0) {
                return Complex.ONE;
            }
            if (re == -1.0) {
                return Complex.NEG_ONE;
            }
            if (re == -2.0) {
                return Complex.NEG_TWO;
            }
        }
        // the real work
        return new Complex(re, im);
    }

    /**
     * The static method finds the roots present in lr but not in rr
     *
     * @param lr An array of complex roots
     * @param lrlength The number of roots in lroots to take into account
     * @param rr An array of complex roots
     * @param rrlength The number of roots in lroots to take into account
     * @param epsilon
     * @return An array containing the difference between lr and rr
     */
    public static Complex[] difference(final Complex[] lr, final int lrlength,
            final Complex[] rr, final int rrlength, final double epsilon) {
        boolean[] flags = new boolean[lrlength];
        int rem = 0;
        for (int i = 0; i < rrlength; i++) {
            for (int j = 0; j < lrlength; j++) {
                if (!flags[j] && (lr[j].minus(rr[i]).abs() <= epsilon)) {
                    flags[j] = true;
                    rem++;
                }
            }
        }

        Complex[] remroots = new Complex[lrlength - rem];
        for (int i = 0, j = 0; i < lrlength; i++) {
            if (!flags[i]) {
                remroots[j++] = lr[i];
            }
        }

        return remroots;
    }

    /**
     *
     * @param l
     * @param r
     * @return
     */
    public static double distance(Complex l, Complex r) {
        return ComplexMath.abs(l.re - r.re, l.im - r.im);
    }

    /**
     * The static method finds the common roots in subsets of the arrays passed
     * as parameters
     *
     * @param lnroots The number of roots in lroots to take into account
     * @param lroots An array of complex roots
     * @param rnroots The number of roots in rr to take into account
     * @param rroots An array of complex roots
     * @param epsilon Contains the number of items in the intersection on return
     * @return An array containing the common roots of lr and rr
     */
    public static Complex[] intersection(final int lnroots,
            final Complex[] lroots, final int rnroots, final Complex[] rroots,
            final double epsilon) {
        if ((lnroots == 0) || (rnroots == 0)) {
            return null;
        }

        int croots = 0;
        boolean[] lflags = new boolean[lnroots];
        boolean[] rflags = new boolean[rnroots];

        // determine common roots
        for (int i = 0; i < lnroots; i++) {
            for (int j = 0; j < rnroots; j++) {
                if (!rflags[j] && (lroots[i].minus(rroots[j]).abs() <= epsilon)) {
                    lflags[i] = true;
                    rflags[j] = true;
                    croots++;
                    break;
                }
            }
        }

        Complex[] outroots = new Complex[croots];
        for (int i = 0, k = 0; i < lnroots; i++) {
            if (lflags[i]) {
                outroots[k++] = lroots[i];
            }
        }

        return outroots;
    }

    /**
     *
     * @param xin
     */
    public static void lejaOrder(final Complex[] xin) {
        if (xin == null) {
            return;
        }
        int n = xin.length;
        if (n == 0) {
            return;
        }

        double dist = 0, tmp = 0;
        int m = 0;
        for (int i = 0; i < n; ++i) {
            tmp = xin[i].absSquare();
            if (dist < tmp) {
                dist = tmp;
                m = i;
            }
        }
        Arrays2.swap(xin, 0, m);

        for (int i = 1; i < n; ++i) {
            dist = 0;
            m = i;
            for (int j = i; j < n; ++j) {
                tmp = 1;
                for (int k = 0; k < i; ++k) {
                    double tre = xin[k].re - xin[j].re;
                    double tim = xin[k].im - xin[j].im;
                    tmp *= tre * tre + tim * tim;
                }
                if (dist < tmp) {
                    dist = tmp;
                    m = j;
                }
            }
            Arrays2.swap(xin, i, m);
        }

    }

    /**
     * Returns a Complex from its norm and its argument if c = a + i*b r =
     * sqrt(a*a + b*b) theta = atan(b/a)
     *
     * @param r Norm of the complex number
     * @param theta Argument of the complex number
     * @return
     */
    public static Complex polar(double r, double theta) {
        if (r < 0.0) {
            theta += Math.PI;
            r = -r;
        }

        theta = theta % TWOPI;

        return cart(r * Math.cos(theta), r * Math.sin(theta));
    }

//    /**
//     * 
//     * @return
//     */
//    public static Complex random(IRandomNumberGenerator rng) {
//	return cart(rng.nextDouble() * 2 - 1, rng.nextDouble() * 2 - 1);
//    }
    /**
     *
     * @param c
     * @param nRoots
     * @return
     */
    public static Complex[] roots(final Complex c, final int nRoots) {
        if (nRoots <= 0) {
            return null;
        }
        Complex[] roots = Complex.unitRoots(nRoots);
        double arg = c.arg();
        double abs = c.abs();
        double p = 1;
        p /= nRoots;
        abs = Math.pow(abs, p);
        arg *= p;
        Complex tmp = Complex.polar(abs, arg);
        for (int i = 0; i < nRoots; ++i) {
            roots[i] = roots[i].times(tmp);
        }
        return roots;
    }

    /**
     * The static method forms a union of the roots passed as parameters
     *
     * @param lr An array of complex roots
     * @param rr An array of complex roots
     * @return An array containing the union of lr and rr
     */
    public static Complex[] union(final Complex[] lr, final Complex[] rr) {
        if (lr == null) {
            return rr;
        } else if (rr == null) {
            return lr;
        } else {
            return union(lr, lr.length, rr, rr.length);
        }
    }

    /**
     * The static method forms a union of a subset of the roots passed as
     * parameters
     *
     * @param lr An array of complex roots
     * @param lrlength The number of roots in lr to take into account
     * @param rr An array of complex roots
     * @param rrlength The number of roots in rr to take into account
     * @return An array containing the union of lr and rr
     */
    public static Complex[] union(final Complex[] lr, final int lrlength,
            final Complex[] rr, final int rrlength) {
        if (Arrays2.isNullOrEmpty(lr)) {
            if (Arrays2.isNullOrEmpty(rr)) {
                return null;
            }
            return Arrays2.copyOf(rr);
        }

        if (Arrays2.isNullOrEmpty(rr)) {
            return Arrays2.copyOf(lr);
        }

        Complex[] rslt = new Complex[lrlength + rrlength];

        System.arraycopy(lr, 0, rslt, 0, lrlength);
        System.arraycopy(rr, 0, rslt, lrlength, rrlength);
//	for (int i = 0; i < lrlength; ++i)
//	    rslt[i] = lr[i];
//	for (int i = 0; i < rrlength; i++)
//	    rslt[i + lrlength] = rr[i];

        return rslt;
    }

    private static final Complex[] mroots = ur(12), qroots = ur(4);

    /**
     *
     * @param nRoots
     * @return
     */
    public static Complex[] unitRoots(final int nRoots) {
        if (nRoots <= 0) {
            return null;
        } else if (nRoots == 4) {
            return qroots.clone();
        } else if (nRoots == 12) {
            return mroots.clone();
        } else {
            return ur(nRoots);
        }
    }

    private static Complex[] ur(final int nRoots) {
        Complex[] roots = new Complex[nRoots];
        roots[0] = Complex.ONE;
        if (nRoots == 2) {
            roots[1] = Complex.NEG_ONE;
        } else if (nRoots == 4) {
            roots[1] = Complex.I;
            roots[2] = Complex.NEG_ONE;
            roots[3] = Complex.NEG_I;
        } else if (nRoots != 1) {
            // cos (k+1)z = 2*cos z*cos kz-cos(k-1)z
            // sin (k+1)z = 2*cos z*sin kz-sin(k-1)z
            // if cos z ==w, we have
            // k=0 : 1, 0
            // k=1 : w, sqrt(1-w*w)
            // k=2 : 2*w*w - 1, 2w*sqrt(1-w*w)
            // k=3 : ...
            double v = 2 * Math.PI / nRoots;
//	    double z = Math.cos(v);
//	    double x0 = 1, x1 = z;
//	    double y0 = 0, y1 = Math.sin(v);
//	    roots[1] = Complex.cart(x1, y1);
            for (int q = 1; q < nRoots; ++q) {
                double w = v * q;
                roots[q] = Complex.cart(Math.cos(w), Math.sin(w));
//		double xtmp = 2 * z * x1 - x0;
//		double ytmp = 2 * z * y1 - y0;
//		roots[q] = Complex.cart(xtmp, ytmp);
//		x0 = x1;
//		y0 = y1;
//		x1 = xtmp;
//		y1 = ytmp;
            }
        }
        return roots;
    }

    /**
     *
     */
    private final double re;

    /**
     *
     */
    private final double im;

    // ///////////////////////////////////////////
    /**
     * Constructs a Complex representing a real number. The im-part is zero.
     *
     * @param re
     * @return
     */
    public static Complex cart(final double re) {
        return Complex.cart(re, 0);
    }

    private Complex(final double re, final double im) {
        this.re = re;
        this.im = im;
    }

    public Complex sqrt() {
        return ComplexMath.sqrt(re, im);
    }

    /**
     * Returns the conjugate of this complex number.
     *
     * @return
     */
    public Complex conj() {
        return Complex.cart(re, -im);
    }

    /**
     *
     * @param c
     * @return
     */
    public Complex div(final Complex c) {
        double dRe, dIm;
        double scalar;

        if (Math.abs(c.re) >= Math.abs(c.im)) {
            scalar = 1.0 / (c.re + c.im * (c.im / c.re));

            dRe = scalar * (re + im * (c.im / c.re));
            dIm = scalar * (im - re * (c.im / c.re));

        } else {
            scalar = 1.0 / (c.re * (c.re / c.im) + c.im);

            dRe = scalar * (re * (c.re / c.im) + im);
            dIm = scalar * (im * (c.re / c.im) - re);
        }// endif
        return Complex.cart(dRe, dIm);
    }

    /**
     *
     * @param b
     * @return
     */
    public Complex div(final double b) {
        if (b == 1.0) {
            return this;
        }
        return Complex.cart(re / b, im / b);
    }

    /**
     * Decides if two Complex numbers are "sufficiently" alike to be considered
     * equal.
     *
     * @param z
     * @param tolerance
     * @return
     */
    public boolean equals(final Complex z, final double tolerance) {
        // still true when _equal_ to tolerance? ...
        return ComplexMath.abs(re - z.re, im - z.im) <= tolerance;
        // ...and tolerance is always non-negative
    }// end Equals(Complex,double)

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof Complex && equals((Complex) obj));
    }

    private boolean equals(Complex other) {
        return (re == other.re) && (im == other.im);
    }

    @Override
    public double getIm() {
        return im;
    }

    @Override
    public double getRe() {
        return re;
    }

    @Override
    public int hashCode() {
        return 31 * (31 * 1 + Double.hashCode(re)) + Double.hashCode(im);
    }

    /**
     * Returns the inverse of this complex.
     *
     * @return
     */
    public Complex inv() {
        double scalar, zRe, zIm;
        if (Math.abs(re) >= Math.abs(im)) {
            scalar = 1.0 / (re + im * (im / re));

            zRe = scalar;
            zIm = scalar * (-im / re);
        } else {
            scalar = 1.0 / (re * (re / im) + im);

            zRe = scalar * (re / im);
            zIm = -scalar;
        }
        return Complex.cart(zRe, zIm);
    }

    /**
     *
     * @param b
     * @return
     */
    public Complex minus(final Complex b) {
        return Complex.cart(re - b.re, im - b.im);
    }

    /**
     *
     * @param z
     * @return
     */
    public Complex minus(final double z) {
        if (z == 0.0) {
            return this;
        }
        return Complex.cart(re - z, im);
    }

    /**
     *
     * @return
     */
    public Complex negate() {
        return Complex.cart(-re, -im);
    }

    /**
     *
     * @param b
     * @return
     */
    public Complex plus(final Complex b) {
        return Complex.cart(re + b.re, im + b.im);
    }

    /**
     *
     * @param a
     * @return
     */
    public Complex plus(final double a) {
        if (a == 0.0) {
            return this;
        }
        return Complex.cart(re + a, im);
    }

    /**
     *
     * @param b
     * @return
     */
    public Complex times(final Complex b) {
        return Complex.cart((re * b.re) - (im * b.im), (re * b.im)
                + (im * b.re));
    }

    /**
     *
     * @param z
     * @return
     */
    public Complex times(final double z) {
        if (z == 1.0) {
            return this;
        }
        if (z == 0.0) {
            return Complex.ZERO;
        }
        return Complex.cart(re * z, im * z);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("(");
        result.append(re);

        if (im < 0.0) {
            result.append(" - ").append(-im);
        } else if (im == 0.0) {
            result.append(" - ").append(0.0);
        } else {
            result.append(" + ").append(+im);
        }

        result.append("i)");
        return result.toString();
    }
}
