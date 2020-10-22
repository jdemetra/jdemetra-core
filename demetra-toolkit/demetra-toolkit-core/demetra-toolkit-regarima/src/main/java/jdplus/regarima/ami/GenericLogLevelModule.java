/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.regarima.ami;

import demetra.DemetraException;
import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import nbbrd.design.BuilderPattern;
import demetra.util.IntList;
import jdplus.arima.ArimaModel;
import jdplus.regarima.GlsArimaProcessor;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaModel;
import jdplus.stats.RobustStandardDeviationComputer;

/**
 *
 * @author PALATEJ
 */
public class GenericLogLevelModule {

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(GenericLogLevelModule.class)
    public static class Builder {

        private int[] periodicities = new int[]{12};
        private double abnormal = 5;
        private double logPreference = 0;

        public Builder periodicities(int[] periodicities) {
            this.periodicities = periodicities;
            return this;
        }

        public Builder abnormalityThreshold(double abnormal) {
            this.abnormal = abnormal;
            return this;
        }

        public Builder logPreference(double logPreference) {
            this.logPreference = logPreference;
            return this;
        }

        public GenericLogLevelModule build() {
            return new GenericLogLevelModule(this);
        }

    }


    private final int[] periodicities;
    private final double abnormal;
    private final double logPreference;

    private GenericLogLevelModule(Builder builder) {
        this.periodicities = builder.periodicities;
        this.abnormal = builder.abnormal;
        this.logPreference = builder.logPreference;
    }
    private double log, level;

    public void process(DoubleSeq data) {
        DoubleSeq ldata = data.log();
        int n=data.length();

        // compute minimal differencing
        DoubleSeq d = data;
        DoubleSeq ld = ldata;
        int del=1;
            d = d.delta(1);
            ld = ld.delta(1);
        for (int i = 0; i < periodicities.length; ++i) {
            d = d.delta(periodicities[i]);
            ld = ld.delta(periodicities[i]);
            del+=periodicities[i];
        }

        double std = RobustStandardDeviationComputer.mad(50, true).compute(d);
        double lstd = RobustStandardDeviationComputer.mad(50, true).compute(ld);

        // we exclude figures that are really abnormal in both transformations
        IntList missing = new IntList();
        for (int i = 0; i < d.length(); ++i) {
            double cur = d.get(i), lcur = ld.get(i);
            if (Math.abs(cur) > abnormal * std && Math.abs(lcur) < abnormal * lstd) {
                missing.add(i+del);
            }
        }
        MultiPeriodicAirlineMapping mapping = new MultiPeriodicAirlineMapping(periodicities);
        GlsArimaProcessor<ArimaModel> processor = GlsArimaProcessor.builder(ArimaModel.class)
                .precision(1e-5)
                .build();
        RegArimaModel model = RegArimaModel.<ArimaModel>builder()
                .y(data)
                .missing(missing.toArray())
                .arima(mapping.map(mapping.getDefaultParameters()))
                .build();

        RegArimaEstimation e = processor.process(model, mapping);
        if (e != null) {
            level = Math.log(e.getConcentratedLikelihood().ssq()
                    * e.getConcentratedLikelihood().factor());
        }

        double slog = 0;
        int m = 0;
        DoubleSeqCursor cursor = ldata.cursor();
        cursor.skip(del);
        for (int i = del; i < n; ++i) {
            double lx=cursor.getAndNext();
            if (lx <= 0) {
                throw new DemetraException();
            }
            if (!missing.contains(i)) {
                slog += lx;
                ++m;
            }
        }
        slog /= m;

        RegArimaModel<ArimaModel> logModel = model.toBuilder()
                .y(ldata)
                .build();
        RegArimaEstimation el = processor.process(logModel, mapping);

        if (el != null) {
            log = Math.log(el.getConcentratedLikelihood().ssq()
                    * el.getConcentratedLikelihood().factor())
                    + 2 * slog;
        }
    }

    /**
     * @return the log
     */
    public double getLog() {
        return log;
    }

    /**
     * @return the level
     */
    public double getLevel() {
        return level;
    }

    public boolean isChoosingLog() {
        return log + logPreference < level;
    }
}
