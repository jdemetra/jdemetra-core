/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.arima.regarima.internals;

import demetra.arima.IArimaModel;
import demetra.design.Immutable;
import demetra.maths.matrices.Matrix;

/**
 *
 * @author Jean Palate
 * @param <S>
 */
@Immutable
public class RegArmaEstimation<S extends IArimaModel> {

    private final RegArmaModel<S> model;
    private final double objective;
    private final boolean converged;
    private final double[] score;
    private final Matrix information;
    private final int ndf;

    RegArmaEstimation(final RegArmaModel<S> model, final double objective, final boolean converged, final double[] score,
            final Matrix information, final int ndf) {
        this.model = model;
        this.objective = objective;
        this.converged = converged;
        this.score = score;
        this.information=information;
        this.ndf=ndf;
    }

    /**
     * @return the model
     */
    public RegArmaModel<S> getModel() {
        return model;
    }

    /**
     * @return the objective
     */
    public double getObjective() {
        return objective;
    }

    /**
     * @return the converged
     */
    public boolean isConverged() {
        return converged;
    }
    
    public int getDegreesOfFreedom(){
        return ndf;
    }

    /**
     * @return the curvature
     */
    public Matrix information() {
        return information.deepClone();
    }

    /**
     * @return the score
     */
    public double[] score() {
        return score.clone();
    }

//    public Matrix information(int ndf) {
//        try {
//            // we have to compute the Hessian of the Arma model build on the residuals
//            // otherwise, we might under-estimate the T-Stats.
//            // the differences seem to be very small;
//            // it is normal: coeff and params are asymptotically independent...
//            DataBlock res = m_efn.fn.dmodel.calcRes(new ReadDataBlock(m_efn.getLikelihood().getB()));
//            RegModel Y = new RegModel();
//            Y.setY(res);
//            // we have to complete the model for missing values...
//            if (m_efn.fn.missings != null) {
//                for (int i = 0; i < m_efn.fn.missings.length; ++i) {
//                    Y.addX(m_efn.fn.dmodel.X(i));
//                }
//            }
//            ArmaFunction<S> fn = new ArmaFunction(Y, m_efn.fn.d, m_efn.fn.missings, m_efn.fn.mapper);
////                ArmaFunction<S> fn = new ArmaFunction(m_efn.fn.dmodel, m_efn.fn.d, m_efn.fn.missings, m_efn.fn.mapper);
//            fn.llog = m_llog;
//            fn.filter = new ModifiedLjungBoxFilter();
//            ArmaEvaluation<S> eval = fn.evaluate(m_efn.getParameters());
//
//            if (!m_llog) {
//                ISsqFunctionDerivatives derivatives = fn.getDerivatives((ISsqFunctionInstance) eval);
//                m_information = derivatives.getHessian();
//                m_score = derivatives.getGradient();
//                m_information.mul((.5 * ndf) / eval.getSsqE());
//                for (int i = 0; i < m_score.length; ++i) {
//                    m_score[i] *= (-.5 * ndf) / eval.getSsqE();
//                }
//                //m_information.mul(1 / eval.getLikelihood().getSsqErr());
//            } else {
//                IFunctionDerivatives derivatives = fn.getDerivatives((IFunctionInstance) eval);
//                m_information = derivatives.getHessian();
//                m_score = derivatives.getGradient();
////                    m_information.mul(-ndf);
//                for (int i = 0; i < m_score.length; ++i) {
//                    m_score[i] = -m_score[i];
//                }
//            }
//        } catch (Exception err) {
//            if (m_curvature != null) {
//                m_information = m_curvature.clone();
//                if (!m_llog) {
//                    m_information.mul((.5 * ndf) / m_obj);
//                    for (int i = 0; i < m_score.length; ++i) {
//                        m_score[i] *= (-.5 * ndf) / m_obj;
//                    }
//                } else {
//                    for (int i = 0; i < m_score.length; ++i) {
//                        m_score[i] = -m_score[i];
//                    }
//                }
//            }
//        }
//
//    }
}
