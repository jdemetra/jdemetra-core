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
package ec.tstoolkit.modelling.arima.x13;

import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.RegStatus;
import ec.tstoolkit.modelling.RegressionTestSpec;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.modelling.Variable;
import ec.tstoolkit.modelling.arima.IModelBuilder;
import ec.tstoolkit.modelling.arima.ModelDescription;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.PreadjustmentType;
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
public class X13ModelBuilder implements IModelBuilder {

    private final RegArimaSpecification spec_;
    private final ProcessingContext context_;

    public X13ModelBuilder(RegArimaSpecification spec) {
        spec_ = spec;
        context_ = ProcessingContext.getActiveContext();
    }

    public X13ModelBuilder(RegArimaSpecification spec, ProcessingContext context) {
        spec_ = spec;
        if (context != null) {
            context_ = context;
        } else {
            context_ = ProcessingContext.getActiveContext();
        }
    }

    private void initializeArima(ModelDescription model) {
        if (spec_.isUsingAutoModel()) {
            model.setAirline(true);
            //model.setMean(true); // to be checked. 28/1/2013
        } else if (spec_.getArima() == null) {
            model.setAirline(true);
        } else {
            // should be changed...
            ArimaSpec arima = spec_.getArima();
            SarimaComponent cmp = new SarimaComponent();
            cmp.setFrequency(model.getFrequency());
            cmp.setMean(arima.isMean());
            cmp.setPhi(arima.getPhi());
            cmp.setTheta(arima.getTheta());
            cmp.setD(arima.getD());
            cmp.setBPhi(arima.getBPhi());
            cmp.setBTheta(arima.getBTheta());
            cmp.setBD(arima.getBD());
            model.setArimaComponent(cmp);
        }
    }

    private void initializeVariables(ModelDescription model, RegressionSpec regSpec) {

        if (regSpec == null) {
            return;
        }
        if (regSpec.getTradingDays().isUsed()) {
            initializeTradingDays(model, regSpec.getTradingDays());
        }
        if (regSpec.getMovingHolidays() != null) {
            initializeMovingHolidays(model, regSpec.getMovingHolidays());
        }
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
        PreadjustmentType type = PreadjustmentType.None;
        if (fnSpec.getFunction() != DefaultTransformationType.Auto) {
            if (fnSpec.getAdjust() == LengthOfPeriodType.LeapYear) {
                type = PreadjustmentType.LeapYear;
            }
            if (fnSpec.getAdjust() == LengthOfPeriodType.LengthOfPeriod) {
                type = PreadjustmentType.LengthOfPeriod;
            }
        }
        model.setTransformation(fnSpec.getFunction(), type);
    }

//    private void initializeCalendar(ModelDescription model, CalendarSpec calendar) {
//        if (calendar == null) {
//            return;
//        }
//        initializeTradingDays(model, calendar.getTradingDays());
//        initializeEaster(model, calendar.getEaster());
//    }
//    private void initializeTradingDays(ModelDescription model, TradingDaysSpec td) {
//        if (td == null) {
//            return;
//        }
//        if (td.getHolidays() != null) {
//            initializeHolidays(model, td);
//        } else if (td.getUserVariables() != null) {
//            initializeUserHolidays(model, td);
//        } else if (td.getTradingDaysType() != TradingDaysType.None) {
//            initializeDefaultTradingDays(model, td);
//        }
//    }
//    private void initializeEaster(ModelDescription model, EasterSpec easter) {
//        if (easter == null) {
//            return;
//        }
//        EasterVariable var = new EasterVariable();
//        var.setDuration(easter.getDuration());
//        var.setType(EasterVariable.Type.Tramo);
//        var.includeEaster(easter.isEasterIncluded());
//        var.includeEasterMonday(easter.isEasterMondayIncluded());
//        Variable evar = new Variable(var, ComponentType.CalendarEffect);
//        evar.status = easter.isTest() ? RegStatus.ToRemove : RegStatus.Prespecified;
//        model.getMovingHolidays().add(evar);
//
//    }
    private void initializeOutliers(ModelDescription model, OutlierDefinition[] outliers) {
        ArrayList<IOutlierVariable> pvar = new ArrayList<>();
        for (int i = 0; i < outliers.length; ++i) {
            IOutlierVariable v = RegArimaSpecification.fac.make(outliers[i]);
            pvar.add(v);
        }
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
        TradingDaysType ttd = td.getTradingDaysType();
        LengthOfPeriodType tlp = td.getLengthOfPeriod();

        boolean auto = td.isAutoAdjust();

        if (ttd != TradingDaysType.None) {
            ITsVariable var = new GregorianCalendarVariables(cal, ttd);
            Variable tvar = new Variable(var, ComponentType.CalendarEffect);
            if (td.getTest() == RegressionTestSpec.Add) {
                tvar.status = RegStatus.ToAdd;
            } else if (td.getTest() == RegressionTestSpec.Remove) {
                tvar.status = RegStatus.ToRemove;
            } else {
                tvar.status = RegStatus.Prespecified;
            }
            model.getCalendars().add(tvar);
        }
        if (tlp != LengthOfPeriodType.None) {
            LeapYearVariable var = new LeapYearVariable(tlp);
            Variable tvar = new Variable(var, ComponentType.CalendarEffect);
            if (td.getTest() == RegressionTestSpec.Add) {
                tvar.status = RegStatus.ToAdd;
            } else if (td.getTest() == RegressionTestSpec.Remove) {
                tvar.status = RegStatus.ToRemove;
            } else {
                tvar.status = RegStatus.Prespecified;
            }
            model.getCalendars().add(tvar);
        }

        if (auto) {
            model.setTransformation(PreadjustmentType.Auto);
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
                throw new X13Exception(userVariables[i] + " not found");
            }
        }

        TsVariableGroup var = new TsVariableGroup("User-defined calendar variables", vars);
        ITradingDaysVariable tradingDays = AbstractTsVariableBox.tradingDays(var);
        Variable tvar = new Variable(tradingDays, ComponentType.CalendarEffect);
        if (td.getTest() == RegressionTestSpec.Add) {
            tvar.status = RegStatus.ToAdd;
        } else if (td.getTest() == RegressionTestSpec.Remove) {
            tvar.status = RegStatus.ToRemove;
        } else {
            tvar.status = RegStatus.Prespecified;
        }
        model.getCalendars().add(tvar);
    }

    private void initializeTradingDays(ModelDescription model, TradingDaysSpec tradingDays) {
        if (tradingDays.isStockTradingDays()) {
            initializeStockTradingDays(model, tradingDays);
        }
        if (tradingDays.getHolidays() != null) {
            initializeHolidays(model, tradingDays);
        } else if (tradingDays.getUserVariables() != null) {
            initializeUserHolidays(model, tradingDays);
        } else {
            initializeDefaultTradingDays(model, tradingDays);
        }
    }

    private void initializeMovingHolidays(ModelDescription model, MovingHolidaySpec[] mh) {
        for (int i = 0; i < mh.length; ++i) {
            if (mh[i].getType() == MovingHolidaySpec.Type.Easter) {
                EasterVariable var = new EasterVariable();
                var.setType(EasterVariable.Type.Uscb);
                Variable tvar = new Variable(var, ComponentType.CalendarEffect);
                var.setDuration(mh[i].getW());
                if (mh[i].getTest() == RegressionTestSpec.Add) {
                    tvar.status = RegStatus.ToAdd;
                } else if (mh[i].getTest() == RegressionTestSpec.Remove) {
                    tvar.status = RegStatus.ToRemove;
                } else {
                    tvar.status = RegStatus.Prespecified;
                }
                model.getMovingHolidays().add(tvar);
            } else if (mh[i].getType() == MovingHolidaySpec.Type.JulianEaster) {
                JulianEasterVariable var = new JulianEasterVariable();
                Variable tvar = new Variable(var, ComponentType.CalendarEffect);
                var.setDuration(mh[i].getW());
                if (mh[i].getTest() == RegressionTestSpec.Add) {
                    tvar.status = RegStatus.ToAdd;
                } else if (mh[i].getTest() == RegressionTestSpec.Remove) {
                    tvar.status = RegStatus.ToRemove;
                } else {
                    tvar.status = RegStatus.Prespecified;
                }
                model.getMovingHolidays().add(tvar);
            }
        }
    }

    private void initializeDefaultTradingDays(ModelDescription model, TradingDaysSpec td) {
        TradingDaysType ttd = td.getTradingDaysType();
        LengthOfPeriodType tlp = td.getLengthOfPeriod();

        boolean auto = td.isAutoAdjust();

        if (ttd != TradingDaysType.None) {
            ITsVariable var = GregorianCalendarVariables.getDefault(ttd);
            Variable tvar = new Variable(var, ComponentType.CalendarEffect);
            if (td.getTest() == RegressionTestSpec.Add) {
                tvar.status = RegStatus.ToAdd;
            } else if (td.getTest() == RegressionTestSpec.Remove) {
                tvar.status = RegStatus.ToRemove;
            } else {
                tvar.status = RegStatus.Prespecified;
            }
            model.getCalendars().add(tvar);
        }
        if (tlp != LengthOfPeriodType.None) {
            LeapYearVariable var = new LeapYearVariable(tlp);
            Variable tvar = new Variable(var, ComponentType.CalendarEffect);
            if (td.getTest() == RegressionTestSpec.Add) {
                tvar.status = RegStatus.ToAdd;
            } else if (td.getTest() == RegressionTestSpec.Remove) {
                tvar.status = RegStatus.ToRemove;
            } else {
                tvar.status = RegStatus.Prespecified;
            }
            model.getCalendars().add(tvar);
        }

        if (auto) {
            model.setTransformation(PreadjustmentType.Auto);
        }
    }

    private void initializeStockTradingDays(ModelDescription model, TradingDaysSpec td) {
        ITsVariable var = new StockTradingDaysVariables(td.getStockTradingDays());
        Variable tvar = new Variable(var, ComponentType.CalendarEffect);
        if (td.getTest() == RegressionTestSpec.Add) {
            tvar.status = RegStatus.ToAdd;
        } else if (td.getTest() == RegressionTestSpec.Remove) {
            tvar.status = RegStatus.ToRemove;
        } else {
            tvar.status = RegStatus.Prespecified;
        }
        model.getCalendars().add(tvar);
    }

//    private void initializeDefaultTradingDays(ModelDescription model, TradingDaysSpec td) {
//        TradingDaysType tdType = td.getTradingDaysType();
//        ITsVariable var = GregorianCalendarVariables.getDefault(tdType);
//        Variable tvar = new Variable(var, ComponentType.CalendarEffect);
//        tvar.status = td.isTest() ? RegStatus.ToRemove : RegStatus.Prespecified;
//
//        model.getCalendars().add(tvar);
//
//        if (td.isLeapYear()) {
//            LeapYearVariable lp = new LeapYearVariable(LengthOfPeriodType.LeapYear);
//            Variable lvar = new Variable(lp, ComponentType.CalendarEffect);
//            lvar.status = td.isTest() ? RegStatus.ToRemove : RegStatus.Prespecified;
//            model.getCalendars().add(lvar);
//        }
//    }
}
