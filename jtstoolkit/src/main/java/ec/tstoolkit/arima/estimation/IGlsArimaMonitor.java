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
import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.eco.RegModel;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.realfunctions.IFunctionInstance;
import ec.tstoolkit.maths.realfunctions.IFunctionMinimizer;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.maths.realfunctions.ProxyMinimizer;

/**
 * 
 * @param <S>
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class IGlsArimaMonitor<S extends IArimaModel> implements
        IRegArimaProcessor<S> {

    private IArmaFilter m_filter1 = new KalmanFilter(false),
            m_filter2 = new KalmanFilter(true);// new AnsleyFilter();
    private int m_flimit = 1;
    private boolean m_ml = true, m_llog;
    private IFunctionMinimizer m_min = null;// new
    // ec.tstoolkit.maths.functions.minpack.LMMinimizer();
    private IParametricMapping<S> m_mapper;
    private boolean m_bconverged;
    private ArmaFunction<S> m_fn;
    private ArmaEvaluation<S> m_efn;
    private Matrix m_curvature;
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
     * The mapper initialized through a call to the setStationaryMapper method.
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

    /**
     * 
     * @return
     */
    public boolean hasConverged() {
        return m_bconverged;
    }

    /**
     * 
     * @param regs
     * @return
     */
    public S initialize(RegArimaModel<S> regs) {
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
    public boolean isUsingML() {
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
        try {
            IFunctionMinimizer fmin;
            if (m_min != null) {
                fmin = m_min.exemplar();
                if (fmin instanceof ProxyMinimizer) {
                    m_llog = false;
                }
            } else {
                fmin = new ProxyMinimizer(
                        new ec.tstoolkit.maths.realfunctions.levmar.LevenbergMarquardtMethod());
                fmin.setConvergenceCriterion(m_precision);
                m_llog = false;
            }

            ArmaEvaluation<S> nfn = null;

            RegModel dmodel = regs.getDModel();
            RegModel cmodel = new RegModel();
            S arma = start;
            if (arma == null) {
                arma = regs.getArma();
            }
            ArmaFunction<S> fn = new ArmaFunction<>(dmodel, regs.getArima().getNonStationaryARCount(), regs.getMissings(), m_mapper);
            if (regs.getVarsCount() > m_flimit) {
                fn.filter = m_filter2;
            } else {
                fn.filter = m_filter1;
            }
            m_efn = new ArmaEvaluation<>(fn, arma);
            ConcentratedLikelihood ll = m_efn.getLikelihood();
            double obj1 = ll.getLogLikelihood();
            double obj0 = 0;
            int niter = 0;
            do {
                obj0 = obj1;
                DataBlock e = dmodel.calcRes(new DataBlock(ll.getB()));
                cmodel.setY(e);
                ArmaFunction<S> cfn = new ArmaFunction<>(cmodel, 0, null,
                        m_mapper);
                cfn.llog = m_llog;
                cfn.ml = m_ml;
                cfn.filter = m_filter1;
                ArmaEvaluation<S> fstart = new ArmaEvaluation<>(cfn, arma);
                m_bconverged = fmin.minimize(cfn, fstart);
                nfn = (ArmaEvaluation<S>) fmin.getResult();
                arma = nfn.arma;

                // computes the residuals, if need be
                if (dmodel.getVarsCount() > 0) {
                    m_efn = new ArmaEvaluation<>(fn, arma);
                    ll = m_efn.getLikelihood();
                    obj1 = ll.getLogLikelihood();
                } else {
                    m_efn = nfn;
                    ll = nfn.getLikelihood();
                }
                if (niter++ > 50)
                    break;
            } while (Double.isNaN(obj1)
                    || (obj1 - obj0) > m_precision);

            RegArimaModel<S> nregs = new RegArimaModel<>(regs);
            S narima = m_mapper.map(m_efn.getParameters());
            if (narima != null) {
                nregs.setArima(narima);
            }
            RegArimaEstimation<S> rslt = new RegArimaEstimation<>(nregs, ll);

            m_curvature = fmin.getCurvature();
            m_obj = fmin.getObjective();

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
     * The mapper should be able to generate a suitable stationary model of type
     * S, for a given set of parameters.
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
    public void useML(boolean value) {
        m_ml = value;
    }

    public Matrix getCurvature() {
        if (m_efn == null) {
            return null;
        }
        if (m_curvature == null) {
            m_curvature = m_fn.getDerivatives((IFunctionInstance) m_efn).getHessian();
        }
        return m_curvature;
    }

    public Matrix getInformationMatrix() {
        if (m_efn == null) {
            return null;
        }
        if (m_curvature == null) {
            m_curvature = m_fn.getDerivatives((IFunctionInstance) m_efn).getHessian();
        }
        if (m_llog) {
            return m_curvature;
        } else {
            return m_curvature.times( .5/m_obj);
        }
    }

    public double getMinimum() {
        return m_obj;
    }
}
