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
package jdplus.x13.regarima;

import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.regression.ILengthOfPeriodVariable;
import demetra.timeseries.regression.ITradingDaysVariable;
import demetra.timeseries.regression.Variable;
import jdplus.regarima.AICcComparator;
import jdplus.regarima.IRegArimaProcessor;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaUtility;
import jdplus.regarima.ami.ModellingUtility;
import jdplus.regsarima.regular.IModelComparator;
import jdplus.regsarima.regular.IRegressionModule;
import jdplus.regsarima.regular.ModelDescription;
import jdplus.regsarima.regular.ProcessingResult;
import jdplus.regsarima.regular.RegSarimaModelling;
import jdplus.sarima.SarimaModel;
import nbbrd.design.BuilderPattern;
import nbbrd.design.Development;

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
    public ProcessingResult test(RegSarimaModelling context) {

        ModelDescription description = context.getDescription();
        IRegArimaProcessor<SarimaModel> processor = RegArimaUtility.processor(true, eps);

        // builds models with and without td
        ModelDescription ntddesc = ModelDescription.copyOf(description, null);
        boolean removed = ntddesc.removeVariable(var -> ModellingUtility.isTradingDays(var));
        if (lp != null) {
            if (ntddesc.isAdjusted()) {
                ntddesc.setPreadjustment(LengthOfPeriodType.None);
            } else {
                ntddesc.removeVariable(var -> ModellingUtility.isLengthOfPeriod(var));
            }

        }

        ModelDescription tddesc = ModelDescription.copyOf(ntddesc);
        tddesc.addVariable(Variable.variable("td", td, X13ModelBuilder.calendarAMI));
        if (lp != null) {
            if (tddesc.isLogTransformation() && adjust != LengthOfPeriodType.None) {
                tddesc.setPreadjustment(adjust);
            } else {
                tddesc.addVariable(Variable.variable("lp", lp, X13ModelBuilder.calendarAMI));
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
