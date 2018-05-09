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
package demetra.tramo;

import demetra.data.DoubleSequence;
import demetra.design.BuilderPattern;
import demetra.design.Development;
import demetra.modelling.TransformationType;
import demetra.regarima.IRegArimaProcessor;
import demetra.regarima.RegArimaEstimation;
import demetra.regarima.RegArimaModel;
import demetra.regarima.ami.ProcessingResult;
import demetra.regarima.RegArimaUtility;
import demetra.sarima.SarimaModel;
import demetra.regarima.regular.ILogLevelModule;
import demetra.regarima.regular.ModelDescription;
import demetra.regarima.regular.RegArimaContext;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class LogLevelModule implements ILogLevelModule {
    
    public static Builder builder(){
        return new Builder();
    }

    @BuilderPattern(LogLevelModule.class)
    public static class Builder {
        
        private double precision=1e-7;
        private double logpreference=0;
        
        public Builder logPreference(double lp){
            this.logpreference=lp;
            return this;
        }

        public Builder estimationPrecision(double eps){
            this.precision=eps;
            return this;
        }

        public LogLevelModule build() {
            return new LogLevelModule(logpreference, precision);
        }
        
    }

    private final double logpreference, precision;
    private double level, log = 1, slog;
    private RegArimaEstimation<SarimaModel> e, el;

    /**
     *
     */
    private  LogLevelModule(double logpreference, double precision) {
        this.logpreference=logpreference;
        this.precision=precision;
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
     * @return
     * @since 2.2
     */
    public double getLogCorrection() {
        return slog;
    }

    /**
     * @param data
     * @param frequency
     * @param seas
     * @return
     */
    public boolean process(DoubleSequence data, int frequency, boolean seas) {
        return process(RegArimaUtility.airlineModel(data, true, frequency, seas));
    }
    
    public boolean process(RegArimaModel<SarimaModel> model) {
        IRegArimaProcessor processor = TramoUtility.processor(true, precision);
        e = processor.process(model);
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
                .y(DoubleSequence.ofInternal(lx))
                .build();
        el = processor.process(logModel);

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
    public ProcessingResult process(RegArimaContext context) {
        ModelDescription desc = context.getDescription();
        if (desc.isLogTransformation())
            return ProcessingResult.Unprocessed;
        double[] data=desc.transformation().getData();
        if (! process(DoubleSequence.ofInternal(data), desc.getAnnualFrequency(), context.isSeasonal()))
            return ProcessingResult.Failed;
        if (isChoosingLog()){
            desc.setLogTransformation(true);
            context.setEstimation(null);
            return ProcessingResult.Changed;
        }else{
            return ProcessingResult.Unchanged;
        }
    }
    
    

}
