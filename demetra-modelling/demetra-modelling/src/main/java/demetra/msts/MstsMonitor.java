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
import demetra.ssf.akf.AkfFunction;
import demetra.ssf.dk.SsfFunction;
import demetra.ssf.implementations.MultivariateCompositeSsf;
import demetra.ssf.multivariate.M2uAdapter;
import demetra.ssf.multivariate.SsfMatrix;
import demetra.ssf.univariate.ISsfData;
import java.util.ArrayList;
import java.util.List;

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
        private double precision = 1e-9, smallVar = SMALL_VAR;
        private int maxIter = MAXITER;
        private int maxIterOptimzer = MAXITER_MIN;
        private boolean bfgs;
        private boolean minpack;
        private boolean resetParameters;
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

        public Builder resetParameters(boolean reset) {
            this.resetParameters = reset;
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

    private final boolean marginalLikelihood;
    private final boolean concentratedLikelihood;
    private final double precision, smallStde;
    private final int maxIter;
    private final int maxIterOptimzer;
    private final boolean bfgs;
    private final boolean minpack;
    private final boolean resetParameters;

    private Matrix data;
    private MstsMapping model;
    private MultivariateCompositeSsf ssf;
    private DoubleSequence prslts, fullp;
    private ILikelihood ll;

    private final List<VarianceParameter> smallVariances = new ArrayList<>();

    private MstsMonitor(Builder builder) {
        this.bfgs = builder.bfgs;
        this.minpack = builder.minpack;
        this.maxIterOptimzer = builder.maxIterOptimzer;
        this.precision = builder.precision;
        this.marginalLikelihood = builder.marginalLikelihood;
        this.concentratedLikelihood = builder.concentratedLikelihood;
        this.maxIter = builder.maxIter;
        this.resetParameters = builder.resetParameters;
        this.smallStde = Math.sqrt(builder.smallVar);
    }

    private ILikelihoodFunction function() {
        SsfMatrix s = new SsfMatrix(data);
        if (this.marginalLikelihood) {
            return AkfFunction.builder(M2uAdapter.of(s), model, m -> M2uAdapter.of(m))
                    .useParallelProcessing(true)
                    .useMaximumLikelihood(true)
                    .useScalingFactor(concentratedLikelihood)
                    .build();
        } else {
            return SsfFunction.builder(M2uAdapter.of(s), model, m -> M2uAdapter.of(m))
                    .useParallelProcessing(true)
                    .useMaximumLikelihood(true)
                    .useScalingFactor(concentratedLikelihood)
                    .useFastAlgorithm(true)
                    .build();
        }
    }

    public void process(Matrix data, MstsMapping model, DoubleSequence fullInitial) {

        this.data = data;
        this.model = model;
        smallVariances.clear();
        DoubleSequence start = fullInitial == null ? model.getDefaultParameters() : model.functionParameters(fullInitial);
        prslts = start;
        int niter = 0;
        int fniter = this.concentratedLikelihood ? 100 : 500;
        do {
            ILikelihoodFunction fn = function();
            ILikelihoodFunctionPoint rslt = min(fn, fniter, this.resetParameters ? start : prslts);
            ll = rslt.getLikelihood();
            prslts = rslt.getParameters();
            fullp = model.trueParameters(prslts);
            if (!fixSmallVariance(1e-1) && !freeSmallVariance()) {
                break;
            }
        } while (niter++ < maxIter);

        // Final estimation. To do anyway
        ILikelihoodFunction fn = function();
        ILikelihoodFunctionPoint rslt = min(fn, this.maxIterOptimzer, this.resetParameters ? start : prslts);
        ll = rslt.getLikelihood();
        prslts = rslt.getParameters();
        fullp = model.trueParameters(prslts);

        ssf = model.map(prslts);
    }

    private ILikelihoodFunctionPoint min(ILikelihoodFunction fn, int niter, DoubleSequence start) {
        if (bfgs) {
            LbfgsMinimizer lm = new LbfgsMinimizer();
            lm.setFunctionPrecision(precision);
            lm.setMaxIter(niter);
            lm.minimize(fn.evaluate(start));
            return (ILikelihoodFunctionPoint) lm.getResult();
        } else {
            ISsqFunctionMinimizer lm = minpack ? new MinPackMinimizer() : new LevenbergMarquardtMinimizer();
            lm.setFunctionPrecision(precision);
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
            small.fix(smallStde);
            try {
                DoubleSequence nprslts = model.functionParameters(fullp);
                ILikelihood nll = function().evaluate(nprslts).getLikelihood();
                double d = nll.logLikelihood() - ll.logLikelihood();
                if (d > 0 && d > dll) {
                    dll = d;
                    cur = small;
                }
            } catch (Exception err) {
            }
            small.fix(0);
        }
        if (cur != null) {
            cur.free(smallStde);
            prslts = model.functionParameters(fullp);
            smallVariances.remove(cur);
            return true;
        } else {
            return false;
        }

    }

    private boolean fixSmallVariance(double eps) {
        List<VarianceParameter> svar = model.smallVariances(prslts, eps);
        if (svar.isEmpty()) {
            return false;
        }
        double dll = 0;
        VarianceParameter cur = null;
        for (VarianceParameter small : svar) {
            double olde = small.fix(0);
            try {
                DoubleSequence nprslts = model.functionParameters(fullp);
                ILikelihood nll = function().evaluate(nprslts).getLikelihood();
                small.fix(1e-4);
                nprslts = model.functionParameters(fullp);
                ILikelihood nll2 = function().evaluate(nprslts).getLikelihood();
                double ndll = nll.logLikelihood() - nll2.logLikelihood();
                if (ndll > dll) {
                    dll = ndll;
                    cur = small;
                }
            } catch (Exception err) {

            }
            small.free(olde);
        }
        if (cur != null) {
            cur.fix(0);
            prslts = model.functionParameters(fullp);
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
    public DoubleSequence getParameters() {
        return prslts;
    }

    /**
     * @return the ll
     */
    public ILikelihood getLikelihood() {
        return ll;
    }

    public DoubleSequence fullParameters() {
        return model.trueParameters(prslts);
    }
}
