/*
 * Copyright 2016 National Bank of Belgium
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
package ec.tstoolkit.dstats;

import ec.tstoolkit.maths.Constants;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.random.IRandomNumberGenerator;

/**
 *
 * @author Jean Palate
 */
public class Gamma implements IContinuousDistribution {

    public static final double MAXGAM = 171.624376956302725;

    private final double alpha, beta;

    /**
     * Constructs a Gamma distribution. Example: alpha=1.0, lambda=1.0. alpha =
     * mean*mean / variance; lambda = 1 / (variance / mean);
     *
     * @param alpha Shape
     * @param beta Scale
     * @throws IllegalArgumentException if <tt>alpha &lt;= 0.0 || lambda &lt;=
     * 0.0</tt>.
     */
    public Gamma(double alpha, double beta) {
        this.alpha = alpha;
        this.beta = beta;
    }

    @Override
    public double getDensity(double x) throws DStatException {
        if (x < 0) {
            throw new IllegalArgumentException();
        }
        if (x == 0) {
            if (alpha == 1.0) {
                return 1/beta;
            } else {
                return 0.0;
            }
        }
        if (alpha == 1.0) {
            return Math.exp(-x / beta) / beta;
        }

        return Math.exp((alpha - 1.0) * Math.log(x / beta) - x / beta - logGamma(alpha)) / beta;
    }

    public double getShape() {
        return alpha;
    }

    public double getScale() {
        return beta;
    }

    @Override
    public double getLeftBound() {
        return 0;
    }

    @Override
    public double getRightBound() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Gamma with shape = ");
        sb.append(alpha);
        sb.append(" and scale = ");
        sb.append(beta);
        return sb.toString();
    }

    @Override
    public double getExpectation() throws DStatException {
        return alpha * beta; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getProbability(double x, ProbabilityType pt) throws DStatException {
        if (x < 0.0 || pt == ProbabilityType.Point) {
            return 0.0;
        }
        if (pt == ProbabilityType.Lower) {
            return incompleteGamma(alpha, x / beta);
        } else {
            return 1 - incompleteGamma(alpha, x / beta);
        }
    }

    @Override
    public double getProbabilityInverse(double p, ProbabilityType pt) throws DStatException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getVariance() throws DStatException {
        return alpha * (beta * beta);
    }

    @Override
    public BoundaryType hasLeftBound() {
        return BoundaryType.Finite;
    }

    @Override
    public BoundaryType hasRightBound() {
        return BoundaryType.None;
    }

    @Override
    public boolean isSymmetrical() {
        return false;
    }

    @Override
    public double random(IRandomNumberGenerator rng) throws DStatException {
        double a = alpha;
        double aa = -1.0, aaa = -1.0,
                b = 0.0, c = 0.0, d = 0.0, e, r, s = 0.0, si = 0.0, ss = 0.0, q0 = 0.0,
                q1 = 0.0416666664, q2 = 0.0208333723, q3 = 0.0079849875,
                q4 = 0.0015746717, q5 = -0.0003349403, q6 = 0.0003340332,
                q7 = 0.0006053049, q8 = -0.0004701849, q9 = 0.0001710320,
                a1 = 0.333333333, a2 = -0.249999949, a3 = 0.199999867,
                a4 = -0.166677482, a5 = 0.142873973, a6 = -0.124385581,
                a7 = 0.110368310, a8 = -0.112750886, a9 = 0.104089866,
                e1 = 1.000000000, e2 = 0.499999994, e3 = 0.166666848,
                e4 = 0.041664508, e5 = 0.008345522, e6 = 0.001353826,
                e7 = 0.000247453;

        double gds, p, q, t, sign_u, u, v, w, x;
        double v1, v2, v12;

        if (a < 1.0) { // CASE A: Acceptance rejection algorithm gs
            b = 1.0 + 0.36788794412 * a;              // Step 1
            while (true) {
                p = b * rng.nextDouble();
                if (p <= 1.0) {                       // Step 2. Case gds <= 1
                    gds = Math.exp(Math.log(p) / a);
                    if (Math.log(rng.nextDouble()) <= -gds) {
                        return (gds * beta);
                    }
                } else {                                // Step 3. Case gds > 1
                    gds = -Math.log((b - p) / a);
                    if (Math.log(rng.nextDouble()) <= ((a - 1.0) * Math.log(gds))) {
                        return (gds * beta);
                    }
                }
            }
        } else {        // CASE B: Acceptance complement algorithm gd (gaussian distribution, box muller transformation)
            if (a != aa) {                        // Step 1. Preparations
                ss = a - 0.5;
                s = Math.sqrt(ss);
                d = 5.656854249 - 12.0 * s;
            }
            // Step 2. Normal deviate
            do {
                v1 = 2.0 * rng.nextDouble() - 1.0;
                v2 = 2.0 * rng.nextDouble() - 1.0;
                v12 = v1 * v1 + v2 * v2;
            } while (v12 > 1.0);
            t = v1 * Math.sqrt(-2.0 * Math.log(v12) / v12);
            x = s + 0.5 * t;
            gds = x * x;
            if (t >= 0.0) {
                return (gds * beta);         // Immediate acceptance
            }
            u = rng.nextDouble();                // Step 3. Uniform random number
            if (d * u <= t * t * t) {
                return (gds * beta); // Squeeze acceptance
            }
            if (a != aaa) {                           // Step 4. Set-up for hat case
                r = 1.0 / a;
                q0 = ((((((((q9 * r + q8) * r + q7) * r + q6) * r + q5) * r + q4)
                        * r + q3) * r + q2) * r + q1) * r;
                if (a > 3.686) {
                    if (a > 13.022) {
                        b = 1.77;
                        si = 0.75;
                        c = 0.1515 / s;
                    } else {
                        b = 1.654 + 0.0076 * ss;
                        si = 1.68 / s + 0.275;
                        c = 0.062 / s + 0.024;
                    }
                } else {
                    b = 0.463 + s - 0.178 * ss;
                    si = 1.235;
                    c = 0.195 / s - 0.079 + 0.016 * s;
                }
            }
            if (x > 0.0) {                        // Step 5. Calculation of q
                v = t / (s + s);                  // Step 6.
                if (Math.abs(v) > 0.25) {
                    q = q0 - s * t + 0.25 * t * t + (ss + ss) * Math.log(1.0 + v);
                } else {
                    q = q0 + 0.5 * t * t * ((((((((a9 * v + a8) * v + a7) * v + a6)
                            * v + a5) * v + a4) * v + a3) * v + a2) * v + a1) * v;
                }								  // Step 7. Quotient acceptance
                if (Math.log(1.0 - u) <= q) {
                    return (gds * beta);
                }
            }

            while (true) {              			      // Step 8. Double exponential deviate t
                do {
                    e = -Math.log(rng.nextDouble());
                    u = rng.nextDouble();
                    u = u + u - 1.0;
                    sign_u = (u > 0) ? 1.0 : -1.0;
                    t = b + (e * si) * sign_u;
                } while (t <= -0.71874483771719); // Step 9. Rejection of t
                v = t / (s + s);                  // Step 10. New q(t)
                if (Math.abs(v) > 0.25) {
                    q = q0 - s * t + 0.25 * t * t + (ss + ss) * Math.log(1.0 + v);
                } else {
                    q = q0 + 0.5 * t * t * ((((((((a9 * v + a8) * v + a7) * v + a6)
                            * v + a5) * v + a4) * v + a3) * v + a2) * v + a1) * v;
                }
                if (q <= 0.0) {
                    continue;           // Step 11.
                }
                if (q > 0.5) {
                    w = Math.exp(q) - 1.0;
                } else {
                    w = ((((((e7 * q + e6) * q + e5) * q + e4) * q + e3) * q + e2)
                            * q + e1) * q;
                }                    			  // Step 12. Hat acceptance
                if (c * u * sign_u <= w * Math.exp(e - 0.5 * t * t)) {
                    x = s + 0.5 * t;
                    return (x * x * beta);
                }
            }
        }
    }

    /**
     * Returns the beta function of the arguments.
     * <pre>
     *                   -     -
     *                  | (a) | (b)
     * beta( a, b )  =  -----------.
     *                     -
     *                    | (a+b)
     * </pre>
     *
     * @param a
     * @param b
     * @return
     */
    public static double beta(double a, double b) throws ArithmeticException {
        double y;

        y = a + b;
        y = gamma(y);
        if (y == 0.0) {
            return 1.0;
        }

        if (a > b) {
            y = gamma(a) / y;
            y *= gamma(b);
        } else {
            y = gamma(b) / y;
            y *= gamma(a);
        }

        return (y);
    }

    /**
     * Returns the Gamma function of the argument.
     *
     * @param x
     * @return
     */
    public static double gamma(double x) throws ArithmeticException {

        double P[] = {
            1.60119522476751861407E-4,
            1.19135147006586384913E-3,
            1.04213797561761569935E-2,
            4.76367800457137231464E-2,
            2.07448227648435975150E-1,
            4.94214826801497100753E-1,
            9.99999999999999996796E-1
        };
        double Q[] = {
            -2.31581873324120129819E-5,
            5.39605580493303397842E-4,
            -4.45641913851797240494E-3,
            1.18139785222060435552E-2,
            3.58236398605498653373E-2,
            -2.34591795718243348568E-1,
            7.14304917030273074085E-2,
            1.00000000000000000320E0
        };
//double MAXGAM = 171.624376956302725;
//double LOGPI  = 1.14472988584940017414;

        double p, z;
        int i;

        double q = Math.abs(x);

        if (q > 33.0) {
            if (x < 0.0) {
                p = Math.floor(q);
                if (p == q) {
                    throw new ArithmeticException("gamma: overflow");
                }
                i = (int) p;
                z = q - p;
                if (z > 0.5) {
                    p += 1.0;
                    z = q - p;
                }
                z = q * Math.sin(Math.PI * z);
                if (z == 0.0) {
                    throw new ArithmeticException("gamma: overflow");
                }
                z = Math.abs(z);
                z = Math.PI / (z * stirlingFormula(q));

                return -z;
            } else {
                return stirlingFormula(x);
            }
        }

        z = 1.0;
        while (x >= 3.0) {
            x -= 1.0;
            z *= x;
        }

        while (x < 0.0) {
            if (x == 0.0) {
                throw new ArithmeticException("gamma: singular");
            } else if (x > -1.E-9) {
                return (z / ((1.0 + 0.5772156649015329 * x) * x));
            }
            z /= x;
            x += 1.0;
        }

        while (x < 2.0) {
            if (x == 0.0) {
                throw new ArithmeticException("gamma: singular");
            } else if (x < 1.e-9) {
                return (z / ((1.0 + 0.5772156649015329 * x) * x));
            }
            z /= x;
            x += 1.0;
        }

        if ((x == 2.0) || (x == 3.0)) {
            return z;
        }

        x -= 2.0;
        p = Polynomial.revaluate(P, x);
        q = Polynomial.revaluate(Q, x);
        return z * p / q;
    }

    /**
     * Returns the Incomplete Beta Function evaluated from zero to <tt>xx</tt>;
     * formerly named <tt>ibeta</tt>.
     *
     * @param aa the alpha parameter of the beta distribution.
     * @param bb the beta parameter of the beta distribution.
     * @param xx the integration end point.
     * @return
     */
    public static double incompleteBeta(double aa, double bb, double xx) throws ArithmeticException {
        double a, b, t, x, xc, w, y;
        boolean flag;

        if (aa <= 0.0 || bb <= 0.0) {
            throw new ArithmeticException("ibeta: Domain error!");
        }

        if ((xx <= 0.0) || (xx >= 1.0)) {
            if (xx == 0.0) {
                return 0.0;
            }
            if (xx == 1.0) {
                return 1.0;
            }
            throw new ArithmeticException("ibeta: Domain error!");
        }

        flag = false;
        if ((bb * xx) <= 1.0 && xx <= 0.95) {
            t = powerSeries(aa, bb, xx);
            return t;
        }

        w = 1.0 - xx;

        /* Reverse a and b if x is greater than the mean. */
        if (xx > (aa / (aa + bb))) {
            flag = true;
            a = bb;
            b = aa;
            xc = xx;
            x = w;
        } else {
            a = aa;
            b = bb;
            xc = w;
            x = xx;
        }

        if (flag && (b * x) <= 1.0 && x <= 0.95) {
            t = powerSeries(a, b, x);
            if (t <= Constants.MACHEP) {
                t = 1.0 - Constants.MACHEP;
            } else {
                t = 1.0 - t;
            }
            return t;
        }

        /* Choose expansion for better convergence. */
        y = x * (a + b - 2.0) - (a - 1.0);
        if (y < 0.0) {
            w = incompleteBetaFraction1(a, b, x);
        } else {
            w = incompleteBetaFraction2(a, b, x) / xc;
        }

        /* Multiply w by the factor
         a      b   _             _     _
         x  (1-x)   | (a+b) / ( a | (a) | (b) ) .   */
        y = a * Math.log(x);
        t = b * Math.log(xc);
        if ((a + b) < MAXGAM && Math.abs(y) < Constants.MAXLOG && Math.abs(t) < Constants.MAXLOG) {
            t = Math.pow(xc, b);
            t *= Math.pow(x, a);
            t /= a;
            t *= w;
            t *= gamma(a + b) / (gamma(a) * gamma(b));
            if (flag) {
                if (t <= Constants.MACHEP) {
                    t = 1.0 - Constants.MACHEP;
                } else {
                    t = 1.0 - t;
                }
            }
            return t;
        }
        /* Resort to logarithms.  */
        y += t + logGamma(a + b) - logGamma(a) - logGamma(b);
        y += Math.log(w / a);
        if (y < Constants.MINLOG) {
            t = 0.0;
        } else {
            t = Math.exp(y);
        }

        if (flag) {
            if (t <= Constants.MACHEP) {
                t = 1.0 - Constants.MACHEP;
            } else {
                t = 1.0 - t;
            }
        }
        return t;
    }

    /**
     * Continued fraction expansion #1 for incomplete beta integral; formerly
     * named <tt>incbcf</tt>.
     *
     * @param a
     * @param b
     * @param x
     * @return
     */
    public static double incompleteBetaFraction1(double a, double b, double x) throws ArithmeticException {
        double xk, pk, pkm1, pkm2, qk, qkm1, qkm2;
        double k1, k2, k3, k4, k5, k6, k7, k8;
        double r, t, ans, thresh;
        int n;

        k1 = a;
        k2 = a + b;
        k3 = a;
        k4 = a + 1.0;
        k5 = 1.0;
        k6 = b - 1.0;
        k7 = k4;
        k8 = a + 2.0;

        pkm2 = 0.0;
        qkm2 = 1.0;
        pkm1 = 1.0;
        qkm1 = 1.0;
        ans = 1.0;
        r = 1.0;
        n = 0;
        thresh = 3.0 * Constants.MACHEP;
        do {
            xk = -(x * k1 * k2) / (k3 * k4);
            pk = pkm1 + pkm2 * xk;
            qk = qkm1 + qkm2 * xk;
            pkm2 = pkm1;
            pkm1 = pk;
            qkm2 = qkm1;
            qkm1 = qk;

            xk = (x * k5 * k6) / (k7 * k8);
            pk = pkm1 + pkm2 * xk;
            qk = qkm1 + qkm2 * xk;
            pkm2 = pkm1;
            pkm1 = pk;
            qkm2 = qkm1;
            qkm1 = qk;

            if (qk != 0) {
                r = pk / qk;
            }
            if (r != 0) {
                t = Math.abs((ans - r) / r);
                ans = r;
            } else {
                t = 1.0;
            }

            if (t < thresh) {
                return ans;
            }

            k1 += 1.0;
            k2 += 1.0;
            k3 += 2.0;
            k4 += 2.0;
            k5 += 1.0;
            k6 -= 1.0;
            k7 += 2.0;
            k8 += 2.0;

            if ((Math.abs(qk) + Math.abs(pk)) > Constants.BIG) {
                pkm2 *= Constants.BIGINV;
                pkm1 *= Constants.BIGINV;
                qkm2 *= Constants.BIGINV;
                qkm1 *= Constants.BIGINV;
            }
            if ((Math.abs(qk) < Constants.BIGINV) || (Math.abs(pk) < Constants.BIGINV)) {
                pkm2 *= Constants.BIG;
                pkm1 *= Constants.BIG;
                qkm2 *= Constants.BIG;
                qkm1 *= Constants.BIG;
            }
        } while (++n < 300);

        return ans;
    }

    /**
     * Continued fraction expansion #2 for incomplete beta integral; formerly
     * named <tt>incbd</tt>.
     */
    static double incompleteBetaFraction2(double a, double b, double x) throws ArithmeticException {
        double xk, pk, pkm1, pkm2, qk, qkm1, qkm2;
        double k1, k2, k3, k4, k5, k6, k7, k8;
        double r, t, ans, z, thresh;
        int n;

        k1 = a;
        k2 = b - 1.0;
        k3 = a;
        k4 = a + 1.0;
        k5 = 1.0;
        k6 = a + b;
        k7 = a + 1.0;
        k8 = a + 2.0;

        pkm2 = 0.0;
        qkm2 = 1.0;
        pkm1 = 1.0;
        qkm1 = 1.0;
        z = x / (1.0 - x);
        ans = 1.0;
        r = 1.0;
        n = 0;
        thresh = 3.0 * Constants.MACHEP;
        do {
            xk = -(z * k1 * k2) / (k3 * k4);
            pk = pkm1 + pkm2 * xk;
            qk = qkm1 + qkm2 * xk;
            pkm2 = pkm1;
            pkm1 = pk;
            qkm2 = qkm1;
            qkm1 = qk;

            xk = (z * k5 * k6) / (k7 * k8);
            pk = pkm1 + pkm2 * xk;
            qk = qkm1 + qkm2 * xk;
            pkm2 = pkm1;
            pkm1 = pk;
            qkm2 = qkm1;
            qkm1 = qk;

            if (qk != 0) {
                r = pk / qk;
            }
            if (r != 0) {
                t = Math.abs((ans - r) / r);
                ans = r;
            } else {
                t = 1.0;
            }

            if (t < thresh) {
                return ans;
            }

            k1 += 1.0;
            k2 -= 1.0;
            k3 += 2.0;
            k4 += 2.0;
            k5 += 1.0;
            k6 += 1.0;
            k7 += 2.0;
            k8 += 2.0;

            if ((Math.abs(qk) + Math.abs(pk)) > Constants.BIG) {
                pkm2 *= Constants.BIGINV;
                pkm1 *= Constants.BIGINV;
                qkm2 *= Constants.BIGINV;
                qkm1 *= Constants.BIGINV;
            }
            if ((Math.abs(qk) < Constants.BIGINV) || (Math.abs(pk) < Constants.BIGINV)) {
                pkm2 *= Constants.BIG;
                pkm1 *= Constants.BIG;
                qkm2 *= Constants.BIG;
                qkm1 *= Constants.BIG;
            }
        } while (++n < 300);

        return ans;
    }

    /**
     * Returns the Incomplete Gamma function; formerly named <tt>igamma</tt>.
     *
     * @param a the parameter of the gamma distribution.
     * @param x the integration end point.
     * @return
     */
    public static double incompleteGamma(double a, double x)
            throws ArithmeticException {

        double ans, ax, c, r;

        if (x <= 0 || a <= 0) {
            return 0.0;
        }

        if (x > 1.0 && x > a) {
            return 1.0 - incompleteGammaComplement(a, x);
        }

        /* Compute  x**a * exp(-x) / gamma(a)  */
        ax = a * Math.log(x) - x - logGamma(a);
        if (ax < -Constants.MAXLOG) {
            return (0.0);
        }

        ax = Math.exp(ax);

        /* power series */
        r = a;
        c = 1.0;
        ans = 1.0;

        do {
            r += 1.0;
            c *= x / r;
            ans += c;
        } while (c / ans > Constants.MACHEP);

        return (ans * ax / a);

    }

    /**
     * Returns the Complemented Incomplete Gamma function; formerly named
     * <tt>igamc</tt>.
     *
     * @param a the parameter of the gamma distribution.
     * @param x the integration start point.
     * @return
     */
    public static double incompleteGammaComplement(double a, double x) throws ArithmeticException {
        double ans, ax, c, yc, r, t, y, z;
        double pk, pkm1, pkm2, qk, qkm1, qkm2;

        if (x <= 0 || a <= 0) {
            return 1.0;
        }

        if (x < 1.0 || x < a) {
            return 1.0 - incompleteGamma(a, x);
        }

        ax = a * Math.log(x) - x - logGamma(a);
        if (ax < -Constants.MAXLOG) {
            return 0.0;
        }

        ax = Math.exp(ax);

        /* continued fraction */
        y = 1.0 - a;
        z = x + y + 1.0;
        c = 0.0;
        pkm2 = 1.0;
        qkm2 = x;
        pkm1 = x + 1.0;
        qkm1 = z * x;
        ans = pkm1 / qkm1;

        do {
            c += 1.0;
            y += 1.0;
            z += 2.0;
            yc = y * c;
            pk = pkm1 * z - pkm2 * yc;
            qk = qkm1 * z - qkm2 * yc;
            if (qk != 0) {
                r = pk / qk;
                t = Math.abs((ans - r) / r);
                ans = r;
            } else {
                t = 1.0;
            }

            pkm2 = pkm1;
            pkm1 = pk;
            qkm2 = qkm1;
            qkm1 = qk;
            if (Math.abs(pk) > Constants.BIG) {
                pkm2 *= Constants.BIGINV;
                pkm1 *= Constants.BIGINV;
                qkm2 *= Constants.BIGINV;
                qkm1 *= Constants.BIGINV;
            }
        } while (t > Constants.MACHEP);

        return ans * ax;
    }

    /**
     * Returns the natural logarithm of the gamma function; formerly named
     * <tt>lgamma</tt>.
     *
     * @param x
     * @return
     */
    public static double logGamma(double x) throws ArithmeticException {
        double p, q, w, z;

        double A[] = {
            8.11614167470508450300E-4,
            -5.95061904284301438324E-4,
            7.93650340457716943945E-4,
            -2.77777777730099687205E-3,
            8.33333333333331927722E-2
        };
        double B[] = {
            -1.37825152569120859100E3,
            -3.88016315134637840924E4,
            -3.31612992738871184744E5,
            -1.16237097492762307383E6,
            -1.72173700820839662146E6,
            -8.53555664245765465627E5
        };
        double C[] = {
            /* 1.00000000000000000000E0, */
            -3.51815701436523470549E2,
            -1.70642106651881159223E4,
            -2.20528590553854454839E5,
            -1.13933444367982507207E6,
            -2.53252307177582951285E6,
            -2.01889141433532773231E6
        };

        if (x < -34.0) {
            q = -x;
            w = logGamma(q);
            p = Math.floor(q);
            if (p == q) {
                throw new ArithmeticException("lgam: Overflow");
            }
            z = q - p;
            if (z > 0.5) {
                p += 1.0;
                z = p - q;
            }
            z = q * Math.sin(Math.PI * z);
            if (z == 0.0) {
                throw new ArithmeticException("lgamma: Overflow");
            }
            z = Constants.LOGPI - Math.log(z) - w;
            return z;
        }

        if (x < 13.0) {
            z = 1.0;
            while (x >= 3.0) {
                x -= 1.0;
                z *= x;
            }
            while (x < 2.0) {
                if (x == 0.0) {
                    throw new ArithmeticException("lgamma: Overflow");
                }
                z /= x;
                x += 1.0;
            }
            if (z < 0.0) {
                z = -z;
            }
            if (x == 2.0) {
                return Math.log(z);
            }
            x -= 2.0;
            p = x * Polynomial.revaluate(B, x) / Polynomial.revaluate(C, x);
            return (Math.log(z) + p);
        }

        if (x > 2.556348e305) {
            throw new ArithmeticException("lgamma: Overflow");
        }

        q = (x - 0.5) * Math.log(x) - x + 0.91893853320467274178;
        //if( x > 1.0e8 ) return( q );
        if (x > 1.0e8) {
            return (q);
        }

        p = 1.0 / (x * x);
        if (x >= 1000.0) {
            q += ((7.9365079365079365079365e-4 * p
                    - 2.7777777777777777777778e-3) * p
                    + 0.0833333333333333333333) / x;
        } else {
            q += Polynomial.revaluate(A, p) / x;
        }
        return q;
    }

    /**
     * Power series for incomplete beta integral; formerly named
     * <tt>pseries</tt>. Use when b*x is small and x not too close to 1.
     */
    static double powerSeries(double a, double b, double x) throws ArithmeticException {
        double s, t, u, v, n, t1, z, ai;

        ai = 1.0 / a;
        u = (1.0 - b) * x;
        v = u / (a + 1.0);
        t1 = v;
        t = u;
        n = 2.0;
        s = 0.0;
        z = Constants.MACHEP * ai;
        while (Math.abs(v) > z) {
            u = (n - b) * x / n;
            t *= u;
            v = t / (a + n);
            s += v;
            n += 1.0;
        }
        s += t1;
        s += ai;

        u = a * Math.log(x);
        if ((a + b) < MAXGAM && Math.abs(u) < Constants.MAXLOG) {
            t = gamma(a + b) / (gamma(a) * gamma(b));
            s = s * t * Math.pow(x, a);
        } else {
            t = logGamma(a + b) - logGamma(a) - logGamma(b) + u + Math.log(s);
            if (t < Constants.MINLOG) {
                s = 0.0;
            } else {
                s = Math.exp(t);
            }
        }
        return s;
    }

    private static final double MAXSTIR = 143.01608;

    /**
     * Returns the Gamma function computed by Stirling's formula; formerly named
     * <tt>stirf</tt>. The polynomial STIR is valid for 33 <= x <= 172.
     */
    static double stirlingFormula(double x) throws ArithmeticException {
        double STIR[] = {
            7.87311395793093628397E-4,
            -2.29549961613378126380E-4,
            -2.68132617805781232825E-3,
            3.47222221605458667310E-3,
            8.33333333333482257126E-2};

        double w = 1.0 / x;
        double y = Math.exp(x);

        w = 1.0 + w * Polynomial.revaluate(STIR, w);

        if (x > MAXSTIR) {
            /* Avoid overflow in Math.pow() */
            double v = Math.pow(x, 0.5 * x - 0.25);
            y = v * (v / y);
        } else {
            y = Math.pow(x, x - 0.5) / y;
        }
        y = Constants.SQTPI * y * w;
        return y;
    }
}
