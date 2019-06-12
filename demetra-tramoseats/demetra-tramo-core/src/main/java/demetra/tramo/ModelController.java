/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramo;

import demetra.regarima.regular.IModelEstimator;
import demetra.regarima.regular.ModelDescription;
import demetra.regarima.regular.ModelEstimation;
import demetra.regarima.regular.PreprocessingModel;
import demetra.regarima.regular.ProcessingResult;
import demetra.regarima.regular.RegArimaModelling;

/**
 *
 * @author palatej
 */
abstract class ModelController {

    private IModelEstimator estimator;
    private PreprocessingModel refmodel_;

    PreprocessingModel getReferenceModel() {
        return refmodel_;
    }

    void setReferenceModel(PreprocessingModel model) {
        refmodel_ = model;
    }

    IModelEstimator getEstimator() {
        return estimator;
    }

    void setEstimator(IModelEstimator estimator) {
        this.estimator = estimator;
    }

    abstract ProcessingResult process(RegArimaModelling modelling, TramoProcessor.Context context);

    /**
     *
     * @param context
     * @return True if the mean is significant
     */
    protected boolean checkMean(RegArimaModelling context) {
        ModelDescription desc = context.getDescription();
        ModelEstimation estimation = context.getEstimation();
        if (!desc.isMean()) {
            return true;
        }
        int nhp = desc.getArimaComponent().getFreeParametersCount();
        double ser = estimation.getConcentratedLikelihood().ser(0, nhp, true);
        return Math.abs(estimation.getConcentratedLikelihood().coefficient(0) / ser) >= 1.96;
    }

    protected boolean estimate(RegArimaModelling context, boolean checkmean) {
        if (!estimator.estimate(context)) {
            return false;
        }
        if (checkmean) {
            if (!checkMean(context)) {
                context.getDescription().setMean(false);

                if (!estimator.estimate(context)) {
                    return false;
                }
            }
        }
        return true;
    }

    protected void transferInformation(RegArimaModelling from, RegArimaModelling to) {
        to.setDescription(from.getDescription());
        to.setEstimation(from.getEstimation());
        //        to.information.clear();
        //        to.information.copy(from.information);
    }

    protected void transferInformation(PreprocessingModel from, RegArimaModelling to) {
        to.setDescription(from.getDescription());
        to.setEstimation(from.getEstimation());
        //        to.information.clear();
        //        to.information.copy(from.information);
    }
}
