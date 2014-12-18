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
package ec.tstoolkit.arima.estimation;

import ec.tstoolkit.BaseException;
import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.RegModel;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.realfunctions.IFunctionDerivatives;
import ec.tstoolkit.maths.realfunctions.IFunctionInstance;
import ec.tstoolkit.maths.realfunctions.IFunctionMinimizer;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.maths.realfunctions.ISsqFunctionDerivatives;
import ec.tstoolkit.maths.realfunctions.ISsqFunctionInstance;
import ec.tstoolkit.maths.realfunctions.ProxyMinimizer;

/**
 *
 * @param <S>
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class GlsArimaMonitor<S extends IArimaModel> implements
        IRegArimaProcessor<S> {

    private IArmaFilter m_filter1 = new KalmanFilter(false),
            m_filter2 = new KalmanFilter(true);// new AnsleyFilter();
    private int m_flimit = 1;
    private boolean m_ml = true, m_llog = false;
    private IFunctionMinimizer m_min = null;// new
    // ec.tstoolkit.maths.functions.minpack.LMMinimizer();
    private IParametricMapping<S> m_mapper;
    private boolean m_bconverged;
    private ArmaFunction<S> m_fn;
    private ArmaEvaluation<S> m_efn;
    private Matrix m_information, m_curvature;
    private double[] m_score;
    private double m_obj;
    private double m_precision = 1e-7;

    /**
     *
     * @return
     */
    public IArmaFilter getFilter1() {
        return m_filter1;
    }

    /**
     *
     * @return
     */
    public IArmaFilter getFilter2() {
        return m_filter2;
    }

    /**
     *
     * @return
     */
    public int getFilterLimit() {
        return m_flimit;
    }

    /**
     *
     * @return
     */
    public IParametricMapping<S> getMapping() {
        return m_mapper;
    }

    /**
     *
     * @return
     */
    public IFunctionMinimizer getMinimizer() {
        return m_min;
    }

    @Override
    public double getPrecision() {
        return m_precision;
    }

    public double[] getScore() {
        return m_score;
    }

    /**
     *
     * @return
     */
    public boolean hasConverged() {
        return m_bconverged;
    }

//    public Matrix getCurvature() {
//        if (m_efn == null) {
//            return null;
//        }
//        if (m_curvature == null){
//            m_curvature=m_fn.getDerivatives((IFunctionInstance)m_efn).getHessian();
//        }
//        return m_curvature;
//        Matrix H = m_fn.getDerivatives((IFunctionInstance) m_efn).getHessian();
//        return H;
//    }
    public Matrix getObservedInformation(int ndf) {
        if (m_efn == null) {
            return null;
        }
        if (m_information == null) {
            try {
                // we have to compute the Hessian of the Arma model build on the residuals
                // otherwise, we might under-estimate the T-Stats.
                // the differences seem to be very small;
                // it is normal: coeff and params are asymptotically independent...
                DataBlock res = m_efn.fn.dmodel.calcRes(new ReadDataBlock(m_efn.getLikelihood().getB()));
                RegModel Y = new RegModel();
                Y.setY(res);
                // we have to complete the model for missing values...
                if (m_efn.fn.missings != null){
                    for (int i=0; i<m_efn.fn.missings.length; ++i){
                        Y.addX(m_efn.fn.dmodel.X(i));
                    }
                }
                ArmaFunction<S> fn = new ArmaFunction(Y, m_efn.fn.d, m_efn.fn.missings, m_efn.fn.mapper);
//                ArmaFunction<S> fn = new ArmaFunction(m_efn.fn.dmodel, m_efn.fn.d, m_efn.fn.missings, m_efn.fn.mapper);
                fn.llog = m_llog;
                fn.filter = new ModifiedLjungBoxFilter();
                ArmaEvaluation<S> eval = fn.evaluate(m_efn.getParameters());

                if (!m_llog) {
                    ISsqFunctionDerivatives derivatives = fn.getDerivatives((ISsqFunctionInstance) eval);
                    m_information = derivatives.getHessian();
                    m_score = derivatives.getGradient();
                    m_information.mul((.5 * ndf) / eval.getSsqE());
                    for (int i = 0; i < m_score.length; ++i) {
                        m_score[i] *= (-.5 * ndf) / eval.getSsqE();
                    }
                    //m_information.mul(1 / eval.getLikelihood().getSsqErr());
                } else {
                    IFunctionDerivatives derivatives = fn.getDerivatives((IFunctionInstance) eval);
                    m_information = derivatives.getHessian();
                    m_score = derivatives.getGradient();
//                    m_information.mul(-ndf);
                    for (int i = 0; i < m_score.length; ++i) {
                        m_score[i] = -m_score[i];
                    }
                }
            } catch (Exception err) {
                if (m_curvature != null) {
                    m_information = m_curvature.clone();
                    if (!m_llog) {
                        m_information.mul((.5 * ndf) / m_obj);
                        for (int i = 0; i < m_score.length; ++i) {
                            m_score[i] *= (-.5 * ndf) / m_obj;
                        }
                    } else {
                        for (int i = 0; i < m_score.length; ++i) {
                            m_score[i] = -m_score[i];
                        }
                    }
                }
            }
//            m_information = m_fn.getDerivatives((IFunctionInstance) m_efn).getHessian();
        }
        return m_information;
    }

    public double getMinimum() {
        return m_obj;
    }

    /**
     *
     * @param regs
     * @return
     */
    public S initialize(RegArimaModel<S> regs) {
        clear();
        return null;
    }

    /**
     *
     * @return
     */
    public boolean isUsingLogLikelihood() {
        return m_llog;
    }

    /**
     *
     * @return
     */
    public boolean isUsingMaximumLikelihood() {
        return m_ml;
    }

    @Override
    public RegArimaEstimation<S> optimize(RegArimaModel<S> regs) {
        return optimize(regs, regs.getArma());
    }

    /**
     *
     * @param regs
     * @param start
     * @return
     */
    @SuppressWarnings("unchecked")
    public RegArimaEstimation<S> optimize(RegArimaModel<S> regs, S start) {
        clear();
        if (m_mapper.getDim() == 0) {
            return new RegArimaEstimation<>(regs, regs.computeLikelihood());
        }
        try {
            IFunctionMinimizer fmin;
            if (m_min != null) {
                fmin = m_min.exemplar();
                fmin.setConvergenceCriterion(m_precision);
//                if (fmin instanceof ProxyMinimizer) {
//                    m_llog = false;
//                }
            } else {
                fmin = new ProxyMinimizer(
                        new ec.tstoolkit.maths.realfunctions.levmar.LevenbergMarquardtMethod());
                fmin.setConvergenceCriterion(m_precision);
                m_llog = false;
            }

            S arma = start;
            if (arma == null) {
                arma = regs.getArma();
            }
            m_fn = new ArmaFunction<>(regs.getDModel(), regs.getArima().getNonStationaryARCount(), regs
                    .getMissings(), m_mapper);
            if (regs.getVarsCount() > m_flimit) {
                m_fn.filter = m_filter2;
            } else {
                m_fn.filter = m_filter1;
            }
            m_fn.ml = m_ml;
            m_fn.llog = m_llog;
            // starting values...
            ArmaEvaluation<S> fstart = new ArmaEvaluation<>(m_fn, arma);
            m_bconverged = fmin.minimize(m_fn, fstart);
            m_efn = (ArmaEvaluation<S>) fmin.getResult();
            RegArimaModel<S> nregs = new RegArimaModel<>(regs);
            S narima = m_mapper.map(m_efn.getParameters());
            if (narima != null) {
                nregs.setArima(narima);
            }
            RegArimaEstimation<S> rslt = new RegArimaEstimation<>(nregs, m_efn
                    .getLikelihood());

            m_obj = fmin.getObjective();
            m_curvature = fmin.getCurvature();
            m_score = fmin.getGradient();
            return rslt;
        } catch (BaseException ex) {
            return null;
        }
    }

    /**
     *
     * @param regs
     * @return
     */
    @Override
    public RegArimaEstimation<S> process(RegArimaModel<S> regs) {
        S start = initialize(regs);
        return optimize(regs, start);
    }

    /**
     *
     * @param value
     */
    public void setFilter1(IArmaFilter value) {
        m_filter1 = value;
    }

    /**
     *
     * @param value
     */
    public void setFilter2(IArmaFilter value) {
        m_filter2 = value;
    }

    /**
     *
     * @param value
     */
    public void setFilterLimit(int value) {
        m_flimit = value;
    }

    /**
     *
     * @param value
     */
    public void setMapping(IParametricMapping<S> value) {
        m_mapper = value;
    }

    /**
     *
     * @param value
     */
    public void setMinimizer(IFunctionMinimizer value) {
        m_min = value;
        if (m_min != null) {
            m_min.setConvergenceCriterion(m_precision);
        }
    }

    /**
     *
     * @param value
     */
    @Override
    public void setPrecision(double value) {
        m_precision = value;
        if (m_min != null) {
            m_min.setConvergenceCriterion(value);
        }
    }

    /**
     *
     * @param value
     */
    public void useLogLikelihood(boolean value) {
        m_llog = value;
    }

    /**
     *
     * @param value
     */
    public void useMaximumLikelihood(boolean value) {
        m_ml = value;
    }

    private void clear() {
        m_obj = Double.NaN;
        m_information = null;
        m_curvature = null;
        m_score = null;
        m_fn = null;
        m_efn = null;

    }
}
