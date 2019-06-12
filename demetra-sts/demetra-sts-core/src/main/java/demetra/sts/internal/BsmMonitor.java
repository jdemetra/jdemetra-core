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
package demetra.sts.internal;

import jdplus.data.DataBlock;
import jdplus.data.normalizer.AbsMeanNormalizer;
import demetra.design.Development;
import jdplus.maths.functions.IFunction;
import jdplus.maths.functions.IFunctionPoint;
import jdplus.maths.functions.TransformedFunction;
import jdplus.maths.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.maths.functions.minpack.MinPackMinimizer;
import jdplus.maths.functions.riso.LbfgsMinimizer;
import jdplus.maths.functions.ssq.ProxyMinimizer;
import demetra.likelihood.DiffuseConcentratedLikelihood;
import jdplus.ssf.dk.SsfFunction;
import jdplus.ssf.dk.SsfFunctionPoint;
import jdplus.ssf.univariate.SsfData;
import demetra.sts.BasicStructuralModel;
import demetra.sts.BsmEstimationSpec;
import demetra.sts.Component;
import demetra.sts.BsmSpec;
import demetra.sts.SsfBsm2;
import demetra.sts.internal.BsmMapping.Transformation;
import demetra.data.DoubleSeq;
import jdplus.maths.matrices.FastMatrix;
import jdplus.maths.functions.FunctionMinimizer;
import jdplus.maths.functions.bfgs.Bfgs;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class BsmMonitor {

    private FastMatrix m_x;

    private double[] m_y;

    // mapper definition
    private BsmSpec modelSpec = new BsmSpec();

    private int period = 1;

    private BsmMapping mapping;

    private BasicStructuralModel m_bsm;

    private double eps = 1e-9;

    private boolean m_bconverged = false, diffuseRegressors, scalingFactor = true;

    private FunctionMinimizer.Builder minimizer = null;// new
    // ec.tstoolkit.maths.functions.minpack.LMMinimizer();

    private double m_dsmall = 0.01;

    private DiffuseConcentratedLikelihood m_ll;
    private SsfFunction<BasicStructuralModel, SsfBsm2> fn_;
    private SsfFunctionPoint<BasicStructuralModel, SsfBsm2> fnmax_;

    private double m_factor;

    /**
     *
     */
    public BsmMonitor() {
    }

    private boolean _estimate() {
        m_bconverged = false;

        if (m_bsm == null) {
            m_bsm = initialize();
        }

        if (mapping.getDim() == 0) {
            return true;
        }
        fn_ = null;
        fnmax_ = null;

        if (scalingFactor) {
            FunctionMinimizer fmin = minimizer(eps, 10);
            for (int i = 0; i < 3; ++i) {
                fn_ = buildFunction(null, scalingFactor);
                DoubleSeq parameters = mapping.map(m_bsm);
                m_bconverged = fmin.minimize(fn_.evaluate(parameters));
                fnmax_ = (SsfFunctionPoint<BasicStructuralModel, SsfBsm2>) fmin.getResult();
                m_bsm = fnmax_.getCore();
                m_ll = fnmax_.getLikelihood();

                Component cmp = m_bsm.fixMaxVariance(1);
                if (cmp != mapping.getFixedComponent()) {
                    mapping.setFixedComponent(cmp);
                } else {
                    break;
                }
            }
        }

        if (!scalingFactor || !m_bconverged) {
            FunctionMinimizer fmin = minimizer(eps, 100);
            fn_ = buildFunction(null, scalingFactor);
            DoubleSeq parameters = mapping.map(m_bsm);
            m_bconverged = fmin.minimize(fn_.evaluate(parameters));
            fnmax_ = (SsfFunctionPoint<BasicStructuralModel, SsfBsm2>) fmin.getResult();
            m_bsm = fnmax_.getCore();
            m_ll = fnmax_.getLikelihood();
            if (scalingFactor) {
                Component cmp = m_bsm.fixMaxVariance(1);
                if (cmp != mapping.getFixedComponent()) {
                    mapping.setFixedComponent(cmp);
                }
            }
        }

        boolean ok = m_bconverged;
        if (fixsmallvariance(m_bsm))// bsm.FixSmallVariances(1e-4))
        {
            updateSpec(m_bsm);
            // update the likelihood !
            fn_ = buildFunction(null, scalingFactor);
            DoubleSeq parameters = mapping.map(m_bsm);
            fnmax_ = (SsfFunctionPoint<BasicStructuralModel, SsfBsm2>) fn_.evaluate(parameters);
            m_ll = fnmax_.getLikelihood();
            ok = false;
        }
//        if (m_factor != 1) {
//            m_ll = m_ll.rescale(m_factor, null);
//        }
        return ok;
    }

    private FunctionMinimizer minimizer(double eps, int niter) {
        if (minimizer != null) {
            return minimizer
                    .functionPrecision(eps)
                    .maxIter(niter)
                    .build();
        } else if (scalingFactor) {
            return new ProxyMinimizer(LevenbergMarquardtMinimizer
                    .builder()
                    .functionPrecision(eps)
                    .maxIter(niter)
                    .build());
        } else {
            return Bfgs
                    .builder()
                    .functionPrecision(eps)
                    .maxIter(niter)
                    .build();
        }

    }

    private SsfFunction<BasicStructuralModel, SsfBsm2> buildFunction(BsmMapping mapping, boolean ssq) {
        SsfData data = new SsfData(m_y);

        return SsfFunction.builder(data, mapping == null ? this.mapping : mapping, model -> SsfBsm2.of(model))
                .regression(m_x, diffuseItems())
                .useFastAlgorithm(true)
                .useParallelProcessing(false)
                .useLog(!ssq)
                .useScalingFactor(scalingFactor)
                .build();

    }

    private int[] diffuseItems() {
        int[] idiffuse = null;
        if (m_x != null && diffuseRegressors) {
            idiffuse = new int[m_x.getColumnsCount()];
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

    @SuppressWarnings("unchecked")
    private boolean fixsmallvariance(BasicStructuralModel model) {
        // return false;
        double vmin = m_dsmall;
        int imin = -1;
        BsmMapping mapper = new BsmMapping(model.specification(), period,
                BsmMapping.Transformation.None);
        if (scalingFactor) {
            mapper.setFixedComponent(mapping.getFixedComponent());
        }
        SsfFunction<BasicStructuralModel, SsfBsm2> fn = buildFunction(mapper, scalingFactor);
        DoubleSeq p = mapper.map(model);
        SsfFunctionPoint instance = new SsfFunctionPoint(fn, p);
        double ll = instance.getLikelihood().logLikelihood();
        int nvar = mapper.getVarsCount();
        for (int i = 0; i < nvar; ++i) {
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
        Component cmp = mapper.getComponent(imin);
        model.setVariance(cmp, 0);
        modelSpec.fixComponent(0, cmp);
        return true;
    }

    /**
     *
     * @return
     */
    public DiffuseConcentratedLikelihood getLikelihood() {
        return m_ll;
    }

    /**
     *
     * @return
     */
    public double getLRatioForSmallVariance() {
        return m_dsmall;
    }

    /**
     *
     * @return
     */
    public double getPrecision() {
        return eps;
    }

    /**
     *
     * @return
     */
    public BasicStructuralModel getResult() {
        if (m_bsm == null && m_y != null) {
            estimate();
        }
        return m_bsm;
    }

    /**
     *
     * @return
     */
    public BsmSpec getSpecification() {
        return modelSpec;
    }

    /**
     *
     * @return
     */
    public boolean hasConverged() {
        return m_bconverged;
    }

    @SuppressWarnings("unchecked")
    private BasicStructuralModel initialize() {
        // Search for the highest Variance
        // m_mapper = new BsmMapper(m_spec, m_freq);
        // BasicStructuralModel start = new BasicStructuralModel(m_spec,
        // m_freq);
        // if (m_spec.hasNoise())
        // m_mapper.setFixedComponent(Component.Noise);
        // else if (m_spec.hasLevel())
        // m_mapper.setFixedComponent(Component.Level);
        // else if (m_mapper.getDim() >= 1)
        // m_mapper.setFixedComponent(m_mapper.getComponent(0));
        // return start;

        mapping = new BsmMapping(modelSpec, period);
        BasicStructuralModel start = new BasicStructuralModel(modelSpec, period);
        if (!scalingFactor) {
            return start;
        }
        if (mapping.getDim() == 1) {
            mapping.setFixedComponent(mapping.getComponent(0));
            return start;
        }
        // m_mapper.setFixedComponent(Component.Noise);

        BsmMapping mapping = new BsmMapping(modelSpec, period,
                BsmMapping.Transformation.None);
        SsfFunction<BasicStructuralModel, SsfBsm2> fn = buildFunction(mapping,
                true);

        DoubleSeq p = mapping.map(start);
        SsfFunctionPoint instance = new SsfFunctionPoint(fn, p);
        double lmax = instance.getLikelihood().logLikelihood();
        int imax = -1;
        int nvars = mapping.getVarsCount();
        for (int i = 0; i < nvars; ++i) {
            DataBlock np = DataBlock.of(p);
            np.set(.5);
            np.set(i, 1);
            int ncur = nvars;
            if (mapping.hasCycleDumpingFactor()) {
                np.set(ncur++, .9);
            }
            if (mapping.hasCycleLength()) {
                np.set(ncur, 1);
            }
            instance = new SsfFunctionPoint(fn, np);
            double nll = instance.getLikelihood().logLikelihood();
            if (nll > lmax) {
                lmax = nll;
                imax = i;
            }
        }
        if (imax < 0) {
            if (modelSpec.hasNoise()) {
                this.mapping.setFixedComponent(Component.Noise);
            } else if (modelSpec.hasLevel()) {
                this.mapping.setFixedComponent(Component.Level);
            } else {
                this.mapping.setFixedComponent(this.mapping.getComponent(0));
            }
            return start;
        } else {
            Component cmp = mapping.getComponent(imax);
            this.mapping.setFixedComponent(cmp);
            DataBlock np = DataBlock.of(p);
            np.set(.1);
            np.set(imax, 1);
            if (mapping.hasCycleDumpingFactor()) {
                np.set(nvars++, .9);
            }
            if (mapping.hasCycleLength()) {
                np.set(nvars, 1);
            }
            return mapping.map(np);
        }
    }

    /**
     *
     * @return
     */
    public boolean isUsingDiffuseRegressors() {
        return diffuseRegressors;
    }

    /**
     *
     * @param y
     * @param freq
     * @return
     */
    public boolean process(DoubleSeq y, int freq) {
        return process(y, null, freq);
    }

    /**
     *
     * @param y
     * @param x
     * @param freq
     * @return
     */
    public boolean process(DoubleSeq y, FastMatrix x, int freq) {
        m_y = y.toArray();
        m_x = x;
        period = freq;
        if (scalingFactor) {
            AbsMeanNormalizer normalizer = new AbsMeanNormalizer();
            DataBlock Y = DataBlock.of(m_y);
//            m_factor = normalizer.normalize(Y);
        } else {
//            m_factor = 1;
        }
        boolean rslt = estimate();
        return rslt;
    }

    /**
     *
     * @param value
     */
    public void setLRatioForSmallVariance(double value) {
        m_dsmall = value;
    }

    /**
     *
     * @param value
     */
    public void setPrecision(double value) {
        eps = value;
    }

    /**
     *
     * @param mspec
     * @param spec
     */
    public void setSpecifications(BsmSpec mspec, BsmEstimationSpec spec) {
        modelSpec = mspec.clone();
        eps = spec.getPrecision();
        diffuseRegressors = spec.isDiffuseRegression();
        scalingFactor = spec.isScalingFactor();
        if (!scalingFactor) {
            minimizer = LbfgsMinimizer.builder();
        } else {
            switch (spec.getOptimizer()) {
                case LevenbergMarquardt:
                    minimizer = ProxyMinimizer.builder(LevenbergMarquardtMinimizer.builder());
                    break;
                case MinPack:
                    minimizer = ProxyMinimizer.builder(MinPackMinimizer.builder());
                    break;
                case LBFGS:
                    minimizer = LbfgsMinimizer.builder();
                    break;
                default:
                    minimizer = null;
            }
        }
        m_bsm = null;
    }

    public void setSpecification(BsmSpec spec) {
        this.modelSpec = spec.clone();
        m_bsm = null;
    }

    private void updateSpec(BasicStructuralModel bsm) {
        modelSpec = bsm.specification();
        Component fixed = mapping.getFixedComponent();
        mapping = new BsmMapping(modelSpec, period, mapping.transformation);
        mapping.setFixedComponent(fixed);
    }

    /**
     *
     * @param value
     */
    public void useDiffuseRegressors(boolean value) {
        diffuseRegressors = value;
    }

    public IFunction likelihoodFunction() {
        BsmMapping mapper = new BsmMapping(m_bsm.specification(), m_bsm.getPeriod(), Transformation.None);
        SsfFunction<BasicStructuralModel, SsfBsm2> fn = buildFunction(mapper, false);
        double a = (m_ll.dim() - m_ll.ndiffuse());
//        double a = (m_ll.dim() - m_ll.ndiffuse()) * Math.log(m_factor);
        return new TransformedFunction(fn, TransformedFunction.linearTransformation(-a, 1));
    }

    public IFunctionPoint maxLikelihoodFunction() {
        BsmMapping mapper = new BsmMapping(m_bsm.specification(), m_bsm.getPeriod(), Transformation.None);
        IFunction ll = likelihoodFunction();
        return ll.evaluate(mapper.map(m_bsm));
    }

}
