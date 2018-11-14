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
package demetra.x12;

import demetra.data.DoubleSequence;
import demetra.data.transformation.DataTransformation.LogJacobian;
import demetra.data.LogTransformation;
import demetra.design.BuilderPattern;
import demetra.design.Development;
import demetra.modelling.TransformationType;
import demetra.regarima.IRegArimaProcessor;
import demetra.regarima.RegArimaEstimation;
import demetra.regarima.RegArimaModel;
import demetra.regarima.regular.ProcessingResult;
import demetra.regarima.RegArimaUtility;
import demetra.sarima.SarimaModel;
import demetra.regarima.regular.ILogLevelModule;
import demetra.regarima.regular.ModelDescription;
import demetra.regarima.regular.ModelEstimation;
import demetra.regarima.regular.RegArimaModelling;
import demetra.timeseries.calendars.LengthOfPeriodType;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class LogLevelModule implements ILogLevelModule {

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(LogLevelModule.class)
    public static class Builder {

        private double aiccdiff = -2;
        private double precision = 1e-7;
        private LengthOfPeriodType adjust = LengthOfPeriodType.None;

        public Builder adjust(LengthOfPeriodType adjust) {
            this.adjust = adjust;
            return this;
        }

        public Builder estimationPrecision(double eps) {
            this.precision = eps;
            return this;
        }

        public Builder comparator(double aiccdiff) {
            this.aiccdiff = aiccdiff;
            return this;
        }

        public LogLevelModule build() {
            return new LogLevelModule(aiccdiff, precision, adjust);
        }

    }

    private final double aiccDiff;
    private final double precision;
       private final LengthOfPeriodType adjust;
    private ModelEstimation level, log;
    private double aiccLevel, aiccLog;

    private LogLevelModule(double aiccdiff, final double precision, final LengthOfPeriodType adjust) {
        this.aiccDiff = aiccdiff;
        this.precision = precision;
        this.adjust=adjust;
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
            return aiccLevel > aiccLog - aiccDiff;
        }
    }

    @Override
    public ProcessingResult process(RegArimaModelling context) {
        clear();
        ModelDescription model = context.getDescription();
        if (model.getSeries().getValues().anyMatch(z->z<=0))
            return ProcessingResult.Failed;
        IRegArimaProcessor processor = X12Utility.processor(true, precision);
        level = model.estimate(processor);

        ModelDescription logmodel = new ModelDescription(model);
        logmodel.setLogTransformation(true);
        if (adjust != LengthOfPeriodType.None){
            logmodel.remove("lp");
            logmodel.setTransformation(adjust);
        }
        log = logmodel.estimate(processor);
        if (level != null) {
            aiccLevel = level.getStatistics().getAICC();
        }
        if (log != null) {
            aiccLog = log.getStatistics().getAICC();
        }
        if (isChoosingLog()){
            context.setDescription(logmodel);
            context.setEstimation(log);
            return ProcessingResult.Changed;
        }else
            return ProcessingResult.Unchanged;
        
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
    }

    /**
     * @return the level_
     */
    public ModelEstimation getLevel() {
        return level;
    }

    /**
     * @return the log_
     */
    public ModelEstimation getLog() {
        return log;
    }

}
