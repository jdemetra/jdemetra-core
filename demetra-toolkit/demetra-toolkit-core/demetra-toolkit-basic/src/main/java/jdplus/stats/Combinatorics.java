/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.stats;

import jdplus.math.Arithmetics;

/**
 *
 * @author Jean Palate
 */
public class Combinatorics {

    public static long binomialCoefficient(final int n, final int k) {
        if (k > n || n <= 0 || k < 0) {
            throw new IllegalArgumentException();
        }
        if ((n == k) || (k == 0)) {
            return 1;
        }
        if ((k == 1) || (k == n - 1)) {
            return n;
        }
        if (k > n / 2) {
            return binomialCoefficient(n, n - k);
        }

        long result = 1;
        if (n <= 61) {
            int i = n - k + 1;
            for (int j = 1; j <= k; j++) {
                result = result * i / j;
                i++;
            }
        } else if (n <= 66) {
            int i = n - k + 1;
            for (int j = 1; j <= k; j++) {
                final long d = Arithmetics.gcd(i, j);
                result = (result / (j / d)) * (i / d);
                i++;
            }
        } else {
            int i = n - k + 1;
            for (int j = 1; j <= k; j++) {
                final long d = Arithmetics.gcd(i, j);
                result = Arithmetics.mulAndCheck(result / (j / d), i / d);
                i++;
            }
        }
        return result;
    }

}
