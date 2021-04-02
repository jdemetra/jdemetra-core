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
package jdplus.tramo;

import jdplus.tramo.internal.TramoUtility;
import nbbrd.design.BuilderPattern;
import nbbrd.design.Development;
import demetra.modelling.TransformationType;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaModel;
import jdplus.regsarima.regular.ProcessingResult;
import jdplus.regarima.RegArimaUtility;
import jdplus.sarima.SarimaModel;
import jdplus.regsarima.regular.ILogLevelModule;
import jdplus.regsarima.regular.ModelDescription;
import jdplus.regsarima.regular.RegSarimaModelling;
import demetra.data.DoubleSeq;
import demetra.processing.ProcessingLog;
import jdplus.regarima.IRegArimaComputer;

/**
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

        private double precision = 1e-5;
        private double logpreference = 0;
        private boolean seasonal = true;

        public Builder logPreference(double lp) {
            this.logpreference = lp;
            return this;
        }

        public Builder estimationPrecision(double eps) {
            this.precision = eps;
            return this;
        }

        public Builder seasonal(boolean seasonal) {
            this.seasonal = seasonal;
            return this;
        }

        public LogLevelModule build() {
            return new LogLevelModule(logpreference, precision, seasonal);
        }

    }

    private final double logpreference, precision;
    private final boolean seasonal;
    private double level, log = 1, slog;
    private RegArimaEstimation<SarimaModel> e, el;

    /**
     *
     */
    private LogLevelModule(double logpreference, double precision, boolean seasonal) {
        this.logpreference = logpreference;
        this.precision = precision;
        this.seasonal = seasonal;

    }

    /**
     *
     * @param ifreq
     * @param data
     * @param seas
     * @param model
     * @return
     */
    /**
     *
     */
    public void clear() {
        level = 0;
        log = 1;
        e = null;
        el = null;
    }

    /**
     *
     * @return
     */
    public RegArimaEstimation<SarimaModel> getLevelEstimation() {
        return e;
    }

    /**
     *
     * @return
     */
    public double getLevelLL() {
        return level;
    }

    /**
     *
     * @return
     */
    public RegArimaEstimation<SarimaModel> getLogEstimation() {
        return el;
    }

    /**
     *
     * @return
     */
    public double getLogLL() {
        return log;
    }

    /**
     *
     * @return
     */
    public double getLogPreference() {
        return logpreference;
    }

    /**
     *
     * @return
     */
    public boolean isChoosingLog() {
        return el == null ? false : log + logpreference < level;
    }

    /**
     *
     * @return @since 2.2
     */
    public double getLogCorrection() {
        return slog;
    }

    /**
     * @param data
     * @param frequency
     * @param seas
     * @param log
     * @return
     */
    public boolean process(DoubleSeq data, int frequency, boolean seas, ProcessingLog log) {
        return process(RegArimaUtility.airlineModel(data, true, frequency, seas), log);
    }

    public boolean process(RegArimaModel<SarimaModel> model, ProcessingLog logs) {
        IRegArimaComputer processor = TramoUtility.processor(true, precision);
        e = processor.process(model, null);
        if (e != null) {
            level = Math.log(e.getConcentratedLikelihood().ssq()
                    * e.getConcentratedLikelihood().factor());
        }

        double[] lx = model.getY().toArray();
        slog = 0;
        for (int i = 0; i < lx.length; ++i) {
            if (lx[i] <= 0) {
                return false;
            }
            lx[i] = Math.log(lx[i]);
            slog += lx[i];
        }
        slog /= lx.length;

        RegArimaModel<SarimaModel> logModel = model.toBuilder()
                .y(DoubleSeq.of(lx))
                .build();
        el = processor.process(logModel, null);

        if (el != null) {
            log = Math.log(el.getConcentratedLikelihood().ssq()
                    * el.getConcentratedLikelihood().factor())
                    + 2 * slog;
        }
        return true;
    }

    public TransformationType getTransformation() {
        return this.isChoosingLog() ? TransformationType.Log : TransformationType.None;
    }

    @Override
    public ProcessingResult process(RegSarimaModelling context) {
        ModelDescription desc = context.getDescription();
        if (desc.isLogTransformation()) {
            return ProcessingResult.Unprocessed;
        }
        DoubleSeq data = desc.getTransformedSeries().getValues();
        ProcessingLog logs = context.getLog();
        if (logs != null) {
            logs.push(LL);
        }
        if (!process(data, desc.getAnnualFrequency(), seasonal, logs)) {
            if (logs != null) {
                logs.warning("failed");
                logs.pop();
            }
            return ProcessingResult.Failed;
        }
        if (logs != null) {
            logs.step("level", this.level);
            logs.step("log", this.log);
            logs.pop();
        }
        if (isChoosingLog()) {
            desc.setLogTransformation(true);
            context.clearEstimation();
            return ProcessingResult.Changed;
        } else {
            return ProcessingResult.Unchanged;
        }
    }

}
