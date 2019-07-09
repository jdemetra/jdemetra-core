/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.filters;

import jdplus.data.DataBlock;
import jdplus.maths.linearfilters.FiniteFilter;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.FastMatrix;
import jdplus.maths.matrices.SubMatrix;
import jdplus.maths.matrices.decomposition.Householder;

/**
 *
 * @author Jean Palate
 */
public class FSTFilterFactory {

    private final int nlags, nleads;

    private final FidelityCriterion F = new FidelityCriterion();
    private final SmoothnessCriterion S;
    private final TimelinessCriterion T;
    private double f, s, t;

    FSTFilterFactory(int nlags, int nleads) {
        this.nlags = nlags;
        this.nleads = nleads;
        S = new SmoothnessCriterion(nlags, nleads);
        T = new TimelinessCriterion(nlags, nleads);
    }

    FiniteFilter make(double a, double b, double c) {
        int n = nlags + nleads + 1;
        int p = 3;
        CanonicalMatrix J = CanonicalMatrix.square(n + p);
        SubMatrix X = J.extract(0, n, 0, n);
        if (a != 0) {
            F.add(a, X);
        }
        if (b != 0) {
            S.add(b, X);
        }
        if (c != 0) {
            T.add(c, X);
        }
        SubMatrix C = J.extract(n, p, 0, n);
        C.row(0).set(1);
        for (int q = 1; q < p; ++q) {
            final int t = q;
            C.row(q).set(k -> kpow(k - nlags, t));
        }
        J.extract(0, n, n, p).copy(C.transpose());
        DataBlock z = DataBlock.make(n + p);
        z.set(n, 1);
        Householder hous = new Householder(false);
        hous.decompose(J);
        hous.solve(z);
        hous.decompose(CanonicalMatrix.of(T.T));
        DataBlock y=z.deepClone();
        hous.solve(y);
        t=y.dot(z)/c;
        return FiniteFilter.ofInternal(z.extract(0, n).toArray(), -nlags);
    }

    private static double kpow(int k, int d) {
        long z = k;
        for (int i = 1; i < d; ++i) {
            z *= k;
        }
        return z;
    }

    /**
     * @return the f
     */
    public double getF() {
        return f;
    }

    /**
     * @return the s
     */
    public double getS() {
        return s;
    }

    /**
     * @return the t
     */
    public double getT() {
        return t;
    }

    public static class FidelityCriterion {

        void add(double weight, FastMatrix X) {
            X.diagonal().add(weight);
        }
    }

    public static class SmoothnessCriterion {

        private final CanonicalMatrix S;
        private double[] W = new double[]{20, -15, 6, -1};

        public SmoothnessCriterion(int nlags, int nleads) {
            int n = nlags + nleads + 1;
            S = CanonicalMatrix.square(n);
            S.diagonal().set(W[0]);
            for (int i = 1; i < W.length; ++i) {
                S.subDiagonal(i).set(W[i]);
                S.subDiagonal(-i).set(W[i]);
            }
        }

        void add(double weight, FastMatrix X) {
            X.addAY(weight, S);
        }
    }

    public static class TimelinessCriterion {

        private final double w0, w1;

        private final CanonicalMatrix T;

        public TimelinessCriterion(int nlags, int nleads) {
            int n = 2 * Math.max(nlags, nleads) + 1;
            int m = nlags + nleads + 1;
            T = CanonicalMatrix.square(m);
            w0 = 0;
            w1 = Math.PI / 3;
            double[] sin1 = new double[n];
            double[] sin0 = new double[n];
            for (int i = 0; i < n; ++i) {
                sin1[i] = Math.sin(i * w1);
            }
            if (w0 != 0) {
                for (int i = 0; i < n; ++i) {
                    sin0[i] = Math.sin(i * w0);
                }
            }
            for (int i = -nlags; i <= nleads; ++i) {
                for (int j = -nlags; j <= nleads; ++j) {
                    int sum = Math.abs(i + j), diff = Math.abs(i - j);
                    if (sum == 0) {
                        if (diff != 0) {
                            double dk = w1 - w0, dl = (sin1[diff] - sin0[diff]) / diff;
                            T.set(i + nlags, j + nlags, .5 * (dl - dk));
                        }
                    } else if (diff == 0) {
                        double dk = (sin1[sum] - sin0[sum]) / sum, dl = w1 - w0;
                        T.set(i + nlags, j + nlags, .5 * (dl - dk));
                    } else {
                        double dk = (sin1[sum] - sin0[sum]) / sum, dl = (sin1[diff] - sin0[diff]) / diff;
                        T.set(i + nlags, j + nlags, .5 * (dl - dk));
                    }
                }
            }
        }

        void add(double weight, FastMatrix X) {
            X.addAY(weight, T);
        }
    }

}
