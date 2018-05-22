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
import demetra.dstats.F;
import demetra.dstats.ProbabilityType;
import demetra.likelihood.ConcentratedLikelihood;
import demetra.modelling.Variable;
import demetra.modelling.regression.IEasterVariable;
import demetra.modelling.regression.ILengthOfPeriodVariable;
import demetra.modelling.regression.ITradingDaysVariable;
import demetra.regarima.IRegArimaProcessor;
import demetra.regarima.RegArimaEstimation;
import demetra.regarima.ami.IRegressionModule;
import demetra.regarima.regular.ModelDescription;
import demetra.regarima.ami.ProcessingResult;
import demetra.regarima.regular.RegArimaContext;
import demetra.regarima.RegArimaUtility;

/**
 *
 * @author gianluca
 */
public class AutomaticFRegressionTest implements IRegressionModule {

    public static final double DEF_TMEAN = 1.96, DEF_TLP = 2, DEF_TEASTER = 2.2, DEF_FPVAL = 0.01;

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(AutomaticFRegressionTest.class)
    public static class Builder {

        private ITradingDaysVariable td, wd;
        private ILengthOfPeriodVariable lp;
        private IEasterVariable easter;
        private double tmean = DEF_TMEAN, tlp = DEF_TLP, teaster = DEF_TEASTER;
        private double fpvalue = DEF_FPVAL;
        private boolean testMean = true;
        private double precision = 1e-5;

        public Builder tradingDays(ITradingDaysVariable td) {
            this.td = td;
            return this;
        }

        public Builder workingDays(ITradingDaysVariable wd) {
            this.wd = wd;
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

        public Builder lpThreshold(double tlp) {
            this.tlp = tlp;
            return this;
        }

        public Builder fPValue(double f) {
            this.fpvalue = f;
            return this;
        }

        public Builder testMean(boolean test) {
            this.testMean = test;
            return this;
        }

        public Builder estimationPrecision(double eps) {
            this.precision = eps;
            return this;
        }

        public AutomaticFRegressionTest build() {
            return new AutomaticFRegressionTest(this);
        }
    }

    private final ITradingDaysVariable td, wd;
    private final ILengthOfPeriodVariable lp;
    private final IEasterVariable easter;
    private final double tmean, teaster, tlp;
    private final double fpvalue;
    private final boolean testMean;
    private final double precision;

    private AutomaticFRegressionTest(Builder builder) {
        this.td = builder.td;
        this.wd = builder.wd;
        this.lp = builder.lp;
        this.easter = builder.easter;
        this.fpvalue = builder.fpvalue;
        this.tmean = builder.tmean;
        this.teaster = builder.teaster;
        this.tlp = builder.tlp;
        this.testMean = builder.testMean;
        this.precision=builder.precision;
    }

    @Override
    public ProcessingResult test(RegArimaContext context) {

        ModelDescription current = context.getDescription();
//      First case TD=0 or Just test EE
        ModelDescription test0 = createTestModel(context, null, null);
        IRegArimaProcessor processor = RegArimaUtility.processor(current.getArimaComponent().defaultMapping(), true, precision);
        RegArimaEstimation regarima0 = processor.process(test0.regarima());
        ConcentratedLikelihood ll0 = regarima0.getConcentratedLikelihood();
        int nhp = test0.getArimaComponent().getFreeParametersCount();
        double SS0 = ll0.ssq();

        if (td == null) {
            return update(current, test0, null, ll0, nhp);
        }

        //      Second case TD=TradindDay only
        ModelDescription test6 = createTestModel(context, td, null);
        RegArimaEstimation regarima6 = processor.process(test6.regarima());
        ConcentratedLikelihood ll6 = regarima6.getConcentratedLikelihood();
        double SS6 = ll6.ssq(), SSmc6 = SS6 / (ll6.degreesOfFreedom() - nhp);
        double Ftd = (SS0 - SS6) / (SSmc6 * 6);
        double pFtd6 = 0.0;
        if (Ftd >= 0) {
            F f0 = new F(6, ll6.degreesOfFreedom() - nhp);
            pFtd6 = f0.getProbability(Ftd, ProbabilityType.Lower);
        }

//      Third case TD=WorkingDay only
        ModelDescription test1 = createTestModel(context, wd, null);
        RegArimaEstimation regarima1 = processor.process(test1.regarima());
        ConcentratedLikelihood ll1 = regarima1.getConcentratedLikelihood();
        double SS1 = ll1.ssq(), SSmc1 = SS1 / (ll1.degreesOfFreedom() - nhp);
        Ftd = (SS0 - SS1) / SSmc1;
        double pFtd1 = 0.0;
        if (Ftd >= 0) {
            F f1 = new F(1, ll1.degreesOfFreedom() - nhp);
            pFtd1 = f1.getProbability(Ftd, ProbabilityType.Lower);
        }

// Check over the 3 cases        
        if ((pFtd6 > pFtd1) && (pFtd6 > 1 - fpvalue)) {
            // add leap year
            ModelDescription all = createTestModel(context, td, lp);
            RegArimaEstimation regarima = processor.process(all.regarima());
            return update(current, all, td, regarima.getConcentratedLikelihood(), nhp);
        } else if (pFtd1 < 1 - fpvalue) {
            return update(current, test0, null, ll0, nhp);
        } else {
            // add leap year
            ModelDescription all = createTestModel(context, wd, lp);
            RegArimaEstimation regarima = processor.process(all.regarima());
            return update(current, all, wd, regarima.getConcentratedLikelihood(), nhp);
        }
    }

    private ModelDescription createTestModel(RegArimaContext context, ITradingDaysVariable td, ILengthOfPeriodVariable lp) {
        ModelDescription tmp = new ModelDescription(context.getDescription());
        tmp.setAirline(context.isSeasonal());
        tmp.setMean(true);
        if (td != null) {
            tmp.addVariable(new Variable(td, false));
            if (lp != null) {
                tmp.addVariable(new Variable(lp, false));
            }
        }
        if (easter != null) {
            tmp.addVariable(new Variable(easter, false));
        }
        return tmp;
    }

    private ProcessingResult update(ModelDescription current, ModelDescription test, ITradingDaysVariable aTd, ConcentratedLikelihood ll, int nhp) {
        boolean changed = false;
        if (aTd != null) {
            current.addVariable(new Variable(aTd, false));
        }
        if (testMean) {
            boolean mean = Math.abs(ll.tstat(0, nhp, true)) > tmean;
            if (mean != current.getArimaComponent().isMean()) {
                current.setMean(mean);
                changed = true;
            }
        }
        if (aTd != null && lp != null) {
            int pos = 1 + test.findPosition(lp);
            if (Math.abs(ll.tstat(pos, nhp, true)) > tlp) {
                current.addVariable(new Variable(lp, false));
                changed = true;
            }
        }
        if (easter != null) {
            int pos = 1 + test.findPosition(easter);
            if (Math.abs(ll.tstat(pos, nhp, true)) > teaster) {
                current.addVariable(new Variable(easter, false));
                changed = true;
            }
        }
        return changed ? ProcessingResult.Changed : ProcessingResult.Unchanged;
    }
}
