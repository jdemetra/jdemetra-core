/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.filters;

import demetra.data.DoubleSeq;
import demetra.math.Complex;
import internal.jdplus.maths.functions.gsl.integration.QAGS;
import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;
import jdplus.data.DataBlock;
import jdplus.linearsystem.LinearSystemSolver;
import jdplus.math.functions.IFunction;
import jdplus.math.functions.IFunctionDerivatives;
import jdplus.math.functions.IFunctionPoint;
import jdplus.math.functions.IParametersDomain;
import jdplus.math.functions.NumericalDerivatives;
import jdplus.math.functions.ParamValidation;
import jdplus.math.functions.bfgs.Bfgs;
import jdplus.math.linearfilters.FiniteFilter;
import jdplus.math.linearfilters.IFiniteFilter;
import jdplus.math.linearfilters.SymmetricFilter;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.QuadraticForm;
import jdplus.math.matrices.decomposition.Gauss;
import jdplus.math.matrices.decomposition.Householder;
import jdplus.math.matrices.decomposition.LUDecomposition;
import jdplus.math.polynomials.Polynomial;
import jdplus.math.polynomials.UnitRoots;

/**
 *
 * @author Jean Palate
 */
public class FSTFilter {

    public static Builder builder() {
        return new Builder();
    }

    @lombok.Value
    private static class Key {

        private int pdegree;
        private int sdegree;
        private int nlags, nleads;
        private double w0, w1;
        private boolean antiphase;
    }

    private static final Map<Key, FSTFilter> dictionary = new HashMap<>();

    public static class Builder {

        private int pdegree = 2;
        private int sdegree = 3;
        private int nlags = 6, nleads = 6;
        private double w0 = 0, w1 = Math.PI / 18;
        private boolean antiphase = true;

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

        public Builder timelinessAntiphaseCriterion(boolean b) {
            this.antiphase = b;
            return this;
        }

        public Builder timelinessLimits(double w0, double w1) {
            this.w0 = w0;
            this.w1 = w1;
            return this;
        }

        public FSTFilter build() {
            synchronized (dictionary) {
                Key key = new Key(pdegree, sdegree, nlags, nleads,
                        w0, w1, antiphase);
                FSTFilter f = dictionary.get(key);
                if (f == null) {
                    f = new FSTFilter(this);
                    dictionary.put(key, f);
                }
                return f;
            }
        }
    }

    private final FidelityCriterion F = new FidelityCriterion();
    private final SmoothnessCriterion S = new SmoothnessCriterion();
    private final TimelinessCriterion T = new TimelinessCriterion();
    private final int nlags, nleads, p;
    private final Matrix C, SM;
    private final DoubleSeq a;

    private FSTFilter(Builder builder) {
        this.nlags = builder.nlags;
        this.nleads = builder.nleads;
        int n = nlags + nleads + 1;
        this.p = builder.pdegree + 1;
        T.antiphase(builder.antiphase)
                .bounds(builder.w0, builder.w1);
        C = Matrix.make(p, n);
        C.row(0).set(1);
        for (int q = 1; q < p; ++q) {
            final int t = q;
            C.row(q).set(k -> kpow(k - nlags, t));
        }
        SM = S.buildMatrix(builder.sdegree, nleads, nlags);
        double[] q = new double[p];
        q[0] = 1;
        a = DoubleSeq.of(q);
    }

    private Results makeQuadratic(double wf, double ws, double wt) {
        int n = nlags + nleads + 1;
        Matrix J = Matrix.square(n + p);
        J.extract(n, p, 0, n).copy(C);
        J.extract(0, n, n, p).copyTranspose(C);

        Matrix X = J.extract(0, n, 0, n);
        if (wf != 0) {
            X.diagonal().add(wf);
        }
        if (ws != 0) {
            X.addAY(ws, SM);
        }
        Matrix TM = null;
        if (wt != 0 && nlags != nleads) {
            TM = T.buildMatrix(nlags, nleads);
            X.addAY(wt, TM);
        }
        DataBlock z = DataBlock.make(n + p);
        z.extract(n, p).copy(a);
        LinearSystemSolver.robustSolver().solve(J, z);
        DataBlock w = z.extract(0, n);
        Results.Builder builder = Results.builder();
        builder.filter(FiniteFilter.ofInternal(w.toArray(), -nlags));

        double q = 0;
        if (wf > 0) {
            double f = w.ssq();
            q += wf * f;
            builder.f(f);
        }
        if (ws > 0) {
            double s = QuadraticForm.apply(SM, w);
            q += ws * s;
            builder.s(s);
        }
        if (TM != null) {
            double t = QuadraticForm.apply(TM, w);
            q += wt * t;
            builder.t(t);
        }
        return builder
                .z(q)
                .build();
    }

    private Results makeNumeric(double wf, double ws, double wt) {

        int n = nlags + nleads + 1;
        FSTFunction fn = new FSTFunction(this, ws, wt);
        Bfgs bfgs = Bfgs.builder()
                .functionPrecision(1e-15)
                .absolutePrecision(1e-15)
                .build();
//        DataBlock w0 = DataBlock.make(n-p);
//        w0.set(1.0/n);
//        bfgs.minimize(fn.evaluate(w0));
        double[] w0 = makeQuadratic(wf, ws, wt).filter.weightsToArray();
        bfgs.minimize(fn.evaluate(DoubleSeq.of(w0, p, n - p)));
        FSTFunction.Point rslt = (FSTFunction.Point) bfgs.getResult();

        return Results.builder()
                .filter(rslt.F)
                .f(rslt.f)
                .s(rslt.s)
                .t(rslt.t)
                .z(rslt.z)
                .build();
    }

    @lombok.Value
    @lombok.Builder(builderClassName = "Builder")
    public static class Results {

        private FiniteFilter filter;
        private double f, s, t, z;
    }

    public Results make(double ws, double wt) {
        if (ws < 0 || wt < 0 || ws + wt > 1) {
            throw new IllegalArgumentException();
        }
        if (wt == 0 || T.antiphase || nleads == nlags) {
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

        public static double fidelity(IFiniteFilter f) {
            return DoubleSeq.of(f.weightsToArray()).ssq();
        }

        void add(double weight, Matrix X) {
            X.diagonal().add(weight);
        }
    }

    public static class SmoothnessCriterion {

        public static double smoothness(IFiniteFilter f) {
            DataBlock w = DataBlock.of(f.weightsToArray());
            Matrix M = buildMatrix(3, f.getUpperBound(), -f.getLowerBound());
            return QuadraticForm.ofSymmetric(M).apply(w);
        }

        private static final double[] W1 = new double[]{2, -1};
        private static final double[] W2 = new double[]{6, -4, 1};
        private static final double[] W3 = new double[]{20, -15, 6, -1};

        public SmoothnessCriterion() {
        }

        public static Matrix buildMatrix(int deg, int nleads, int nlags) {
            int n = nlags + nleads + 1;
            if (2 * deg >= n) {
                throw new IllegalArgumentException();
            }
            Matrix S = Matrix.square(n);
            double[] W = weights(deg);
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
        
        public static double timeliness(IFiniteFilter f, double bandpass){
            TimelinessCriterion c=new TimelinessCriterion().antiphase(true).bounds(0, bandpass);
            Matrix M = c.buildMatrix(-f.getLowerBound(), f.getUpperBound());
            DataBlock w = DataBlock.of(f.weightsToArray());
             return QuadraticForm.ofSymmetric(M).apply(w);
        }

        private double w0 = 0, w1 = Math.PI / 18;
        private boolean antiphase = false;

        public TimelinessCriterion() {
        }

        public TimelinessCriterion bounds(double a, double b) {
            this.w0 = a;
            this.w1 = b;
            return this;
        }

        public TimelinessCriterion antiphase(boolean antiphase) {
            this.antiphase = antiphase;
            return this;
        }

        public Matrix buildMatrix(int nlags, int nleads) {
            int n = 2 * Math.max(nlags, nleads) + 1;
            int m = nlags + nleads + 1;
            Matrix T = Matrix.square(m);
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

    private static class FSTFunction implements IFunction {

        private final FSTFilter core;
        private final double ws, wt;
        private final LUDecomposition C0;

        /**
         *
         * @param nlags
         * @param nleads
         * @param G Quadratic form
         * @param C Matrix of the constraints
         * @param a constraints (same dim as number of rows of C)
         */
        FSTFunction(final FSTFilter core, final double ws, final double wt) {
            this.core = core;
            this.ws = ws;
            this.wt = wt;
            C0 = Gauss.decompose(core.C.extract(0, core.p, 0, core.p));
        }

        @Override
        public Point evaluate(DoubleSeq parameters) {
            return new Point(parameters);
        }

        public class Point implements IFunctionPoint {

            private final DoubleSeq parameters;

            private FiniteFilter F;
            private double f, s, t, z;

            Point(DoubleSeq parameters) {
                this.parameters = parameters;
            }

            @Override
            public IFunctionDerivatives derivatives() {
                return new NumericalDerivatives(this, true, true);
            }

            @Override
            public IFunction getFunction() {
                return FSTFunction.this;
            }

            @Override
            public DoubleSeq getParameters() {
                return parameters;
            }

            @Override
            public double getValue() {
                // Step 1. Create the full set of weights
                int n = core.nlags + core.nleads + 1;
                int nc = core.p;
                double[] w = new double[n];
                parameters.copyTo(w, nc);
                for (int i = 0; i < nc; ++i) {
                    w[i] = core.a.get(i) - core.C.row(i).drop(nc, 0).dot(parameters);
                }
                DataBlock w0 = DataBlock.of(w, 0, nc);
                C0.solve(w0);
                // Actual computation
                z = 0;
                DoubleSeq W = DoubleSeq.of(w);
                double wf = 1 - ws - wt;
                if (wf > 0) {
                    f = W.ssq();
                    z += wf * f;
                }
                if (ws > 0) {
                    s = QuadraticForm.of(core.SM).apply(W);
                    z += ws * s;
                }
                F = FiniteFilter.ofInternal(w, -core.nlags);
                DoubleUnaryOperator fn = x -> {
                    Complex fr = F.frequencyResponse(x);
                    return fr.getIm() * fr.getIm() / (fr.abs() + fr.getRe());
                };
                QAGS qags = QAGS.builder()
                        .absoluteTolerance(1e-15)
                        .build();
                qags.integrate(fn, core.T.w0, core.T.w1);
                t = qags.getResult();
                z += wt * t;
                return z;
            }
        }

        @Override
        public IParametersDomain getDomain() {
            return new IParametersDomain() {
                @Override
                public boolean checkBoundaries(DoubleSeq inparams) {
                    for (int i = 0; i < inparams.length(); ++i) {
                        double v = inparams.get(i);
                        if (Math.abs(v) > 1) {
                            return false;
                        }
                    }
                    return true;
                }

                @Override
                public double epsilon(DoubleSeq inparams, int idx) {
                    return 1e-9;
                }

                @Override
                public int getDim() {
                    return core.C.getColumnsCount() - core.p;
                }

                @Override
                public double lbound(int idx) {
                    return -1;
                }

                @Override
                public double ubound(int idx) {
                    return 1;
                }

                @Override
                public ParamValidation validate(DataBlock ioparams) {
                    boolean changed = false;
                    for (int i = 0; i < ioparams.length(); ++i) {
                        double v = ioparams.get(i);
                        if (Math.abs(v) > 1) {
                            ioparams.set(i, 1 / v);
                            changed = true;
                        }
                    }
                    return changed ? ParamValidation.Changed : ParamValidation.Valid;
                }
            };
        }

    }

}
