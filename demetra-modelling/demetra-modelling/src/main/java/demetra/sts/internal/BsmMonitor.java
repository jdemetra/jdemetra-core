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

import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import demetra.data.normalizer.AbsMeanNormalizer;
import demetra.design.Development;
import demetra.maths.functions.IFunction;
import demetra.maths.functions.IFunctionMinimizer;
import demetra.maths.functions.IFunctionPoint;
import demetra.maths.functions.TransformedFunction;
import demetra.maths.functions.levmar.LevenbergMarquardtMinimizer;
import demetra.maths.functions.minpack.MinPackMinimizer;
import demetra.maths.functions.riso.LbfgsMinimizer;
import demetra.maths.functions.ssq.ProxyMinimizer;
import demetra.maths.matrices.Matrix;
import demetra.ssf.dk.DkConcentratedLikelihood;
import demetra.ssf.dk.SsfFunction;
import demetra.ssf.dk.SsfFunctionPoint;
import demetra.ssf.univariate.SsfData;
import demetra.sts.BasicStructuralModel;
import demetra.sts.BsmEstimationSpec;
import demetra.sts.Component;
import demetra.sts.BsmSpec;
import demetra.sts.SsfBsm2;
import demetra.sts.internal.BsmMapping.Transformation;


/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class BsmMonitor {

    private Matrix m_x;

    private double[] m_y;

    // mapper definition
    private BsmSpec m_spec = new BsmSpec();

    private int m_freq = 1;

    private BsmMapping m_mapping;

    private BasicStructuralModel m_bsm;

    private double m_eps = 1e-9;

    private boolean m_bconverged = false, m_dregs;

    private IFunctionMinimizer m_min = null;// new
    // ec.tstoolkit.maths.functions.minpack.LMMinimizer();

    private double m_dsmall = 0.01;

    private DkConcentratedLikelihood m_ll;
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

        if (m_mapping.getDim() == 0) {
            return true;
        }
        fn_ = null;
        fnmax_ = null;

        IFunctionMinimizer fmin;
        if (m_min != null) {
            fmin = m_min.exemplar();
        } else {
            // ec.tstoolkit.maths.realfunctions.QRMarquardt qr=new
            // ec.tstoolkit.maths.realfunctions.QRMarquardt();
            // qr.setIncreaseStep(32);
            // fmin = new ProxyMinimizer(qr);
            fmin = new ProxyMinimizer(new LevenbergMarquardtMinimizer());
            //fmin = new ec.tstoolkit.maths.realfunctions.riso.LbfgsMinimizer();
            //fmin = new ec.tstoolkit.maths.realfunctions.jbfgs.Bfgs();
            fmin.setFunctionPrecision(m_eps);
        }

        fmin.setMaxIter(10);
        for (int i = 0; i < 3; ++i) {
            fn_ = buildFunction(null, true);
            DoubleSequence parameters = m_mapping.map(m_bsm);
            fmin.minimize(fn_.evaluate(parameters));
            m_bconverged = fmin.getIterCount() < fmin.getMaxIter();
            fnmax_ = (SsfFunctionPoint<BasicStructuralModel, SsfBsm2>) fmin.getResult();
            m_bsm = fnmax_.getCore();
            m_ll = fnmax_.getLikelihood();

            Component cmp = m_bsm.fixMaxVariance(1);
            if (cmp != m_mapping.getFixedComponent()) {
                m_mapping.setFixedComponent(cmp);
            } else {
                break;
            }
        }

        if (!m_bconverged) {
            fmin.setMaxIter(30);
            fn_ = buildFunction(null,
                    true);
            DoubleSequence parameters = m_mapping.map(m_bsm);
            fmin.minimize(fn_.evaluate(parameters));
            m_bconverged = fmin.getIterCount() < fmin.getMaxIter();
            fnmax_ = (SsfFunctionPoint<BasicStructuralModel, SsfBsm2>) fmin.getResult();
            m_bsm = fnmax_.getCore();
            m_ll = fnmax_.getLikelihood();
            Component cmp = m_bsm.fixMaxVariance(1);
            if (cmp != m_mapping.getFixedComponent()) {
                m_mapping.setFixedComponent(cmp);
            }
        }

        boolean ok=m_bconverged;
        if (fixsmallvariance(m_bsm))// bsm.FixSmallVariances(1e-4))
        {
            updateSpec(m_bsm);
            // update the likelihood !
            fn_ = buildFunction(null,
                    true);
            DoubleSequence parameters = m_mapping.map(m_bsm);
            fnmax_=(SsfFunctionPoint<BasicStructuralModel, SsfBsm2>) fn_.evaluate(parameters);
            m_ll=fnmax_.getLikelihood();
            ok=false;
        }
        if (m_factor != 1) {
            m_ll=m_ll.rescale(m_factor, null);
        }
        return ok;
    }

    private SsfFunction<BasicStructuralModel, SsfBsm2> buildFunction(BsmMapping mapping, boolean ssq) {
        SsfData data = new SsfData(m_y);
        
        return SsfFunction.builder(data, mapping == null ? m_mapping : mapping, model->SsfBsm2.of(model))
                .regression(m_x, diffuseItems())
                .useFastAlgorithm(true)
                .useParallelProcessing(false)
                .useLog(!ssq)
                .build();
        
    }

    private int[] diffuseItems() {
        int[] idiffuse = null;
        if (m_x != null && m_dregs) {
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
        BsmMapping mapper = new BsmMapping(model.specification(), m_freq,
                BsmMapping.Transformation.None);
        mapper.setFixedComponent(m_mapping.getFixedComponent());
        SsfFunction<BasicStructuralModel, SsfBsm2> fn = buildFunction(mapper, true);
        DoubleSequence p = mapper.map(model);
        SsfFunctionPoint instance = new SsfFunctionPoint(fn, p);
        double ll = instance.getLikelihood().logLikelihood();
        int nvar=mapper.getVarsCount();
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
        m_spec.fixComponent(cmp);
        return true;
    }

    /**
     *
     * @return
     */
    public DkConcentratedLikelihood getLikelihood() {
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
        return m_eps;
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
        return m_spec;
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

        m_mapping = new BsmMapping(m_spec, m_freq);
        BasicStructuralModel start = new BasicStructuralModel(m_spec, m_freq);
        if (m_mapping.getDim() == 1) {
            m_mapping.setFixedComponent(m_mapping.getComponent(0));
            return start;
        }
        // m_mapper.setFixedComponent(Component.Noise);

        BsmMapping mapping = new BsmMapping(m_spec, m_freq,
                BsmMapping.Transformation.None);
        SsfFunction<BasicStructuralModel, SsfBsm2> fn = buildFunction(mapping,
                true);

        DoubleSequence p = mapping.map(start);
        SsfFunctionPoint instance = new SsfFunctionPoint(fn, p);
        double lmax = instance.getLikelihood().logLikelihood();
        int imax = -1;
        int nvars= mapping.getVarsCount();
        for (int i = 0; i < nvars; ++i) {
            DataBlock np = DataBlock.of(p);
            np.set(.5);
            np.set(i, 1);
            int ncur=nvars;
            if (mapping.hasCycleDumpingFactor()){
                np.set(ncur++, .9);
            }
            if (mapping.hasCycleLength()){
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
            if (m_spec.hasNoise()) {
                m_mapping.setFixedComponent(Component.Noise);
            } else if (m_spec.hasLevel()) {
                m_mapping.setFixedComponent(Component.Level);
            } else {
                m_mapping.setFixedComponent(m_mapping.getComponent(0));
            }
            return start;
        } else {
            Component cmp = mapping.getComponent(imax);
            m_mapping.setFixedComponent(cmp);
            DataBlock np = DataBlock.of(p);
            np.set(.1);
            np.set(imax, 1);
            if (mapping.hasCycleDumpingFactor()){
                np.set(nvars++, .9);
            }
            if  (mapping.hasCycleLength()){
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
        return m_dregs;
    }

    /**
     *
     * @param y
     * @param freq
     * @return
     */
    public boolean process(DoubleSequence y, int freq) {
        return process(y, null, freq);
    }

    /**
     *
     * @param y
     * @param x
     * @param freq
     * @return
     */
    public boolean process(DoubleSequence y, Matrix x, int freq) {
        AbsMeanNormalizer normalizer = new AbsMeanNormalizer();
        m_y=y.toArray();
        DataBlock Y=DataBlock.ofInternal(m_y);
        m_factor = normalizer.normalize(Y);
        m_x = x;
        m_freq = freq;
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
        m_eps = value;
    }

    /**
     *
     * @param spec
     */
    public void setSpecifications(BsmSpec mspec, BsmEstimationSpec spec) {
        m_spec = mspec.clone();
        m_eps = spec.getPrecision();
        m_dregs = spec.isDiffuseRegression();
        switch (spec.getOptimizer()) {
            case LevenbergMarquardt:
                m_min = new ProxyMinimizer(new LevenbergMarquardtMinimizer());
                break;
            case MinPack:
                m_min = new ProxyMinimizer(new MinPackMinimizer());
                break;
            case LBFGS:
                m_min = new LbfgsMinimizer();
                break;
            default:
                m_min = null;
        }
        m_bsm = null;
    }

    public void setSpecification(BsmSpec spec) {
        m_spec = spec.clone();
        m_bsm = null;
    }

    private void updateSpec(BasicStructuralModel bsm) {
        m_spec = bsm.specification();
        Component fixed = m_mapping.getFixedComponent();
        m_mapping = new BsmMapping(m_spec, m_freq, m_mapping.transformation);
        m_mapping.setFixedComponent(fixed);
    }

    /**
     *
     * @param value
     */
    public void useDiffuseRegressors(boolean value) {
        m_dregs = value;
    }

    public IFunction likelihoodFunction() {
        BsmMapping mapper = new BsmMapping(m_bsm.specification(), m_bsm.getFrequency(), Transformation.None);
        SsfFunction<BasicStructuralModel, SsfBsm2> fn = buildFunction(mapper, false);
        double a = (m_ll.dim() - m_ll.getD()) * Math.log(m_factor);
        return new TransformedFunction(fn, TransformedFunction.linearTransformation(-a, 1));
    }

    public IFunctionPoint maxLikelihoodFunction() {
        BsmMapping mapper = new BsmMapping(m_bsm.specification(), m_bsm.getFrequency(), Transformation.None);
        IFunction ll = likelihoodFunction();
        return ll.evaluate(mapper.map(m_bsm));
    }

}
