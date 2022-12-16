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
package jdplus.x13.regarima;

import demetra.regarima.TradingDaysSpec;
import demetra.timeseries.calendars.LengthOfPeriodType;
import nbbrd.design.BuilderPattern;
import demetra.timeseries.regression.Variable;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regsarima.regular.IRegressionModule;
import jdplus.regsarima.regular.ModelDescription;
import jdplus.regsarima.regular.ProcessingResult;
import jdplus.regsarima.regular.RegSarimaModelling;
import jdplus.sarima.SarimaModel;
import demetra.timeseries.regression.ILengthOfPeriodVariable;
import demetra.timeseries.regression.ITradingDaysVariable;
import jdplus.regarima.IRegArimaComputer;
import jdplus.regarima.RegArimaUtility;
import jdplus.regsarima.regular.TradingDaysRegressionComparator;
import jdplus.stats.likelihood.ConcentratedLikelihoodWithMissing;

/**
 * * @author gianluca, jean Correction 22/7/2014. pre-specified Easter effect
 * was not handled with auto-td
 */
public class TradingDaysWaldTest implements IRegressionModule {

    public static final double DEF_TLP = 2;

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(TradingDaysWaldTest.class)
    public static class Builder {

        /**
         * Increasing complexity
         */
        private ITradingDaysVariable td[];
        private ILengthOfPeriodVariable lp;
        private double tlp = DEF_TLP;
        private double fpvalue = TradingDaysSpec.DEF_AUTO_PVALUE1, pconstraint = TradingDaysSpec.DEF_AUTO_PVALUE2;
        private double precision = 1e-3;
        private boolean adjust = true;

        public Builder tradingDays(ITradingDaysVariable[] td) {
            this.td = td.clone();
            return this;
        }

        public Builder leapYear(ILengthOfPeriodVariable lp) {
            this.lp = lp;
            return this;
        }

        public Builder lpThreshold(double tlp) {
            this.tlp = tlp;
            return this;
        }

        public Builder pmodel(double p) {
            this.fpvalue = p;
            return this;
        }

        public Builder pconstraint(double p) {
            this.pconstraint = p;
            return this;
        }

        public Builder estimationPrecision(double eps) {
            this.precision = eps;
            return this;
        }

        public Builder adjust(boolean adjust) {
            this.adjust = adjust;
            return this;
        }

        public TradingDaysWaldTest build() {
            return new TradingDaysWaldTest(this);
        }
    }

    private final ITradingDaysVariable[] td;
    private final ILengthOfPeriodVariable lp;
    private final double tlp;
    private final double fpvalue, pconstraint;
    private final double precision;
    private final boolean adjust;

    private TradingDaysWaldTest(Builder builder) {
        this.td = builder.td;
        this.lp = builder.lp;
        this.tlp = builder.tlp;
        this.fpvalue = builder.fpvalue;
        this.pconstraint = builder.pconstraint;
        this.precision = builder.precision;
        this.adjust = builder.adjust;
    }

    @Override
    public ProcessingResult test(RegSarimaModelling context) {

        // first step: test all trading days
        ModelDescription current = context.getDescription();
        RegArimaEstimation<SarimaModel>[] estimations = TradingDaysRegressionComparator.test(current, td, lp, precision);
        int best = TradingDaysRegressionComparator.waldTest(estimations, fpvalue, pconstraint);

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
            tmp.addVariable(Variable.variable("td", td, X13ModelBuilder.calendarAMI));
        }
        if (lp != null) {
            tmp.addVariable(Variable.variable("lp", lp, X13ModelBuilder.calendarAMI));
        }

        return tmp;
    }

    private ProcessingResult update(ModelDescription current, ModelDescription test, ITradingDaysVariable aTd, ILengthOfPeriodVariable aLp, ConcentratedLikelihoodWithMissing ll, int nhp) {
        boolean changed = false;
        boolean preadjustment=adjust && current.isLogTransformation();
        Variable var = current.variable("td");
        if (aTd != null) {
            if (var != null) {
                if (!var.getCore().equals(aTd)) {
                    current.remove("td");
                    current.addVariable(Variable.variable("td", aTd, X13ModelBuilder.calendarAMI));
                    changed = true;
                }
            } else {
                current.addVariable(Variable.variable("td", aTd, X13ModelBuilder.calendarAMI));
                changed = true;
            }

        } else if (var != null) {
            current.remove("td");
            changed = true;
        }

        var = current.variable("lp");
        if (aLp != null) {
            int pos = test.findPosition(lp);
            double tstat = ll.tstat(pos, nhp, true);
            if (Math.abs(tstat) > tlp) {
                if (var == null) {
                    if (preadjustment && tstat > 0) {
                        if (!current.isAdjusted()) {
                            current.setPreadjustment(aLp.getType());
                            return ProcessingResult.Changed;
                        }
                    } else {
                        current.addVariable(Variable.variable("lp", lp, X13ModelBuilder.calendarAMI));
                        return ProcessingResult.Changed;
                    }
                } else if (preadjustment && tstat > 0) {
                    current.setPreadjustment(aLp.getType());
                    current.remove("lp");
                    return ProcessingResult.Changed;
                } else {
                    return ProcessingResult.Unchanged;
                }
            }
        }
        if (var != null) {
            current.remove("lp");
            changed = true;
        } else if (current.isAdjusted() && preadjustment) {
            current.setPreadjustment(LengthOfPeriodType.None);
            changed = true;
        }
        return changed ? ProcessingResult.Changed : ProcessingResult.Unchanged;
    }
}
