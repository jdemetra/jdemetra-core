/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.msts;

import demetra.data.DoubleSeq;
import demetra.likelihood.ILikelihood;
import demetra.likelihood.ILikelihoodFunction;
import demetra.likelihood.ILikelihoodFunctionPoint;
import demetra.maths.functions.IFunctionMinimizer;
import demetra.maths.functions.bfgs.Bfgs;
import demetra.maths.functions.levmar.LevenbergMarquardtMinimizer;
import demetra.maths.functions.minpack.MinPackMinimizer;
import demetra.maths.functions.riso.LbfgsMinimizer;
import demetra.maths.functions.ssq.ISsqFunctionMinimizer;
import demetra.maths.matrices.Matrix;
import demetra.ssf.likelihood.MarginalLikelihoodFunction;
import demetra.ssf.dk.SsfFunction;
import demetra.ssf.implementations.MultivariateCompositeSsf;
import demetra.ssf.multivariate.M2uAdapter;
import demetra.ssf.multivariate.SsfMatrix;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

        private boolean marginalLikelihood;
        private double precision = 1e-9, smallVar = SMALL_VAR, precision2 = 1e-7, precision3 = 1e-3;
        private int maxIter = MAXITER;
        private int maxIterOptimzer = MAXITER_MIN;
        private boolean bfgs;
        private boolean lbfgs;
        private boolean minpack;
        private boolean concentratedLikelihood = true;

        public Builder marginalLikelihood(boolean ml) {
            this.marginalLikelihood = ml;
            return this;
        }

        public Builder concentratedLikelihood(boolean cl) {
            this.concentratedLikelihood = cl;
            if (!cl) {
                bfgs();
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

        public Builder bfgs() {
            this.bfgs = true;
            this.lbfgs=false;
            this.minpack = false;
            return this;
        }

        public Builder lbfgs() {
            this.bfgs = false;
            this.lbfgs = true;
            this.minpack = false;
            return this;
        }

        public Builder minpack() {
            this.bfgs = false;
            this.lbfgs = false;
            this.minpack = true;
            return this;
        }

        public Builder lm() {
            this.bfgs = false;
            this.lbfgs = false;
            this.minpack = false;
            return this;
        }

        public MstsMonitor build() {
            return new MstsMonitor(this);
        }
    }
    private static final int NITER = 8;
    private final boolean marginalLikelihood;
    private final boolean concentratedLikelihood;
    private final double precision, precision2, precision3, smallStde;
    private final int maxIter;
    private final int maxIterOptimzer;
    private final boolean bfgs, lbfgs;
    private final boolean minpack;

    private Matrix data;
    private MstsMapping model;
    private MultivariateCompositeSsf ssf;
    private DoubleSeq fullp;
    private ILikelihood ll;
    private VarianceInterpreter fixedVariance;

    private final List<VarianceInterpreter> smallVariances = new ArrayList<>();
//    private final List<LoadingParameter> smallLoadings = new ArrayList<>();

    private MstsMonitor(Builder builder) {
        this.bfgs = builder.bfgs;
        this.lbfgs = builder.lbfgs;
        this.minpack = builder.minpack;
        this.maxIterOptimzer = builder.maxIterOptimzer;
        this.precision = builder.precision;
        this.precision2 = builder.precision2;
        this.precision3 = builder.precision3;
        this.marginalLikelihood = builder.marginalLikelihood;
        this.concentratedLikelihood = builder.concentratedLikelihood;
        this.maxIter = builder.maxIter;
        this.smallStde = Math.sqrt(builder.smallVar);
    }

    private ILikelihoodFunction function(boolean concentrated) {
        SsfMatrix s = new SsfMatrix(data);
        if (this.marginalLikelihood) {
            return MarginalLikelihoodFunction.builder(M2uAdapter.of(s), model, m -> M2uAdapter.of(m))
                    .useParallelProcessing(true)
                    .useMaximumLikelihood(true)
                    .useScalingFactor(concentrated)
                    .build();
        } else {
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

    public void process(Matrix data, MstsMapping model, DoubleSeq fullInitial) {
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
            ILikelihoodFunction fn = function(false);
            ILikelihoodFunctionPoint rslt = min(fn, false, precision3, 30, p);
            ll = rslt.getLikelihood();
            p = rslt.getParameters();
            double[] mp = model.modelParameters(p).toArray();
            fixedVariance = model.fixMaxVariance(mp, 1);
            fullp = DoubleSeq.ofInternal(mp);
        }

        double curll = 0;
        for (int k = 0; k < NITER; ++k) {
            int niter = 0;
            do {
                DoubleSeq p = model.functionParameters(fullp);
                ILikelihoodFunction fn = function(concentratedLikelihood);
                ILikelihoodFunctionPoint rslt = min(fn, concentratedLikelihood, curll == 0 ? precision3 : precision2, fniter, p);
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
                    for (VarianceInterpreter vp : smallVariances){
                        vp.freeStde(smallStde);
                    }
                    smallVariances.clear();
                    fullp=DoubleSeq.ofInternal(mp);
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
                ILikelihoodFunction fn = function(concentratedLikelihood);
                DoubleSeq curp = model.functionParameters(fullp);
                ILikelihoodFunctionPoint rslt = min(fn, concentratedLikelihood, curll == 0 ? precision3 : precision2, fniter, curp);
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
        ILikelihoodFunction fn = function(concentratedLikelihood);
        ILikelihoodFunctionPoint rslt = min(fn, concentratedLikelihood, precision, this.maxIterOptimzer, p);
        ll = rslt.getLikelihood();
        p = rslt.getParameters();
        fullp = model.modelParameters(p);
        ssf = model.map(p);
    }

    private ILikelihoodFunctionPoint min(ILikelihoodFunction fn, boolean concentrated, double eps, int niter, DoubleSeq start) {
        if (fn.getDomain().getDim() == 0) {
            return fn.evaluate(start);
        }
        if (!concentrated) {
            IFunctionMinimizer m = new LbfgsMinimizer();
//            IFunctionMinimizer m = lbfgs ? new LbfgsMinimizer(): Bfgs.builder().build();
            m.setFunctionPrecision(eps);
            m.setMaxIter(niter);
            m.minimize(fn.evaluate(start));
            return (ILikelihoodFunctionPoint) m.getResult();
        } else {
            ISsqFunctionMinimizer lm = minpack ? new MinPackMinimizer() : new LevenbergMarquardtMinimizer();
            lm.setFunctionPrecision(eps);
            lm.setMaxIter(niter);
            lm.minimize(fn.evaluate(start));
            return (ILikelihoodFunctionPoint) lm.getResult();
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
                ILikelihood nll = function(concentratedLikelihood).evaluate(nprslts).getLikelihood();
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
                ILikelihood nll = function(concentratedLikelihood).evaluate(p).getLikelihood();
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
    public Matrix getData() {
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
    public ILikelihood getLikelihood() {
        return ll;
    }

    public DoubleSeq fullParameters() {
        return fullp;
    }
}
