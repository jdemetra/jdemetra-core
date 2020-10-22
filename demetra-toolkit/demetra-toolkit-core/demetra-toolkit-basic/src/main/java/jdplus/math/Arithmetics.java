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
package jdplus.math;

import demetra.math.MathException;
import nbbrd.design.Development;

/**
 * Utilities on integer numbers
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.experimental.UtilityClass
public final class Arithmetics {

    /**
     *
     * @param n
     * @return
     */
    public int[] divisors(final int n) {
        int[] tmp = new int[1 + n / 2];
        int nd = divisors(n, tmp);
        int[] rslt = new int[nd];
        for (int i = 0; i < nd; ++i) {
            rslt[i] = tmp[i];
        }
        return rslt;
    }

    /**
     *
     * @param n
     * @param buffer
     * @return
     */
    public int divisors(final int n, final int[] buffer) {
        if (n == 1) {
            return 0;
        }
        int d = 1;
        int idx = 0;
        while (d * 2 <= n) {
            if (n % d == 0) {
                buffer[idx++] = d;
            }
            ++d;
        }
        return idx;
    }

    /**
     * Computes the greatest common divisor of two integers.
     *
     * @param a
     * @param b
     * @return
     */
    public int gcd(final int a, final int b) {
        if (a == 0) {
            return b;
        } else if (b == 0) {
            return a;
        }
        int u = a < 0 ? -a : a;
        int v = b < 0 ? -b : b;

        final int aTwos = Integer.numberOfTrailingZeros(u);
        u >>= aTwos;
        final int bTwos = Integer.numberOfTrailingZeros(v);
        v >>= bTwos;
        final int shift = Math.min(aTwos, bTwos);

        while (u != v) {
            final int delta = u - v;
            v = Math.min(u, v);
            u = Math.abs(delta);
            u >>= Integer.numberOfTrailingZeros(u);
        }
        // Recover the common power of 2.
        return u << shift;
    }

    /**
     * Computes the greatest common divisor of two longs. See Knuth and
     * CommonMath package from Apache
     *
     * @param a
     * @param b
     * @return
     */
    public long gcd(final long a, final long b) {
        long p = a;
        long q = b;
        if ((p == 0) || (q == 0)) {
            if ((p == Long.MIN_VALUE) || (q == Long.MIN_VALUE)) {
                throw new MathException(MathException.OVERFLOW);
            }
            return Math.abs(p) + Math.abs(q);
        }
        // make u and v negative
        if (p > 0) {
            p = -p;
        }
        if (q > 0) {
            q = -q;
        }
        int k = 0;
        while ((p & 1) == 0 && (q & 1) == 0 && k < 63) {
            p >>= 1;
            q >>= 1;
            k++;
        }
        if (k == 63) {
            throw new MathException(MathException.OVERFLOW);
        }
        long t = ((p & 1) == 1) ? q : -(p >> 1);
        do {
            while ((t & 1) == 0) {
                t >>= 1;
            }
            if (t > 0) {
                p = -t;
            } else {
                q = t;
            }
            t = (q - p) >> 1;
        } while (t != 0);
        return -p * (1L << k);
    }

    /**
     * Computes the least common multiple of two integers.
     *
     * @param a
     * @param b
     * @return
     */
    public int lcm(int a, int b) {
        return a * (b / gcd(a, b));
    }

    /**
     * Computes the sum of the powers of the first n integers = 1 + 2^k + 3^k +
     * ...+ (n)^k
     *
     * @param k
     * @param n
     * @return
     */
    public double sumOfPowers(int k, long n) {
        double dn = n;
        switch (k) {
            case 1:
                return dn * (dn + 1) / 2;
            case 2:
                return dn * (dn + 1) * (2 * dn + 1) / 6;
            case 3:
                return dn * dn * (dn + 1) * (dn + 1) / 4;
            case 4:
                return dn * (dn + 1) * (2 * dn + 1) * (3 * dn * dn + 3 * dn - 1) / 30;
            case 5:
                return dn * dn * (dn + 1) * (dn + 1) * (2 * dn * dn + 2 * dn - 1) / 12;
            case 6: {
                double n2 = dn * dn, n3 = n2 * dn, n4 = n3 * dn;
                return dn * (dn + 1) * (2 * dn + 1) * (3 * n4 + 6 * n3 - 3 * dn + 1) / 42;
            }
            case 7: {
                double n2 = dn * dn, n3 = n2 * dn, n4 = n3 * dn;
                return dn * dn * (dn + 1) * (dn + 1) * (3 * n4 + 6 * n3 - n2 - 4 * dn + 2) / 24;
            }
            case 8: {
                double n2 = dn * dn, n3 = n2 * dn, n4 = n3 * dn, n5 = n4 * dn, n6 = n5 * dn;
                return dn * (dn + 1) * (2 * dn + 1) * (5 * n6 + 15 * n5 + 5 * n4 - 15 * n3 - n2 + 9 * dn - 3) / 90;
            }
            case 9: {
                double n2 = dn * dn, n3 = n2 * dn, n4 = n3 * dn;
                return n2 * (dn + 1) * (dn + 1) * (n2 + dn - 1) * (2 * n4 + 4 * n3 - n2 - 3 * dn + 3) / 20;
            }
            case 10: {
                double n2 = dn * dn, n3 = n2 * dn, n4 = n3 * dn, n5 = n4 * dn, n6 = n5 * dn;
                return dn * (dn + 1) * (2 * dn + 1) * (n2 + dn - 1) * (3 * n6 + 9 * n5 + 2 * n4 - 11 * n3 + 3 * n2 + 10 * dn - 5) / 66;
            }
            default: // should use the Bernoulli formula //
                long s = 1;
                for (int i = 2; i <= dn; ++i) {
                    long c = i;
                    for (int j = 2; j <= k; ++j) {
                        c *= i;
                    }
                    s += c;
                }
                return s;
        }
    }

    /**
     * Multiply 
     * @param a
     * @param b
     * @return
     * @throws MathException 
     */
    public static long mulAndCheck(long a, long b) throws MathException {
        long ret;
        if (a > b) {
            // use symmetry to reduce boundary cases
            ret = mulAndCheck(b, a);
        } else if (a < 0) {
            if (b < 0) {
                // check for positive overflow with negative a, negative b
                if (a >= Long.MAX_VALUE / b) {
                    ret = a * b;
                } else {
                    throw new MathException();
                }
            } else if (b > 0) {
                // check for negative overflow with negative a, positive b
                if (Long.MIN_VALUE / b <= a) {
                    ret = a * b;
                } else {
                    throw new MathException();

                }
            } else {
                // assert b == 0
                ret = 0;
            }
        } else if (a > 0) {
            // assert a > 0
            // assert b > 0

            // check for positive overflow with positive a, positive b
            if (a <= Long.MAX_VALUE / b) {
                ret = a * b;
            } else {
                throw new MathException();
            }
        } else {
            // assert a == 0
            ret = 0;
        }
        return ret;
    }

    public static int mulAndCheck(int x, int y) throws MathException {
        long m = ((long) x) * ((long) y);
        if (m < Integer.MIN_VALUE || m > Integer.MAX_VALUE) {
            throw new MathException();
        }
        return (int) m;
    }

}
