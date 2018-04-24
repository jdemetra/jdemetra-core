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
import demetra.data.IDataTransformation.LogJacobian;
import demetra.data.LogTransformation;
import demetra.design.BuilderPattern;
import demetra.design.Development;
import demetra.modelling.TransformationType;
import demetra.regarima.IRegArimaProcessor;
import demetra.regarima.RegArimaEstimation;
import demetra.regarima.RegArimaModel;
import demetra.regarima.ami.RegArimaUtility;
import demetra.sarima.SarimaModel;
import demetra.regarima.ami.ILogLevelModule;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class LogLevelModule implements ILogLevelModule<SarimaModel> {

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(LogLevelModule.class)
    public static class Builder {

        private double aiccdiff = -2;
        private double precision = 1e-7;

        public Builder estimationPrecision(double eps) {
            this.precision = eps;
            return this;
        }

        public Builder comparator(double aiccdiff) {
            this.aiccdiff = aiccdiff;
            return this;
        }

        public LogLevelModule build() {
            return new LogLevelModule(aiccdiff, precision);
        }

    }

    private final double aiccDiff;
    private final double precision;
    private RegArimaEstimation<SarimaModel> level, log;
    private double aiccLevel, aiccLog;

    private LogLevelModule(double aiccdiff, final double precision) {
        this.aiccDiff = aiccdiff;
        this.precision = precision;
    }

    public double getEpsilon() {
        return precision;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isChoosingLog() {
        if (log == null) {
            return false;
        } else if (level == null) {
            return true;
        } else {
            return aiccLevel > aiccLog - aiccDiff;
        }
    }

    /**
     * @param data
     * @param frequency
     * @param seas
     * @return
     */
    public boolean process(DoubleSequence data, int frequency, boolean seas) {
        return process(RegArimaUtility.airlineModel(data, false, frequency, seas));
    }
    @Override
    public boolean process(RegArimaModel<SarimaModel> levelModel) {
        clear();
        IRegArimaProcessor processor = RegArimaUtility.processor(true, precision);
        level = processor.process(levelModel);

        int d = levelModel.arima().getDifferencingOrder();
        DoubleSequence y = levelModel.getY();
        LogJacobian lj = new LogJacobian(d, y.length());
        LogTransformation lt = new LogTransformation();
        if (lt.canTransform(y)) {
            DoubleSequence ly = lt.transform(y, lj);
            RegArimaModel<SarimaModel> logModel = levelModel.toBuilder()
                    .y(ly)
                    .build();

            log = processor.process(logModel);
            if (level != null) {
                aiccLevel = level.statistics(0).getAICC();
            }
            if (log != null) {
                aiccLog = log.statistics(lj.value).getAICC();
            }
        }
        return true;
    }

    public TransformationType getTransformation() {
        return this.isChoosingLog() ? TransformationType.Log : TransformationType.None;
    }
    
    public double getAICcLevel(){
        return this.aiccLevel;
    }

    public double getAICcLog(){
        return this.aiccLog;
    }

    private void clear() {
        log = null;
        level = null;
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
