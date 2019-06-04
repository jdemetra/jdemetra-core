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
package jdplus.maths;

import demetra.design.Development;
import demetra.maths.Complex;
import demetra.maths.ComplexComputer;
import demetra.maths.ComplexType;
import static demetra.maths.ComplexType.abs;
import static demetra.maths.ComplexType.arg;

/**
 * Mathematical functions on complex numbers
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.experimental.UtilityClass
public final class ComplexMath {

    double absSquare(final double x, final double y) {
        return x * x + y * y;
    }

    /**
     *
     * @param c
     * @return
     */
    public Complex acos(final ComplexType c) {
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
    public Complex acosec(final ComplexType c) {
        // acosec(c) = asin(1/c)

        ComplexComputer tmp = new ComplexComputer(c);
        tmp.inv();
        return asin(tmp.result());
    }

    /**
     *
     * @param c
     * @return
     */
    public Complex acosh(final ComplexType c) {
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
    public Complex acot(final ComplexType c) {
        // acot(c) = -i/2 * log( (ic-1)/(ic+1) )
        double cr = c.getRe(), ci = c.getIm();
        ComplexComputer tmp = new ComplexComputer(-ci - 1, cr);
        tmp.div(1 - ci, cr);

        Complex ltmp = tmp.result();
        // -i/2*log
        double re = ltmp.getRe(), im = ltmp.getIm();
        return Complex.cart(0.5 * arg(re, im), -0.5 * Math.log(abs(re, im)));
    }

    /**
     *
     * @param c
     * @return
     */
    public Complex acoth(final ComplexType c) {
        // atanh(z) = 1/2 * log( (c+1)/(c-1) )
        double cr = c.getRe(), ci = c.getIm();

        ComplexComputer tmp = new ComplexComputer(cr + 1, ci);
        tmp.div(cr - 1, ci);

        return Complex.cart(0.5 * Math.log(tmp.abs()), 0.5 * tmp.arg()); // principal
        // value
    }

    /**
     *
     * @param c
     * @return
     */
    public Complex asec(final Complex c) {
        // asec(c) = -i * log(1/c + i*sqrt(1 - 1/c*c))
        // asec(c) = acos(1/c)

        ComplexComputer tmp = new ComplexComputer(c);
        tmp.inv();
        return acos(tmp.result());
    }

    /**
     *
     * @param c
     * @return
     */
    public Complex asin(final ComplexType c) {
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
    public Complex asinh(final ComplexType c) {
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
    public Complex atan(final ComplexType c) {
        // atan(c) = -i/2 * log( (i-c)/(i+c) )
        double cr = c.getRe(), ci = c.getIm();
        ComplexComputer tmp = new ComplexComputer(-cr, 1 - ci);
        tmp.div(cr, 1 + ci);

        Complex ltmp = tmp.result();
        // -i*log
        double re = ltmp.getRe(), im = ltmp.getIm();
        return Complex.cart(0.5 * arg(re, im), -0.5 * Math.log(abs(re, im)));
    }

    /**
     *
     * @param c
     * @return
     */
    public Complex atanh(final ComplexType c) {
        // atanh(z) = 1/2 * log( (1+c)/(1-c) )
        double cr = c.getRe(), ci = c.getIm();

        ComplexComputer tmp = new ComplexComputer(cr + 1, ci);
        tmp.div(1 - cr, -ci);

        return Complex.cart(0.5 * Math.log(tmp.abs()), 0.5 * tmp.arg()); // principal
        // value
    }

    /**
     *
     * @param c
     * @return
     */
    public Complex cos(final ComplexType c) {
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
    public Complex cosec(final ComplexType c) {
        // cosec(c) = 1 / sin(c)
        ComplexComputer builder = new ComplexComputer(sin(c));
        builder.inv();
        return builder.result();
    }

    /**
     *
     * @param c
     * @return
     */
    public Complex cosh(final ComplexType c) {
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
    public Complex cot(final ComplexType c) {
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
        ComplexComputer result = new ComplexComputer(0.5 * (re1 + re2),
                0.5 * (im1 + im2));
        result.div(0.5 * (im1 - im2), -0.5 * (re1 - re2));
        return result.result();
    }

    /**
     *
     * @param c
     * @return
     */
    public Complex coth(final ComplexType c) {
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

        ComplexComputer result = new ComplexComputer(re1 + re2, im1 + im2);
        result.div(re1 - re2, im1 - im2);
        return result.result();
    }

    // exp, log, pow, sqrt
    /**
     *
     * @param c
     * @return
     */
    public Complex exp(final ComplexType c) {
        return exp(c.getRe(), c.getIm());
    }

    Complex exp(final double a, final double b) {
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
    public Complex log(final ComplexType c) {
        return Complex.cart(Math.log(c.abs()), c.arg()); // principal value
    }

    Complex log(final double re, final double im) {
        return Complex.cart(Math.log(abs(re, im)), arg(re, im)); // principal
        // value
    }

    /**
     *
     * @param c
     * @param exponent
     * @return
     */
    public Complex pow(final ComplexType c, final ComplexType exponent) {
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
    public Complex pow(final ComplexType c, final double exponent) {
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
    public Complex sec(final ComplexType c) {
        // sec(c) = 1 / cos(c)
        ComplexComputer builder = new ComplexComputer(cos(c));
        builder.inv();
        return builder.result();
    }

    /**
     *
     * @param c
     * @return
     */
    public Complex sin(final ComplexType c) {
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
    public Complex sinh(final ComplexType c) {
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
     */
    public Complex sqrt(final ComplexType c) {
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
    public Complex tan(final ComplexType c) {
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
        ComplexComputer result = new ComplexComputer(0.5 * (im1 - im2), -0.5
                * (re1 - re2));
        result.div(0.5 * (re1 + re2), 0.5 * (im1 + im2));
        return result.result();
    }

    /**
     *
     * @param c
     * @return
     */
    public Complex tanh(final ComplexType c) {
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

        ComplexComputer result = new ComplexComputer(re1 - re2, im1 - im2);
        result.div(re1 + re2, im1 + im2);
        return result.result();
    }


}
