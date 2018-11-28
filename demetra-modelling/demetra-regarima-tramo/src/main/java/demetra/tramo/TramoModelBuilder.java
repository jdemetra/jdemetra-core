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

import demetra.data.AverageInterpolator;
import demetra.design.Development;
import demetra.information.InformationSet;
import demetra.modelling.PreadjustmentVariable;
import demetra.modelling.TransformationType;
import demetra.modelling.Variable;
import demetra.modelling.regression.AdditiveOutlier;
import demetra.modelling.regression.EasterVariable;
import demetra.modelling.regression.GenericTradingDaysVariables;
import demetra.modelling.regression.IEasterVariable;
import demetra.modelling.regression.ILengthOfPeriodVariable;
import demetra.modelling.regression.IOutlier;
import demetra.modelling.regression.ITradingDaysVariable;
import demetra.modelling.regression.ITsVariable;
import demetra.modelling.regression.JulianEasterVariable;
import demetra.modelling.regression.LengthOfPeriodVariable;
import demetra.modelling.regression.LevelShift;
import demetra.modelling.regression.ModellingContext;
import demetra.modelling.regression.OutlierDefinition;
import demetra.modelling.regression.PeriodicOutlier;
import demetra.modelling.regression.StockTradingDaysVariables;
import demetra.modelling.regression.TradingDaysType;
import demetra.modelling.regression.TransitoryChange;
import demetra.modelling.regression.TsVariable;
import demetra.modelling.regression.TsVariableBox;
import demetra.modelling.regression.TsVariableGroup;
import demetra.regarima.regular.IModelBuilder;
import demetra.regarima.regular.ModelDescription;
import demetra.regarima.regular.SarimaComponent;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDataSupplier;
import demetra.timeseries.TsDomain;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.GenericTradingDays;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.simplets.TsDataToolkit;
import java.time.LocalDateTime;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
class TramoModelBuilder implements IModelBuilder {

    private final TramoSpec spec;
    private final ModellingContext context;

    public TramoModelBuilder(TramoSpec spec, ModellingContext context) {
        this.spec = spec;
        if (context != null) {
            this.context = context;
        } else {
            this.context = ModellingContext.getActiveContext();
        }
    }

    private void initializeArima(ModelDescription model) {
        int freq = model.getAnnualFrequency();
        boolean yearly = freq == 1;
        if (spec.isUsingAutoModel()) {
            model.setAirline(!yearly);
            model.setMean(true);
        } else if (spec.getArima() == null) {
            model.setAirline(!yearly);
        } else {
            SarimaComponent cmp = model.getArimaComponent();
            ArimaSpec arima = spec.getArima();
            cmp.setPeriod(freq);
            cmp.setMu(arima.getMu());
            cmp.setPhi(arima.getPhi());
            cmp.setTheta(arima.getTheta());
            cmp.setD(arima.getD());
            if (!yearly) {
                cmp.setBPhi(arima.getBPhi());
                cmp.setBTheta(arima.getBTheta());
                cmp.setBD(arima.getBd());
            }
        }
    }

    private void initializeVariables(ModelDescription model, RegressionSpec regSpec) {

        if (!regSpec.isUsed()) {
            return;
        }
        Map<String, double[]> preadjustment = regSpec.getAllFixedCoefficients();
        initializeCalendar(model, regSpec.getCalendar(), preadjustment);
        if (regSpec.getOutliersCount() > 0) {
            initializeOutliers(model, regSpec.getOutliers(), preadjustment);
        }
//        if (regSpec.getUserDefinedVariablesCount() > 0) {
//            initializeUsers(model, regSpec.getUserDefinedVariables(), preadjustment);
//        }
//        if (regSpec.getInterventionVariablesCount() > 0) {
//            initializeInterventions(model, regSpec.getInterventionVariables(), preadjustment);
//        }
//        if (regSpec.getRampsCount() > 0) {
//            initializeRamps(model, regSpec.getRamps(), preadjustment);
//        }
    }

    @Override
    public ModelDescription build(TsData series, InformationSet log) {
        TsData nseries = TsDataToolkit.select(series, spec.getTransform().getSpan());
        ModelDescription cur = new ModelDescription(TsDataToolkit.select(nseries, spec.getEstimate().getSpan()));

        initializeMissing(cur);
        initializeTransformation(cur, spec.getTransform());
        initializeArima(cur);
        initializeVariables(cur, spec.getRegression());

        return cur;
    }

    private void initializeMissing(ModelDescription cur) {
        cur.interpolate(AverageInterpolator.interpolator());
    }

    private void initializeTransformation(ModelDescription model, TransformSpec fnSpec) {
        if (fnSpec.getFunction() == TransformationType.Log) {
            model.setLogTransformation(true);
        }
    }

    private void initializeCalendar(ModelDescription model, CalendarSpec calendar, Map<String, double[]> preadjustment) {
        initializeTradingDays(model, calendar.getTradingDays(), preadjustment);
        initializeEaster(model, calendar.getEaster(), preadjustment);
    }

    private void initializeTradingDays(ModelDescription model, TradingDaysSpec td, Map<String, double[]> preadjustment) {
        if (!td.isUsed() || td.isTest()) {
            return;
        }
        if (td.isStockTradingDays()) {
            initializeStockTradingDays(model, td, preadjustment);
//        } else if (td.getHolidays() != null) {
//            initializeHolidays(model, td, preadjustment);
        } else if (td.getUserVariables() != null) {
            initializeUserTradingDays(model, td, preadjustment);
        } else {
            initializeDefaultTradingDays(model, td, preadjustment);
        }
    }

    private void initializeEaster(ModelDescription model, EasterSpec easter, Map<String, double[]> preadjustment) {
        if (!easter.isUsed() || easter.isTest()) {
            return;
        }
        add(model, easter(spec), preadjustment);
    }

    private void initializeOutliers(ModelDescription model, OutlierDefinition[] outliers, Map<String, double[]> preadjustment) {
        int freq = model.getAnnualFrequency();
        IOutlier<TsDomain>[] vars = new IOutlier[outliers.length];
        TransitoryChange.Factory tc = new TransitoryChange.Factory(spec.getOutliers().getDeltaTC());
        PeriodicOutlier.Factory so = new PeriodicOutlier.Factory(freq, false);
        for (int i = 0; i < outliers.length; ++i) {
            String code = outliers[i].getCode();
            LocalDateTime pos = outliers[i].getPosition();
            IOutlier v;
            switch (code) {
                case AdditiveOutlier.CODE:
                    v = AdditiveOutlier.FACTORY.make(pos);
                    break;
                case LevelShift.CODE:
                    v = LevelShift.FACTORY_ZEROSTARTED.make(pos);
                    break;
                case PeriodicOutlier.CODE:
                    v = so.make(pos);
                    break;
                case TransitoryChange.CODE:
                    v = tc.make(pos);
                    break;
                default:
                    v = null;
            }
            if (v != null) {
                v = (IOutlier) v.rename(IOutlier.defaultName(code, pos, model.getDomain()));
                double[] c = preadjustment.get(v.getName());
                if (c != null) {
                    model.addPreadjustmentVariable(new PreadjustmentVariable(v, c));
                } else {
                    model.addVariable(new Variable(v, true));
                }
            }
        }
    }

//    private void initializeUsers(ModelDescription model, TsVariableDescriptor[] uvars, Map<String, double[]> preadjustment) {
//        if (uvars == null) {
//            return;
//        }
//        for (int i = 0; i < uvars.length; ++i) {
//            ITsVariable var = uvars[i].toTsVariable(context);
//            String sname = ITsVariable.shortName(var.getName());
//            if (preadjustment.containsKey(sname)) {
//                PreadjustmentVariable pv = PreadjustmentVariable.userVariable(var, uvars[i].getEffect().type(), preadjustment.get(sname));
//                model.addPreadjustment(pv);
//            } else {
//                Variable uvar = Variable.userVariable(var, uvars[i].getEffect().type(), RegStatus.Prespecified);
//                model.addVariable(uvar);
//            }
//        }
//    }
//
//    private void initializeInterventions(ModelDescription model, InterventionVariable[] interventionVariables, Map<String, double[]> preadjustment) {
//        if (interventionVariables == null) {
//            return;
//        }
//        for (int i = 0; i < interventionVariables.length; ++i) {
//            InterventionVariable var = interventionVariables[i];
//            String sname = ITsVariable.shortName(var.getName());
//            if (preadjustment.containsKey(sname)) {
//                PreadjustmentVariable pv = PreadjustmentVariable.userVariable(var, Variable.searchType(var), preadjustment.get(sname));
//                model.addPreadjustment(pv);
//            } else {
//                Variable uvar = Variable.userVariable(var, Variable.searchType(var), RegStatus.Prespecified);
//                model.addVariable(uvar);
//            }
//        }
//    }
//
//    private void initializeRamps(ModelDescription model, Ramp[] ramps, Map<String, double[]> preadjustment) {
//        if (ramps == null) {
//            return;
//        }
//        for (int i = 0; i < ramps.length; ++i) {
//            Ramp var = ramps[i];
//            String sname = ITsVariable.shortName(var.getName());
//            if (preadjustment.containsKey(sname)) {
//                PreadjustmentVariable pv = PreadjustmentVariable.userVariable(var, ComponentType.Trend, preadjustment.get(sname));
//                model.addPreadjustment(pv);
//            } else {
//                Variable uvar = Variable.userVariable(var, ComponentType.Trend, RegStatus.Prespecified);
//                model.addVariable(uvar);
//            }
//        }
//    }
//
//    private void initializeHolidays(ModelDescription model, TradingDaysSpec td, Map<String, double[]> preadjustment) {
//        IGregorianCalendarProvider cal = context.getGregorianCalendars().get(td.getHolidays());
//        if (cal == null) {
//            return;
//        }
//        TradingDaysType tdType = td.getTradingDaysType();
//        if (tdType != TradingDaysType.None) {
//            GregorianCalendarVariables var = new GregorianCalendarVariables(cal, tdType);
//            String sname = ITsVariable.shortName(var.getName());
//            if (preadjustment.containsKey(sname)) {
//                PreadjustmentVariable pvar = PreadjustmentVariable.calendarVariable(var, preadjustment.get(sname));
//                model.addPreadjustment(pvar);
//            } else {
//                Variable tvar = Variable.calendarVariable(var,
//                        td.isTest() ? RegStatus.ToRemove : RegStatus.Prespecified);
//                model.addVariable(tvar);
//            }
//        }
//        if (td.isLeapYear()) {
//            LeapYearVariable lp = new LeapYearVariable(LengthOfPeriodType.LeapYear);
//            String sname = ITsVariable.shortName(lp.getName());
//            if (preadjustment.containsKey(sname)) {
//                PreadjustmentVariable pvar = PreadjustmentVariable.calendarVariable(lp, preadjustment.get(sname));
//                model.addPreadjustment(pvar);
//            } else {
//                Variable lvar = Variable.calendarVariable(lp,
//                        td.isTest() ? RegStatus.ToRemove : RegStatus.Prespecified);
//                model.addVariable(lvar);
//            }
//        }
//    }
    private void initializeUserTradingDays(ModelDescription model, TradingDaysSpec td, Map<String, double[]> preadjustment) {
        add(model, userTradingDays(td, context), preadjustment);
    }

    private void initializeDefaultTradingDays(ModelDescription model, TradingDaysSpec td, Map<String, double[]> preadjustment) {
        add(model, defaultTradingDays(td), preadjustment);
        add(model, leapYear(td), preadjustment);
    }

    private void initializeStockTradingDays(ModelDescription model, TradingDaysSpec td, Map<String, double[]> preadjustment) {
        add(model, stockTradingDays(td), preadjustment);
    }

    private static ITradingDaysVariable stockTradingDays(TradingDaysSpec td) {
        return new StockTradingDaysVariables(td.getStockTradingDays(), null);
    }

    private void add(ModelDescription model, ITsVariable<TsDomain> var, Map<String, double[]> preadjustment) {
        if (var == null) {
            return;
        }
        double[] c = preadjustment.get(var.getName());
        if (c != null) {
            model.addPreadjustmentVariable(new PreadjustmentVariable(var, c));
        } else {
            model.addVariable(new Variable(var, true));
        }

    }

    public static ITradingDaysVariable tradingDays(TramoSpec spec, ModellingContext context) {
        TradingDaysSpec tdspec = spec.getRegression().getCalendar().getTradingDays();
        if (!tdspec.isUsed()) {
            return null;
        }
        if (tdspec.isStockTradingDays()) {
            return new StockTradingDaysVariables(tdspec.getStockTradingDays(), null);
//        } else if (tdspec.getHolidays() != null) {
//            initializeHolidays(model, td, preadjustment);
        } else if (tdspec.getUserVariables() != null) {
            return userTradingDays(tdspec, context);
        } else {
            return defaultTradingDays(tdspec);
        }
    }

    static ITradingDaysVariable td(TramoSpec spec, DayClustering dc, ModellingContext context) {
        TradingDaysSpec tdspec = spec.getRegression().getCalendar().getTradingDays();
        if (!tdspec.isUsed()) {
            return null;
        }
        if (tdspec.isStockTradingDays()) {
            return null;
//        } else if (tdspec.getHolidays() != null) {
//            initializeHolidays(model, td, preadjustment);
        } else if (tdspec.getUserVariables() != null) {
            return null;
        } else {
            GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
            return new GenericTradingDaysVariables(gtd);
        }
    }

    private static ITradingDaysVariable defaultTradingDays(TradingDaysSpec td) {
        if (td.getTradingDaysType() == TradingDaysType.None) {
            return null;
        }
        TradingDaysType tdType = td.getTradingDaysType();
        if (td.isAutomatic())
            tdType=TradingDaysType.WorkingDays;
        DayClustering dc = tdType == (TradingDaysType.TradingDays) ? DayClustering.TD7 : DayClustering.TD2;
        GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
        return new GenericTradingDaysVariables(gtd);
    }

    private static ITradingDaysVariable userTradingDays(TradingDaysSpec td, ModellingContext context) {
        String[] userVariables = td.getUserVariables();
        ITsVariable<TsDomain>[] vars = new ITsVariable[userVariables.length];

        for (int i = 0; i < vars.length; ++i) {
            TsDataSupplier provider = context.getTsVariable(userVariables[i]);
            if (provider == null) {
                throw new TramoException(userVariables[i] + " not found");
            }
            TsData s = provider.get();
            if (s == null) {
                throw new TramoException(userVariables[i] + " not found");
            }
            vars[i] = new TsVariable(s, userVariables[i], userVariables[i]);
        }
        return TsVariableBox.tradingDays(new TsVariableGroup(vars, "usertd", null));

    }

    public static ILengthOfPeriodVariable leapYear(TradingDaysSpec tdspec) {
        if (!tdspec.isLeapYear()) {
            return null;
        } else {
            return new LengthOfPeriodVariable(LengthOfPeriodType.LeapYear);
        }
    }

    public static IEasterVariable easter(TramoSpec spec) {
        EasterSpec espec = spec.getRegression().getCalendar().getEaster();
        if (!espec.isUsed()) {
            return null;
        }
        if (espec.isJulian()) {
            return JulianEasterVariable.builder()
                    .duration(espec.getDuration())
                    .gregorianDates(true)
                    .build();
        } else {
            int endpos;
            switch (espec.getOption()) {
                case IncludeEaster:
                    endpos = 0;
                    break;
                case IncludeEasterMonday:
                    endpos = 1;
                    break;
                default:
                    endpos = -1;
            }
            return EasterVariable.builder()
                    .duration(espec.getDuration())
                    .meanCorrection(EasterVariable.Correction.Simple)
                    .endPosition(endpos)
                    .build();
        }
    }
}
