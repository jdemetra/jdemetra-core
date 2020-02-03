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
package demetra.x12;

import demetra.design.BuilderPattern;
import demetra.design.Development;
import demetra.timeseries.regression.Variable;
import jdplus.regarima.IRegArimaProcessor;
import jdplus.regsarima.regular.IRegressionModule;
import jdplus.regsarima.regular.ProcessingResult;
import jdplus.regarima.RegArimaUtility;
import jdplus.regarima.AICcComparator;
import jdplus.regsarima.regular.IModelComparator;
import jdplus.regsarima.regular.ModelDescription;
import jdplus.regsarima.regular.ModelEstimation;
import jdplus.regsarima.regular.RegArimaModelling;
import jdplus.sarima.SarimaModel;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.regression.ILengthOfPeriodVariable;
import demetra.timeseries.regression.ITradingDaysVariable;
import jdplus.regarima.RegArimaEstimation;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class CalendarEffectsDetectionModule implements IRegressionModule {

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(CalendarEffectsDetectionModule.class)
    public static class Builder {

        private ITradingDaysVariable td;
        private ILengthOfPeriodVariable lp;
        private LengthOfPeriodType adjust = LengthOfPeriodType.None;
        private IModelComparator comparator = new AICcComparator(0);
        private double eps = 1e-5;

        public Builder tradingDays(ITradingDaysVariable td) {
            this.td = td;
            return this;
        }

        public Builder leapYear(ILengthOfPeriodVariable lp) {
            this.lp = lp;
            return this;
        }

        public Builder estimationPrecision(double eps) {
            this.eps = eps;
            return this;
        }

        public Builder adjust(LengthOfPeriodType adjust) {
            this.adjust = adjust;
            return this;
        }

        public Builder modelComparator(IModelComparator comparator) {
            this.comparator = comparator;
            return this;
        }

        public CalendarEffectsDetectionModule build() {
            return new CalendarEffectsDetectionModule(this);
        }
    }

    private final IModelComparator comparator;
    private final ITradingDaysVariable td;
    private final ILengthOfPeriodVariable lp;
    private final LengthOfPeriodType adjust;
    private final double eps;

    private CalendarEffectsDetectionModule(Builder builder) {
        this.comparator = builder.comparator;
        this.eps = builder.eps;
        this.lp = builder.lp;
        this.td = builder.td;
        this.adjust = builder.adjust;
    }

    @Override
    public ProcessingResult test(RegArimaModelling context) {

        ModelDescription description = context.getDescription();
        IRegArimaProcessor<SarimaModel> processor = RegArimaUtility.processor(description.getArimaComponent().defaultMapping(), true, eps);

        // builds models with and without td
        ModelDescription ntddesc = ModelDescription.copyOf(description, null);
        boolean removed = ntddesc.removeVariable(var->var.isTradingDays());
        if (lp != null) {
            if (ntddesc.isAdjusted()) {
                ntddesc.setTransformation(LengthOfPeriodType.None);
            } else {
                ntddesc.removeVariable(var->var.isLengthOfPeriod());
            }

        }

        ModelDescription tddesc = ModelDescription.copyOf(ntddesc);
        tddesc.addVariable(new Variable(td, "td", false));
        if (lp != null) {
            if (tddesc.isLogTransformation() && adjust != LengthOfPeriodType.None) {
                tddesc.setTransformation(adjust);
            } else {
                tddesc.addVariable(new Variable(lp, "lp", false));
            }
        }

        RegArimaEstimation<SarimaModel> tdest = tddesc.estimate(processor);
        RegArimaEstimation<SarimaModel> ntdest = ntddesc.estimate(processor);

        boolean changed = false;
        if (comparator.compare(ntdest, tdest) == 0) {
            if (!removed) {
                changed = true;
            }
            context.set(tddesc, tdest);
        } else {
            if (removed) {
                changed = true;
            }
            context.set(ntddesc, ntdest);
        }

        return changed ? ProcessingResult.Changed : ProcessingResult.Unchanged;
    }
}
