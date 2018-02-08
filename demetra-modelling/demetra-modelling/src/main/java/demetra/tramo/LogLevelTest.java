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
import demetra.design.Development;
import demetra.design.IBuilder;
import demetra.modelling.DataTransformation;
import demetra.regarima.IRegArimaProcessor;
import demetra.regarima.RegArimaEstimation;
import demetra.regarima.RegArimaModel;
import demetra.regarima.ami.ILogLevelTest;
import demetra.sarima.SarimaModel;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class LogLevelTest implements ILogLevelTest<SarimaModel> {
    
    public static Builder builder(){
        return new Builder();
    }
    
    public static class Builder implements IBuilder<LogLevelTest>{
        
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

        @Override
        public LogLevelTest build() {
            return new LogLevelTest(logpreference, precision);
        }
        
    }

    private final double logpreference, precision;
    private double level, log = 1, slog;
    private RegArimaEstimation<SarimaModel> e, el;

    /**
     *
     */
    private  LogLevelTest(double logpreference, double precision) {
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
    @Override
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
        return process(TramoUtility.airlineModel(data, true, frequency, seas));
    }
    
    @Override
    public boolean process(RegArimaModel<SarimaModel> levelModel) {
        IRegArimaProcessor processor = TramoUtility.processor(true, precision);
        e = processor.process(levelModel);
        if (e != null) {
            level = Math.log(e.getConcentratedLikelihood().ssq()
                    * e.getConcentratedLikelihood().factor());
        }

        
        double[] lx = levelModel.getY().toArray();
        slog = 0;
        for (int i = 0; i < lx.length; ++i) {
            if (lx[i] <= 0) {
                return false;
            }
            lx[i] = Math.log(lx[i]);
            slog += lx[i];
        }
        slog /= lx.length;

        
        RegArimaModel<SarimaModel> logModel = levelModel.toBuilder()
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

    public DataTransformation getTransformation() {
        return this.isChoosingLog() ? DataTransformation.Log : DataTransformation.None;
    }

}
