package demetra.maths;

import demetra.design.Development;

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
     * abs(z) = sqrt(re*re + im*im)
     *
     * @param re Real part
     * @param im Imaginary part
     * @return Absolute value of re + i * im
     */
    static double abs(final double re, final double im) {
        return Utilities.hypotenuse(re, im);
//	if (re == 0 && im == 0)
//	    return 0;
//	final double absX = Math.abs(re);
//	final double absY = Math.abs(im);
//
//	if (absX >= absY) {
//	    final double d = im / re;
//	    return absX * Math.sqrt(1.0 + d * d);
//	} else {
//	    final double d = re / im;
//	    return absY * Math.sqrt(1.0 + d * d);
//	}
    }

    static double absSquare(final double x, final double y) {
        return x * x + y * y;
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex acos(ComplexParts c) {
        // acos(c) = -i * log(c + i*sqrt(1 - c*c))
        double cr = c.getRe(), ci = c.getIm();

        // 1-c*c
        double tmpr = 1 - (cr * cr - ci * ci);
        double tmpi = -2 * cr * ci;

        // sqrt(1-c*c)
        final double scale = abs(tmpr, tmpi);

        if (scale > 0.0) {
            if (tmpr > 0.0) {
                tmpr = Math.sqrt(0.5 * (scale + tmpr));
                tmpi = .5 * tmpi / tmpr;
            } else {
                double tmp = Math.sqrt(0.5 * (scale - tmpr));
                if (tmpi < 0.0) {
                    tmp = -tmp;
                }
                tmpr = 0.5 * tmpi / tmp;
                tmpi = tmp;
            }
        }

        // c + i*sqrt(1-c*c)
        double re = cr - tmpi;
        double im = ci + tmpr;

        // -i*log
        return Complex.cart(arg(re, im), -Math.log(abs(re, im)));
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex acosec(ComplexParts c) {
        // acosec(c) = asin(1/c)

        ComplexBuilder tmp = ComplexBuilder.of(c);
        tmp.inv();
        return asin(tmp.build());
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex acosh(ComplexParts c) {
        // acosh(c) = log(c + sqrt(c*c - 1))
        double cr = c.getRe(), ci = c.getIm();
        // c*c + 1
        double tmpr = cr * cr - ci * ci - 1;
        double tmpi = 2 * cr * ci;

        final double scale = abs(tmpr, tmpi);

        if (scale > 0.0) {
            if (tmpr > 0.0) {
                tmpr = Math.sqrt(0.5 * (scale + tmpr));
                tmpi = .5 * tmpi / tmpr;
            } else {
                double tmp = Math.sqrt(0.5 * (scale - tmpr));
                if (tmpi < 0.0) {
                    tmp = -tmp;
                }
                tmpr = 0.5 * tmpi / tmp;
                tmpi = tmp;
            }
        }

        tmpr += cr;
        tmpi += ci;
        return log(tmpr, tmpi);
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex acot(ComplexParts c) {
        // acot(c) = -i/2 * log( (ic-1)/(ic+1) )
        double cr = c.getRe(), ci = c.getIm();
        ComplexBuilder tmp = ComplexBuilder.of(-ci - 1, cr);
        tmp.div(1 - ci, cr);

        Complex ltmp = tmp.build();
        // -i/2*log
        double re = ltmp.getRe(), im = ltmp.getIm();
        return Complex.cart(0.5 * arg(re, im), -0.5 * Math.log(abs(re, im)));
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex acoth(ComplexParts c) {
        // atanh(z) = 1/2 * log( (c+1)/(c-1) )
        double cr = c.getRe(), ci = c.getIm();

        ComplexBuilder tmp = ComplexBuilder.of(cr + 1, ci);
        tmp.div(cr - 1, ci);

        double re = tmp.getRe(), im = tmp.getIm();
        return Complex.cart(0.5 * Math.log(abs(re, im)), 0.5 * arg(re, im)); // principal
        // value
    }

    static double arg(final double re, final double im) {
        return Math.atan2(im, re);
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex asec(ComplexParts c) {
        // asec(c) = -i * log(1/c + i*sqrt(1 - 1/c*c))
        // asec(c) = acos(1/c)

        ComplexBuilder tmp = ComplexBuilder.of(c);
        tmp.inv();
        return acos(tmp.build());
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex asin(ComplexParts c) {
        // asin(c) = -i * log(i*c + sqrt(1 - c*c))

        double cr = c.getRe(), ci = c.getIm();

        // 1-c*c
        double tmpr = 1 - (cr * cr - ci * ci);
        double tmpi = -2 * cr * ci;

        // sqrt(1-c*c)
        final double scale = abs(tmpr, tmpi);

        if (scale > 0.0) {
            if (tmpr > 0.0) {
                tmpr = Math.sqrt(0.5 * (scale + tmpr));
                tmpi = .5 * tmpi / tmpr;
            } else {
                double tmp = Math.sqrt(0.5 * (scale - tmpr));
                if (tmpi < 0.0) {
                    tmp = -tmp;
                }
                tmpr = 0.5 * tmpi / tmp;
                tmpi = tmp;
            }
        }

        // i*c + sqrt(1-c*c)
        tmpr -= ci;
        tmpi += cr;

        // -i*log
        return Complex.cart(arg(tmpr, tmpi), -Math.log(abs(tmpr, tmpi)));
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex asinh(ComplexParts c) {
        // asinh(c) = log(c + sqrt(c*c + 1))
        double cr = c.getRe(), ci = c.getIm();
        // c*c + 1
        double tmpr = cr * cr - ci * ci + 1;
        double tmpi = 2 * cr * ci;

        final double scale = abs(tmpr, tmpi);

        if (scale > 0.0) {
            if (tmpr > 0.0) {
                tmpr = Math.sqrt(0.5 * (scale + tmpr));
                tmpi = .5 * tmpi / tmpr;
            } else {
                double tmp = Math.sqrt(0.5 * (scale - tmpr));
                if (tmpi < 0.0) {
                    tmp = -tmp;
                }
                tmpr = 0.5 * tmpi / tmp;
                tmpi = tmp;
            }
        }

        tmpr += cr;
        tmpi += ci;
        return log(tmpr, tmpi);
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex atan(ComplexParts c) {
        // atan(c) = -i/2 * log( (i-c)/(i+c) )
        double cr = c.getRe(), ci = c.getIm();
        ComplexBuilder tmp = ComplexBuilder.of(-cr, 1 - ci);
        tmp.div(cr, 1 + ci);

        Complex ltmp = tmp.build();
        // -i*log
        double re = ltmp.getRe(), im = ltmp.getIm();
        return Complex.cart(0.5 * arg(re, im), -0.5 * Math.log(abs(re, im)));
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex atanh(ComplexParts c) {
        // atanh(z) = 1/2 * log( (1+c)/(1-c) )
        double cr = c.getRe(), ci = c.getIm();

        ComplexBuilder tmp = ComplexBuilder.of(cr + 1, ci);
        tmp.div(1 - cr, -ci);

        double re = tmp.getRe(), im = tmp.getIm();
        return Complex.cart(0.5 * Math.log(abs(re, im)), 0.5 * arg(re, im)); // principal
        // value
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex cos(ComplexParts c) {
        // cos(c) = ( e(ic)+e(-ic)) / 2

        double ric = -c.getIm();
        double iic = c.getRe();

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
        return Complex.cart(0.5 * (re1 + re2), 0.5 * (im1 + im2));
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex cosec(ComplexParts c) {
        // cosec(c) = 1 / sin(c)
        ComplexBuilder builder = ComplexBuilder.of(sin(c));
        builder.inv();
        return builder.build();
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex cosh(ComplexParts c) {
        // cosh(c) = ( exp(c) + exp(-c) ) / 2

        double rc = c.getRe(), ic = c.getIm();

        // e(c)
        // e(c) ...
        double scalar = Math.exp(rc);
        double cic = Math.cos(ic);
        double sic = Math.sin(ic);

        double re1 = scalar * cic;
        double im1 = scalar * sic;

        // e(-c)
        scalar = Math.exp(-rc);
        double re2 = scalar * cic;
        double im2 = scalar * (-sic);

        return Complex.cart(0.5 * (re1 + re2), 0.5 * (im1 + im2));
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex cot(ComplexParts c) {
        // cot(c) = cos(c) / sin(c)
        // cos(c) = ( e(ic)+e(-ic)) / 2
        // sin(c) = ( e(ic)-e(-ic)) / (2*i)

        double ric = -c.getIm();
        double iic = c.getRe();

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

        // 
        ComplexBuilder result = ComplexBuilder.of(0.5 * (re1 + re2),
                0.5 * (im1 + im2));
        result.div(0.5 * (im1 - im2), -0.5 * (re1 - re2));
        return result.build();
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex coth(ComplexParts c) {
        // cosh(c) = ( exp(c) + exp(-c) ) / 2
        // sinh(c) = ( exp(c) - exp(-c) ) / 2
        // coth(c) = cosh(c) / sinh(c)
        // coth(c) = ( exp(c) + exp(-c) ) / ( exp(c) - exp(-c) )

        double rc = c.getRe(), ic = c.getIm();

        // e(c)
        // e(c) ...
        double scalar = Math.exp(rc);
        double cic = Math.cos(ic);
        double sic = Math.sin(ic);

        double re1 = scalar * cic;
        double im1 = scalar * sic;

        // e(-c)
        scalar = Math.exp(-rc);
        double re2 = scalar * cic;
        double im2 = scalar * (-sic);

        ComplexBuilder result = ComplexBuilder.of(re1 + re2, im1 + im2);
        result.div(re1 - re2, im1 - im2);
        return result.build();
    }

    // exp, log, pow, sqrt
    /**
     *
     * @param c
     * @return
     */
    public static Complex exp(ComplexParts c) {
        return exp(c.getRe(), c.getIm());
    }

    private static Complex exp(final double a, final double b) {
        // e(a+ib)=e(a)*e(ib)
        double scalar = Math.exp(a);
        // e(ib) = cos(b) + i sin(b)
        return Complex.cart(scalar * Math.cos(b), scalar * Math.sin(b));
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex log(ComplexParts c) {
        return Complex.cart(Math.log(c.abs()), c.arg()); // principal value
    }

    static Complex log(final double re, final double im) {
        return Complex.cart(Math.log(abs(re, im)), arg(re, im)); // principal
        // value
    }

    /**
     *
     * @param c
     * @param exponent
     * @return
     */
    public static Complex pow(ComplexParts c, ComplexParts exponent) {
        double re = Math.log(c.abs());
        double im = c.arg();

        double rtmp = re * exponent.getRe() - im * exponent.getIm();
        double itmp = re * exponent.getIm() + im * exponent.getRe();

        double scalar = Math.exp(rtmp);

        return Complex.cart(scalar * Math.cos(itmp), scalar * Math.sin(itmp));
    }

    /**
     *
     * @param c
     * @param exponent
     * @return
     */
    public static Complex pow(ComplexParts c, final double exponent) {
        double re = exponent * Math.log(c.abs());
        double im = exponent * c.arg();

        double scalar = Math.exp(re);

        return Complex.cart(scalar * Math.cos(im), scalar * Math.sin(im));
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex sec(ComplexParts c) {
        // sec(c) = 1 / cos(c)
        ComplexBuilder builder = ComplexBuilder.of(cos(c));
        builder.inv();
        return builder.build();
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex sin(ComplexParts c) {
        // sin(c) = ( e(ic)-e(-ic)) / (2*i)

        double ric = -c.getIm();
        double iic = c.getRe();

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
        return Complex.cart(0.5 * (im1 - im2), -0.5 * (re1 - re2));
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex sinh(ComplexParts c) {
        // sinh(c) = ( exp(c) - exp(-c) ) / 2

        double rc = c.getRe(), ic = c.getIm();

        // e(c)
        // e(c) ...
        double scalar = Math.exp(rc);
        double cic = Math.cos(ic);
        double sic = Math.sin(ic);

        double re1 = scalar * cic;
        double im1 = scalar * sic;

        // e(-c)
        scalar = Math.exp(-rc);
        double re2 = scalar * cic;
        double im2 = scalar * (-sic);

        return Complex.cart(0.5 * (re1 - re2), 0.5 * (im1 - im2));
    }

    /**
     *
     * @param c
     * @return
     * @deprecated use {@link Complex#sqrt()} instead
     */
    @Deprecated
    public static Complex sqrt(ComplexParts c) {
        return sqrt(c.getRe(), c.getIm());
    }

    static Complex sqrt(final double re, final double im) {
        final double scale = abs(re, im);

        if (scale > 0.0) {
            if (re > 0.0) {
                double tmp = Math.sqrt(0.5 * (scale + re));
                return Complex.cart(tmp, 0.5 * im / tmp);
            } else {
                double tmp = Math.sqrt(0.5 * (scale - re));

                if (im < 0.0) {
                    tmp = -tmp;
                }

                return Complex.cart(0.5 * im / tmp, tmp);
            }
        } else {
            return Complex.ZERO;
        }
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex tan(ComplexParts c) {
        // tan(c) = sin(c) / cos(c)
        // cos(c) = ( e(ic)+e(-ic)) / 2
        // sin(c) = ( e(ic)-e(-ic)) / (2*i)

        double ric = -c.getIm();
        double iic = c.getRe();

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

        // 
        ComplexBuilder result = ComplexBuilder.of(0.5 * (im1 - im2), -0.5
                * (re1 - re2));
        result.div(0.5 * (re1 + re2), 0.5 * (im1 + im2));
        return result.build();
    }

    /**
     *
     * @param c
     * @return
     */
    public static Complex tanh(ComplexParts c) {
        // cosh(c) = ( exp(c) + exp(-c) ) / 2
        // sinh(c) = ( exp(c) - exp(-c) ) / 2
        // tanh(c) = sinh(c) / cosh(c)
        // tanh(c) = ( exp(c) - exp(-c) ) / ( exp(c) + exp(-c) )

        double rc = c.getRe(), ic = c.getIm();

        // e(c)
        // e(c) ...
        double scalar = Math.exp(rc);
        double cic = Math.cos(ic);
        double sic = Math.sin(ic);

        double re1 = scalar * cic;
        double im1 = scalar * sic;

        // e(-c)
        scalar = Math.exp(-rc);
        double re2 = scalar * cic;
        double im2 = scalar * (-sic);

        ComplexBuilder result = ComplexBuilder.of(re1 - re2, im1 - im2);
        result.div(re1 + re2, im1 + im2);
        return result.build();
    }

    private ComplexMath() {
        // static class
    }
}
