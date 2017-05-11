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
import demetra.design.IBuilder;

/**
 * Encapsulation of some operators on complex numbers. The use of this class is
 * recommended when several operations on complex numbers must be done. It
 * avoids the creation of intermediary objects and it is thus faster.
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public final class ComplexBuilder implements ComplexParts, IBuilder<Complex> {

    /**
     * Creates a new object from a complex number
     *
     * @param c
     * @return
     */
    public static ComplexBuilder of(ComplexParts c) {
        return new ComplexBuilder(c.getRe(), c.getIm());
    }

    /**
     * Creates a new object from a real number
     *
     * @param re
     * @return
     */
    public static ComplexBuilder cart(double re) {
        return new ComplexBuilder(re, 0);
    }

    /**
     * Creates a new object with a real and an imaginary number
     *
     * @param re Real part
     * @param im Imaginary part
     * @return
     */
    public static ComplexBuilder cart(double re, double im) {
        return new ComplexBuilder(re, im);
    }

    private double re;
    private double im;

    private ComplexBuilder(double re, double im) {
        this.re = re;
        this.im = im;
    }

    @Override
    public double getRe() {
        return re;
    }

    @Override
    public double getIm() {
        return im;
    }

    private ComplexBuilder set(double re, double im) {
        this.re = re;
        this.im = im;
        return this;
    }

    /**
     * Adds a complex number to this object
     *
     * @param c A complex number
     * @return This object is returned
     */
    public ComplexBuilder add(ComplexParts c) {
        re += c.getRe();
        im += c.getIm();
        return this;
    }

    /**
     * Adds a real number to this object
     *
     * @param a A real number
     * @return This object is returned
     */
    public ComplexBuilder add(final double a) {
        re += a;
        return this;
    }

    /**
     * Changes the sign of this object
     *
     * @return This object is returned
     */
    public ComplexBuilder chs() {
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
    public ComplexBuilder div(ComplexParts c) {
        return ComplexMath.div(re, im, c.getRe(), c.getIm(), this::set);
    }

    public ComplexBuilder div(double x, double y) {
        return ComplexMath.div(re, im, x, y, this::set);
    }

    /**
     * Divide this object by a real number
     *
     * @param r The right operand
     * @return This object is returned
     */
    public ComplexBuilder div(final double r) {
        re /= r;
        im /= r;
        return this;
    }

    /**
     * Inverts this object
     *
     * @return This object is returned
     */
    public ComplexBuilder inv() {
        return ComplexMath.inv(re, im, this::set);
    }

    /**
     * Multiplies this object by a complex number
     *
     * @param c The right operand
     * @return This object is returned
     */
    public ComplexBuilder mul(ComplexParts c) {
        return mul(c.getRe(), c.getIm());
    }

    /**
     * Multiplies this object by a real number
     *
     * @param r The right operand
     * @return This object is returned
     */
    public ComplexBuilder mul(final double r) {
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
    public ComplexBuilder mul(final double x, final double y) {
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
    public ComplexBuilder sub(ComplexParts c) {
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
    public ComplexBuilder sub(final double r) {
        re -= r;
        return this;
    }

    @Override
    public Complex build() {
        return Complex.cart(re, im);
    }
}
