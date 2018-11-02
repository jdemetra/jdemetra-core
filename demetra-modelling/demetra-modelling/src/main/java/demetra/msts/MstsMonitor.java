/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.msts;

import demetra.data.DoubleSequence;
import demetra.likelihood.ILikelihood;
import demetra.likelihood.ILikelihoodFunction;
import demetra.likelihood.ILikelihoodFunctionPoint;
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
import java.util.Optional;
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
        private static final double SMALL_VAR = 1e-12;

        private boolean marginalLikelihood;
        private double precision = 1e-9, smallVar = SMALL_VAR, precision2 = 1e-7, precision3 = 1e-3;
        private int maxIter = MAXITER;
        private int maxIterOptimzer = MAXITER_MIN;
        private boolean bfgs;
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
            this.minpack = false;
            return this;
        }

        public Builder minpack() {
            this.bfgs = false;
            this.minpack = true;
            return this;
        }

        public Builder lm() {
            this.bfgs = false;
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
    private final boolean bfgs;
    private final boolean minpack;

    private Matrix data;
    private MstsMapping model;
    private MultivariateCompositeSsf ssf;
    private DoubleSequence fullp;
    private ILikelihood ll;
    private VarianceParameter fixedVariance;

    private final List<VarianceParameter> smallVariances = new ArrayList<>();
//    private final List<LoadingParameter> smallLoadings = new ArrayList<>();

    private MstsMonitor(Builder builder) {
        this.bfgs = builder.bfgs;
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
                        && p instanceof VarianceParameter
                        && ((VarianceParameter) p).defValue() > 0)
                .count() == 0;
    }

    public void process(Matrix data, MstsMapping model, DoubleSequence fullInitial) {
        fixedVariance = null;
        this.data = data;
        this.model = model;
        fullp = fullInitial;
        if (fullp == null) {
            fullp = model.modelParameters(model.getDefaultParameters());
        }
        int fniter = concentratedLikelihood ? 30 : 50;
        if (needFixedVariance()) {
            Optional<IMstsParametersBlock> first = model.parameters().filter(var()).findFirst();
            if (first.isPresent()) {
                fixedVariance = (VarianceParameter) first.get();
                fixedVariance.fixStde(1);
            }
        }

        int nv=0;
        double curll = 0;
        for (int k = 0; k < NITER; ++k) {
            int niter = 0;
            do {
                DoubleSequence p = model.functionParameters(fullp);
                ILikelihoodFunction fn = function(concentratedLikelihood);
                ILikelihoodFunctionPoint rslt = min(fn, concentratedLikelihood, curll == 0 ? precision3 : precision2, fniter, p);
                ll = rslt.getLikelihood();
                p = rslt.getParameters();
                fullp = model.modelParameters(p);
                if (!fixSmallVariance(1e-3) && !freeSmallVariance()) {
                    break;
                }
            } while (niter++ < maxIter);

            boolean changed = false;
                // we can change twice the fixed variance
            if (needFixedVariance() && nv < 3) {
                VarianceParameter mvar = model.findMaxVariance(fullp);
                if (mvar != fixedVariance ) {
                    ++nv;
                    fixedVariance.free();
                    mvar.fixStde(1);
                    fixedVariance = mvar;
                    curll=0;
                    changed = true;
                }
            }
            if (!changed) {
                // Fix all variances and loadings
                List<IMstsParametersBlock> fixedBlocks = model.parameters()
                        .filter(p -> !p.isFixed() && p.isPotentialInstability())
                        .collect(Collectors.toList());

                model.fixModelParameters(p -> p.isPotentialInstability(), fullp);
                if (model.parameters().filter(p -> !p.isFixed()).count() > 0) {
                    ILikelihoodFunction fn = function(concentratedLikelihood);
                    DoubleSequence curp = model.functionParameters(fullp);
                    ILikelihoodFunctionPoint rslt = min(fn, concentratedLikelihood, curll == 0 ? precision3 : precision2, fniter, curp);
                    ll = rslt.getLikelihood();
                    DoubleSequence np = rslt.getParameters();
                    fullp = model.modelParameters(np);
                }
                // Free the fixed parameters
                for (IMstsParametersBlock p : fixedBlocks) {
                    p.free();
                }

                if (curll != 0 && Math.abs(curll - ll.logLikelihood()) < 1e-4) {
                    break;
                } else {
                    curll = ll.logLikelihood();
                }
            }
        }
        DoubleSequence p = model.functionParameters(fullp);
        // Final estimation. To do anyway
        ILikelihoodFunction fn = function(concentratedLikelihood);
        ILikelihoodFunctionPoint rslt = min(fn, concentratedLikelihood, precision, this.maxIterOptimzer, p);
        ll = rslt.getLikelihood();
        p = rslt.getParameters();
        fullp = model.modelParameters(p);
        ssf = model.map(p);
    }

    private ILikelihoodFunctionPoint min(ILikelihoodFunction fn, boolean concentrated, double eps, int niter, DoubleSequence start) {
        if (fn.getDomain().getDim() == 0) {
            return fn.evaluate(start);
        }
        if (!concentrated) {
            LbfgsMinimizer lm = new LbfgsMinimizer();
            lm.setFunctionPrecision(eps);
            lm.setMaxIter(niter);
            lm.minimize(fn.evaluate(start));
            return (ILikelihoodFunctionPoint) lm.getResult();
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
        VarianceParameter cur = null;
        for (VarianceParameter small : smallVariances) {
            small.fixStde(smallStde);
            try {
                DoubleSequence nprslts = model.functionParameters(fullp);
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

    private Predicate<IMstsParametersBlock> var() {
        return p -> !p.isFixed() && p instanceof VarianceParameter;
    }

    private boolean fixSmallVariance(double eps) {
//        List<VarianceParameter> svar = model.parameters()
//                .filter(var())
//                .map(p -> (VarianceParameter) p)
//                .collect(Collectors.toList());
        List<VarianceParameter> svar = model.smallVariances(fullp, eps);
        if (svar.isEmpty()) {
            return false;
        }
        double dll = 0;
        VarianceParameter cur = null;
        for (VarianceParameter small : svar) {
            double olde = small.fixStde(0);
            try {
                DoubleSequence p = model.functionParameters(fullp);
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

    private void fixVariance() {
        // we fix the first one
        DoubleSequence p = model.functionParameters(fullp);
        ILikelihoodFunction fn = function(false);
        ILikelihoodFunctionPoint rslt = min(fn, false, precision2, 15, p);
        p = rslt.getParameters();
        DoubleSequence fp = model.modelParameters(p);
        VarianceParameter mv = model.findMaxVariance(fp);
        mv.fixStde(1);
    }

    private void fixVariance2() {
        List<VarianceParameter> svar = model.parameters()
                .filter(var())
                .map(p -> (VarianceParameter) p)
                .collect(Collectors.toList());
        if (svar.isEmpty()) {
            return;
        }
        double dll = 0;
        for (VarianceParameter var : svar) {
            var.fixStde(.01);
        }
        // current params
        DoubleSequence p = model.functionParameters(fullp);
        ILikelihood ll0 = function(true).evaluate(p).getLikelihood();
        VarianceParameter cur = null;
        for (VarianceParameter var : svar) {
            var.fixStde(1);
            try {
                ILikelihood nll = function(true).evaluate(p).getLikelihood();
                double ndll = nll.logLikelihood() - ll0.logLikelihood();
                if (dll == 0 || ndll > dll) {
                    dll = ndll;
                    cur = var;
                }
            } catch (Exception err) {
            }
            var.fixStde(.01);
        }
        // restore the initial var
        model.fixModelParameters(var(), fullp);
        for (VarianceParameter var : svar) {
            if (var != cur) {
                var.free();
            } else {
                var.fixStde(1);
            }
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
    public DoubleSequence getParameters() {
        return model.functionParameters(fullp);
    }

    /**
     * @return the ll
     */
    public ILikelihood getLikelihood() {
        return ll;
    }

    public DoubleSequence fullParameters() {
        return fullp;
    }
}
