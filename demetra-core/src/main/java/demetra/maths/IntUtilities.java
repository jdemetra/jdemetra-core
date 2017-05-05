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

/**
 * Uitlities on integer numbers
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public final class IntUtilities {

    /**
     * 
     * @param n
     * @return
     */
    public static int[] divisors(final int n) {
	int[] tmp = new int[1 + n / 2];
	int nd = divisors(n, tmp);
	int[] rslt = new int[nd];
	for (int i = 0; i < nd; ++i)
	    rslt[i] = tmp[i];
	return rslt;
    }

    /**
     * 
     * @param n
     * @param buffer
     * @return
     */
    public static int divisors(final int n, final int[] buffer) {
	if (n == 1)
	    return 0;
	int d = 1;
	int idx = 0;
	while (d * 2 <= n) {
	    if (n % d == 0)
		buffer[idx++] = d;
	    ++d;
	}
	return idx;
    }

//    /**
//     * 
//     * @param a
//     * @param b
//     * @return
//     */
//    public static int PGCD(final int a, final int b) {
//	return (b == 0) ? a : PGCD(b, a % b);
//    }
//
//    /**
//     * 
//     * @param a
//     * @param b
//     * @return
//     */
//    public static int PPCM(int a, int b)
//    {
//	int r = 1;
//	int div = 2;
//	while ((a != 1) || (b != 1)) {
//	    boolean ok = false;
//	    if (a % div == 0) {
//		a /= div;
//		ok = true;
//	    }
//	    if (b % div == 0) {
//		b /= div;
//		ok = true;
//	    }
//	    if (ok)
//		r *= div;
//	    if (!ok)
//		++div;
//	}
//	return r;
//    }

    /**
     * Computes the greatest common divisor of two integers.
     *
     * @param a
     * @param b
     * @return
     */
    public static int gcd(int a, int b) {
        while (b > 0) {
            int temp = b;
            b = a % b; // % is remainder  
            a = temp;
        }
        return a;
    }

    /**
     * Computes the least common multiple of two integers.
     *
     * @param a
     * @param b
     * @return
     */
    public static int lcm(int a, int b) {
        return a * (b / gcd(a, b));
    }
    
    private IntUtilities() {
    }
}
