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
package jdplus.math;

import demetra.math.Complex;
import demetra.math.ComplexType;
import nbbrd.design.Development;

/**
 * Encapsulation of some operators on complex numbers. The use of this class is
 * recommended when several operations on complex numbers must be done. It
 * avoids the creation of intermediary objects and it is thus faster.
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public final class ComplexComputer  implements ComplexType{

    /**
     * Real part
     */
    private double re;
    /**
     * Imaginary part
     */
    private double im;
    
    /**
     * Creates a new object from a complex number
     *
     * @param c
     */
    public ComplexComputer(final ComplexType c) {
        re = c.getRe();
        im = c.getIm();
    }

    /**
     * Creates a new object from a real number
     *
     * @param re
     */
    public ComplexComputer(final double re) {
        this.re = re;
        im = 0;
    }

    /**
     * Creates a new object with a real and an imaginary number
     *
     * @param re Real part
     * @param im Imaginary part
     */
    public ComplexComputer(final double re, final double im) {
        this.re = re;
        this.im = im;
    }
    
    @Override
    public double getRe(){
        return re;
    }

    @Override
    public double getIm(){
        return im;
    }
    /**
     * Adds a complex number to this object
     *
     * @param c A complex number
     * @return This object is returned
     */
    public ComplexComputer add(final ComplexType c) {
        re += c.getRe();
        im += c.getIm();
        return this;
    }

    /**
     * Adds a multiple of a complex number to this object
     *
     * @param a The multiplier
     * @param c A complex number
     * @return This object is returned
     */
    public ComplexComputer addAC(final double a, final ComplexType c) {
        re += a * c.getRe();
        im += a * c.getIm();
        return this;
    }

    /**
     * Adds a real number to this object
     *
     * @param a A real number
     * @return This object is returned
     */
    public ComplexComputer add(final double a) {
        re += a;
        return this;
    }

    /**
     * Changes the sign of this object
     *
     * @return This object is returned
     */
    public ComplexComputer chs() {
        re = -re;
        im = -im;
        return this;
    }

    /**
     * Divide this object by a complex number
     *
     * @param c The right operand
     * @return This object is returned
     */
    public ComplexComputer div(final ComplexType c) {
        return div(c.getRe(), c.getIm());
    }

    /**
     * Divide this object by a real number
     *
     * @param r The right operand
     * @return This object is returned
     */
    public ComplexComputer div(final double r) {
        re /= r;
        im /= r;
        return this;
    }

    /**
     * Divides this object by a complex number (= x + i*y)
     *
     * @param x The real part
     * @param y The imaginary part
     * @return This object is returned
     */
    public ComplexComputer div(final double x, final double y) {
        double dRe, dIm;
        double scalar;

        if (Math.abs(x) >= Math.abs(y)) {
            scalar = 1.0 / (x + y * (y / x));

            dRe = scalar * (re + im * (y / x));
            dIm = scalar * (im - re * (y / x));

        } else {
            scalar = 1.0 / (x * (x / y) + y);

            dRe = scalar * (re * (x / y) + im);
            dIm = scalar * (im * (x / y) - re);
        }// endif
        re = dRe;
        im = dIm;
        return this;
    }

    /**
     * Inverts this object
     *
     * @return This object is returned
     */
    public ComplexComputer inv() {
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
        re = zRe;
        im = zIm;
        return this;
    }

    /**
     * Multiplies this object by a complex number
     *
     * @param c The right operand
     * @return This object is returned
     */
    public ComplexComputer mul(final ComplexType c) {
        return mul(c.getRe(), c.getIm());
    }

    /**
     * Multiplies this object by a real number
     *
     * @param r The right operand
     * @return This object is returned
     */
    public ComplexComputer mul(final double r) {
        if (r == 0) {
            re = 0;
            im = 0;
        } else {
            re *= r;
            im *= r;
        }
        return this;
    }

    /**
     * Divides this object by a complex number (= x + i*y)
     *
     * @param x The real part
     * @param y The imaginary part
     * @return This object is returned
     */
    public ComplexComputer mul(final double x, final double y) {
        final double tmp = re * x - im * y;
        im = re * y + im * x;
        re = tmp;
        return this;
    }

    /**
     * Subtracts a complex number to this object
     *
     * @param c The right operand
     * @return This object is returned
     */
    public ComplexComputer sub(final ComplexType c) {
        re -= c.getRe();
        im -= c.getIm();
        return this;
    }

    /**
     * Subtracts a real number to this object
     *
     * @param r The right operand
     * @return This object is returned
     */
    public ComplexComputer sub(final double r) {
        re -= r;
        return this;
    }

    public Complex result() {
        return Complex.cart(re, im);
    }
}
