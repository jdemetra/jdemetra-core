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
package ec.tstoolkit.modelling.arima.x13;

import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.arima.estimation.LikelihoodStatistics;
import ec.tstoolkit.modelling.arima.RegArimaEstimator;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.modelling.arima.AICcComparator;
import ec.tstoolkit.modelling.arima.IModelComparator;
import ec.tstoolkit.modelling.arima.IPreprocessingModule;
import ec.tstoolkit.modelling.arima.ModelDescription;
import ec.tstoolkit.modelling.arima.ModelEstimation;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.PreprocessingDictionary;
import ec.tstoolkit.modelling.arima.PreprocessingModelBuilder;
import ec.tstoolkit.modelling.arima.ProcessingResult;
import ec.tstoolkit.sarima.SarimaModel;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class EasterDetection implements IPreprocessingModule {

    private IModelComparator comparer_;
    private int[] duration_ = new int[]{1, 8, 15};
    private double eps_ = 1e-5;

    public double getEpsilon() {
        return eps_;
    }

    public void setEpsilon(double val) {
        eps_ = val;
    }

    public int[] getDurations() {
        return duration_;
    }

    /**
     * @param duration
     */
    public void setDurations(int[] duration) {
        duration_ = duration;
    }

    public void setDuration(int duration) {
        duration_ = new int[]{duration};
    }

    public EasterDetection() {
        comparer_ = new AICcComparator();
    }

    public EasterDetection(IModelComparator cmp) {
        comparer_ = cmp;
    }

    private void addInfo(ModelEstimation[] emodels, InformationSet information, boolean easter) {
//        InformationSet subset = information.subSet(PreprocessingDictionary.EASTER);
//        subset.set("easter", easter);
//        if (emodels != null) {
//            for (int i = 0; i < emodels.length; ++i) {
//                if (emodels[i] != null) {
//                    StringBuilder ename = new StringBuilder();
//                    ename.append("easter(").append(duration_[i]).append(')');
//                    subset.set(ename.toString(), emodels[i].getStatistics().AICC);
//                    subset.set("stats_" + ename.toString(), emodels[i].getStatistics());
//                }
//            }
//        }
    }

    public void clear() {
    }

    @Override
    public ProcessingResult process(ModellingContext context) {
        clear();
        addStartInfo(context);
        // builds models with and without td
        IParametricMapping<SarimaModel> mapping = X13Preprocessor.createDefaultMapping(context.description);
        RegArimaEstimator monitor = new RegArimaEstimator(mapping);
        monitor.setPrecision(eps_);
        int n = duration_.length;
        int icur = -1;
        ModelEstimation[] emodels = new ModelEstimation[n];
        ModelDescription[] desc = new ModelDescription[n];
        int nhp = mapping.getDim();
        if (context.estimation == null) {
            context.estimation = new ModelEstimation(context.description.buildRegArima(), context.description.getLikelihoodCorrection());
            context.estimation.compute(monitor, nhp);
        }
        ModelDescription refdesc = context.description.clone();
        ModelEstimation refest = null;
        if (!PreprocessingModelBuilder.updateEaster(refdesc, 0)) {
            refest = context.estimation;
        } else {
            refest = new ModelEstimation(refdesc.buildRegArima(), refdesc.getLikelihoodCorrection());
            refest.compute(monitor, nhp);
        }

        for (int i = 0; i < n; ++i) {
            desc[i] = context.description.clone();
            if (!PreprocessingModelBuilder.updateEaster(desc[i], duration_[i])) {
                icur = i;
                emodels[i] = context.estimation;
            } else {
                try {
                    emodels[i] = new ModelEstimation(desc[i].buildRegArima(), desc[i].getLikelihoodCorrection());
                    emodels[i].compute(monitor, nhp);
                } catch (Exception err) {

                }
            }
        }

        // choose best model
        int imodel = comparer_.compare(refest, emodels);
        if (imodel < 0) {
            context.description = refdesc;
            context.estimation = refest;
        } else {
            context.description = desc[imodel];
            context.estimation = emodels[imodel];
        }

        addInfo(emodels, context.information, imodel >= 0);
        addEndInfo(emodels, context, imodel);
        return icur == imodel ? ProcessingResult.Unchanged : ProcessingResult.Changed;
    }

    private void addStartInfo(ModellingContext context) {
//        if (context.processingLog != null) {
//            context.processingLog.add(ProcessingInformation.start(EASTER_LENGTH,
//                    EasterDetection.class.getName()));
//        }
    }

    private void addEndInfo(ModelEstimation[] models, ModellingContext context, int sel) {
//        if (context.processingLog != null) {
//            for (int i = 0; i < duration_.length; ++i) {
//                StringBuilder msg = new StringBuilder();
//                msg.append("easter(").append(duration_[i]).append("): AICC=")
//                        .append(models[i].getStatistics().AICC);
//                context.processingLog.add(ProcessingInformation.info(EASTER_LENGTH,
//                        EasterDetection.class.getName(), msg.toString(), null));
//
//            }
//        }
//        if (sel >= 0) {
//            StringBuilder msg = new StringBuilder();
//            msg.append("easter(").append(duration_[sel]).append(") has been chosen");
//            context.processingLog.add(ProcessingInformation.info(EASTER_LENGTH,
//                    EasterDetection.class.getName(), msg.toString(), null));
//        } else {
//            context.processingLog.add(ProcessingInformation.info(EASTER_LENGTH,
//                    EasterDetection.class.getName(), "No easter effect", null));
//
//        }
    }

    private static final String EASTER_LENGTH = "EASTER test";
}
