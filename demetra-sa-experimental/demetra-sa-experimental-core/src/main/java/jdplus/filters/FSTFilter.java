/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.filters;

import jdplus.data.DataBlock;
import jdplus.maths.linearfilters.FiniteFilter;
import jdplus.maths.linearfilters.SymmetricFilter;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.FastMatrix;
import jdplus.maths.matrices.QuadraticForm;
import jdplus.maths.matrices.SubMatrix;
import jdplus.maths.matrices.decomposition.Householder;
import jdplus.maths.polynomials.Polynomial;
import jdplus.maths.polynomials.UnitRoots;

/**
 *
 * @author Jean Palate
 */
public class FSTFilter {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private int pdegree = 2;
        private int sdegree = 3;
        private int nlags = 6, nleads = 6;
        private double w0 = 0, w1 = Math.PI / 18;
        private boolean antialias = true;

        public Builder degreeOfSmoothness(int d) {
            this.sdegree = d;
            return this;
        }

        public Builder polynomialPreservation(int d) {
            this.pdegree = d;
            return this;
        }

        public Builder nlags(int nlags) {
            this.nlags = nlags;
            return this;
        }

        public Builder nleads(int nleads) {
            this.nleads = nleads;
            return this;
        }

        public Builder timelinessAntialiasCriterion(boolean b) {
            this.antialias = b;
            return this;
        }

        public Builder timelinessLimits(double w0, double w1) {
            this.w0 = w0;
            this.w1 = w1;
            return this;
        }

        public FSTFilter build() {
            return new FSTFilter(this);
        }
    }

    private final FidelityCriterion F = new FidelityCriterion();
    private final SmoothnessCriterion S = new SmoothnessCriterion();
    private final TimelinessCriterion T = new TimelinessCriterion();
    private final int nlags, nleads, p;

    private FSTFilter(Builder builder) {
        this.nlags = builder.nlags;
        this.nleads = builder.nleads;
        this.p = builder.pdegree + 1;
        S.degree(builder.sdegree);
        T.antialias(builder.antialias)
                .bounds(builder.w0, builder.w1);
    }

    private Results makeQuadratic(double wf, double ws, double wt) {
        Results.Builder builder = Results.builder();
        int n = nlags + nleads + 1;
        CanonicalMatrix J = CanonicalMatrix.square(n + p);
        CanonicalMatrix SM = S.buildMatrix(nlags, nleads), TM = T.buildMatrix(nlags, nleads);
        SubMatrix C = J.extract(n, p, 0, n);
        C.row(0).set(1);
        for (int q = 1; q < p; ++q) {
            final int t = q;
            C.row(q).set(k -> kpow(k - nlags, t));
        }
        J.extract(0, n, n, p).copy(C.transpose());

        SubMatrix X = J.extract(0, n, 0, n);
        X.set(0);
        if (wf != 0) {
            X.diagonal().add(wf);
        }
        if (ws != 0) {
            X.addAY(ws, SM);
        }
        if (wt != 0) {
            X.addAY(wt, TM);
        }
        DataBlock z = DataBlock.make(n + p);
        z.set(n, 1);
        Householder hous = new Householder(false);
        hous.decompose(J);
        hous.solve(z);
        DataBlock w = z.extract(0, n);
        builder.filter(FiniteFilter.ofInternal(w.toArray(), -nlags));

        if (wf > 0) {
            builder.f(w.ssq() * wf);
        }
        if (ws > 0) {
            builder.s(QuadraticForm.apply(SM, w) * ws);
        }
        if (wt > 0 && TM != null) {
            builder.t(QuadraticForm.apply(TM, w) * wt);
        }
        return builder.build();
    }

    private Results makeNumeric(double d, double ws, double wt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @lombok.Value
    @lombok.Builder(builderClassName = "Builder")
    public static class Results {

        private FiniteFilter filter;
        private double f, s, t;
    }

    public Results make(double ws, double wt) {
        if (ws < 0 || wt < 0 || ws + wt > 1) {
            throw new IllegalArgumentException();
        }
        if (wt == 0 || T.antialias) {
            return makeQuadratic(1 - ws - wt, ws, wt);
        } else {
            return makeNumeric(1 - ws - wt, ws, wt);
        }
    }

    private static double kpow(int k, int d) {
        long z = k;
        for (int i = 1; i < d; ++i) {
            z *= k;
        }
        return z;
    }

    public static class FidelityCriterion {

        void add(double weight, FastMatrix X) {
            X.diagonal().add(weight);
        }
    }

    public static class SmoothnessCriterion {

        private static final double[] W1 = new double[]{2, -1};
        private static final double[] W2 = new double[]{6, -4, 1};
        private static final double[] W3 = new double[]{20, -15, 6, -1};
        private int degree = 3;

        public SmoothnessCriterion() {
        }

        public SmoothnessCriterion degree(int degree) {
            this.degree = degree;
            return this;
        }

        public CanonicalMatrix buildMatrix(int nleads, int nlags) {
            int n = nlags + nleads + 1;
            if (2 * degree >= n) {
                throw new IllegalArgumentException();
            }
            CanonicalMatrix S = CanonicalMatrix.square(n);
            double[] W = weights(degree);
            S.diagonal().set(W[0]);
            for (int i = 1; i < W.length; ++i) {
                S.subDiagonal(i).set(W[i]);
                S.subDiagonal(-i).set(W[i]);
            }
            return S;
        }

        public static double[] weights(int degree) {
            switch (degree) {
                case 1:
                    return W1;
                case 2:
                    return W2;
                case 3:
                    return W3;
                default:
                    Polynomial D = UnitRoots.D(degree);
                    SymmetricFilter S = SymmetricFilter.convolutionOf(D, 1);
                    return S.coefficientsAsPolynomial().toArray();
            }
        }
    }

    public static class TimelinessCriterion {

        private double w0 = 0, w1 = 2 * Math.PI / 36;
        private boolean antialias = false;

        public TimelinessCriterion() {
        }

        public TimelinessCriterion bounds(double a, double b) {
            this.w0 = a;
            this.w1 = b;
            return this;
        }

        public TimelinessCriterion antialias(boolean antialias) {
            this.antialias = antialias;
            return this;
        }

        public CanonicalMatrix buildMatrix(int nlags, int nleads) {
            int n = 2 * Math.max(nlags, nleads) + 1;
            int m = nlags + nleads + 1;
            CanonicalMatrix T = CanonicalMatrix.square(m);
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
            return T;
        }
    }

}
