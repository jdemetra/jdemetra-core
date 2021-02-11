/*
 * Copyright 2021 National Bank of Belgium.
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
package demetra.math;

import nbbrd.design.Development;

/**
 * Complex number
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.Value
public class Complex implements ComplexType{

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

        theta = theta % Constants.TWOPI;

        return cart(r * Math.cos(theta), r * Math.sin(theta));
    }
    
    /**
     * Real part
     */
    private double re;
    /**
     * Imaginary part
     */
    private double im;
    
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

    /**
     * Returns the conjugate of this complex number.
     *
     * @return
     */
    public Complex conj() {
        return Complex.cart(re, -im);
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
        return ComplexType.abs(re - z.re, im - z.im) <= tolerance;
        // ...and tolerance is always non-negative
    }

    /**
     * Returns true if either the real or imaginary component of this Complex is
     * an infinite value.
     *
     * @return
     */
    public boolean isInfinity() {
        return (Double.isInfinite(re) || Double.isInfinite(im));
    }

    /**
     * Returns true if either the real or imaginary component of this Complex is
     * a Not-a-Number (NaN) value.
     *
     * @return
     */
    public boolean isNaN() {
        return (Double.isNaN(re) || Double.isNaN(im));
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

   
    public double absSquare() {
        return re*re+im*im;
    }

    /**
     *
     * @param r
     * @return
     */
    public double distance(Complex r) {
        return ComplexType.abs(re - r.getRe(), im - r.getIm());
    }

    /**
     *
     * @param r
     * @return
     */
    public double squareDistance(Complex r) {
        double dr=re - r.getRe(), di=im - r.getIm();
        return dr*dr+di*di;
    }
    /**
     *
     * @param b
     * @return
     */
    public Complex minus(final Complex b) {
        return Complex.cart(re - b.getRe(), im - b.getIm());
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
     * a+b
     *
     * @param b
     * @return
     */
    public Complex plus(final Complex b) {
        return Complex.cart(re + b.getRe(), im + b.getIm());
    }

    /**
     * a+z
     *
     * @param z
     * @return
     */
    public Complex plus(final double z) {
        if (z == 0.0) {
            return this;
        }
        return Complex.cart(re + z, im);
    }

    /**
     *
     * @param b
     * @return
     */
    public Complex times(final Complex b) {
        return Complex.cart((re * b.getRe()) - (im * b.getIm()), (re * b.getIm())
                + (im * b.getRe()));
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

    /**
     *
     * @param b
     * @return
     */
    public Complex div(final Complex b) {
        double bRe = b.re, bIm = b.im;
        double dRe, dIm;
        double scalar;

        if (Math.abs(bRe) >= Math.abs(bIm)) {
            scalar = 1.0 / (bRe + bIm * (bIm / bRe));

            dRe = scalar * (re + im * (bIm / bRe));
            dIm = scalar * (im - re * (bIm / bRe));

        } else {
            scalar = 1.0 / (bRe * (bRe / bIm) + bIm);

            dRe = scalar * (re * (bRe / bIm) + im);
            dIm = scalar * (im * (bRe / bIm) - re);
        }// endif
        return cart(dRe, dIm);
    }

    /**
     *
     * @param z
     * @return
     */
    public Complex div(final double z) {
        if (z == 1.0) {
            return this;
        }
        return Complex.cart(re / z, im / z);
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

}
