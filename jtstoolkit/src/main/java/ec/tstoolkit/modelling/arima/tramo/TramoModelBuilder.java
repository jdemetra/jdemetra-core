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
package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.RegStatus;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.modelling.Variable;
import ec.tstoolkit.modelling.arima.IModelBuilder;
import ec.tstoolkit.modelling.arima.ModelDescription;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.sarima.SarimaComponent;
import ec.tstoolkit.timeseries.calendars.IGregorianCalendarProvider;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.AbstractTsVariableBox;
import ec.tstoolkit.timeseries.regression.EasterVariable;
import ec.tstoolkit.timeseries.regression.GregorianCalendarVariables;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import ec.tstoolkit.timeseries.regression.ITradingDaysVariable;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.InterventionVariable;
import ec.tstoolkit.timeseries.regression.JulianEasterVariable;
import ec.tstoolkit.timeseries.regression.LaggedTsVariable;
import ec.tstoolkit.timeseries.regression.LeapYearVariable;
import ec.tstoolkit.timeseries.regression.OutlierDefinition;
import ec.tstoolkit.timeseries.regression.Ramp;
import ec.tstoolkit.timeseries.regression.StockTradingDaysVariables;
import ec.tstoolkit.timeseries.regression.TsVariableGroup;
import java.util.ArrayList;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class TramoModelBuilder implements IModelBuilder {

    private final TramoSpecification spec_;
    private final ProcessingContext context_;

    public TramoModelBuilder(TramoSpecification spec) {
        spec_ = spec;
        context_ = ProcessingContext.getActiveContext();
    }

    public TramoModelBuilder(TramoSpecification spec, ProcessingContext context) {
        spec_ = spec;
        if (context != null) {
            context_ = context;
        } else {
            context_ = ProcessingContext.getActiveContext();
        }
    }

    private void initializeArima(ModelDescription model) {
        boolean yearly = model.getFrequency() == 1;
        if (spec_.isUsingAutoModel()) {
            model.setAirline(!yearly);
            model.setMean(true);
        } else if (spec_.getArima() == null) {
            model.setAirline(!yearly);
        } else {
            // should be changed...
            ArimaSpec arima = spec_.getArima();
            SarimaComponent cmp = new SarimaComponent(model.getFrequency());
            cmp.setMean(arima.isMean());
            cmp.setPhi(arima.getPhi());
            cmp.setTheta(arima.getTheta());
            cmp.setD(arima.getD());
            if (!yearly) {
                cmp.setBPhi(arima.getBPhi());
                cmp.setBTheta(arima.getBTheta());
                cmp.setBD(arima.getBD());
            }
            model.setArimaComponent(cmp);
        }
    }

    private void initializeVariables(ModelDescription model, RegressionSpec regSpec) {

        if (!regSpec.isUsed()) {
            return;
        }
        initializeCalendar(model, regSpec.getCalendar());
        if (regSpec.getOutliersCount() > 0) {
            initializeOutliers(model, regSpec.getOutliers());
        }
        if (regSpec.getUserDefinedVariablesCount() > 0) {
            initializeUsers(model, regSpec.getUserDefinedVariables());
        }
        if (regSpec.getInterventionVariablesCount() > 0) {
            initializeInterventions(model, regSpec.getInterventionVariables());
        }
        if (regSpec.getRampsCount() > 0) {
            initializeRamps(model, regSpec.getRamps());
        }
    }

    @Override
    public boolean initialize(ModellingContext context) {
        initializeTransformation(context.description, spec_.getTransform());
        initializeArima(context.description);
        initializeVariables(context.description, spec_.getRegression());

        return true;
    }

    private void initializeTransformation(ModelDescription model, TransformSpec fnSpec) {
        if (fnSpec == null) {
            return;
        }
        model.setTransformation(fnSpec.getFunction());
    }

    private void initializeCalendar(ModelDescription model, CalendarSpec calendar) {
        if (calendar == null) {
            return;
        }
        initializeTradingDays(model, calendar.getTradingDays());
        initializeEaster(model, calendar.getEaster());
    }

    private void initializeTradingDays(ModelDescription model, TradingDaysSpec td) {
        if (!td.isUsed()) {
            return;
        }
        if (td.isStockTradingDays()) {
            initializeStockTradingDays(model, td);
        } else if (td.getHolidays() != null) {
            initializeHolidays(model, td);
        } else if (td.getUserVariables() != null) {
            initializeUserHolidays(model, td);
        } else if (td.isUsed()) {
            initializeDefaultTradingDays(model, td);
        }
    }

    private void initializeEaster(ModelDescription model, EasterSpec easter) {
        if (!easter.isUsed() || model.getFrequency() < 4) {
            return;
        }
        if (easter.isJulian()) {
            JulianEasterVariable var = new JulianEasterVariable();
            var.setDuration(easter.getDuration());
            Variable evar = new Variable(var, ComponentType.CalendarEffect);
            evar.status = easter.isTest() ? RegStatus.ToRemove : RegStatus.Prespecified;
            model.getMovingHolidays().add(evar);
        } else {
            EasterVariable var = new EasterVariable();
            var.setDuration(easter.getDuration());
            var.setType(EasterVariable.Type.Tramo);
            var.includeEaster(easter.getOption().containsEaster());
            var.includeEasterMonday(easter.getOption().containsEasterMonday());
            Variable evar = new Variable(var, ComponentType.CalendarEffect);
            evar.status = easter.isTest() ? RegStatus.ToRemove : RegStatus.Prespecified;
            model.getMovingHolidays().add(evar);
        }

    }

    private void initializeOutliers(ModelDescription model, OutlierDefinition[] outliers) {
        ArrayList<IOutlierVariable> var = new ArrayList<>();
        ArrayList<IOutlierVariable> pvar = new ArrayList<>();
        for (int i = 0; i < outliers.length; ++i) {
            IOutlierVariable v = TramoSpecification.fac.make(outliers[i], model.getEstimationDomain().getFrequency());
            if (outliers[i].prespecified) {
                pvar.add(v);
            } else {
                var.add(v);
            }
        }
        model.addOutliers(var);
        model.addPrespecifiedOutliers(pvar);

    }

    private void initializeUsers(ModelDescription model, TsVariableDescriptor[] uvars) {
        if (uvars == null) {
            return;
        }
        for (int i = 0; i < uvars.length; ++i) {
            ITsVariable var = uvars[i].toTsVariable(context_);
            model.getUserVariables().add(new Variable(var, uvars[i].getEffect().type(), RegStatus.Prespecified));
        }
    }

    private void initializeInterventions(ModelDescription model, InterventionVariable[] interventionVariables) {
        if (interventionVariables == null) {
            return;
        }
        for (int i = 0; i < interventionVariables.length; ++i) {
            Variable var = new Variable(interventionVariables[i], Variable.searchType(interventionVariables[i]), RegStatus.Prespecified);
            model.getUserVariables().add(var);
        }
    }

    private void initializeRamps(ModelDescription model, Ramp[] ramps) {
        if (ramps == null) {
            return;
        }
        for (int i = 0; i < ramps.length; ++i) {
            Variable var = new Variable(ramps[i], ComponentType.Trend, RegStatus.Prespecified);
            model.getUserVariables().add(var);
        }
    }

    private void initializeHolidays(ModelDescription model, TradingDaysSpec td) {
        IGregorianCalendarProvider cal = context_.getGregorianCalendars().get(td.getHolidays());
        if (cal == null) {
            return;
        }
        TradingDaysType tdType = td.getTradingDaysType();
        ITsVariable var = new GregorianCalendarVariables(cal, tdType);
        Variable tvar = new Variable(var, ComponentType.CalendarEffect);
        tvar.status = td.isTest() ? RegStatus.ToRemove : RegStatus.Prespecified;
        model.getCalendars().add(tvar);
        if (td.isLeapYear()) {
            LeapYearVariable lp = new LeapYearVariable(LengthOfPeriodType.LeapYear);
            Variable lvar = new Variable(lp, ComponentType.CalendarEffect);
            lvar.status = td.isTest() ? RegStatus.ToRemove : RegStatus.Prespecified;
            model.getCalendars().add(lvar);
        }
    }

    private void initializeUserHolidays(ModelDescription model, TradingDaysSpec td) {
        String[] userVariables = td.getUserVariables();
        if (userVariables == null || userVariables.length == 0) {
            return;
        }
        ITsVariable[] vars = new ITsVariable[userVariables.length];

        for (int i = 0; i < vars.length; ++i) {
            vars[i] = context_.getTsVariable(userVariables[i]);
            if (vars[i] == null) {
                throw new TramoException(userVariables[i] + " not found");
            }
        }

        TsVariableGroup var = new TsVariableGroup("User-defined calendar variables", vars);
        ITradingDaysVariable tradingDays = AbstractTsVariableBox.tradingDays(var);
        Variable tvar = new Variable(tradingDays, ComponentType.CalendarEffect);
        tvar.status = td.isTest() ? RegStatus.ToRemove : RegStatus.Prespecified;
        model.getCalendars().add(tvar);
    }

    private void initializeDefaultTradingDays(ModelDescription model, TradingDaysSpec td) {
        TradingDaysType tdType = td.getTradingDaysType();
        if (tdType != TradingDaysType.None) {
            ITsVariable var = GregorianCalendarVariables.getDefault(tdType);
            Variable tvar = new Variable(var, ComponentType.CalendarEffect);
            tvar.status = td.isTest() ? RegStatus.ToRemove : RegStatus.Prespecified;
            model.getCalendars().add(tvar);
        }
        if (td.isLeapYear()) {
            LeapYearVariable lp = new LeapYearVariable(LengthOfPeriodType.LeapYear);
            Variable lvar = new Variable(lp, ComponentType.CalendarEffect);
            lvar.status = td.isTest() ? RegStatus.ToRemove : RegStatus.Prespecified;
            model.getCalendars().add(lvar);
        }
    }

    private void initializeStockTradingDays(ModelDescription model, TradingDaysSpec td) {
        ITsVariable var = new StockTradingDaysVariables(td.getStockTradingDays());
        Variable tvar = new Variable(var, ComponentType.CalendarEffect);
        tvar.status = td.isTest() ? RegStatus.ToRemove : RegStatus.Prespecified;
        model.getCalendars().add(tvar);
    }
}
