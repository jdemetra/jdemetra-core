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

import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.arima.AICcComparator;
import ec.tstoolkit.modelling.arima.IModelComparator;
import ec.tstoolkit.modelling.arima.IPreprocessingModule;
import ec.tstoolkit.modelling.arima.ModelDescription;
import ec.tstoolkit.modelling.arima.ModelEstimation;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.PreprocessingDictionary;
import ec.tstoolkit.modelling.arima.ProcessingResult;
import ec.tstoolkit.modelling.arima.RegArimaEstimator;
import ec.tstoolkit.sarima.SarimaModel;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class LogLevelTest implements IPreprocessingModule {

    private final IModelComparator comparator_;
    private ModelEstimation level_, log_;
    private boolean adjust_ = true;
    private double eps_ = 1e-5;

    public double getEpsilon() {
        return eps_;
    }

    public void setEpsilon(double val) {
        eps_ = val;
    }

    /**
     *
     */
    public LogLevelTest() {
        comparator_ = new AICcComparator(-2);
    }

    /**
     *
     * @param aicdiff
     */
    public LogLevelTest(final double aicdiff) {
        comparator_ = new AICcComparator(aicdiff);
    }

    /**
     *
     * @return
     */
    public boolean isChoosingLog() {
        if (log_ == null) {
            return false;
        } else if (level_ == null) {
            return true;
        } else {
            return comparator_.compare(level_, log_) == 0;
        }
    }

    @Override
    public ProcessingResult process(ModellingContext context) {
        clear();
        addStartInfo(context);
        ModelDescription desc = context.description.clone();
        desc.setTransformation(DefaultTransformationType.None);
        ModelDescription ldesc = null;
        DescriptiveStatistics stats = new DescriptiveStatistics(context.description.getY());
        if (stats.isPositive()) {
            ldesc = context.description.clone();
            ldesc.setTransformation(DefaultTransformationType.Log);
        }
        IParametricMapping<SarimaModel> mapping = X13Preprocessor.createDefaultMapping(context.description);
        RegArimaEstimator monitor = new RegArimaEstimator(mapping);
        monitor.setPrecision(eps_);
        //monitor.setPrecision(1e-7);
        level_ = context.estimation;
        if (level_ == null) {
            try {
                level_ = new ModelEstimation(desc.buildRegArima(), desc.getLikelihoodCorrection());
                level_.compute(monitor, mapping.getDim());
            } catch (Exception err) {
                level_ = null;
            }
        }
        if (ldesc != null) {
            try {
                log_ = new ModelEstimation(ldesc.buildRegArima(), ldesc.getLikelihoodCorrection());
                log_.compute(monitor, mapping.getDim());
            } catch (Exception err) {
                log_ = null;
            }
        }

        if (comparator_.compare(log_, level_) == 0) {
            context.description = desc;
            context.estimation = level_;
        } else {
            context.description = ldesc;
            context.estimation = log_;
        }
        addInfo(context.information);
        addEndInfo(context);
        return ProcessingResult.Changed;
    }

    public DefaultTransformationType getTransformation() {
        return this.isChoosingLog() ? DefaultTransformationType.Log : DefaultTransformationType.None;
    }

    private void addInfo(InformationSet information) {
        InformationSet subset = information.subSet(PreprocessingDictionary.TRANSFORMATION);
//        subset.set("function", getTransformation());
        if (level_ != null) {
//            subset.set("stats_level", level_.getStatistics());
            subset.set("level", level_.getStatistics().AICC);
        }
        if (log_ != null) {
//            subset.set("stats_log", log_.getStatistics());
            subset.set("log", log_.getStatistics().AICC);
        }
    }

    private void clear() {
        log_ = null;
        level_ = null;
    }

    private void addStartInfo(ModellingContext context) {
//        if (context.processingLog != null) {
//            context.processingLog.add(ProcessingInformation.start(LOG_LEVEL,
//                    ec.tstoolkit.modelling.arima.tramo.LogLevelTest.class.getName()));
//        }
    }

    private void addEndInfo(ModellingContext context) {
//        if (context.processingLog != null) {
//            if (level_ != null) {
//                addLevelInfo(context);
//            }
//            if (log_ != null) {
//                addLogInfo(context);
//            }
//            StringBuilder builder = new StringBuilder();
//            builder.append(isChoosingLog() ? LOGS : LEVELS);
//            context.processingLog.add(ProcessingInformation.info(LOG_LEVEL,
//                    LogLevelTest.class.getName(), builder.toString(), null));
//        }
    }

    private void addLogInfo(ModellingContext context) {
//        LikelihoodStatistics stats = log_.getStatistics();
//        StringBuilder builder = new StringBuilder();
//        builder.append("AICC of the model fit to logs = ").append(stats.AICC);
//        context.processingLog.add(ProcessingInformation.info(LOG_LEVEL,
//                ec.tstoolkit.modelling.arima.tramo.LogLevelTest.class.getName(), builder.toString(), log_.getArima()));
    }

    private void addLevelInfo(ModellingContext context) {
//        LikelihoodStatistics stats = level_.getStatistics();
//        StringBuilder builder = new StringBuilder();
//        builder.append("AICC of the model fit to levels = ").append(stats.AICC);
//        context.processingLog.add(ProcessingInformation.info(LOG_LEVEL,
//                ec.tstoolkit.modelling.arima.tramo.LogLevelTest.class.getName(), builder.toString(), level_.getArima()));
    }

    private static final String LOG_LEVEL = "Log/level test",
            LOGS = "Logs chosen", LEVELS = "Levels chosen", LOGS_TITLE = "Likelihood statistics for model fit to logs", LEVELS_TITLE = "Likelihood statistics for model fit to levels";

    /**
     * @return the level_
     */
    public ModelEstimation getLevel() {
        return level_;
    }

    /**
     * @return the log_
     */
    public ModelEstimation getLog() {
        return log_;
    }

}
