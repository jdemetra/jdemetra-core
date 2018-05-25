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

import demetra.design.BuilderPattern;
import demetra.design.Development;
import demetra.likelihood.ConcentratedLikelihood;
import demetra.modelling.Variable;
import demetra.modelling.regression.IEasterVariable;
import demetra.modelling.regression.ILengthOfPeriodVariable;
import demetra.modelling.regression.ITradingDaysVariable;
import demetra.regarima.IRegArimaProcessor;
import demetra.regarima.RegArimaEstimation;
import demetra.regarima.RegArimaModel;
import demetra.regarima.regular.IRegressionModule;
import demetra.regarima.regular.ModelDescription;
import demetra.regarima.regular.ProcessingResult;
import demetra.regarima.regular.RegArimaModelling;
import demetra.regarima.RegArimaUtility;
import demetra.sarima.GlsSarimaProcessor;
import demetra.sarima.SarimaModel;

/**
 * This module test for the presence of td, easter and mean in 
 * the initial model (after log/level test)
 * On entry, the model only contains pre-specified regression variables 
 * On exit, it can also contain td, lp, easter and mean
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class DefaultRegressionTest implements IRegressionModule {

    public static final double CVAL = 1.96;
    public static final double T0 = 2, T1 = 2.6;
    public static final double T2 = 2.2;

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(DefaultRegressionTest.class)
    public static class Builder {

        private ITradingDaysVariable td;
        private ILengthOfPeriodVariable lp;
        private IEasterVariable easter;
        private double tmean = CVAL, teaster = CVAL;
        private double twd = T2, t0td = T0, t1td = T1;
        private double fpvalue = 0.05;
        private double precision=1e-5;

        private boolean joinTest = false;
        private boolean testMean = true;

        public Builder tradingDays(ITradingDaysVariable td) {
            this.td = td;
            return this;
        }

        public Builder leapYear(ILengthOfPeriodVariable lp) {
            this.lp = lp;
            return this;
        }

        public Builder easter(IEasterVariable easter) {
            this.easter = easter;
            return this;
        }

        public Builder meanThreshold(double tmean) {
            this.tmean = tmean;
            return this;
        }

        public Builder easterThreshold(double teaster) {
            this.teaster = teaster;
            return this;
        }

        public Builder wdThreshold(double t) {
            this.twd = t;
            return this;
        }

        public Builder tdThreshold0(double t) {
            this.t0td = t;
            return this;
        }

        public Builder tdThreshold1(double t) {
            this.t1td = t;
            return this;
        }

        public Builder fPValue(double f) {
            this.fpvalue = f;
            return this;
        }

        public Builder useJoinTest(boolean join) {
            this.joinTest = join;
            return this;
        }

        public Builder testMean(boolean test) {
            this.testMean = test;
            return this;
        }

        public DefaultRegressionTest build() {
            return new DefaultRegressionTest(this);
        }

        public Builder estimationPrecision(double eps) {
            this.precision = eps;
            return this;
        }

    }

    private final ITradingDaysVariable td;
    private final ILengthOfPeriodVariable lp;
    private final IEasterVariable easter;
    private final IRegressionTest tdTest, wdTest, lpTest, mhTest, meanTest;
    private final double precision;

//    private IRegressionTest tdTest_, wdTest_, lpTest_, mhTest_, meanTest_;
    private DefaultRegressionTest(Builder builder) {
        this.td = builder.td;
        this.lp = builder.lp;
        this.easter = builder.easter;
        tdTest = builder.joinTest ? new FRegressionTest(.05) : new TRegressionTest(builder.t0td, builder.t1td);
        wdTest = new TRegressionTest(builder.twd);
        lpTest = new TRegressionTest(builder.t0td);
        mhTest = new TRegressionTest(builder.teaster);
        meanTest = builder.testMean ? new TRegressionTest(builder.tmean) : null;
        precision=builder.precision;
    }

    private ModelDescription createTestModel(RegArimaModelling current) {
        ModelDescription model = new ModelDescription(current.getDescription());
        // add td, lp and easter
        if (td != null) {
            model.addVariable(new Variable(td, false));
        }
        if (lp != null) {
            model.addVariable(new Variable(lp, false));
        }
        if (easter != null) {
            model.addVariable(new Variable(easter, false));
        }
        model.setAirline(current.isSeasonal());
        model.setMean(true);
        return model;
    }

    @Override
    public ProcessingResult test(final RegArimaModelling context) {
        // estimate the model.
        ModelDescription currentModel = context.getDescription();
        ModelDescription tmpModel = createTestModel(context);
        boolean changed = false;
        RegArimaModel<SarimaModel> regarima = tmpModel.regarima();
        IRegArimaProcessor<SarimaModel> processor = RegArimaUtility.processor(tmpModel.getArimaComponent().defaultMapping(), true, precision);
        RegArimaEstimation<SarimaModel> rslt = processor.process(regarima);
        ConcentratedLikelihood ll = rslt.getConcentratedLikelihood();

        int start = regarima.isMean() ? 1 : 0;
        int nhp = tmpModel.getArimaComponent().getFreeParametersCount();
        // td
        boolean usetd = false;
        if (td != null) {
            Variable variable = tmpModel.variable(td);
            if (variable != null && !variable.isPrespecified()) {
                int pos = start + tmpModel.findPosition(variable.getVariable());
                int dim = variable.getVariable().getDim();
                IRegressionTest test = dim == 1 ? wdTest : tdTest;
                if (test.accept(ll, nhp, pos, dim, null)) {
                    usetd = true;
                } else {
                    currentModel.addVariable(new Variable(td, false));
                    changed = true;
                }
            }
        }
        if (lp != null) {
            Variable variable = tmpModel.variable(lp);
            if (variable != null && !variable.isPrespecified()) {
                int pos = start + tmpModel.findPosition(variable.getVariable());
                if (!usetd || !lpTest.accept(ll, nhp, pos, 1, null)) {
                    currentModel.addVariable(new Variable(lp, false));
                    changed = true;
                }
            }
        }

        if (easter != null) {
            Variable variable = tmpModel.variable(easter);
            if (variable != null && !variable.isPrespecified()) {
                int pos = start + tmpModel.findPosition(variable.getVariable());
                if (!mhTest.accept(ll, nhp, pos, 1, null)) {
                    currentModel.addVariable(new Variable(easter, false));
                    changed = true;
                }
            }
        }
        if (meanTest != null && !meanTest.accept(ll, nhp, 0, 1, null)) {
            currentModel.setMean(false);
            changed = true;
        }

        if (changed) {
            context.setEstimation(null);
        }

        return changed ? ProcessingResult.Changed : ProcessingResult.Unchanged;
    }

}
