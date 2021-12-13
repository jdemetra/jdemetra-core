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
package jdplus.x13.regarima;

import demetra.modelling.TransformationType;
import demetra.processing.ProcessingLog;
import demetra.timeseries.calendars.LengthOfPeriodType;
import jdplus.regarima.IRegArimaComputer;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regsarima.regular.ILogLevelModule;
import jdplus.regsarima.regular.ModelDescription;
import jdplus.regsarima.regular.ProcessingResult;
import jdplus.regsarima.regular.RegSarimaModelling;
import jdplus.sarima.SarimaModel;
import nbbrd.design.BuilderPattern;
import nbbrd.design.Development;

/**
 * Identification of log/level transformation
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class LogLevelModule implements ILogLevelModule {

    public static final String LL = "log-level test";

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(LogLevelModule.class)
    public static class Builder {

        private double aiccdiff = -2;
        private double precision = 1e-7;
        private LengthOfPeriodType preadjust = LengthOfPeriodType.None;

        /**
         * When a pre-adjustment is specified, the model in logs is computed as
         * follows:
         * - if the regression variables contain a length of period/leap year,
         * this variable (which must be named "lp") is removed and the specified
         * pre-adjustment is used instead.
         * - otherwise, the model is not modified (same model for logs and
         * levels)
         *
         * The likelihood is automatically adjusted to take into account
         * possible transformations
         *
         * This feature is new in JD+ 3.0
         *
         * @param preadjust
         * @return
         * @since ("3.0")
         */
        public Builder preadjust(LengthOfPeriodType preadjust) {
            this.preadjust = preadjust;
            return this;
        }

        /**
         * Precision used in the estimation of the models (1e-5 by default).
         * In most cases, the precision can be smaller than for the estimation
         * of the final model.
         *
         * @param eps
         * @return
         */
        public Builder estimationPrecision(double eps) {
            this.precision = eps;
            return this;
        }

        /**
         * Correction on the AICc of the model in logs.
         * Negative corrections will favour logs (-2 by default)
         * Same as aiccdiff in the original fortran program
         *
         * @param aiccdiff
         * @return
         */
        public Builder aiccLogCorrection(double aiccdiff) {
            this.aiccdiff = aiccdiff;
            return this;
        }

        public LogLevelModule build() {
            return new LogLevelModule(aiccdiff, precision, preadjust);
        }

    }

    private final double aiccDiff;
    private final double precision;
    private final LengthOfPeriodType preadjust;
    private RegArimaEstimation<SarimaModel> level, log;
    private double aiccLevel, aiccLog;

    private LogLevelModule(double aiccdiff, final double precision, final LengthOfPeriodType preadjust) {
        this.aiccDiff = aiccdiff;
        this.precision = precision;
        this.preadjust = preadjust;
    }

    public double getEpsilon() {
        return precision;
    }

    /**
     *
     * @return
     */
    public boolean isChoosingLog() {
        if (log == null) {
            return false;
        } else if (level == null) {
            return true;
        } else {
            // the best is the smallest (default aiccdiff is negative to favor logs)
            return aiccLevel > aiccLog + aiccDiff;
        }
    }

    @Override
    public ProcessingResult process(RegSarimaModelling modelling) {
        clear();
        ProcessingLog logs = modelling.getLog();
        try {
            logs.push(LL);
            ModelDescription model = modelling.getDescription();
            if (model.getSeries().getValues().anyMatch(z -> z <= 0)) {
                return ProcessingResult.Failed;
            }
            IRegArimaComputer processor = X13Utility.processor(true, precision);
            level = model.estimate(processor);

            ModelDescription logmodel = ModelDescription.copyOf(model);
            logmodel.setLogTransformation(true);
            if (preadjust != LengthOfPeriodType.None && logmodel.remove("lp")) {
                logmodel.setPreadjustment(preadjust);
            }
            log = logmodel.estimate(processor);
            if (level != null) {
                aiccLevel = level.statistics().getAICC();
                logs.info("level", level.statistics());
            }
            if (log != null) {
                aiccLog = log.statistics().getAICC();
                logs.info("level", level.statistics());
            }
            if (level == null && log == null)
                return ProcessingResult.Changed;
            
            if (isChoosingLog()) {
                modelling.set(logmodel, log);
                return ProcessingResult.Changed;
            } else {
                return ProcessingResult.Unchanged;
            }
        } finally {
            logs.pop();
        }
    }

    public TransformationType getTransformation() {
        return this.isChoosingLog() ? TransformationType.Log : TransformationType.None;
    }

    public double getAICcLevel() {
        return this.aiccLevel;
    }

    public double getAICcLog() {
        return this.aiccLog;
    }

    private void clear() {
        log = null;
        level = null;
        aiccLevel=0;
        aiccLog=0;
    }

    /**
     * @return the level_
     */
    public RegArimaEstimation<SarimaModel> getLevel() {
        return level;
    }

    /**
     * @return the log_
     */
    public RegArimaEstimation<SarimaModel> getLog() {
        return log;
    }

}
