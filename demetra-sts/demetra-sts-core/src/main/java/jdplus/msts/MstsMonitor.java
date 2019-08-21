/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.msts;

import jdplus.maths.functions.bfgs.Bfgs;
import jdplus.maths.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.maths.functions.minpack.MinPackMinimizer;
import jdplus.maths.functions.riso.LbfgsMinimizer;
import jdplus.ssf.likelihood.MarginalLikelihoodFunction;
import jdplus.ssf.dk.SsfFunction;
import jdplus.ssf.implementations.MultivariateCompositeSsf;
import jdplus.ssf.multivariate.M2uAdapter;
import jdplus.ssf.multivariate.SsfMatrix;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import demetra.data.DoubleSeq;
import demetra.likelihood.Likelihood;
import demetra.maths.Optimizer;
import demetra.ssf.LikelihoodType;
import jdplus.maths.matrices.FastMatrix;
import jdplus.likelihood.LikelihoodFunction;
import jdplus.likelihood.LikelihoodFunctionPoint;
import jdplus.maths.functions.FunctionMinimizer;
import jdplus.maths.functions.ssq.SsqFunctionMinimizer;
import jdplus.ssf.likelihood.DiffuseLikelihoodFunction;

/**
 *
 * @author palatej
 */
public class MstsMonitor {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private static final int MAXITER = 20, MAXITER_MIN = 500;
        private static final double SMALL_VAR = 1e-8;

        private LikelihoodType likelihood = LikelihoodType.Diffuse;
        private Optimizer optimizer = Optimizer.LevenbergMarquardt;
        private double precision = 1e-9, smallVar = SMALL_VAR, precision2 = 1e-7, precision3 = 1e-3;
        private int maxIter = MAXITER;
        private int maxIterOptimzer = MAXITER_MIN;
        private boolean concentratedLikelihood = true;

        public Builder likelihood(LikelihoodType lt) {
            this.likelihood = lt;
            return this;
        }

        public Builder concentratedLikelihood(boolean cl) {
            this.concentratedLikelihood = cl;
            if (!cl) {
                optimizer = Optimizer.BFGS;
            }
            return this;
        }

        public Builder precision(double eps) {
            this.precision = eps;
            return this;
        }

        public Builder smallVariance(double var) {
            if (var <= 0) {
                throw new IllegalArgumentException();
            }
            this.smallVar = var;
            return this;
        }

        public Builder maxIter(int maxiter) {
            this.maxIter = maxiter;
            return this;
        }

        public Builder maxIterOptimizer(int maxiter) {
            this.maxIterOptimzer = maxiter;
            return this;
        }

        public Builder optimizer(Optimizer optimizer) {
            this.optimizer = optimizer;
            return this;
        }

        public MstsMonitor build() {
            return new MstsMonitor(this);
        }
    }
    private static final int NITER = 8;
    private final boolean concentratedLikelihood;
    private final double precision, precision2, precision3, smallStde;
    private final int maxIter;
    private final int maxIterOptimzer;
    private final LikelihoodType likelihood;
    private final Optimizer optimizer;

    private FastMatrix data;
    private MstsMapping model;
    private MultivariateCompositeSsf ssf;
    private DoubleSeq fullp;
    private Likelihood ll;
    private VarianceInterpreter fixedVariance;

    private final List<VarianceInterpreter> smallVariances = new ArrayList<>();
//    private final List<LoadingParameter> smallLoadings = new ArrayList<>();

    private MstsMonitor(Builder builder) {
        this.likelihood = builder.likelihood;
        this.optimizer = builder.optimizer;
        this.maxIterOptimzer = builder.maxIterOptimzer;
        this.precision = builder.precision;
        this.precision2 = builder.precision2;
        this.precision3 = builder.precision3;
        this.concentratedLikelihood = builder.concentratedLikelihood;
        this.maxIter = builder.maxIter;
        this.smallStde = Math.sqrt(builder.smallVar);
    }

    private LikelihoodFunction function(boolean concentrated) {
        SsfMatrix s = new SsfMatrix(data);
        switch (likelihood) {
            case Marginal:
                return MarginalLikelihoodFunction.builder(M2uAdapter.of(s), model, m -> M2uAdapter.of(m))
                        .useParallelProcessing(true)
                        .useMaximumLikelihood(true)
                        .useScalingFactor(concentrated)
                        .build();
            case Augmented:
                return DiffuseLikelihoodFunction.builder(M2uAdapter.of(s), model, m -> M2uAdapter.of(m))
                        .useMaximumLikelihood(true)
                        .useScalingFactor(concentrated)
                        .useFastAlgorithm(true)
                        .useParallelProcessing(true)
                        .residuals(concentrated)
                        .build();
            default:
                return SsfFunction.builder(M2uAdapter.of(s), model, m -> M2uAdapter.of(m))
                        .useParallelProcessing(true)
                        .useMaximumLikelihood(true)
                        .useScalingFactor(concentrated)
                        .useFastAlgorithm(true)
                        .build();
        }
    }

    private boolean needFixedVariance() {
        if (fixedVariance != null) {
            return true;
        }
        if (!concentratedLikelihood || !model.isScalable()) {
            return false;
        }
        // No fixed variance
        return model.parameters()
                .filter(p -> p.isFixed()
                && p instanceof VarianceInterpreter
                && ((VarianceInterpreter) p).stde() > 0)
                .count() == 0;
    }

    public void process(FastMatrix data, MstsMapping model, DoubleSeq fullInitial) {
        fixedVariance = null;
        this.data = data;
        this.model = model;
        fullp = fullInitial;
        if (fullp == null) {
            fullp = model.modelParameters(model.getDefaultParameters());
        }
        int fniter = 30;
        if (needFixedVariance()) {
            DoubleSeq p = model.functionParameters(fullp);
            LikelihoodFunction fn = function(false);
            LikelihoodFunctionPoint rslt = min(fn, false, precision3, 30, p);
            ll = rslt.getLikelihood();
            p = rslt.getParameters();
            double[] mp = model.modelParameters(p).toArray();
            fixedVariance = model.fixMaxVariance(mp, 1);
            fullp = DoubleSeq.of(mp);
        }

        double curll = 0;
        for (int k = 0; k < NITER; ++k) {
            int niter = 0;
            do {
                DoubleSeq p = model.functionParameters(fullp);
                LikelihoodFunction fn = function(concentratedLikelihood);
                LikelihoodFunctionPoint rslt = min(fn, concentratedLikelihood, curll == 0 ? precision3 : precision2, fniter, p);
                ll = rslt.getLikelihood();
                p = rslt.getParameters();
                fullp = model.modelParameters(p);
                if (!fixSmallVariance(1e-6) && !freeSmallVariance()) {
                    break;
                }
            } while (niter++ < maxIter);

            if (needFixedVariance()) {
                VarianceInterpreter old = fixedVariance;
                double[] mp = fullp.toArray();
                fixedVariance = model.fixMaxVariance(mp, 1);
                if (old != fixedVariance) {
                    old.free();
                    // ?
                    for (VarianceInterpreter vp : smallVariances) {
                        vp.freeStde(smallStde);
                    }
                    smallVariances.clear();
                    fullp = DoubleSeq.of(mp);
                    DoubleSeq p = model.functionParameters(fullp);
                    ll = function(concentratedLikelihood).evaluate(p).getLikelihood();
                }
            }
            // Fix all variances and loadings
            List<ParameterInterpreter> fixedBlocks = model.parameters()
                    .filter(p -> !p.isFixed() && p.isScaleSensitive(true))
                    .collect(Collectors.toList());

            model.fixModelParameters(p -> p.isScaleSensitive(true), fullp);
            if (model.parameters().filter(p -> !p.isFixed()).count() > 0) {
                LikelihoodFunction fn = function(concentratedLikelihood);
                DoubleSeq curp = model.functionParameters(fullp);
                LikelihoodFunctionPoint rslt = min(fn, concentratedLikelihood, curll == 0 ? precision3 : precision2, fniter, curp);
                ll = rslt.getLikelihood();
                DoubleSeq np = rslt.getParameters();
                fullp = model.modelParameters(np);
            }
            // Free the fixed parameters
            for (ParameterInterpreter p : fixedBlocks) {
                p.free();
            }

            if (curll != 0 && Math.abs(curll - ll.logLikelihood()) < precision2) {
                break;
            } else {
                curll = ll.logLikelihood();
            }
        }
        DoubleSeq p = model.functionParameters(fullp);
        // Final estimation. To do anyway
        LikelihoodFunction fn = function(concentratedLikelihood);
        LikelihoodFunctionPoint rslt = min(fn, concentratedLikelihood, precision, this.maxIterOptimzer, p);
        ll = rslt.getLikelihood();
        p = rslt.getParameters();
        fullp = model.modelParameters(p);
        ssf = model.map(p);
    }

    private LikelihoodFunctionPoint min(LikelihoodFunction fn, boolean concentrated, double eps, int niter, DoubleSeq start) {
        if (fn.getDomain().getDim() == 0) {
            return fn.evaluate(start);
        }
        Optimizer cur = optimizer;
        if (!concentrated && (cur == Optimizer.LevenbergMarquardt || cur == Optimizer.MinPack)) {
            cur = Optimizer.BFGS;
        }

        switch (cur) {
            case LBFGS: {
                FunctionMinimizer m = LbfgsMinimizer
                        .builder()
                        .functionPrecision(eps)
                        .maxIter(niter)
                        .build();
                m.minimize(fn.evaluate(start));
                return (LikelihoodFunctionPoint) m.getResult();
            }
            case BFGS: {
                FunctionMinimizer m = Bfgs
                        .builder()
                        .functionPrecision(eps)
                        .maxIter(niter)
                        .build();
                m.minimize(fn.evaluate(start));
                return (LikelihoodFunctionPoint) m.getResult();
            }
            case MinPack: {
                SsqFunctionMinimizer lm = MinPackMinimizer.builder()
                        .functionPrecision(eps)
                        .maxIter(niter)
                        .build();
                lm.minimize(fn.evaluate(start));
                return (LikelihoodFunctionPoint) lm.getResult();
            }
            default:
                SsqFunctionMinimizer lm = LevenbergMarquardtMinimizer.builder()
                        .functionPrecision(eps)
                        .maxIter(niter)
                        .build();
                lm.minimize(fn.evaluate(start));
                return (LikelihoodFunctionPoint) lm.getResult();

        }
    }

    private boolean freeSmallVariance() {
        if (smallVariances.isEmpty()) {
            return false;
        }
        double dll = 0;
        VarianceInterpreter cur = null;
        for (VarianceInterpreter small : smallVariances) {
            small.fixStde(smallStde);
            try {
                DoubleSeq nprslts = model.functionParameters(fullp);
                Likelihood nll = function(concentratedLikelihood).evaluate(nprslts).getLikelihood();
                double d = nll.logLikelihood() - ll.logLikelihood();
                if (d > dll) {
                    dll = d;
                    cur = small;
                }
            } catch (Exception err) {
            }
            small.fixStde(0);
        }
        if (cur != null) {
            cur.freeStde(smallStde);
            smallVariances.remove(cur);
            return true;
        } else {
            return false;
        }

    }

    private Predicate<ParameterInterpreter> var() {
        return p -> !p.isFixed() && p instanceof VarianceInterpreter;
    }

    private boolean fixSmallVariance(double eps) {
//        List<VarianceParameter> svar = model.parameters()
//                .filter(var())
//                .map(p -> (VarianceParameter) p)
//                .collect(Collectors.toList());
        List<VarianceInterpreter> svar = model.smallVariances(fullp, eps);
        if (svar.isEmpty()) {
            return false;
        }
        double dll = 0;
        VarianceInterpreter cur = null;
        for (VarianceInterpreter small : svar) {
            double olde = small.fixStde(0);
            try {
                DoubleSeq p = model.functionParameters(fullp);
                Likelihood nll = function(concentratedLikelihood).evaluate(p).getLikelihood();
                double ndll = nll.logLikelihood() - ll.logLikelihood();
                if (ndll > dll) {
                    dll = ndll;
                    cur = small;
                }
            } catch (Exception err) {
            }
            small.freeStde(olde);
        }
        if (cur != null) {
            cur.fixStde(0);
            smallVariances.add(cur);
            return true;
        } else {
            return false;
        }

    }

    /**
     * @return the data
     */
    public FastMatrix getData() {
        return data;
    }

    /**
     * @return the model
     */
    public MstsMapping getModel() {
        return model;
    }

    /**
     * @return the ssf
     */
    public MultivariateCompositeSsf getSsf() {
        return ssf;
    }

    /**
     * @return the prslts
     */
    public DoubleSeq getParameters() {
        return model.functionParameters(fullp);
    }

    /**
     * @return the ll
     */
    public Likelihood getLikelihood() {
        return ll;
    }

    public DoubleSeq fullParameters() {
        return fullp;
    }
}
