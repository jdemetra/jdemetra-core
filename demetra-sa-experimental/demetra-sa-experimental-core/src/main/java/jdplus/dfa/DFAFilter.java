package jdplus.dfa;

import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

import demetra.data.DoubleSeq;
import jdplus.data.DataBlock;
import jdplus.math.functions.FunctionException;
import jdplus.math.functions.IFunction;
import jdplus.math.functions.IFunctionDerivatives;
import jdplus.math.functions.IFunctionPoint;
import jdplus.math.functions.IParametersDomain;
import jdplus.math.functions.NumericalDerivatives;
import jdplus.math.functions.NumericalIntegration;
import jdplus.math.functions.ParamValidation;
import jdplus.math.functions.bfgs.Bfgs;
import jdplus.math.linearfilters.FiniteFilter;
import jdplus.math.linearfilters.IFiniteFilter;
import jdplus.math.linearfilters.SymmetricFilter;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.decomposition.Gauss;
import jdplus.math.matrices.decomposition.LUDecomposition;

public class DFAFilter {
	public static Builder builder() {
        return new Builder();
    }
	
    @lombok.Value
	private static class Key {
        private int pdegree;
        private int nlags, nleads;
        private double w0, w1;
        private SymmetricFilter target;
        private DoubleUnaryOperator spectralDensity;
    }

    private static final Map<Key, DFAFilter> dictionary = new HashMap<>();

    public static class Builder {

        private int pdegree = 0;
        private int nlags = 6, nleads = 6;
        private double w0 = 0, w1 = Math.PI / 18;
        private SymmetricFilter target;
        private DoubleUnaryOperator spectralDensity;
        
        public Builder polynomialPreservation(int d) {
            this.pdegree = d;
            return this;
        }
        public Builder symetricFilter(SymmetricFilter sf) {
            this.target = sf;
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

        public Builder timelinessLimits(double w0, double w1) {
            this.w0 = w0;
            this.w1 = w1;
            return this;
        }
        public Builder density(DoubleUnaryOperator density) {
            this.spectralDensity = density;
            return this;
        }

        public DFAFilter build() {
            synchronized (dictionary) {
                Key key = new Key(pdegree, nlags, nleads, w0, w1, target, spectralDensity);
                DFAFilter f = dictionary.get(key);
                if (f == null) {
                    f = new DFAFilter(this);
                    dictionary.put(key, f);
                }
                return f;
            }
        }
    }

    private final AccuracyCriterion A = new AccuracyCriterion();
    private final SmoothnessCriterion S = new SmoothnessCriterion();
    private final TimelinessCriterion T = new TimelinessCriterion();
    private final ResidualCriterion R = new ResidualCriterion();
    private final int nlags, nleads, p;
    private final FastMatrix C;
    private final DoubleSeq a;
	//private SymmetricFilter target;

    private DFAFilter(Builder builder) {
        this.nlags = builder.nlags;
        this.nleads = builder.nleads;
        this.p = builder.pdegree + 1;
        //this.target = builder.target;
        int n = nlags + nleads + 1;
        A.symmetricFilter(builder.target)
        	.spectralDensity(builder.spectralDensity)
        	.bounds(0, builder.w1);
        S.symmetricFilter(builder.target)
        	.spectralDensity(builder.spectralDensity)
        	.bounds(builder.w1, Math.PI);
        T.symmetricFilter(builder.target)
        	.spectralDensity(builder.spectralDensity)
        	.bounds(0, builder.w1);
        R.symmetricFilter(builder.target)
        	.spectralDensity(builder.spectralDensity)
        	.bounds(builder.w1, Math.PI);
        C = FastMatrix.make(p, n);
        C.row(0).set(1);
        for (int q = 1; q < p; ++q) {
            final int t = q;
            C.row(q).set(k -> kpow(k - nlags, t));
        }
        double[] q = new double[p];
        q[0] = 1;
        a = DoubleSeq.of(q);
    }
    
    
    
    @lombok.Value
    @lombok.Builder
    public static class Results {

        private FiniteFilter filter;
        private double a, s, t, r, z;
    }
    public Results make(double wa, double ws, double wt) {
        if (wa < 0 || ws < 0 || wt < 0 || wa + ws + wt > 1) {
            throw new IllegalArgumentException();
        }
        return makeNumeric(wa, ws, wt, 1-(wa+ws+wt));
    }
    private Results makeNumeric(double wa, double ws, double wt, double wr) {

        int n = nlags + nleads + 1;
        DFAFunction fn = new DFAFunction(this, wa, wt, wt, wr);
        Bfgs bfgs = Bfgs.builder()
                .functionPrecision(1e-15)
                .absolutePrecision(1e-15)
                .build();
//        DataBlock w0 = DataBlock.make(n-p);
//        w0.set(1.0/n);
//        bfgs.minimize(fn.evaluate(w0));
        DataBlock w0 = DataBlock.make(n);
     	w0.set(1.0/n);
        bfgs.minimize(fn.evaluate(DoubleSeq.of(w0.toArray(), p, n - p)));
        DFAFunction.Point rslt = (DFAFunction.Point) bfgs.getResult();

        return Results.builder()
                .filter(rslt.F)
                .a(rslt.accuracy)
                .s(rslt.smoothness)
                .t(rslt.timeliness)
                .r(rslt.residual)
                .z(rslt.z)
                .build();
    }
    private static double kpow(int k, int d) {
        long z = k;
        for (int i = 1; i < d; ++i) {
            z *= k;
        }
        return z;
    }
    
    public static class AccuracyCriterion {

        private double w0 = 0, w1 = Math.PI / 18;
        private SymmetricFilter sf;
        private DoubleUnaryOperator spectralDensity;

        public double accuracy(IFiniteFilter af) {
            DoubleUnaryOperator fn = x -> {
                double y = sf.gainFunction().applyAsDouble(x) - af.gainFunction().applyAsDouble(x);
                return y * y * spectralDensity.applyAsDouble(x);
            };
            double res = 2 * NumericalIntegration.integrate(fn, w0, w1);
            if (!Double.isFinite(w0)) {
                throw new FunctionException("w0");
            }
            if (!Double.isFinite(w1)) {
                throw new FunctionException("w1");
            }
            if (!Double.isFinite(res)) {
                throw new FunctionException("res");
            }
            return res;
        }


        public AccuracyCriterion bounds(double a, double b) {
            this.w0 = a;
            this.w1 = b;
            return this;
        }
        public AccuracyCriterion lowerBounds(double a) {
            this.w0 = a;
            return this;
        }
        public AccuracyCriterion upperBounds(double b) {
            this.w1 = b;
            return this;
        }
        public AccuracyCriterion symmetricFilter(SymmetricFilter f) {
            this.sf = f;
            return this;
        }
        public AccuracyCriterion spectralDensity(DoubleUnaryOperator d) {
            this.spectralDensity = d;
            return this;
        }

    }
    public static class SmoothnessCriterion {

		private double w0 = 0, w1 = Math.PI;
	    private SymmetricFilter sf;
	    private DoubleUnaryOperator spectralDensity;
        public double smoothness(IFiniteFilter af) {
        	 DoubleUnaryOperator fn = x -> {
                 double y = sf.gainFunction().applyAsDouble(x) - af.gainFunction().applyAsDouble(x);
                 return y * y * spectralDensity.applyAsDouble(x);
             };
             return 2 * NumericalIntegration.integrate(fn, w0, w1);
        }

        public SmoothnessCriterion bounds(double a, double b) {
            this.w0 = a;
            this.w1 = b;
            return this;
        }
        public SmoothnessCriterion lowerBounds(double a) {
            this.w0 = a;
            return this;
        }
        public SmoothnessCriterion upperBounds(double b) {
            this.w1 = b;
            return this;
        }
        public SmoothnessCriterion symmetricFilter(SymmetricFilter f) {
            this.sf = f;
            return this;
        }
        public SmoothnessCriterion spectralDensity(DoubleUnaryOperator d) {
            this.spectralDensity = d;
            return this;
        }

    }
    public static class TimelinessCriterion {
  

        public double timeliness(IFiniteFilter af) {
        	 DoubleUnaryOperator fn = x -> {
                 double g = Math.abs(sf.realFrequencyResponse(x));
                 double ga = af.frequencyResponse(x).abs();
                 double s = Math.sin(af.frequencyResponse(x).arg() / 2);
                 return g * ga * s * s * spectralDensity.applyAsDouble(x);
             };
             return 8 * NumericalIntegration.integrate(fn, w0, w1);
        }
        private double w0 = 0, w1 = Math.PI;
	    private SymmetricFilter sf;
	    private DoubleUnaryOperator spectralDensity;
        public TimelinessCriterion bounds(double a, double b) {
            this.w0 = a;
            this.w1 = b;
            return this;
        }
        public TimelinessCriterion lowerBounds(double a) {
            this.w0 = a;
            return this;
        }
        public TimelinessCriterion upperBounds(double b) {
            this.w1 = b;
            return this;
        }
        public TimelinessCriterion symmetricFilter(SymmetricFilter f) {
            this.sf = f;
            return this;
        }
        public TimelinessCriterion spectralDensity(DoubleUnaryOperator d) {
            this.spectralDensity = d;
            return this;
        }

    }
    public static class ResidualCriterion {

        public double residual(IFiniteFilter af) {
        	DoubleUnaryOperator fn = x -> {
                double g = Math.abs(sf.realFrequencyResponse(x));
                double ga = af.frequencyResponse(x).abs();
                double s = Math.sin(af.frequencyResponse(x).arg() / 2);
                return g * ga * s * s * spectralDensity.applyAsDouble(x);
            };
            return 8 * NumericalIntegration.integrate(fn, w0, w1);
        }

        private double w0 = 0, w1 = Math.PI;
        private SymmetricFilter sf;
        private DoubleUnaryOperator spectralDensity;
        public ResidualCriterion bounds(double a, double b) {
            this.w0 = a;
            this.w1 = b;
            return this;
        }
        public ResidualCriterion lowerBounds(double a) {
            this.w0 = a;
            return this;
        }
        public ResidualCriterion upperBounds(double b) {
            this.w1 = b;
            return this;
        }
        public ResidualCriterion symmetricFilter(SymmetricFilter f) {
            this.sf = f;
            return this;
        }
        public ResidualCriterion spectralDensity(DoubleUnaryOperator d) {
            this.spectralDensity = d;
            return this;
        }

    }
    
    private static class DFAFunction implements IFunction {

        private final DFAFilter core;
        private final double accuracyWeight, smoothnessWeight, timelinessWeight, residualWeight;
        private final LUDecomposition C0;

        /**
         *
         * @param nlags
         * @param nleads
         * @param G Quadratic form
         * @param C FastMatrix of the constraints
         * @param a constraints (same dim as number of rows of C)
         */
        DFAFunction(final DFAFilter core,
        		final double accuracyWeight, final double smoothnessWeight,
        		final double timelinessWeight, final double residualWeight) {
            this.core = core;
            this.accuracyWeight = accuracyWeight;
            this.smoothnessWeight = smoothnessWeight;
            this.timelinessWeight = timelinessWeight;
            this.residualWeight = residualWeight;
            C0 = Gauss.decompose(core.C.extract(0, core.p, 0, core.p));
        }

        @Override
        public Point evaluate(DoubleSeq parameters) {
            return new Point(parameters);
        }

        public class Point implements IFunctionPoint {

            private final DoubleSeq parameters;

            private FiniteFilter F;
            private double accuracy, smoothness, timeliness, residual, z;
            private double maxValue = 10000;

            Point(DoubleSeq parameters) {
                this.parameters = parameters;
            }

            @Override
            public IFunctionDerivatives derivatives() {
                return new NumericalDerivatives(this, true, true);
            }

            @Override
            public IFunction getFunction() {
                return DFAFunction.this;
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
               // DoubleSeq W = DoubleSeq.of(w);
                F = FiniteFilter.ofInternal(w, -core.nlags);
                if (accuracyWeight > 0) {
                	accuracy = core.A.accuracy(F);
                	if (!Double.isFinite(accuracy)) {
                		accuracy = maxValue;
                    }
                    z += accuracyWeight * accuracy;
                }
                if (smoothnessWeight > 0) {
                	smoothness = core.S.smoothness(F);
                	if (!Double.isFinite(smoothness)) {
                		smoothness = maxValue;
                    }
                    z += smoothnessWeight * smoothness;
                }
                if (timelinessWeight > 0) {
                	timeliness = core.T.timeliness(F);
                	if (!Double.isFinite(timeliness)) {
                		timeliness = maxValue;
                    }
                    z += timelinessWeight * timeliness;
                }
                if (residualWeight > 0) {
                	residual = core.R.residual(F);
                	if (!Double.isFinite(residual)) {
                		residual = maxValue;
                    }
                    z += residualWeight * residual;
                }
                
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
