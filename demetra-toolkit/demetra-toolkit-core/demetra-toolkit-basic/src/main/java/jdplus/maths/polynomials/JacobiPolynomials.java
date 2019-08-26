/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.maths.polynomials;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class JacobiPolynomials {

    @lombok.Value
    private static final class Key implements Comparable<Key> {

        private double a, b;

        @Override
        public int compareTo(Key t) {
            if (a < t.a) {
                return -1;
            } else if (a > t.a) {
                return 1;
            } else if (b < t.b) {
                return -1;
            } else if (b > t.b) {
                return 1;
            } else {
                return 0;
            }
        }

        double diff() {
            return a - b;
        }

        double sum() {
            return a + b;
        }

        double c(int n) {
            return n + a + b;
        }
    }

    private final Map<Key, List<double[]>> MAP = new HashMap<>();

    public Polynomial jacobi(int degree, double a, double b) {
        synchronized (MAP) {
            Key key = new Key(a, b);
            List<double[]> p = MAP.get(key);
            if (p == null) {
                p = new ArrayList<>();
                p.add(new double[]{1});
                p.add(new double[]{key.diff() / 2, 1 + key.sum() / 2});
                MAP.put(key, p);
            }
            if (p.size() > degree) {
                return Polynomial.ofInternal(p.get(degree));
            } else {
                for (int k = p.size(); k <= degree; ++k) {
                    Polynomial p1 = Polynomial.ofInternal(p.get(k - 1));
                    double ck = key.c(k), c2k = key.c(2 * k), c2km1 = key.c(2 * k - 1), c2km2 = key.c(2 * k - 2);
                    double p11 = c2k * c2km1 * c2km2, p10 = c2km1 * (a * a - b * b);
                    p1 = p1.times(Polynomial.of(p10, p11));
                    double[] pk = p1.toArray();
                    double q = -2 * (k - 1 + a) * (k - 1 + b) * c2k;
                    double[] pkm2 = p.get(k - 2);
                    double d = 2 * k * ck * c2km2;
                    for (int i = 0; i < pkm2.length; ++i) {
                        pk[i] = (pk[i] + q * pkm2[i]) / d;
                    }
                    for (int i = pkm2.length; i < pk.length; ++i) {
                        pk[i] /= d;
                    }

                    p.add(pk);
                }
            }
            return Polynomial.ofInternal(p.get(degree));
        }
    }

}
