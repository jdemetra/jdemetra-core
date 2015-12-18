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
package ec.tstoolkit.structural;

import ec.tstoolkit.data.AbsMeanNormalizer;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.DiffuseConcentratedLikelihood;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.realfunctions.IFunction;
import ec.tstoolkit.maths.realfunctions.IFunctionInstance;
import ec.tstoolkit.maths.realfunctions.IFunctionMinimizer;
import ec.tstoolkit.maths.realfunctions.ProxyMinimizer;
import ec.tstoolkit.maths.realfunctions.TransformedFunction;
import ec.tstoolkit.ssf.FastSsfAlgorithm;
import ec.tstoolkit.ssf.SsfData;
import ec.tstoolkit.ssf.SsfFunction;
import ec.tstoolkit.ssf.SsfFunctionInstance;
import ec.tstoolkit.ssf.SsfModel;
import ec.tstoolkit.structural.BsmMapper.Transformation;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class BsmMonitor {

    private SubMatrix m_x;

    private double[] m_y;

    // mapper definition
    private ModelSpecification m_spec = new ModelSpecification();

    private int m_freq = 1;

    private BsmMapper m_mapper;

    private BasicStructuralModel m_bsm;

    private double m_eps = 1e-9;

    private boolean m_bconverged = false, m_dregs;

    private IFunctionMinimizer m_min = null;// new
    // ec.tstoolkit.maths.functions.minpack.LMMinimizer();

    private double m_dsmall = 0.01;

    private DiffuseConcentratedLikelihood m_ll;
    private SsfFunction<BasicStructuralModel> fn_;
    private SsfFunctionInstance<BasicStructuralModel> fnmax_;

    private double m_factor;

    /**
     *
     */
    public BsmMonitor() {
    }

    @SuppressWarnings("unchecked")
    private boolean _estimate() {
        m_bconverged = false;

        if (m_bsm == null) {
            m_bsm = initialize();
        }

        if (m_mapper.getDim() == 0) {
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
            fmin = new ProxyMinimizer(new ec.tstoolkit.maths.realfunctions.levmar.LevenbergMarquardtMethod());
            //fmin = new ec.tstoolkit.maths.realfunctions.riso.LbfgsMinimizer();
            //fmin = new ec.tstoolkit.maths.realfunctions.jbfgs.Bfgs();
            fmin.setConvergenceCriterion(m_eps);
        }

        fmin.setMaxIter(10);
        for (int i = 0; i < 3; ++i) {
            fn_ = buildFunction(m_bsm, null, true);
            IReadDataBlock parameters = m_mapper.map(m_bsm);
            fmin.minimize(fn_, fn_.evaluate(parameters));
            m_bconverged = fmin.getIterCount() < fmin.getMaxIter();
            fnmax_ = (SsfFunctionInstance<BasicStructuralModel>) fmin.getResult();
            m_bsm = fnmax_.ssf;
            m_ll = fnmax_.getLikelihood();

            Component cmp = m_bsm.fixMaxVariance(1);
            if (cmp != m_mapper.getFixedComponent()) {
                m_mapper.setFixedComponent(cmp);
            } else {
                break;
            }
        }

        if (!m_bconverged) {
            fmin.setMaxIter(30);
            fn_ = buildFunction(m_bsm, null,
                    true);
            IReadDataBlock parameters = m_mapper.map(m_bsm);
            fmin.minimize(fn_, fn_.evaluate(parameters));
            m_bconverged = fmin.getIterCount() < fmin.getMaxIter();
            fnmax_ = (SsfFunctionInstance<BasicStructuralModel>) fmin.getResult();
            m_bsm = fnmax_.ssf;
            m_ll = fnmax_.getLikelihood();
            Component cmp = m_bsm.fixMaxVariance(1);
            if (cmp != m_mapper.getFixedComponent()) {
                m_mapper.setFixedComponent(cmp);
            }
        }

        boolean ok=m_bconverged;
        if (fixsmallvariance(m_bsm))// bsm.FixSmallVariances(1e-4))
        {
            updateSpec(m_bsm);
            // update the likelihood !
            fn_ = buildFunction(m_bsm, null,
                    true);
            IReadDataBlock parameters = m_mapper.map(m_bsm);
            fnmax_=(SsfFunctionInstance<BasicStructuralModel>) fn_.evaluate(parameters);
            m_ll=fnmax_.getLikelihood();
            ok=false;
        }
        if (m_factor != 1) {
            m_ll.rescale(m_factor);
        }
        return ok;
    }

    private SsfFunction<BasicStructuralModel> buildFunction(
            BasicStructuralModel bsm, BsmMapper mapper, boolean ssq) {
        SsfData data = new SsfData(m_y, null);
        SsfModel<BasicStructuralModel> model = new SsfModel<>(
                bsm, data, m_x, diffuseItems());
        FastSsfAlgorithm<BasicStructuralModel> alg = new FastSsfAlgorithm<>();
        alg.useSsq(ssq);
        SsfFunction<BasicStructuralModel> eval = new SsfFunction<>(
                model, mapper == null ? m_mapper : mapper, alg);
        return eval;
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
        BsmMapper mapper = new BsmMapper(model.getSpecification(), m_freq,
                BsmMapper.Transformation.None);
        mapper.setFixedComponent(m_mapper.getFixedComponent());
        SsfFunction<BasicStructuralModel> fn = buildFunction(model.clone(),
                mapper, true);
        IReadDataBlock p = mapper.map(model);
        SsfFunctionInstance instance = new SsfFunctionInstance(fn, p);
        double ll = instance.getLikelihood().getLogLikelihood();
        int nvar=mapper.getVarsCount();
        for (int i = 0; i < nvar; ++i) {
            if (p.get(i) < 1e-2) {
                DataBlock np = new DataBlock(p);
                np.set(i, 0);
                instance = new SsfFunctionInstance(fn, np);
                double llcur = instance.getLikelihood().getLogLikelihood();
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
    public ModelSpecification getSpecification() {
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

        m_mapper = new BsmMapper(m_spec, m_freq);
        BasicStructuralModel start = new BasicStructuralModel(m_spec, m_freq);
        if (m_mapper.getDim() == 1) {
            m_mapper.setFixedComponent(m_mapper.getComponent(0));
            return start;
        }
        // m_mapper.setFixedComponent(Component.Noise);

        BsmMapper mapper = new BsmMapper(m_spec, m_freq,
                BsmMapper.Transformation.None);
        SsfFunction<BasicStructuralModel> fn = buildFunction(start, mapper,
                true);

        SsfFunctionInstance instance = new SsfFunctionInstance(fn, null);
        double lmax = instance.getLikelihood().getLogLikelihood();
        IReadDataBlock p = fn.mapper.map(fn.model.ssf);
        int imax = -1;
        int nvars= mapper.getVarsCount();
        for (int i = 0; i < nvars; ++i) {
            DataBlock np = new DataBlock(p);
            np.set(.5);
            np.set(i, 1);
            int ncur=nvars;
            if (mapper.hasCycleDumpingFactor()){
                np.set(ncur++, .9);
            }
            if (mapper.hasCycleLength()){
                np.set(ncur, 1);
            }
            instance = new SsfFunctionInstance(fn, np);
            double nll = instance.getLikelihood().getLogLikelihood();
            if (nll > lmax) {
                lmax = nll;
                imax = i;
            }
        }
        if (imax < 0) {
            if (m_spec.hasNoise()) {
                m_mapper.setFixedComponent(Component.Noise);
            } else if (m_spec.hasLevel()) {
                m_mapper.setFixedComponent(Component.Level);
            } else {
                m_mapper.setFixedComponent(m_mapper.getComponent(0));
            }
            return start;
        } else {
            Component cmp = mapper.getComponent(imax);
            m_mapper.setFixedComponent(cmp);
            DataBlock np = new DataBlock(p);
            np.set(.1);
            np.set(imax, 1);
            if (mapper.hasCycleDumpingFactor()){
                np.set(nvars++, .9);
            }
            if  (mapper.hasCycleLength()){
                np.set(nvars, 1);
            }
            return mapper.map(np);
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
    public boolean process(double[] y, int freq) {
        return process(y, null, freq);
    }

    /**
     *
     * @param y
     * @param x
     * @param freq
     * @return
     */
    public boolean process(double[] y, SubMatrix x, int freq) {
        AbsMeanNormalizer normalizer = new AbsMeanNormalizer();
        normalizer.process(new ReadDataBlock(y));
        m_y = normalizer.getNormalizedData();
        m_factor = normalizer.getFactor();
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
    public void setSpecification(BsmSpecification spec) {
        m_spec = spec.getModelSpecification().clone();
        m_eps = spec.getPrecision();
        m_dregs = spec.isDiffuseRegressors();
        switch (spec.getOptimizer()) {
            case LevenbergMarquardt:
                m_min = new ProxyMinimizer(new ec.tstoolkit.maths.realfunctions.levmar.LevenbergMarquardtMethod());
                break;
            case MinPack:
                m_min = new ProxyMinimizer(new ec.tstoolkit.maths.realfunctions.minpack.LevenbergMarquardtMinimizer());
                break;
            case LBFGS:
                m_min = new ec.tstoolkit.maths.realfunctions.bfgs.Bfgs();
                break;
            default:
                m_min = null;
        }
        m_bsm = null;
    }

    public void setSpecification(ModelSpecification spec) {
        m_spec = spec.clone();
        m_bsm = null;
    }

    private void updateSpec(BasicStructuralModel bsm) {
        m_spec = bsm.getSpecification();
        Component fixed = m_mapper.getFixedComponent();
        m_mapper = new BsmMapper(m_spec, m_freq, m_mapper.transformation);
        m_mapper.setFixedComponent(fixed);
    }

    /**
     *
     * @param value
     */
    public void useDiffuseRegressors(boolean value) {
        m_dregs = value;
    }

    public IFunction likelihoodFunction() {
        BsmMapper mapper = new BsmMapper(m_bsm.getSpecification(), m_bsm.freq, Transformation.None);
        SsfFunction<BasicStructuralModel> fn = buildFunction(m_bsm, mapper, false);
        double a = (m_ll.getN() - m_ll.getD()) * Math.log(m_factor);
        return new TransformedFunction(fn, TransformedFunction.linearTransformation(-a, 1));
    }

    public IFunctionInstance maxLikelihoodFunction() {
        BsmMapper mapper = new BsmMapper(m_bsm.getSpecification(), m_bsm.freq, Transformation.None);
        IFunction ll = likelihoodFunction();
        return ll.evaluate(mapper.map(m_bsm));
    }

}
