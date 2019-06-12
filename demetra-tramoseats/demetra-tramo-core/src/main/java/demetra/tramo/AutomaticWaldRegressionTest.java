/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
import jdplus.dstats.F;
import demetra.stats.ProbabilityType;
import demetra.likelihood.ConcentratedLikelihoodWithMissing;
import demetra.likelihood.LikelihoodStatistics;
import demetra.modelling.regression.Variable;
import jdplus.regarima.IRegArimaProcessor;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaModel;
import demetra.regarima.regular.IRegressionModule;
import demetra.regarima.regular.ModelDescription;
import demetra.regarima.regular.ProcessingResult;
import demetra.regarima.regular.RegArimaModelling;
import jdplus.regarima.RegArimaUtility;
import jdplus.sarima.SarimaModel;
import java.util.Optional;
import demetra.modelling.regression.ILengthOfPeriodVariable;
import demetra.modelling.regression.ITradingDaysVariable;
import demetra.modelling.regression.IEasterVariable;

/**
 * * @author gianluca, jean Correction 22/7/2014. pre-specified Easter effect
 * was not handled with auto-td
 */
public class AutomaticWaldRegressionTest implements IRegressionModule {

    public static final double DEF_TMEAN = 1.96, DEF_TLP = 2, DEF_TEASTER = 2.2, DEF_FPVAL = 0.01, DEF_PCONSTRAINT = .03;

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(AutomaticWaldRegressionTest.class)
    public static class Builder {

        private ITradingDaysVariable td, wd;
        private ILengthOfPeriodVariable lp;
        private IEasterVariable easter;
        private double tmean = DEF_TMEAN, tlp = DEF_TLP, teaster = DEF_TEASTER;
        private double fpvalue = DEF_FPVAL, pconstraint = DEF_PCONSTRAINT;
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

        public Builder fPValue(double p) {
            this.fpvalue = p;
            return this;
        }

        public Builder PConstraint(double p) {
            this.pconstraint = p;
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

        public AutomaticWaldRegressionTest build() {
            return new AutomaticWaldRegressionTest(this);
        }
    }

    private final ITradingDaysVariable td, wd;
    private final ILengthOfPeriodVariable lp;
    private final IEasterVariable easter;
    private final double tmean, teaster, tlp;
    private final double fpvalue, pconstraint;
    private final boolean testMean;
    private final double precision;

    private AutomaticWaldRegressionTest(Builder builder) {
        this.td = builder.td;
        this.wd = builder.wd;
        this.lp = builder.lp;
        this.easter = builder.easter;
        this.fpvalue = builder.fpvalue;
        this.pconstraint = builder.pconstraint;
        this.tmean = builder.tmean;
        this.teaster = builder.teaster;
        this.tlp = builder.tlp;
        this.testMean = builder.testMean;
        this.precision=builder.precision;
    }

    @Override
    public ProcessingResult test(RegArimaModelling context) {

        ModelDescription current = context.getDescription();
        IRegArimaProcessor processor = RegArimaUtility.processor(current.getArimaComponent().defaultMapping(), true, precision);
        // We compute the full model
        ModelDescription test6 = createTestModel(context, td, null);
        RegArimaEstimation<SarimaModel> regarima6 = processor.process(test6.regarima());
        ConcentratedLikelihoodWithMissing ll = regarima6.getConcentratedLikelihood();
        int nhp = test6.getArimaComponent().getFreeParametersCount();
        int df = ll.degreesOfFreedom() - nhp;
        double sigma = ll.ssq() / df;
        LikelihoodStatistics td6Stats = regarima6.statistics(0);

        ModelDescription test1 = createTestModel(context, wd, null);
        RegArimaModel<SarimaModel> reg = RegArimaModel.of(test1.regarima(), regarima6.getModel().arima());
        RegArimaEstimation<SarimaModel> regarima1 = RegArimaEstimation.of(reg, nhp);
        LikelihoodStatistics td1Stats = regarima1.statistics(0);

        ModelDescription test0 = createTestModel(context, null, null);
        reg = RegArimaModel.of(test0.regarima(), regarima6.getModel().arima());
        RegArimaEstimation<SarimaModel> regarima0 = RegArimaEstimation.of(reg, nhp);
        LikelihoodStatistics td0Stats = regarima0.statistics(0);

        // compute the probabilities
        double pdel = 1;
        double ptd = 1;
        double pwd = 1;

        double fdel = (td1Stats.getSsqErr() - td6Stats.getSsqErr()) / (5 * sigma);
        if (fdel > 0) {
            F f = new F(5, df);
            pdel = f.getProbability(fdel, ProbabilityType.Upper);
        }

        double ftd = (td0Stats.getSsqErr() - td6Stats.getSsqErr()) / (6 * sigma);
        if (ftd > 0) {
            F f = new F(6, df);
            ptd = f.getProbability(ftd, ProbabilityType.Upper);
            double fwd = (td0Stats.getSsqErr() - td1Stats.getSsqErr()) / sigma;
            if (fwd > 0) {
                f = new F(1, df);
                pwd = f.getProbability(fwd, ProbabilityType.Upper);

            }
        }
        
        ITradingDaysVariable tdsel=null;
        ILengthOfPeriodVariable lpsel=lp;

        if (pdel < pconstraint && ptd < fpvalue) {// Prefer TD
            tdsel=td;
        } // Prefer WD
        else if (pwd < fpvalue) {
            tdsel=wd;
        } else
            lpsel=null;
               
        ModelDescription model = createTestModel(context, tdsel, lpsel);
        RegArimaEstimation<SarimaModel> regarima = processor.process(model.regarima());
        return update(current, model, tdsel, regarima.getConcentratedLikelihood(), nhp);
    }

    private ModelDescription createTestModel(RegArimaModelling context, ITradingDaysVariable td, ILengthOfPeriodVariable lp) {
        ModelDescription tmp = new ModelDescription(context.getDescription());
        tmp.setAirline(true);
        tmp.setMean(true);
        if (td != null) {
            tmp.addVariable(new Variable(td, "td", false));
            if (lp != null) {
                tmp.addVariable(new Variable(lp, "lp", false));
            }
        }
        if (easter != null) {
            tmp.addVariable(new Variable(easter, "easter", false));
        }
        return tmp;
    }

    private ProcessingResult update(ModelDescription current, ModelDescription test, ITradingDaysVariable aTd, ConcentratedLikelihoodWithMissing ll, int nhp) {
        boolean changed = false;
        if (aTd != null)
                current.addVariable(new Variable(aTd, "td", false));
        if (testMean) {
            boolean mean = Math.abs(ll.tstat(0, nhp, true)) > tmean;
            if (mean != current.isMean()) {
                current.setMean(mean);
                changed = true;
            }
        }
        if (aTd!= null && lp != null) {
            int pos = 1 + test.findPosition(lp);
            if (Math.abs(ll.tstat(pos, nhp, true)) > tlp) {
                current.addVariable(new Variable(lp, "lp", false));
                changed = true;
            }
        }
        if (easter != null) {
            int pos = 1 + test.findPosition(easter);
            if (Math.abs(ll.tstat(pos, nhp, true)) > teaster) {
                current.addVariable(new Variable(easter, "easter", false));
                changed = true;
            }
        }
        return changed ? ProcessingResult.Changed : ProcessingResult.Unchanged;
    }

}
