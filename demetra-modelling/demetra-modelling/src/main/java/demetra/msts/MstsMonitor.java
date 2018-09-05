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
import demetra.ssf.akf.AkfFunctionPoint;
import demetra.ssf.akf.MarginalLikelihood;
import demetra.ssf.dk.DkToolkit;
import demetra.ssf.dk.SsfFunction;
import demetra.ssf.dk.SsfFunctionPoint;
import demetra.ssf.implementations.MultivariateCompositeSsf;
import demetra.ssf.multivariate.M2uAdapter;
import demetra.ssf.multivariate.SsfMatrix;
import demetra.ssf.univariate.DefaultSmoothingResults;
import demetra.ssf.univariate.ISsf;
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

        private boolean marginalLikelihood;
        private double precision = 1e-9;
        private int maxIter = MAXITER;
        private int maxIterOptimzer = MAXITER_MIN;
        private boolean bfgs;
        private boolean minpack;
        private boolean resetParameters;

        public Builder marginalLikelihood(boolean ml) {
            this.marginalLikelihood = ml;
            return this;
        }

        public Builder precision(double eps) {
            this.precision = eps;
            return this;
        }

       public Builder resetParameters(boolean reset) {
            this.resetParameters=reset;
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
    private final double precision;
    private final int maxIter;
    private final int maxIterOptimzer;
    private final boolean bfgs;
    private final boolean minpack;
    private final boolean resetParameters;

    private Matrix data;
    private MstsMapping model;
    private MultivariateCompositeSsf ssf;
    private DoubleSequence prslts, fullp;
    private DefaultSmoothingResults srslts;
    private ILikelihood ll;
    private int[] cpos;

    private final List<VarianceParameter> smallVariances = new ArrayList<>();

    private MstsMonitor(Builder builder) {
        this.bfgs = builder.bfgs;
        this.minpack = builder.minpack;
        this.maxIterOptimzer = builder.maxIterOptimzer;
        this.precision = builder.precision;
        this.marginalLikelihood = builder.marginalLikelihood;
        this.maxIter = builder.maxIter;
        this.resetParameters = builder.resetParameters;
    }

    private ILikelihoodFunction function(ISsfData data) {
        if (this.marginalLikelihood) {
            return AkfFunction.builder(data, model, m -> M2uAdapter.of(m))
                    .useParallelProcessing(true)
                    .useScalingFactor(true)
                    .useMaximumLikelihood(true)
                    .build();
        } else {
            return SsfFunction.builder(data, model, m -> M2uAdapter.of(m))
                    .useParallelProcessing(true)
                    .useScalingFactor(true)
                    .useMaximumLikelihood(true)
                    .build();
        }
    }

    public void process(Matrix data, MstsMapping model, DoubleSequence fullInitial) {

        this.data = data;
        this.model = model;
        smallVariances.clear();
        SsfMatrix mdata = new SsfMatrix(data);
        ISsfData udata = M2uAdapter.of(mdata);
        DoubleSequence start=fullInitial == null ? model.getDefaultParameters() : model.functionParameters(fullInitial);
        prslts = start;
        int niter = 0;
        do {
            ILikelihoodFunction fn = function(udata);
            ILikelihoodFunctionPoint rslt = min(fn, this.resetParameters ? start : prslts);
            ll = rslt.getLikelihood();
            prslts = rslt.getParameters();
            if (!fixSmallVariance(fn, 1e-3) && !freeSmallVariance(fn)) {
                break;
            }
        } while (niter++ < maxIter);
        ssf = model.map(prslts);
        cpos = ssf.componentsPosition();
        srslts = DkToolkit.sqrtSmooth(M2uAdapter.of(ssf), udata, true);
    }

    private ILikelihoodFunctionPoint min(ILikelihoodFunction fn, DoubleSequence start) {
        if (bfgs) {
            LbfgsMinimizer lm = new LbfgsMinimizer();
            lm.setFunctionPrecision(precision);
            lm.setMaxIter(this.maxIterOptimzer);
            lm.minimize(fn.evaluate(start));
            return (ILikelihoodFunctionPoint) lm.getResult();
        } else {
            ISsqFunctionMinimizer lm = minpack ? new MinPackMinimizer() : new LevenbergMarquardtMinimizer();
            lm.setFunctionPrecision(precision);
            lm.setMaxIter(this.maxIterOptimzer);
            lm.minimize(fn.evaluate(start));
            return (ILikelihoodFunctionPoint) lm.getResult();
        }
    }

    private boolean freeSmallVariance(ILikelihoodFunction fn) {
        if (smallVariances.isEmpty()) {
            return false;
        }
        double dll = 0;
        VarianceParameter cur = null;
        for (VarianceParameter small : smallVariances) {
            fullp = model.trueParameters(prslts);
            small.fix(1e-5);
            DoubleSequence nprslts = model.functionParameters(fullp);
            ILikelihood nll = fn.evaluate(nprslts).getLikelihood();
            double d = nll.logLikelihood() - ll.logLikelihood();
            if (d > 0 && d > dll) {
                dll = d;
                cur = small;
            }
            small.fix(0);
        }
        if (cur != null) {
            cur.free();
            prslts = model.functionParameters(fullp);
            smallVariances.remove(cur);
            return true;
        } else {
            return false;
        }

    }

    private boolean fixSmallVariance(ILikelihoodFunction fn, double eps) {
        List<VarianceParameter> svar = model.smallVariances(prslts, 1e-3);
        if (svar.isEmpty()) {
            return false;
        }
        double dll = 0;
        VarianceParameter cur = null;
        for (VarianceParameter small : svar) {
            fullp = model.trueParameters(prslts);
            small.fix(0);
            DoubleSequence nprslts = model.functionParameters(fullp);
            ILikelihood nll = fn.evaluate(nprslts).getLikelihood();
            double d = nll.logLikelihood() - ll.logLikelihood();
            if (d > 0 && d > dll) {
                dll = d;
                cur = small;
            }
            small.free();
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

    public DoubleSequence smoothedComponent(int pos) {
        ssf.componentsPosition();
        return srslts.getComponent(cpos[pos]).extract(0, data.getRowsCount(), data.getColumnsCount());
    }

    public DoubleSequence varianceOfSmoothedComponent(int pos) {
        ssf.componentsPosition();
        return srslts.getComponentVariance(cpos[pos]).extract(0, data.getRowsCount(), data.getColumnsCount());
    }

    /**
     * @return the ll
     */
    public ILikelihood getLikelihood() {
        return ll;
    }

    public DoubleSequence fullParameters(){
        return model.trueParameters(prslts);
    }
}
