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
import jdplus.regsarima.regular.TradingDaysRegressionComparator;

/**
 * * @author gianluca, jean Correction 22/7/2014. pre-specified Easter effect
 * was not handled with auto-td
 */
public class AutomaticTradingDaysRegressionTest implements IRegressionModule {

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(AutomaticTradingDaysRegressionTest.class)
    public static class Builder {

        /**
         * Increasing complexity
         */
        private ITradingDaysVariable td[];
        private ILengthOfPeriodVariable lp;
        private boolean aic = true;
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

        public Builder adjust(boolean adjust) {
            this.adjust = adjust;
            return this;
        }

        public AutomaticTradingDaysRegressionTest build() {
            return new AutomaticTradingDaysRegressionTest(this);
        }
    }

    private final ITradingDaysVariable[] td;
    private final ILengthOfPeriodVariable lp;
    private final double precision;
    private final boolean aic;
    private final boolean adjust;

    private AutomaticTradingDaysRegressionTest(Builder builder) {
        this.td = builder.td;
        this.lp = builder.lp;
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
        return update(current, tdsel, lpsel);
    }

    private ProcessingResult update(ModelDescription current, ITradingDaysVariable aTd, ILengthOfPeriodVariable aLp) {
        boolean changed = false;
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
            if (var == null) {
                if (adjust) {
                    if (!current.isAdjusted()) {
                        current.setPreadjustment(LengthOfPeriodType.LeapYear);
                        changed = true;
                    }
                } else {
                    current.addVariable(Variable.variable("lp", lp, X13ModelBuilder.calendarAMI));
                    changed = true;
                }
            }
        } else if (var != null) {
            current.remove("lp");
            changed = true;
        } else if (current.isAdjusted() && adjust) {
            current.setPreadjustment(LengthOfPeriodType.None);
            changed = true;
        }
        return changed ? ProcessingResult.Changed : ProcessingResult.Unchanged;
    }
}
