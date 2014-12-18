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

package ec.tstoolkit.maths.matrices.lapack;

/**
 * 
 * @author PCuser
 */
/*
 * //DLAMCH determines double precision machine parameters. /**
 * 
 * //CMACH (input) CHARACTER*1 // Specifies the value to be returned by DLAMCH:
 * // = 'E' or 'e', DLAMCH := eps // = 'S' or 's , DLAMCH := sfmin // = 'B' or
 * 'b', DLAMCH := base // = 'P' or 'p', DLAMCH := eps*base // = 'N' or 'n',
 * DLAMCH := t // = 'R' or 'r', DLAMCH := rnd // = 'M' or 'm', DLAMCH := emin //
 * = 'U' or 'u', DLAMCH := rmin // = 'L' or 'l', DLAMCH := emax // = 'O' or 'o',
 * DLAMCH := rmax
 */
public class Dlamch {

    private final static double ZERO = 0.0, ONE = 1.0;

    /*
     * // eps = relative machine precision // sfmin = safe minimum, such that
     * 1/sfmin does not overflow // base = base of the machine // prec =
     * eps*base // t = number of (base) digits in the mantissa // rnd = 1.0 when
     * rounding occurs in addition, 0.0 otherwise // emin = minimum exponent
     * before (gradual) underflow // rmin = underflow threshold - base**(emin-1)
     * // emax = largest exponent before overflow // rmax = overflow threshold -
     * (base**emax)*(1-eps)
     */
    @SuppressWarnings("unused")
    private static double g_eps, g_sfmin, g_base, g_epsbase, g_rmin, g_rmax;

    @SuppressWarnings("unused")
    private static int g_emin, g_emax, g_t;

    private static int g_beta;
    @SuppressWarnings("unused")
    private static boolean g_ieee1, g_rnd;
    static {
	dlamc1();
	dlamc5(0, true);
    }

    private static void dlamc1() {
	// Purpose
	// =======
	// DLAMC1 determines the machine parameters given by BETA, T, RND, and
	// IEEE1.
	// Arguments
	// =========
	// BETA (output) INTEGER
	// The base of the machine.
	// T (output) INTEGER
	// The number of ( BETA ) digits in the mantissa.
	// RND (output) LOGICAL
	// Specifies whether proper rounding ( RND = .TRUE. ) or
	// chopping ( RND = .FALSE. ) occurs in addition. This may not
	// be a reliable guide to the way in which the machine performs
	// its arithmetic.
	// IEEE1 (output) LOGICAL
	// Specifies whether rounding appears to be done in the IEEE
	// 'round to nearest' style.
	// Further Details
	// ===============
	// The routine is based on the routine ENVRON by Malcolm and
	// incorporates suggestions by Gentleman and Marovich. See
	// Malcolm M. A. (1972) Algorithms to reveal properties of
	// floating-point arithmetic. Comms. of the ACM, 15, 949-951.
	// Gentleman W. M. and Marovich S. B. (1974) More on algorithms
	// that reveal properties of floating point arithmetic units.
	// Comms. of the ACM, 17, 276-277.
	// LBETA, LIEEE1, LT and LRND are the local values of BETA,
	// IEEE1, T and RND.
	// Throughout this routine we use the function DLAMC3 to ensure
	// that relevant values are stored and not held in registers, or
	// are not affected by optimizers.
	// Compute a = 2.0**m with the smallest positive integer m such
	// that
	// fl( a + 1.0 ) = a.
	double a = ONE, c = ONE;
	while (c == ONE) {
	    a = 2 * a;
	    c = dlamc3(a, ONE);
	    c = dlamc3(c, -a);
	}

	double b = ONE;
	c = dlamc3(a, b);
	while (c == a) {
	    b *= 2;
	    c = dlamc3(a, b);
	}

	// Now compute the base. a and c are neighbouring floating point
	// numbers in the interval ( beta**t, beta**( t + 1 ) ) and so
	// their difference is beta. Adding 0.25 to c is to ensure that it
	// is truncated to beta and not ( beta - 1 ).
	double qtr = ONE / 4;
	double savec = c;
	c = dlamc3(c, -a);
	int lbeta = (int) (c + qtr);
	// Now determine whether rounding or chopping occurs, by adding a
	// bit less than beta/2 and a bit more than beta/2 to a.
	b = lbeta;
	double f = dlamc3(b / 2, -b / 100);
	c = dlamc3(f, a);
	boolean lrnd = (c == a);
	f = dlamc3(b / 2, b / 100);
	c = dlamc3(f, a);
	if (lrnd && c == a)
	    lrnd = false;
	// Try and decide whether rounding is done in the IEEE 'round to
	// nearest' style. B/2 is half a unit in the last place of the two
	// numbers A and SAVEC. Furthermore, A is even, i.e. has last bit
	// zero, and SAVEC is odd. Thus adding B/2 to A should not change
	// A, but adding B/2 to SAVEC should change SAVEC.
	double t1 = dlamc3(b / 2, a);
	double t2 = dlamc3(b / 2, savec);
	boolean lieee1 = (t1 == a) && (t2 > savec) && lrnd;
	// Now find the mantissa, t. It should be the integer part of
	// log to the base beta of a, however it is safer to determine t
	// by powering. So we find t as the smallest positive integer for
	// which
	// fl( beta**t + 1.0 ) = 1.0.
	int lt = 0;
	a = ONE;
	c = ONE;
	while (c == ONE) {
	    ++lt;
	    a *= lbeta;
	    c = dlamc3(a, ONE);
	    c = dlamc3(c, -a);
	}
	g_beta = lbeta;
	g_t = lt;
	g_rnd = lrnd;
	g_ieee1 = lieee1;
    }

    @SuppressWarnings("unused")
    private static void dlamc2() {
	// BETA (output) INTEGER
	// The base of the machine.
	// T (output) INTEGER
	// The number of ( BETA ) digits in the mantissa.

	// RND (output) LOGICAL
	// Specifies whether proper rounding ( RND = .TRUE. ) or
	// chopping ( RND = .FALSE. ) occurs in addition. This may not
	// be a reliable guide to the way in which the machine performs
	// its arithmetic.
	// EPS (output) DOUBLE PRECISION
	// The smallest positive number such that
	// fl( 1.0 - EPS ) .LT. 1.0,
	// where fl denotes the computed value.
	// EMIN (output) INTEGER
	// The minimum exponent before (gradual) underflow occurs.
	// RMIN (output) DOUBLE PRECISION
	// The smallest normalized number for the machine, given by
	// BASE**( EMIN - 1 ), where BASE is the floating point value
	// of BETA.
	// EMAX (output) INTEGER
	// The maximum exponent before overflow occurs.

	// RMAX (output) DOUBLE PRECISION
	// The largest positive number for the machine, given by
	// BASE**EMAX * ( 1 - EPS ), where BASE is the floating point
	// value of BETA.
	// Further Details
	// ===============
	// The computation of EPS is based on a routine PARANOIA by
	// W. Kahan of the University of California at Berkeley.

    }

    private static double dlamc3(double a, double b) {
	// Purpose
	// =======
	// DLAMC3 is intended to force A and B to be stored prior to doing
	// the addition of A and B , for use in situations where optimizers
	// might hold one of these in a register.
	return a + b;
    }

    /**
     * IF( FIRST ) THEN FIRST = .FALSE. CALL DLAMC2( BETA, IT, LRND, EPS, IMIN,
     * RMIN, IMAX, RMAX ) BASE = BETA T = IT IF( LRND ) THEN RND = ONE EPS = (
     * BASE**( 1-IT ) ) / 2 ELSE RND = ZERO EPS = BASE**( 1-IT ) END IF PREC =
     * EPS*BASE EMIN = IMIN EMAX = IMAX SFMIN = RMIN SMALL = ONE / RMAX IF(
     * SMALL.GE.SFMIN ) THEN
     * 
     * // Use SMALL plus a bit, to avoid the possibility of rounding // causing
     * overflow when computing 1/sfmin.
     * 
     * SFMIN = SMALL*( ONE+EPS ) END IF END IF
     * 
     * IF( LSAME( CMACH, 'E' ) ) THEN RMACH = EPS ELSE IF( LSAME( CMACH, 'S' ) )
     * THEN RMACH = SFMIN ELSE IF( LSAME( CMACH, 'B' ) ) THEN RMACH = BASE ELSE
     * IF( LSAME( CMACH, 'P' ) ) THEN RMACH = PREC ELSE IF( LSAME( CMACH, 'N' )
     * ) THEN RMACH = T ELSE IF( LSAME( CMACH, 'R' ) ) THEN RMACH = RND ELSE IF(
     * LSAME( CMACH, 'M' ) ) THEN RMACH = EMIN ELSE IF( LSAME( CMACH, 'U' ) )
     * THEN RMACH = RMIN ELSE IF( LSAME( CMACH, 'L' ) ) THEN RMACH = EMAX ELSE
     * IF( LSAME( CMACH, 'O' ) ) THEN RMACH = RMAX END IF
     * 
     * DLAMCH = RMACH RETURN
     * 
     * // End of DLAMCH
     * 
     * END
     * 
     ************************************************************************ 
     * 
     * 
     * 
     * 
     ************************************************************************ 
     * 
     * */
    @SuppressWarnings("unused")
    private static int dlamc4(double start, int base) {
	// Purpose
	// =======
	// DLAMC4 is a service routine for DLAMC2.
	// Arguments
	// =========
	// EMIN (output) EMIN
	// The minimum exponent before (gradual) underflow, computed by
	// setting A = START and dividing by BASE until the previous A
	// can not be recovered.
	// START (input) DOUBLE PRECISION
	// The starting point for determining EMIN.
	// BASE (input) INTEGER
	// The base of the machine.
	double a = start;
	double rbase = ONE / base;
	int emin = 1;
	double b1 = dlamc3(a * rbase, ZERO);
	double c1 = a, c2 = a, d1 = a, d2 = a;

	while (c1 == a && c2 == a && d1 == a && d2 == a) {
	    --emin;
	    a = b1;
	    b1 = dlamc3(a / base, ZERO);
	    c1 = dlamc3(b1 * base, ZERO);
	    d1 = ZERO;
	    for (int i = 0; i < base; ++i)
		d1 += b1;
	    double b2 = dlamc3(a * rbase, ZERO);
	    c2 = dlamc3(b2 / rbase, ZERO);
	    d2 = ZERO;
	    for (int i = 0; i < base; ++i)
		d2 += b2;
	}
	return emin;
    }

    /*
     * SUBROUTINE DLAMC2( BETA, T, RND, EPS, EMIN, RMIN, EMAX, RMAX ) IF( FIRST
     * ) THEN FIRST = .FALSE. ZERO = 0 ONE = 1 TWO = 2 // Start to find EPS.
     * 
     * B = LBETA A = B**( -LT ) LEPS = A
     * 
     * // Try some tricks to see whether or not this is the correct EPS.
     * 
     * B = TWO / 3 HALF = ONE / 2 SIXTH = DLAMC3( B, -HALF ) THIRD = DLAMC3(
     * SIXTH, SIXTH ) B = DLAMC3( THIRD, -HALF ) B = DLAMC3( B, SIXTH ) B = ABS(
     * B ) IF( B.LT.LEPS ) $ B = LEPS
     * 
     * LEPS = 1
     * 
     * + WHILE( ( LEPS.GT.B ).AND.( B.GT.ZERO ) )LOOP 10 CONTINUE IF( (
     * LEPS.GT.B ) .AND. ( B.GT.ZERO ) ) THEN LEPS = B C = DLAMC3( HALF*LEPS, (
     * TWO**5 )*( LEPS**2 ) ) C = DLAMC3( HALF, -C ) B = DLAMC3( HALF, C ) C =
     * DLAMC3( HALF, -B ) B = DLAMC3( HALF, C ) GO TO 10 END IF+ END WHILE
     * 
     * IF( A.LT.LEPS ) $ LEPS = A
     * 
     * // Computation of EPS complete.
     * 
     * // Now find EMIN. Let A = + or - 1, and + or - (1 + BASE**(-3)). // Keep
     * dividing A by BETA until (gradual) underflow occurs. This // is detected
     * when we cannot recover the previous A.
     * 
     * RBASE = ONE / LBETA SMALL = ONE DO 20 I = 1, 3 SMALL = DLAMC3(
     * SMALL*RBASE, ZERO ) 20 CONTINUE A = DLAMC3( ONE, SMALL ) CALL DLAMC4(
     * NGPMIN, ONE, LBETA ) CALL DLAMC4( NGNMIN, -ONE, LBETA ) CALL DLAMC4(
     * GPMIN, A, LBETA ) CALL DLAMC4( GNMIN, -A, LBETA ) IEEE = .FALSE.
     * 
     * IF( ( NGPMIN.EQ.NGNMIN ) .AND. ( GPMIN.EQ.GNMIN ) ) THEN IF(
     * NGPMIN.EQ.GPMIN ) THEN LEMIN = NGPMIN // ( Non twos-complement machines,
     * no gradual underflow; // e.g., VAX ) ELSE IF( ( GPMIN-NGPMIN ).EQ.3 )
     * THEN LEMIN = NGPMIN - 1 + LT IEEE = .TRUE. // ( Non twos-complement
     * machines, with gradual underflow; // e.g., IEEE standard followers ) ELSE
     * LEMIN = MIN( NGPMIN, GPMIN ) // ( A guess; no known machine ) IWARN =
     * .TRUE. END IF
     * 
     * ELSE IF( ( NGPMIN.EQ.GPMIN ) .AND. ( NGNMIN.EQ.GNMIN ) ) THEN IF( ABS(
     * NGPMIN-NGNMIN ).EQ.1 ) THEN LEMIN = MAX( NGPMIN, NGNMIN ) // (
     * Twos-complement machines, no gradual underflow; // e.g., CYBER 205 ) ELSE
     * LEMIN = MIN( NGPMIN, NGNMIN ) // ( A guess; no known machine ) IWARN =
     * .TRUE. END IF
     * 
     * ELSE IF( ( ABS( NGPMIN-NGNMIN ).EQ.1 ) .AND. $ ( GPMIN.EQ.GNMIN ) ) THEN
     * IF( ( GPMIN-MIN( NGPMIN, NGNMIN ) ).EQ.3 ) THEN LEMIN = MAX( NGPMIN,
     * NGNMIN ) - 1 + LT // ( Twos-complement machines with gradual underflow;
     * // no known machine ) ELSE LEMIN = MIN( NGPMIN, NGNMIN ) // ( A guess; no
     * known machine ) IWARN = .TRUE. END IF
     * 
     * ELSE LEMIN = MIN( NGPMIN, NGNMIN, GPMIN, GNMIN ) // ( A guess; no known
     * machine ) IWARN = .TRUE. END IF** Comment out this if block if EMIN is ok
     * IF( IWARN ) THEN FIRST = .TRUE. WRITE( 6, FMT = 9999 )LEMIN END IF**
     * 
     * // Assume IEEE arithmetic if we found denormalised numbers above, // or
     * if arithmetic seems to round in the IEEE style, determined // in routine
     * DLAMC1. A true IEEE machine should have both things // true; however,
     * faulty machines may have one or the other.
     * 
     * IEEE = IEEE .OR. LIEEE1
     * 
     * // Compute RMIN by successive division by BETA. We could compute // RMIN
     * as BASE**( EMIN - 1 ), but some machines underflow during // this
     * computation.
     * 
     * LRMIN = 1 DO 30 I = 1, 1 - LEMIN LRMIN = DLAMC3( LRMIN*RBASE, ZERO ) 30
     * CONTINUE
     * 
     * // Finally, call DLAMC5 to compute EMAX and RMAX.
     * 
     * CALL DLAMC5( LBETA, LT, LEMIN, IEEE, LEMAX, LRMAX ) END IF
     * 
     * BETA = LBETA T = LT RND = LRND EPS = LEPS EMIN = LEMIN RMIN = LRMIN EMAX
     * = LEMAX RMAX = LRMAX
     * 
     * RETURN
     * 
     * 9999 FORMAT( / / ' WARNING. The value EMIN may be incorrect:-', $ ' EMIN
     * = ', I8, / $ ' If, after inspection, the value EMIN looks', $ '
     * acceptable please comment out ', $ / ' the IF block as marked within the
     * code of routine', $ ' DLAMC2,', / ' otherwise supply EMIN explicitly.', /
     * )
     * 
     * // End of DLAMC2
     * 
     * END
     * 
     * ***********************************************************************
     */
    private static void dlamc5(int p, boolean ieee) {
	g_emin = -3;
	g_emax = -g_emin;

	// Purpose
	// =======
	// DLAMC5 attempts to compute RMAX, the largest machine floating-point
	// number, without overflow. It assumes that EMAX + abs(EMIN) sum
	// approximately to a power of 2. It will fail on machines where this
	// assumption does not hold, for example, the Cyber 205 (EMIN = -28625,
	// EMAX = 28718). It will also fail if the value supplied for EMIN is
	// too large (i.e. too close to zero), probably with overflow.
	// Arguments
	// =========
	// BETA (input) INTEGER
	// The base of floating-point arithmetic.
	// P (input) INTEGER
	// The number of base BETA digits in the mantissa of a
	// floating-point value.
	// EMIN (input) INTEGER
	// The minimum exponent before (gradual) underflow.
	// IEEE (input) LOGICAL
	// A logical flag specifying whether or not the arithmetic
	// system is thought to comply with the IEEE standard.
	// EMAX (output) INTEGER
	// The largest exponent before overflow
	// RMAX (output) DOUBLE PRECISION
	// The largest machine floating-point number.
	//
	// First compute LEXP and UEXP, two powers of 2 that bound
	// abs(EMIN). We then assume that EMAX + abs(EMIN) will sum
	// approximately to the bound that is closest to abs(EMIN).
	// (EMAX is the exponent of the required number RMAX).

	int lexp = 1, exbits = 1;
	int etry = 2;
	while (etry <= -g_emin) {
	    lexp = etry;
	    exbits++;
	    etry = lexp * 2;
	}
	int uexp;
	if (lexp == g_emin)
	    uexp = lexp;
	else {
	    uexp = etry;
	    ++exbits;
	}
	// Now -LEXP is less than or equal to EMIN, and -UEXP is greater
	// than or equal to EMIN. EXBITS is the number of bits needed to
	// store the exponent.
	int expsum;
	if ((uexp + g_emin) > (-lexp - g_emin))
	    expsum = 2 * lexp;
	else
	    expsum = 2 * uexp;
	// EXPSUM is the exponent range, approximately equal to
	// EMAX - EMIN + 1 .
	g_emax = expsum + g_emin - 1;
	int nbits = 1 + exbits + p;

	// NBITS is the total number of bits needed to store a
	// floating-point number.
	if (nbits % 2 == 1 && g_beta == 2)
	    // Either there are an odd number of bits used to store a
	    // floating-point number, which is unlikely, or some bits are
	    // not used in the representation of numbers, which is possible,
	    // (e.g. Cray machines) or the mantissa has an implicit bit,
	    // (e.g. IEEE machines, Dec Vax machines), which is perhaps the
	    // most likely. We have to assume the last alternative.
	    // If this is true, then we need to reduce EMAX by one because
	    // there must be some way of representing zero in an implicit-bit
	    // system. On machines like Cray, we are reducing EMAX by one
	    // unnecessarily.
	    --g_emax;
	if (ieee)
	    // Assume we are on an IEEE machine which reserves one exponent
	    // for infinity and NaN.
	    --g_emax;
	// Now create RMAX, the largest machine number, which should
	// be equal to (1.0 - BETA**(-P)) * BETA**EMAX .
	// First compute 1.0 - BETA**(-P), being careful that the
	// result is less than 1.0 .
	double recbas = ONE / g_beta;
	double z = g_beta - ONE;
	double y = ZERO;
	double oldy = ZERO;
	for (int i = 0; i < p; ++i) {
	    z *= recbas;
	    if (y < ONE) {
		oldy = y;
		y = dlamc3(y, z);
	    }
	}
	if (y >= ONE)
	    y = oldy;
	// Now multiply by BETA**EMAX to get RMAX.
	for (int i = 0; i < g_emax; ++i)
	    y = dlamc3(y * g_beta, ZERO);
	g_rmax = y;
    }

    // DLAMCH('E')
    /**
     * 
     * @return
     */
    public static double EPS() {
	return g_eps;
    }

    // DLAMCH('S')

    /**
     * 
     * @return
     */
    public static double SFMIN() {
	return g_sfmin;
    }
}
