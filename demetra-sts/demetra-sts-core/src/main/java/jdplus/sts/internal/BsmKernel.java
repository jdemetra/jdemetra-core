/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.sts.internal;

import demetra.data.DoubleSeq;
import demetra.data.Parameter;
import demetra.math.matrices.MatrixType;
import demetra.sts.BsmEstimationSpec;
import demetra.sts.BsmSpec;
import demetra.sts.Component;
import jdplus.data.DataBlock;
import jdplus.likelihood.DiffuseConcentratedLikelihood;
import jdplus.math.functions.FunctionMinimizer;
import jdplus.math.functions.IFunction;
import jdplus.math.functions.IFunctionPoint;
import jdplus.math.functions.TransformedFunction;
import jdplus.math.functions.bfgs.Bfgs;
import jdplus.math.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.math.functions.minpack.MinPackMinimizer;
import jdplus.math.functions.riso.LbfgsMinimizer;
import jdplus.math.functions.ssq.ProxyMinimizer;
import jdplus.math.matrices.Matrix;
import jdplus.ssf.dk.SsfFunction;
import jdplus.ssf.dk.SsfFunctionPoint;
import jdplus.ssf.univariate.SsfData;
import jdplus.sts.BasicStructuralModel;
import jdplus.sts.SsfBsm2;
import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class BsmKernel {

    private final BsmEstimationSpec estimationSpec;

    private double[] y;
    private int period;
    private Matrix X;

    // mapper definition
    private BsmSpec modelSpec;
    private Component fixedVar = Component.Undefined;
    private BasicStructuralModel bsm;
    private boolean converged = false;

    private DiffuseConcentratedLikelihood likelihood;
    private SsfFunction<BasicStructuralModel, SsfBsm2> fn_;
    private SsfFunctionPoint<BasicStructuralModel, SsfBsm2> fnmax_;
    private double m_factor;
    
    private void clear(){
        modelSpec=null;
        bsm=null;
        converged=false;
        fixedVar=Component.Undefined;
        likelihood=null;
        fn_=null;
        fnmax_=null;
    }

    /**
     *
     * @param espec
     */
    public BsmKernel(BsmEstimationSpec espec) {
        this.estimationSpec = espec == null ? BsmEstimationSpec.DEFAULT : espec;
    }

    private boolean _estimate() {
        converged = false;

        if (bsm == null) {
            bsm = initialize();
            updateSpec();
        }

        fn_ = null;
        fnmax_ = null;

        if (isScaling()) {
            BsmMapping mapping = new BsmMapping(modelSpec, period, fixedVar);
            FunctionMinimizer fmin = minimizer(estimationSpec.getPrecision(), 10);
            for (int i = 0; i < 3; ++i) {
                fn_ = buildFunction(mapping, estimationSpec.isScalingFactor());
                DoubleSeq parameters = mapping.map(bsm);
                converged = fmin.minimize(fn_.evaluate(parameters));
                fnmax_ = (SsfFunctionPoint<BasicStructuralModel, SsfBsm2>) fmin.getResult();
                bsm = fnmax_.getCore();
                likelihood = fnmax_.getLikelihood();

                BasicStructuralModel.ComponentVariance max = bsm.maxVariance();
                bsm = bsm.scaleVariances(1 / max.getVariance());
            updateSpec();
                if (fixedVar != max.getComponent()) {
                    fixedVar = max.getComponent();
                } else {
                    break;
                }
            }
        }

        if (!isScaling() || !converged) {
            FunctionMinimizer fmin = minimizer(estimationSpec.getPrecision(), 100);
            BsmMapping mapping = new BsmMapping(modelSpec, period, isScaling() ? fixedVar : null);
            fn_ = buildFunction(mapping, isScaling());
            DoubleSeq parameters = mapping.map(bsm);
            converged = fmin.minimize(fn_.evaluate(parameters));
            fnmax_ = (SsfFunctionPoint<BasicStructuralModel, SsfBsm2>) fmin.getResult();
            bsm = fnmax_.getCore();
            likelihood = fnmax_.getLikelihood();
            if (isScaling()) {
                BasicStructuralModel.ComponentVariance max = bsm.maxVariance();
                bsm = bsm.scaleVariances(1 / max.getVariance());
                if (fixedVar != max.getComponent()) {
                    fixedVar = max.getComponent();
                }
            }
            updateSpec();
        }

        boolean ok = converged;
        if (fixSmallVariance(bsm)) {
            // update the bsm and the likelihood !
            BsmMapping mapping = new BsmMapping(modelSpec, period, isScaling() ? fixedVar : null);
            fn_ = buildFunction(mapping, isScaling());
            DoubleSeq parameters = mapping.map(bsm);
            fnmax_ = (SsfFunctionPoint<BasicStructuralModel, SsfBsm2>) fn_.evaluate(parameters);
            likelihood = fnmax_.getLikelihood();
            bsm = fnmax_.getCore();
            updateSpec();
            ok = false;
        }
        return ok;
    }

    private FunctionMinimizer minimizer(double eps, int niter) {
        FunctionMinimizer.Builder builder = minimizerBuilder();
        return builder
                .functionPrecision(eps)
                .maxIter(niter)
                .build();
    }

    private SsfFunction<BasicStructuralModel, SsfBsm2> buildFunction(BsmMapping mapping, boolean ssq) {
        SsfData data = new SsfData(y);

        return SsfFunction.builder(data, mapping, model -> SsfBsm2.of(model))
                .regression(X, diffuseItems())
                .useFastAlgorithm(true)
                .useParallelProcessing(false)
                .useLog(!ssq)
                .useScalingFactor(isScaling())
                .build();

    }

    private int[] diffuseItems() {
        int[] idiffuse = null;
        if (X != null && estimationSpec.isDiffuseRegression()) {
            idiffuse = new int[X.getColumnsCount()];
            for (int i = 0; i < idiffuse.length; ++i) {
                idiffuse[i] = i;
            }
        }
        return idiffuse;
    }

    private boolean estimate() {
        for (int i = 0; i < 4; ++i) {
            if (_estimate()) {
                return true;
            }
        }
        return true;

    }

    private boolean fixSmallVariance(BasicStructuralModel model) {
        // return false;
        double vmin = estimationSpec.getLikelihoodRatioThreshold();
        int imin = -1;
        BsmMapping mapping = new BsmMapping(modelSpec, period, isScaling() ? fixedVar : null);
        SsfFunction<BasicStructuralModel, SsfBsm2> fn = buildFunction(mapping, isScaling());
        DoubleSeq p = mapping.map(model);
        SsfFunctionPoint instance = new SsfFunctionPoint(fn, p);
        double ll = instance.getLikelihood().logLikelihood();
        int nvars = mapping.varsCount();
        for (int i = 0; i < nvars; ++i) {
            if (p.get(i) < 1e-2) {
                DataBlock np = DataBlock.of(p);
                np.set(i, 0);
                instance = new SsfFunctionPoint(fn, np);
                double llcur = instance.getLikelihood().logLikelihood();
                double v = 2 * (ll - llcur);
                if (v < vmin) {
                    vmin = v;
                    imin = i;
                }
            }
        }

        if (imin < 0) {
            return false;
        }

        Component cmp = mapping.varPosition(imin);
        modelSpec = modelSpec.fixComponent(cmp, 0);
        return true;
    }

    /**
     *
     * @return
     */
    public DiffuseConcentratedLikelihood getLikelihood() {
        return likelihood;
    }

    /**
     *
     * @return
     */
    public BasicStructuralModel getResult() {
        if (bsm == null && y != null) {
            estimate();
        }
        return bsm;
    }

    /**
     *
     * @return
     */
    public BsmSpec finalSpecification() {
        return modelSpec;
    }

    /**
     *
     * @return
     */
    public boolean hasConverged() {
        return converged;
    }

    @SuppressWarnings("unchecked")
    private BasicStructuralModel initialize() {
        BsmMapping mapping = new BsmMapping(modelSpec, period, fixedVar);
        DoubleSeq p = mapping.getDefaultParameters();
        BasicStructuralModel start = mapping.map(p);
        if (!isScaling()) {
            return start;
        }

        SsfFunction<BasicStructuralModel, SsfBsm2> fn = buildFunction(mapping,
                true);

        SsfFunctionPoint instance = new SsfFunctionPoint(fn, p);
        double lmax = instance.getLikelihood().logLikelihood();
        int imax = -1;
        int nvars = mapping.varsCount();
        for (int i = 0; i < nvars; ++i) {
            DataBlock np = DataBlock.of(p);
            np.set(.5);
            np.set(i, 1);
            instance = new SsfFunctionPoint(fn, np);
            double nll = instance.getLikelihood().logLikelihood();
            if (nll > lmax) {
                lmax = nll;
                imax = i;
            }
        }
        if (imax < 0) {
            if (modelSpec.hasNoise()) {
                fixedVar = Component.Noise;
            } else if (modelSpec.hasLevel()) {
                fixedVar = Component.Level;
            } else {
                fixedVar = mapping.varPosition(0);
            }
            return start;
        } else {
            DataBlock np = DataBlock.of(p);
            for (int i = 0; i < nvars; ++i) {
                np.set(i, i == imax ? 1 : .1);
            }
            BasicStructuralModel nbsm = mapping.map(np);
            fixedVar = mapping.varPosition(imax);
            return nbsm;
        }
    }

    /**
     *
     * @param y
     * @param period
     * @param model
     * @return
     */
    public boolean process(DoubleSeq y, int period, BsmSpec model) {
        return process(y, null, period, model);
    }

    /**
     *
     * @param y
     * @param x
     * @param period
     * @param model
     * @return
     */
    public boolean process(DoubleSeq y, MatrixType x, int period, BsmSpec model) {
        clear();
        this.y = y.toArray();
        this.X = Matrix.of(x);
        this.period = period;
        modelSpec = model;
        boolean rslt = estimate();
        return rslt;
    }

    private boolean isScaling() {
        return estimationSpec.isScalingFactor() && modelSpec.isScalable();
    }

    private FunctionMinimizer.Builder minimizerBuilder() {
        if (!isScaling()) {
            return Bfgs.builder();
        } else {
            switch (estimationSpec.getOptimizer()) {
                case LevenbergMarquardt:
                    return ProxyMinimizer.builder(LevenbergMarquardtMinimizer.builder());
                case MinPack:
                    return ProxyMinimizer.builder(MinPackMinimizer.builder());
                case LBFGS:
                    return LbfgsMinimizer.builder();
                default:
                    return Bfgs.builder();
            }
        }
    }

    public IFunction likelihoodFunction() {
        BsmMapping mapper = new BsmMapping(modelSpec, period, fixedVar);
        SsfFunction<BasicStructuralModel, SsfBsm2> fn = buildFunction(mapper, false);
        double a = (likelihood.dim() - likelihood.ndiffuse());
//        double a = (likelihood.dim() - likelihood.ndiffuse()) * Math.log(m_factor);
        return new TransformedFunction(fn, TransformedFunction.linearTransformation(-a, 1));
    }

    public IFunctionPoint maxLikelihoodFunction() {
        BsmMapping mapper = new BsmMapping(modelSpec, period, fixedVar);
        IFunction ll = likelihoodFunction();
        return ll.evaluate(mapper.map(bsm));
    }

    private void updateSpec() {
        modelSpec = modelSpec.toBuilder()
                .level(nparam(modelSpec.getLevelVar(), bsm.getLevelVar()), nparam(modelSpec.getSlopeVar(), bsm.getSlopeVar()))
                .seasonal(modelSpec.getSeasonalModel(), nparam(modelSpec.getSeasonalVar(), bsm.getSeasonalVar()))
                .noise(nparam(modelSpec.getNoiseVar(), bsm.getNoiseVar()))
                .cycle(nparam(modelSpec.getCycleVar(), bsm.getCycleVar()), 
                        nparam(modelSpec.getCycleDumpingFactor(), bsm.getCycleDumpingFactor()),
                        nparam(modelSpec.getCycleLength(), bsm.getCycleLength()))
                .build();

    }

    private Parameter nparam(Parameter p, double c) {
        if (p == null || p.isFixed()) {
            return p;
        } else {
            return Parameter.estimated(c);
        }
    }

}
