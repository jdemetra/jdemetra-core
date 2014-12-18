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

package ec.tstoolkit.maths;

import ec.tstoolkit.design.Development;

/**
 * Dynamic initialization of mathematical constants.
 * Based on the lapack implementation (dlamch.f),
 * Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,
 * Courant Institute, Argonne National Lab, and Rice University
 * October 31, 1992
 *
 *  The routine is based on the routine  ENVRON  by Malcolm and
 *  incorporates suggestions by Gentleman and Marovich. See
 *
 *     Malcolm M. A. (1972) Algorithms to reveal properties of
 *        floating-point arithmetic. Comms. of the ACM, 15, 949-951.
 *
 *     Gentleman W. M. and Marovich S. B. (1974) More on algorithms
 *        that reveal properties of floating point arithmetic units.
 *        Comms. of the ACM, 17, 276-277.
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class Constants {

    private static double eps_, // relative machine precision
            sfmin_, // safe minimum, such that 1/sfmin does not overflow
            prec_, // eps*base
            emin_, // minimum exponent before (gradual) underflow
            rmin_, // underflow threshold - base**(emin-1)
            emax_, // largest exponent before overflow
            rmax_;  // overflow threshold  - (base**emax)*(1-eps)
    private static int base_, // base of the machine
            t_; //     = number of (base) digits in the mantissa
    private static boolean rnd_; // true when rounding occurs in addition, false otherwise
    private static boolean ieee_; //Specifies whether rounding appears to be done in the IEEE 'round to nearest' style.

    static {
        computeConstants();
    }

    private static double add(double a, double b) {
        return a + b;
    }

    /**
     * The minimum exponent before (gradual) underflow, computed by
     * setting A = START and dividing by BASE until the previous A
     * can not be recovered.
     * @param start The starting point for determining emin.
     * @return
     */
    private static int emin(double start) {
        double a = start;
        double rbase = 1.0 / base_;
        int e = 1;
        double b1 = add(a * rbase, 0), b2;
        double c1 = a, c2 = a, d1 = a, d2 = a;
        do {
            --e;
            a = b1;
            b1 = add(a / base_, 0);
            c1 = add(b1 * base_, 0);
            d1 = 0;
            for (int i = 0; i < base_; ++i) {
                d1 += b1;
            }
            b2 = add(a * rbase, 0);
            c2 = add(b2 / rbase, 0);
            d2 = 0;
            for (int i = 0; i < base_; ++i) {
                d2 += b2;
            }
        } while (c1 == a && c2 == a && d1 == a && d2 == a);
        return e;
    }

    /**
     * Computes beta_, the base of the machine.
     *
     * it_, The number of ( BETA ) digits in the mantissa.
     *
     * lrnd_, which specifies whether proper rounding  ( lrnd_ = true )  or
     * chopping  (lrnd_=false )  occurs in addition. This may not
     * be a reliable guide to the way in which the machine performs
     * its arithmetic.
     *
     * eps_, the smallest positive number such that
     *
     * fl( 1.0 - EPS ) .LT. 1.0,
     * where fl denotes the computed value.
     *
     * emin_, the minimum exponent before (gradual) underflow occurs.
     *
     * rmin_, the smallest normalized number for the machine, given by
     * BASE**( EMIN - 1 ), where  BASE  is the floating point value
     * of BETA.
     *
     * emax_, the maximum exponent before overflow occurs.
     *
     * rmax_, the largest positive number for the machine, given by
     * BASE**EMAX * ( 1 - EPS ), where  BASE  is the floating point
     * value of BETA.
     *
     * The computation of  EPS  is based on a routine PARANOIA by
     * W. Kahan of the University of California at Berkeley.
     */
    public static synchronized void computeConstants() {
        calcBase();
        calcEps();
        calcEmin();
        calcEmax();

        double eps = 1;
        double rbase = 1.0 / base_;
        for (int i = 1; i < t_; ++i) {
            eps *= rbase;
        }
        if (rnd_) {
            eps_ = eps / 2;
        } else {
            eps_ = eps;
        }

        prec_ = eps_ * base_;
        sfmin_ = rmin_;

        double small = 1 / rmax_;
        if (small >= sfmin_) //     Use SMALL plus a bit, to avoid the possibility of rounding
        //     causing overflow when computing  1/sfmin.
        {
            sfmin_ = small * (1 + eps_);
        }
    }

    /**
     * Computes base_, t_, rnd_, ieee1.
     */
    private static void calcBase() {
        //  Compute  a = 2.0**m  with the  smallest positive integer m such
        //  that fl( a + 1.0 ) = a.
        double a = 1, c = 1;
        while (c == 1) {
            a *= 2;
            c = add(a, 1);
            c = add(c, -a);
        }

//       Now compute  b = 2.0**m  with the smallest positive integer m
//       such that fl( a + b ) .gt. a.
        double b = 1;
        c = add(a, b);
        while (c == a) {
            b *= 2;
            c = add(a, b);
        }


//        Now compute the base.  a and c  are neighbouring floating point
//        numbers  in the  interval  ( beta**t, beta**( t + 1 ) )  and so
//        their difference is beta. Adding 0.25 to c is to ensure that it
//        is truncated to beta and not ( beta - 1 ).
        double qtr = 1.0 / 4;
        double savec = c;
        c = add(c, -a);
        base_ = (int) (b);
        b = base_;

//        Now determine whether rounding or chopping occurs,  by adding a
//        bit  less  than  beta/2  and a  bit  more  than  beta/2  to  a.

        double f = add(b / 2, -b / 100);
        c = add(f, a);
        rnd_ = c == a;
        f = add(b / 2, -b / 100);
        c = add(f, a);
        rnd_ = rnd_ && c == a;

//        Try and decide whether rounding is done in the  IEEE  'round to
//        nearest' style. B/2 is half a unit in the last place of the two
//        numbers A and SAVEC. Furthermore, A is even, i.e. has last  bit
//        zero, and SAVEC is odd. Thus adding B/2 to A should not  change
//        A, but adding B/2 to SAVEC should change SAVEC.

        double t1 = add(b / 2, a);
        double t2 = add(b / 2, savec);
        ieee_ = (t1 == a) && (t2 > savec) && rnd_;

//        Now find  the  mantissa, t.  It should  be the  integer part of
//        log to the base beta of a,  however it is safer to determine  t
//        by powering.  So we find t as the smallest positive integer for
//        which fl( beta**t + 1.0 ) = 1.0.
        t_ = 0;
        a = 1;
        c = 1;
        while (c == 1) {
            ++t_;
            a *= b;
            c = add(a, 1);
            c = add(c, -a);
        }
    }

    private static void calcEps() {
        double a = 1;
        double beta = base_;
        for (int i = 0; i < t_; ++i) {
            a /= beta;
        }
        eps_ = a;

//        Try some tricks to see whether or not this is the correct  EPS.
        double b = 2.0 / 3;
        double half = 1.0 / 2;
        double sixth = add(b, -half);
        double third = add(sixth, sixth);
        b = add(third, -half);
        b = add(b, sixth);
        if (b < 0) {
            b = -b;
        }
        if (b < eps_) {
            b = eps_;
        }
        eps_ = 1;
        double two5 = 2.0 * 2 * 2 * 2 * 2;
        while (eps_ > b && b > 0) {
            eps_ = b;
            double c = add(half * eps_, two5 * (eps_ * eps_));
            c = add(half, -c);
            b = add(half, c);
            c = add(half, -b);
            b = add(half, c);
        }

        if (a < eps_) {
            eps_ = a;
        }
    }

    private static void calcEmin() {
//        Now find  EMIN.  Let A = + or - 1, and + or - (1 + BASE**(-3)).
//        Keep dividing  A by BETA until (gradual) underflow occurs. This
//        is detected when we cannot recover the previous A.

        double rbase = 1.0 / base_;
        double small = 1;
        for (int i = 0; i < 3; ++i) {
            small = add(small * rbase, 0);
        }
        double a = add(1, small);
        int ngpmin = emin(1);
        int ngnmin = emin(-1);
        int gpmin = emin(a);
        int gnmin = emin(-a);

        boolean ieee = false;
        if (ngpmin == ngnmin && gpmin == gnmin) {
            if (ngpmin == gpmin) {
                emin_ = ngpmin;
            } else if (gpmin - ngpmin == 3) {
                emin_ = ngpmin - 1 + t_;
                ieee = true;
            }
        } else if (ngpmin == gpmin && ngnmin == gnmin) {
            if (Math.abs(ngpmin - ngnmin) == 1) {
                emin_ = Math.max(ngpmin, ngnmin);
            }
        } else if (Math.abs(ngpmin - ngnmin) == 1 && gpmin == gnmin) {
            if (gpmin - Math.min(ngpmin, ngnmin) == 3) {
                emin_ = Math.max(ngpmin, ngnmin) - 1 + t_;
            }
        } else {
            emin_ = ngpmin;
            if (ngnmin < emin_) {
                emin_ = ngnmin;
            }
            if (gpmin < emin_) {
                emin_ = gpmin;
            }
            if (gnmin < emin_) {
                emin_ = gnmin;
            }
        }
        if (emin_ == 0) {
            emin_ = Math.min(ngpmin, ngnmin);
        }

//        Assume IEEE arithmetic if we found denormalised  numbers above,
//        or if arithmetic seems to round in the  IEEE style,  determined
//        in routine DLAMC1. A true IEEE machine should have both  things
//        true; however, faulty machines may have one or the other.

        ieee_ = ieee || ieee_;

//        Compute  RMIN by successive division by  BETA. We could compute
//       RMIN as BASE**( EMIN - 1 ),  but some machines underflow during
//        this computation.

        rmin_ = 1;
        for (int i = 0; i < 1 - emin_; ++i) {
            rmin_ = add(rmin_ * rbase, 0);
        }
    }

    /**
     *  calcEmax attempts to compute RMAX, the largest machine floating-point
     *  number, without overflow.  It assumes that EMAX + abs(EMIN) sum
     *  approximately to a power of 2.  It will fail on machines where this
     *  assumption does not hold, for example, the Cyber 205 (EMIN = -28625,
     *  EMAX = 28718).  It will also fail if the value supplied for EMIN is
     *  too large (i.e. too close to zero), probably with overflow.
     */
    private static void calcEmax() {
//     First compute LEXP and UEXP, two powers of 2 that bound
//     abs(EMIN). We then assume that EMAX + abs(EMIN) will sum
//     approximately to the bound that is closest to abs(EMIN).
//    (EMAX is the exponent of the required number RMAX).

        int lexp = 1;
        int exbits = 1;
        int etry = lexp * 2;
        int uexp;
        do {
            lexp = etry;
            ++exbits;
            etry = lexp * 2;
        } while (etry <= (-emin_));
        if (lexp == -emin_) {
            uexp = lexp;
        } else {
            uexp = etry;
            ++exbits;
        }

//     Now -LEXP is less than or equal to EMIN, and -UEXP is greater
//     than or equal to EMIN. EXBITS is the number of bits needed to
//     store the exponent.
        int expsum;
        if (uexp + emin_ > -lexp - emin_) {
            expsum = 2 * lexp;
        } else {
            expsum = 2 * uexp;
        }
//     EXPSUM is the exponent range, approximately equal to
//     EMAX - EMIN + 1 .

        emax_ = expsum + emin_ - 1;
        int nbits = 1 + exbits + t_;

//     NBITS is the total number of bits needed to store a
//     floating-point number.

        if (nbits % 2 == 1 && base_ == 2) {
//        Either there are an odd number of bits used to store a
//        floating-point number, which is unlikely, or some bits are
//        not used in the representation of numbers, which is possible,
//        (e.g. Cray machines) or the mantissa has an implicit bit,
//        (e.g. IEEE machines, Dec Vax machines), which is perhaps the
//        most likely. We have to assume the last alternative.
//        If this is true, then we need to reduce EMAX by one because
//        there must be some way of representing zero in an implicit-bit
//        system. On machines like Cray, we are reducing EMAX by one
//        unnecessarily.
            --emax_;
        }
        if (ieee_) {
//        Assume we are on an IEEE machine which reserves one exponent
//        for infinity and NaN.
            --emax_;
        }
//     Now create RMAX, the largest machine number, which should
//     be equal to (1.0 - BETA**(-P)) * BETA**EMAX .
//     First compute 1.0 - BETA**(-P), being careful that the
//     result is less than 1.0 .

        double recbas = 1.0 / base_;
        double z = base_ - 1;
        double y = 0, oldy = 0;
        for (int i = 0; i < t_; ++i) {
            z *= recbas;
            if (y < 1) {
                oldy = y;
            }
            y = add(y, z);
        }
        if (y >= 1) {
            y = oldy;
        }
//        Now multiply by BETA**EMAX to get RMAX.
        for (int i = 0; i < emax_; ++i) {
            y = add(y * base_, 0);
        }
        rmax_ = y;
    }
    private static int imin_, _imax;

    /**
     * Relative machine precision
     * @returns the value
     */
    public static double getEpsilon() {
        return eps_;
    }

    /**
     * Safe minimum, such that 1/(safe minimum) does not overflow.
     * @return
     */
    public static double getSafeMinimum() {
        return sfmin_;
    }

    public static double getMaxExponent() {
        return emax_;
    }

    public static double getMinExponent() {
        return emin_;
    }

    public static double getOverflowThreshold() {
        return rmax_;
    }

    public static double getUnderflowThreshold() {
        return rmin_;
    }

    public boolean isRounding() {
        return rnd_;
    }
}
