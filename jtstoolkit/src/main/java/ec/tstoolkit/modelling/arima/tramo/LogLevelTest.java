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
package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.maths.realfunctions.ProxyMinimizer;
import ec.tstoolkit.maths.realfunctions.levmar.LevenbergMarquardtMethod;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.arima.IPreprocessingModule;
import ec.tstoolkit.modelling.arima.ModelDescription;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.PreprocessingDictionary;
import ec.tstoolkit.modelling.arima.ProcessingResult;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.sarima.estimation.GlsSarimaMonitor;
import ec.tstoolkit.sarima.estimation.SarimaInitializer;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class LogLevelTest extends AbstractTramoModule implements IPreprocessingModule {

    /**
     * Chosen option
     */
    private static final String LOG_LEVEL = "Log/level test",
            LOGS = "Logs chosen", LEVELS = "Levels chosen";

    private double level_, log_ = 1, slog_;
    private double logpreference_;
    private RegArimaEstimation<SarimaModel> e_, el_;

    /**
     *
     */
    public LogLevelTest() {
    }

    /**
     *
     * @param model
     * @return
     */
    public RegArimaModel<SarimaModel> buildModel(int ifreq, double[] data, boolean seas) {
        // use airline model with mean
        SarimaSpecification spec = new SarimaSpecification(ifreq);
        spec.airline(seas);
        RegArimaModel<SarimaModel> m = new RegArimaModel<>(
                new SarimaModel(spec), new DataBlock(data));
        m.setMeanCorrection(true);
        return m;
    }

    /**
     *
     */
    public void clear() {
        level_ = 0;
        log_ = 1;
        e_ = null;
        el_ = null;
    }

    /**
     *
     * @return
     */
    public RegArimaEstimation<SarimaModel> getLevelEstimation() {
        return e_;
    }

    /**
     *
     * @return
     */
    public double getLevelLL() {
        return level_;
    }

    /**
     *
     * @return
     */
    public RegArimaEstimation<SarimaModel> getLogEstimation() {
        return el_;
    }

    /**
     *
     * @return
     */
    public double getLogLL() {
        return log_;
    }

    /**
     *
     * @return
     */
    public double getLogPreference() {
        return logpreference_;
    }

    /**
     *
     * @return
     */
    public boolean isChoosingLog() {
        return el_ == null ? false : log_ + logpreference_ < level_;
    }

    /**
     *
     * @param value
     */
    public void setLogPreference(double value) {
        logpreference_ = value;
    }

    /**
     *
     * @param model
     * @return
     */
    @Override
    public ProcessingResult process(ModellingContext context) {
        try {
            addStartInfo(context);
            clear();
            ModelDescription curModel = context.description;
            if (curModel.getTransformation() != DefaultTransformationType.Auto) {
                return ProcessingResult.Unprocessed;
            }
            context.estimation = null;
            int ifreq = curModel.getFrequency();
            double[] data = curModel.getY();
            RegArimaModel<SarimaModel> reg = buildModel(ifreq, data, context.hasseas);
            //TramoModelEstimator monitor = new TramoModelEstimator();
            SarimaInitializer initializer = new SarimaInitializer();
            initializer.useDefaultIfFailed(false);
            GlsSarimaMonitor monitor = getMonitor(initializer);
            e_ = monitor.process(reg);
            if (e_ != null) {
                level_ = Math.log(e_.likelihood.getSsqErr()
                        * e_.likelihood.getFactor());
                //reg.setArima(e_.model.getArima());
            }
            double[] ldata = data.clone();
            slog_ = 0;
            for (int i = 0; i < ldata.length; ++i) {
                if (ldata[i] <= 0) {
                    curModel.setTransformation(DefaultTransformationType.None);
                    return ProcessingResult.Changed;
                }
                ldata[i] = Math.log(ldata[i]);
                slog_ += ldata[i];
            }
            slog_ /= ldata.length;

            reg = buildModel(ifreq, ldata, context.hasseas);
            el_ = monitor.process(reg);

            if (el_ == null) {
                curModel.setTransformation(DefaultTransformationType.None);
                return ProcessingResult.Changed;
            }
            log_ = Math.log(el_.likelihood.getSsqErr()
                    * el_.likelihood.getFactor())
                    + 2 * slog_;

            curModel.setTransformation(getTransformation());
            return ProcessingResult.Changed;
        } finally {
            addInfo(context.information);
            addEndInfo(context);
        }
    }

    public DefaultTransformationType getTransformation() {
        return this.isChoosingLog() ? DefaultTransformationType.Log : DefaultTransformationType.None;
    }

    private void addInfo(InformationSet information) {
//        InformationSet subset = information.subSet(PreprocessingDictionary.TRANSFORMATION);
//        subset.set("function", getTransformation());
//        subset.set("level", level_);
//        subset.set("log", log_);
    }

    public boolean hasChangedModel() {
        return true;
    }

    private void addEndInfo(ModellingContext context) {
//        if (context.processingLog != null) {
//            if (e_ != null) {
//                addLevelInfo(context);
//            }
//            if (el_ != null) {
//                addLogInfo(context);
//            }
//            StringBuilder builder=new StringBuilder();
//            builder.append(isChoosingLog() ? LOGS : LEVELS);
//            if (e_ != null && el_ != null){
//                builder.append(" (ratio=").append(Math.exp(level_-log_)).append(')');
//            }
//            context.processingLog.add(ProcessingInformation.info(LOG_LEVEL,
//                    LogLevelTest.class.getName(), builder.toString(), null));
//        }
    }

    private void addStartInfo(ModellingContext context) {
//        if (context.processingLog != null) {
//            context.processingLog.add(ProcessingInformation.start(LOG_LEVEL,
//                    LogLevelTest.class.getName()));
//        }
    }

    private void addLogInfo(ModellingContext context) {
//        StringBuilder builder = new StringBuilder();
//        builder.append("Corrected log(SsqErr) for Logs = ").append(log_);
//        context.processingLog.add(ProcessingInformation.info(LOG_LEVEL,
//                LogLevelTest.class.getName(), builder.toString(), el_.model.getArima()));
    }

    private void addLevelInfo(ModellingContext context) {
//        StringBuilder builder = new StringBuilder();
//        builder.append("Log(SsqErr) for Levels = ").append(level_);
//        context.processingLog.add(ProcessingInformation.info(LOG_LEVEL,
//                LogLevelTest.class.getName(), builder.toString(), e_.model.getArima()));
    }
}
