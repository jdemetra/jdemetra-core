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
package jdplus.tramo;

import demetra.timeseries.regression.IEasterVariable;
import demetra.timeseries.regression.ILengthOfPeriodVariable;
import demetra.timeseries.regression.ITradingDaysVariable;
import demetra.timeseries.regression.Variable;
import jdplus.regarima.IRegArimaComputer;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaUtility;
import jdplus.regsarima.regular.IRegressionModule;
import jdplus.regsarima.regular.ModelDescription;
import jdplus.regsarima.regular.ProcessingResult;
import jdplus.regsarima.regular.RegSarimaModelling;
import jdplus.regsarima.regular.TradingDaysRegressionComparator;
import jdplus.sarima.SarimaModel;
import jdplus.stats.likelihood.ConcentratedLikelihoodWithMissing;
import nbbrd.design.BuilderPattern;

/**
 * * @author gianluca, jean Correction 22/7/2014. pre-specified Easter effect
 * was not handled with auto-td
 */
public class AutomaticRegressionTest implements IRegressionModule {

    public static final double DEF_TMEAN = 1.96, DEF_TLP = 2, DEF_TEASTER = 2.2;

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(AutomaticRegressionTest.class)
    public static class Builder {

        /**
         * Increasing complexity
         */
        private ITradingDaysVariable td[];
        private ILengthOfPeriodVariable lp;
        private IEasterVariable easter;
        private boolean aic = true;
        private double tmean = DEF_TMEAN, tlp = DEF_TLP, teaster = DEF_TEASTER;
        private boolean testMean = true;
        private double precision = 1e-5;
        private boolean adjust = false;

        public Builder tradingDays(ITradingDaysVariable[] td) {
            this.td = td.clone();
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

        public Builder testMean(boolean test) {
            this.testMean = test;
            return this;
        }

        public Builder estimationPrecision(double eps) {
            this.precision = eps;
            return this;
        }

        public Builder aic() {
            aic = true;
            return this;
        }

        public Builder bic() {
            aic = false;
            return this;
        }

        /**
         * Indicates if the lp effect can/must be handled as pre-adjustment
         * @param adjust
         * @return 
         */
        public Builder adjust(boolean adjust) {
            this.adjust = adjust;
            return this;
        }

        public AutomaticRegressionTest build() {
            return new AutomaticRegressionTest(this);
        }
    }

    private final ITradingDaysVariable[] td;
    private final ILengthOfPeriodVariable lp;
    private final IEasterVariable easter;
    private final double tmean, teaster, tlp;
    private final boolean testMean;
    private final double precision;
    private final boolean aic;
    private final boolean adjust;

    private AutomaticRegressionTest(Builder builder) {
        this.td = builder.td;
        this.lp = builder.lp;
        this.easter = builder.easter;
        this.tmean = builder.tmean;
        this.teaster = builder.teaster;
        this.tlp = builder.tlp;
        this.testMean = builder.testMean;
        this.precision = builder.precision;
        this.aic = builder.aic;
        this.adjust = builder.adjust;
    }

    @Override
    public ProcessingResult test(RegSarimaModelling context) {

        // first step: test all trading days
        ModelDescription current = context.getDescription();
        RegArimaEstimation<SarimaModel>[] estimations = TradingDaysRegressionComparator.test(current, td, lp, precision);
        int best = aic ? TradingDaysRegressionComparator.bestModel(estimations, TradingDaysRegressionComparator.aiccComparator())
                : TradingDaysRegressionComparator.bestModel(estimations, TradingDaysRegressionComparator.aiccComparator());

        ITradingDaysVariable tdsel = best < 2 ? null : td[best - 2];
        ILengthOfPeriodVariable lpsel = best < 1 ? null : lp;
        IRegArimaComputer processor = RegArimaUtility.processor(true, precision);
        ModelDescription model = createTestModel(context, tdsel, lpsel);
        RegArimaEstimation<SarimaModel> regarima = processor.process(model.regarima(), model.mapping());
        int nhp = current.getArimaSpec().freeParametersCount();
        return update(current, model, tdsel, lpsel, regarima.getConcentratedLikelihood(), nhp);
    }

    private ModelDescription createTestModel(RegSarimaModelling context, ITradingDaysVariable td, ILengthOfPeriodVariable lp) {
        ModelDescription tmp = ModelDescription.copyOf(context.getDescription());
        tmp.setAirline(true);
        tmp.setMean(true);
        if (td != null) {
            tmp.addVariable(Variable.variable("td", td, TramoModelBuilder.calendarAMI));
        }
        if (lp != null) {
            tmp.addVariable(Variable.variable("lp", lp, TramoModelBuilder.calendarAMI));
        }
        if (easter != null) {
            tmp.addVariable(Variable.variable("easter", easter, TramoModelBuilder.calendarAMI));
        }
        return tmp;
    }

    private ProcessingResult update(ModelDescription current, ModelDescription test, ITradingDaysVariable aTd, ILengthOfPeriodVariable aLp, ConcentratedLikelihoodWithMissing ll, int nhp) {
        boolean changed = false;
        boolean preadjustment=adjust && current.isLogTransformation();
        if (aTd != null) {
            current.addVariable(Variable.variable("td", aTd, TramoModelBuilder.calendarAMI));
        }
        if (testMean) {
            boolean mean = Math.abs(ll.tstat(0, nhp, true)) > tmean;
            if (mean != current.isMean()) {
                current.setMean(mean);
                changed = true;
            }
        }
        if (aLp != null) {
            int pos = test.findPosition(aLp);
            double tstat = ll.tstat(pos, nhp, true);
            if (Math.abs(tstat) > tlp) {
                if (preadjustment && tstat > 0) {
                    current.setPreadjustment(lp.getType());
                } else {
                    current.addVariable(Variable.variable("lp", lp, TramoModelBuilder.calendarAMI));
                }
                changed = true;
            }
        }
        if (easter != null) {
            int pos = test.findPosition(easter);
            if (Math.abs(ll.tstat(pos, nhp, true)) > teaster) {
                current.addVariable(Variable.variable("easter", easter, TramoModelBuilder.calendarAMI));
                changed = true;
            }
        }
        return changed ? ProcessingResult.Changed : ProcessingResult.Unchanged;
    }

}
