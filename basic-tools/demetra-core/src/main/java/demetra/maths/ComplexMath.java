package demetra.maths;

import demetra.design.Development;
import demetra.utilities.functions.BiDoubleFunction;

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
/**
 * Mathematical functions on complex numbers
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public final class ComplexMath {

    /**
     *
     * @param c
     * @return
     */
    public static Complex acos(ComplexParts c) {
        return acos(c.getRe(), c.getIm(), Complex::cart);
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex acosec(ComplexParts c) {
        // acosec(c) = asin(1/c)
        return asin(ComplexBuilder.of(c).inv());
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex acosh(ComplexParts c) {
        return acosh(c.getRe(), c.getIm(), Complex::cart);
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex acot(ComplexParts c) {
        return acot(c.getRe(), c.getIm(), Complex::cart);
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex acoth(ComplexParts c) {
        return acoth(c.getRe(), c.getIm(), Complex::cart);
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex asec(ComplexParts c) {
        // asec(c) = -i * log(1/c + i*sqrt(1 - 1/c*c))
        // asec(c) = acos(1/c)
        return acos(ComplexBuilder.of(c).inv());
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex asin(ComplexParts c) {
        return asin(c.getRe(), c.getIm(), Complex::cart);
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex asinh(ComplexParts c) {
        return asinh(c.getRe(), c.getIm(), Complex::cart);
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex atan(ComplexParts c) {
        return atan(c.getRe(), c.getIm(), Complex::cart);
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex atanh(ComplexParts c) {
        return atanh(c.getRe(), c.getIm(), Complex::cart);
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex cos(ComplexParts c) {
        return cos(c.getRe(), c.getIm(), Complex::cart);
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex cosec(ComplexParts c) {
        // cosec(c) = 1 / sin(c)
        return ComplexBuilder.of(sin(c)).inv().build();
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex cosh(ComplexParts c) {
        return cosh(c.getRe(), c.getIm(), Complex::cart);
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex cot(ComplexParts c) {
        return cot(c.getRe(), c.getIm(), Complex::cart);
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex coth(ComplexParts c) {
        return coth(c.getRe(), c.getIm(), Complex::cart);
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex exp(ComplexParts c) {
        return exp(c.getRe(), c.getIm(), Complex::cart);
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex log(ComplexParts c) {
        return log(c.getRe(), c.getIm(), Complex::cart);
    }

    /**
     *
     * @param c
     * @param exponent
     * @return
     */
    public static Complex pow(ComplexParts c, ComplexParts exponent) {
        return pow(c.getRe(), c.getIm(), exponent.getRe(), exponent.getIm(), Complex::cart);
    }

    /**
     *
     * @param c
     * @param exponent
     * @return
     */
    public static Complex pow(ComplexParts c, double exponent) {
        return pow(c.getRe(), c.getIm(), exponent, Complex::cart);
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex sec(ComplexParts c) {
        // sec(c) = 1 / cos(c)
        return ComplexBuilder.of(cos(c)).inv().build();
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex sin(ComplexParts c) {
        return sin(c.getRe(), c.getIm(), Complex::cart);
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex sinh(ComplexParts c) {
        return sinh(c.getRe(), c.getIm(), Complex::cart);
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex tan(ComplexParts c) {
        return tan(c.getRe(), c.getIm(), Complex::cart);
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex tanh(ComplexParts c) {
        return tanh(c.getRe(), c.getIm(), Complex::cart);
    }

    /**
     * abs(z) = sqrt(re*re + im*im)
     *
     * @param re Real part
     * @param im Imaginary part
     * @return Absolute value of re + i * im
     */
    static double abs(double re, double im) {
        return Utilities.hypotenuse(re, im);
    }

    static double absSquare(double re, double im) {
        return re * re + im * im;
    }

    static double arg(double re, double im) {
        return Math.atan2(im, re);
    }

    static <R> R log(double cr, double ci, BiDoubleFunction<R> factory) {
        return factory.apply(Math.log(abs(cr, ci)), arg(cr, ci));
    }

    static <R> R div(double lRe, double lIm, double rRe, double rIm, BiDoubleFunction<R> factory) {
        double dRe, dIm;
        if (Math.abs(rRe) >= Math.abs(rIm)) {
            double scalar = 1.0 / (rRe + rIm * (rIm / rRe));
            dRe = scalar * (lRe + lIm * (rIm / rRe));
            dIm = scalar * (lIm - lRe * (rIm / rRe));
        } else {
            double scalar = 1.0 / (rRe * (rRe / rIm) + rIm);
            dRe = scalar * (lRe * (rRe / rIm) + lIm);
            dIm = scalar * (lIm * (rRe / rIm) - lRe);
        }
        return factory.apply(dRe, dIm);
    }

    static <R> R inv(double re, double im, BiDoubleFunction<R> factory) {
        double zRe, zIm;
        if (Math.abs(re) >= Math.abs(im)) {
            double scalar = 1.0 / (re + im * (im / re));
            zRe = scalar;
            zIm = scalar * (-im / re);
        } else {
            double scalar = 1.0 / (re * (re / im) + im);
            zRe = scalar * (re / im);
            zIm = -scalar;
        }
        return factory.apply(zRe, zIm);
    }

    static <R> R sqrt(double xRe, double xIm, BiDoubleFunction<R> factory) {
        double sRe, sIm;
        {
            final double scale = abs(xRe, xIm);
            if (scale > 0.0) {
                if (xRe > 0.0) {
                    sRe = Math.sqrt(0.5 * (scale + xRe));
                    sIm = 0.5 * xIm / sRe;
                } else {
                    double tmp = Math.sqrt(0.5 * (scale - xRe));
                    if (xIm < 0.0) {
                        tmp = -tmp;
                    }
                    sRe = 0.5 * xIm / tmp;
                    sIm = tmp;
                }
            } else {
                sRe = 0;
                sIm = 0;
            }
        }
        return factory.apply(sRe, sIm);
    }

    // acos(c) = -i * log(c + i*sqrt(1 - c*c))
    static <R> R acos(double cr, double ci, BiDoubleFunction<R> factory) {
        // x = 1 - c*c
        double xRe = 1 - (cr * cr - ci * ci);
        double xIm = -2 * cr * ci;

        // s = sqrt(x)
        double sRe, sIm;
        {
            final double scale = abs(xRe, xIm);
            if (scale > 0.0) {
                if (xRe > 0.0) {
                    sRe = Math.sqrt(0.5 * (scale + xRe));
                    sIm = 0.5 * xIm / sRe;
                } else {
                    double tmp = Math.sqrt(0.5 * (scale - xRe));
                    if (xIm < 0.0) {
                        tmp = -tmp;
                    }
                    sRe = 0.5 * xIm / tmp;
                    sIm = tmp;
                }
            } else {
                sRe = 0;
                sIm = 0;
            }
        }

        // z = c + i * s
        double zRe = cr - sIm;
        double zIm = ci + sRe;

        // -i * log(z)
        return factory.apply(arg(zRe, zIm), -Math.log(abs(zRe, zIm)));
    }

    // acosh(c) = log(c + sqrt(c*c - 1))
    static <R> R acosh(double cr, double ci, BiDoubleFunction<R> factory) {
        // x = c*c - 1
        double xRe = cr * cr - ci * ci - 1;
        double xIm = 2 * cr * ci;

        // s = sqrt(x)
        double sRe, sIm;
        {
            final double scale = abs(xRe, xIm);
            if (scale > 0.0) {
                if (xRe > 0.0) {
                    sRe = Math.sqrt(0.5 * (scale + xRe));
                    sIm = 0.5 * xIm / sRe;
                } else {
                    double tmp = Math.sqrt(0.5 * (scale - xRe));
                    if (xIm < 0.0) {
                        tmp = -tmp;
                    }
                    sRe = 0.5 * xIm / tmp;
                    sIm = tmp;
                }
            } else {
                sRe = 0;
                sIm = 0;
            }
        }

        // log (c + s)
        return log(cr + sRe, ci + sIm, factory);
    }

    // acot(c) = -i/2 * log( (ic-1)/(ic+1) )
    static <R> R acot(double cr, double ci, BiDoubleFunction<R> factory) {
        // l = (ic-1)
        double lRe = -ci - 1, lIm = cr;
        // r = (ic+1)
        double rRe = -ci + 1, rIm = cr;

        // d = l / r
        double dRe, dIm;
        if (Math.abs(rRe) >= Math.abs(rIm)) {
            double scalar = 1.0 / (rRe + rIm * (rIm / rRe));
            dRe = scalar * (lRe + lIm * (rIm / rRe));
            dIm = scalar * (lIm - lRe * (rIm / rRe));
        } else {
            double scalar = 1.0 / (rRe * (rRe / rIm) + rIm);
            dRe = scalar * (lRe * (rRe / rIm) + lIm);
            dIm = scalar * (lIm * (rRe / rIm) - lRe);
        }

        // -i/2 * log(d)
        return factory.apply(0.5 * arg(dRe, dIm), -0.5 * Math.log(abs(dRe, dIm)));
    }

    // atanh(z) = 1/2 * log( (c+1)/(c-1) )
    static <R> R acoth(double cr, double ci, BiDoubleFunction<R> factory) {
        // l = (c+1)
        double lRe = cr + 1, lIm = ci;
        // r = (c-1)
        double rRe = cr - 1, rIm = ci;

        // d = l / r
        double dRe, dIm;
        if (Math.abs(rRe) >= Math.abs(rIm)) {
            double scalar = 1.0 / (rRe + rIm * (rIm / rRe));
            dRe = scalar * (lRe + lIm * (rIm / rRe));
            dIm = scalar * (lIm - lRe * (rIm / rRe));
        } else {
            double scalar = 1.0 / (rRe * (rRe / rIm) + rIm);
            dRe = scalar * (lRe * (rRe / rIm) + lIm);
            dIm = scalar * (lIm * (rRe / rIm) - lRe);
        }

        // 1/2 * log(d)
        return factory.apply(0.5 * Math.log(abs(dRe, dIm)), 0.5 * arg(dRe, dIm));
    }

    // asin(c) = -i * log(i*c + sqrt(1 - c*c))
    static <R> R asin(double cr, double ci, BiDoubleFunction<R> factory) {
        // x = 1 - c*c
        double xRe = 1 - (cr * cr - ci * ci);
        double xIm = -2 * cr * ci;

        // s = sqrt(x)
        double sRe, sIm;
        {
            final double scale = abs(xRe, xIm);
            if (scale > 0.0) {
                if (xRe > 0.0) {
                    sRe = Math.sqrt(0.5 * (scale + xRe));
                    sIm = 0.5 * xIm / sRe;
                } else {
                    double tmp = Math.sqrt(0.5 * (scale - xRe));
                    if (xIm < 0.0) {
                        tmp = -tmp;
                    }
                    sRe = 0.5 * xIm / tmp;
                    sIm = tmp;
                }
            } else {
                sRe = 0;
                sIm = 0;
            }
        }

        // z = i*c + s
        double zRe = sRe - ci;
        double zIm = sIm + cr;

        // -i * log(z)
        return factory.apply(arg(zRe, zIm), -Math.log(abs(zRe, zIm)));
    }

    // asinh(c) = log(c + sqrt(c*c + 1))
    static <R> R asinh(double cr, double ci, BiDoubleFunction<R> factory) {
        // x = c*c + 1
        double xRe = cr * cr - ci * ci + 1;
        double xIm = 2 * cr * ci;

        // s = sqrt(x)
        double sRe, sIm;
        {
            final double scale = abs(xRe, xIm);
            if (scale > 0.0) {
                if (xRe > 0.0) {
                    sRe = Math.sqrt(0.5 * (scale + xRe));
                    sIm = 0.5 * xIm / sRe;
                } else {
                    double tmp = Math.sqrt(0.5 * (scale - xRe));
                    if (xIm < 0.0) {
                        tmp = -tmp;
                    }
                    sRe = 0.5 * xIm / tmp;
                    sIm = tmp;
                }
            } else {
                sRe = 0;
                sIm = 0;
            }
        }

        // log(c + s)
        return log(cr + sRe, ci + sIm, factory);
    }

    // atan(c) = -i/2 * log( (i-c)/(i+c) )
    static <R> R atan(double cr, double ci, BiDoubleFunction<R> factory) {
        // l = (i-c)
        double lRe = -cr, lIm = 1 - ci;
        // r = (i+c)
        double rRe = +cr, rIm = 1 + ci;

        // d = l / r
        double dRe, dIm;
        if (Math.abs(rRe) >= Math.abs(rIm)) {
            double scalar = 1.0 / (rRe + rIm * (rIm / rRe));
            dRe = scalar * (lRe + lIm * (rIm / rRe));
            dIm = scalar * (lIm - lRe * (rIm / rRe));
        } else {
            double scalar = 1.0 / (rRe * (rRe / rIm) + rIm);
            dRe = scalar * (lRe * (rRe / rIm) + lIm);
            dIm = scalar * (lIm * (rRe / rIm) - lRe);
        }

        // -i/2 * log(d)
        return factory.apply(0.5 * arg(dRe, dIm), -0.5 * Math.log(abs(dRe, dIm)));
    }

    // atanh(z) = 1/2 * log( (1+c)/(1-c) )
    static <R> R atanh(double cr, double ci, BiDoubleFunction<R> factory) {
        // l = (1+c)
        double lRe = 1 + cr, lIm = +ci;
        // r = (1-c)
        double rRe = 1 - cr, rIm = -ci;

        // d = l / r
        double dRe, dIm;
        if (Math.abs(rRe) >= Math.abs(rIm)) {
            double scalar = 1.0 / (rRe + rIm * (rIm / rRe));
            dRe = scalar * (lRe + lIm * (rIm / rRe));
            dIm = scalar * (lIm - lRe * (rIm / rRe));
        } else {
            double scalar = 1.0 / (rRe * (rRe / rIm) + rIm);
            dRe = scalar * (lRe * (rRe / rIm) + lIm);
            dIm = scalar * (lIm * (rRe / rIm) - lRe);
        }

        // 1/2 * log(d)
        return factory.apply(0.5 * Math.log(abs(dRe, dIm)), 0.5 * arg(dRe, dIm));
    }

    // cos(c) = ( e(ic)+e(-ic)) / 2
    static <R> R cos(double cr, double ci, BiDoubleFunction<R> factory) {
        double ric = -ci;
        double iic = cr;

        // e(ic) ...
        double scalar = Math.exp(ric);
        double ciic = Math.cos(iic);
        double siic = Math.sin(iic);

        double re1 = scalar * ciic;
        double im1 = scalar * siic;

        // e(-ic)
        scalar = Math.exp(-ric);
        double re2 = scalar * ciic;
        double im2 = scalar * (-siic);

        // result:
        return factory.apply(0.5 * (re1 + re2), 0.5 * (im1 + im2));
    }

    // cosh(c) = ( exp(c) + exp(-c) ) / 2
    static <R> R cosh(double cr, double ci, BiDoubleFunction<R> factory) {
        // e(c)
        // e(c) ...
        double scalar = Math.exp(cr);
        double cic = Math.cos(ci);
        double sic = Math.sin(ci);

        double re1 = scalar * cic;
        double im1 = scalar * sic;

        // e(-c)
        scalar = Math.exp(-cr);
        double re2 = scalar * cic;
        double im2 = scalar * (-sic);

        return factory.apply(0.5 * (re1 + re2), 0.5 * (im1 + im2));
    }

    static <R> R cot(double cr, double ci, BiDoubleFunction<R> factory) {
        // cot(c) = cos(c) / sin(c)
        // cos(c) = ( e(ic)+e(-ic)) / 2
        // sin(c) = ( e(ic)-e(-ic)) / (2*i)

        double ric = -ci;
        double iic = cr;

        // e(ic) ...
        double scalar = Math.exp(ric);
        double ciic = Math.cos(iic);
        double siic = Math.sin(iic);

        double re1 = scalar * ciic;
        double im1 = scalar * siic;

        // e(-ic)
        scalar = Math.exp(-ric);
        double re2 = scalar * ciic;
        double im2 = scalar * (-siic);

        return div(
                0.5 * (re1 + re2), 0.5 * (im1 + im2),
                0.5 * (im1 - im2), -0.5 * (re1 - re2),
                factory);
    }

    static <R> R coth(double cr, double ci, BiDoubleFunction<R> factory) {
        // cosh(c) = ( exp(c) + exp(-c) ) / 2
        // sinh(c) = ( exp(c) - exp(-c) ) / 2
        // coth(c) = cosh(c) / sinh(c)
        // coth(c) = ( exp(c) + exp(-c) ) / ( exp(c) - exp(-c) )

        // e(c)
        double scalar = Math.exp(cr);
        double cic = Math.cos(ci);
        double sic = Math.sin(ci);

        double re1 = scalar * cic;
        double im1 = scalar * sic;

        // e(-c)
        scalar = Math.exp(-cr);
        double re2 = scalar * cic;
        double im2 = scalar * (-sic);

        return div(
                re1 + re2, im1 + im2,
                re1 - re2, im1 - im2,
                factory);
    }

    static <R> R exp(double cr, double ci, BiDoubleFunction<R> factory) {
        // e(a+ib)=e(a)*e(ib)
        double scalar = Math.exp(cr);
        // e(ib) = cos(b) + i sin(b)
        return factory.apply(scalar * Math.cos(ci), scalar * Math.sin(ci));
    }

    static <R> R pow(double cr, double ci, double er, double ei, BiDoubleFunction<R> factory) {
        double re = Math.log(abs(cr, ci));
        double im = arg(cr, ci);

        double rtmp = re * er - im * ei;
        double itmp = re * ei + im * er;

        double scalar = Math.exp(rtmp);

        return factory.apply(scalar * Math.cos(itmp), scalar * Math.sin(itmp));
    }

    static <R> R pow(double cr, double ci, double exponent, BiDoubleFunction<R> factory) {
        double re = exponent * Math.log(abs(cr, ci));
        double im = exponent * arg(cr, ci);

        double scalar = Math.exp(re);

        return factory.apply(scalar * Math.cos(im), scalar * Math.sin(im));
    }

    // sin(c) = ( e(ic)-e(-ic)) / (2*i)
    static <R> R sin(double cr, double ci, BiDoubleFunction<R> factory) {
        double ric = -ci;
        double iic = cr;

        // e(ic) ...
        double scalar = Math.exp(ric);
        double ciic = Math.cos(iic);
        double siic = Math.sin(iic);

        double re1 = scalar * ciic;
        double im1 = scalar * siic;

        // e(-ic)
        scalar = Math.exp(-ric);
        double re2 = scalar * ciic;
        double im2 = scalar * (-siic);

        return factory.apply(0.5 * (im1 - im2), -0.5 * (re1 - re2));
    }

    // sinh(c) = ( exp(c) - exp(-c) ) / 2
    static <R> R sinh(double cr, double ci, BiDoubleFunction<R> factory) {
        // e(c)
        // e(c) ...
        double scalar = Math.exp(cr);
        double cic = Math.cos(ci);
        double sic = Math.sin(ci);

        double re1 = scalar * cic;
        double im1 = scalar * sic;

        // e(-c)
        scalar = Math.exp(-cr);
        double re2 = scalar * cic;
        double im2 = scalar * (-sic);

        return factory.apply(0.5 * (re1 - re2), 0.5 * (im1 - im2));
    }

    static <R> R tan(double cr, double ci, BiDoubleFunction<R> factory) {
        // tan(c) = sin(c) / cos(c)
        // cos(c) = ( e(ic)+e(-ic)) / 2
        // sin(c) = ( e(ic)-e(-ic)) / (2*i)

        double ric = -ci;
        double iic = cr;

        // e(ic) ...
        double scalar = Math.exp(ric);
        double ciic = Math.cos(iic);
        double siic = Math.sin(iic);

        double re1 = scalar * ciic;
        double im1 = scalar * siic;

        // e(-ic)
        scalar = Math.exp(-ric);
        double re2 = scalar * ciic;
        double im2 = scalar * (-siic);

        return div(
                0.5 * (im1 - im2), -0.5 * (re1 - re2),
                0.5 * (re1 + re2), 0.5 * (im1 + im2),
                factory);
    }

    static <R> R tanh(double cr, double ci, BiDoubleFunction<R> factory) {
        // cosh(c) = ( exp(c) + exp(-c) ) / 2
        // sinh(c) = ( exp(c) - exp(-c) ) / 2
        // tanh(c) = sinh(c) / cosh(c)
        // tanh(c) = ( exp(c) - exp(-c) ) / ( exp(c) + exp(-c) )

        // e(c)
        double scalar = Math.exp(cr);
        double cic = Math.cos(ci);
        double sic = Math.sin(ci);

        double re1 = scalar * cic;
        double im1 = scalar * sic;

        // e(-c)
        scalar = Math.exp(-cr);
        double re2 = scalar * cic;
        double im2 = scalar * (-sic);

        return div(
                re1 - re2, im1 - im2,
                re1 + re2, im1 + im2,
                factory);
    }

    private ComplexMath() {
        // static class
    }
}
