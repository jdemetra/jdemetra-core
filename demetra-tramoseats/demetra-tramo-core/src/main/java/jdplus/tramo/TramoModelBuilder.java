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
package jdplus.tramo;

import demetra.data.Parameter;
import jdplus.data.interpolation.AverageInterpolator;
import demetra.design.Development;
import demetra.information.InformationSet;
import demetra.modelling.TransformationType;
import demetra.timeseries.regression.Variable;
import demetra.timeseries.regression.AdditiveOutlier;
import jdplus.modelling.regression.AdditiveOutlierFactory;
import demetra.timeseries.regression.EasterVariable;
import demetra.timeseries.regression.GenericTradingDaysVariable;
import demetra.timeseries.regression.HolidaysCorrectedTradingDays;
import jdplus.modelling.regression.HolidaysCorrectionFactory;
import demetra.timeseries.regression.ITsVariable;
import demetra.timeseries.regression.JulianEasterVariable;
import demetra.timeseries.regression.LengthOfPeriod;
import demetra.timeseries.regression.LevelShift;
import demetra.timeseries.regression.ModellingContext;
import demetra.timeseries.regression.PeriodicOutlier;
import demetra.timeseries.regression.StockTradingDays;
import demetra.timeseries.regression.TradingDaysType;
import demetra.timeseries.regression.TransitoryChange;
import jdplus.regsarima.regular.IModelBuilder;
import jdplus.regsarima.regular.ModelDescription;
import jdplus.regsarima.regular.SarimaComponent;
import demetra.timeseries.TsData;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.LengthOfPeriodType;
import jdplus.timeseries.simplets.TsDataToolkit;
import java.time.LocalDateTime;
import java.util.Map;
import demetra.timeseries.regression.ILengthOfPeriodVariable;
import demetra.timeseries.regression.ITradingDaysVariable;
import demetra.timeseries.regression.IEasterVariable;
import demetra.timeseries.regression.IOutlier;
import jdplus.modelling.regression.LevelShiftFactory;
import jdplus.modelling.regression.PeriodicOutlierFactory;
import jdplus.modelling.regression.TransitoryChangeFactory;
import demetra.timeseries.regression.UserTradingDays;
import demetra.arima.SarimaSpec;
import demetra.sa.ComponentType;
import demetra.timeseries.TsDomain;
import demetra.timeseries.calendars.GenericTradingDays;
import demetra.tramo.CalendarSpec;
import demetra.tramo.EasterSpec;
import demetra.tramo.RegressionSpec;
import demetra.tramo.TradingDaysSpec;
import demetra.tramo.TramoSpec;
import demetra.tramo.TransformSpec;
import jdplus.data.OldParameter;
import jdplus.regarima.ami.Utility;
import org.checkerframework.checker.nullness.qual.NonNull;

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
        } else {
            SarimaComponent cmp = model.getArimaComponent();
            SarimaSpec arima = spec.getArima();
            cmp.setPeriod(freq);
            cmp.setPhi(arima.getPhi());
            cmp.setTheta(arima.getTheta());
            cmp.setD(arima.getD());
            if (!yearly) {
                cmp.setBphi(arima.getBphi());
                cmp.setBtheta(arima.getBtheta());
                cmp.setBd(arima.getBd());
            }
        }
    }

    private void initializeVariables(ModelDescription model, RegressionSpec regSpec) {

        if (!regSpec.isUsed()) {
            return;
        }
        model.setMean(regSpec.isMean());
        Map<String, Parameter[]> coefficients = regSpec.getCoefficients();
        initializeCalendar(model, regSpec.getCalendar(), coefficients);
        if (regSpec.getOutliers().size() > 0) {
            initializeOutliers(model, regSpec.getOutliers().toArray(new IOutlier[0]), coefficients);
        }
//        if (regSpec.getUserDefinedVariablesCount() > 0) {
//            initializeUsers(model, regSpec.getUserDefinedVariables(), coefficients);
//        }
//        if (regSpec.getInterventionVariablesCount() > 0) {
//            initializeInterventions(model, regSpec.getInterventionVariables(), coefficients);
//        }
//        if (regSpec.getRampsCount() > 0) {
//            initializeRamps(model, regSpec.getRamps(), coefficients);
//        }
    }

    @Override
    public ModelDescription build(TsData series, InformationSet log) {
        TsData nseries = TsDataToolkit.select(series, spec.getTransform().getSpan());
        TsDomain edom = nseries.getDomain().select(spec.getEstimate().getSpan());
        ModelDescription cur = new ModelDescription(nseries, edom);

        initializeMissing(cur);
        initializeTransformation(cur, spec.getTransform());
        initializeVariables(cur, spec.getRegression());
        initializeArima(cur);// Mean is initialized here in case of auto-modelling (mean = true)

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

    private void initializeCalendar(ModelDescription model, CalendarSpec calendar, Map<String, Parameter[]> coefficients) {
        initializeTradingDays(model, calendar.getTradingDays(), coefficients);
        initializeEaster(model, calendar.getEaster(), coefficients);
    }

    private void initializeTradingDays(ModelDescription model, TradingDaysSpec td, Map<String, Parameter[]> coefficients) {
        if (!td.isUsed() || td.isTest() || td.isAutomatic()) {
            return;
        }
        if (td.isStockTradingDays()) {
            initializeStockTradingDays(model, td, coefficients);
        } else if (td.getHolidays() != null) {
            initializeHolidays(model, td, coefficients);
        } else if (td.getUserVariables() != null) {
            initializeUserTradingDays(model, td, coefficients);
        } else {
            initializeDefaultTradingDays(model, td, coefficients);
        }
    }

    private void initializeEaster(ModelDescription model, EasterSpec easter, Map<String, Parameter[]> coefficients) {
        if (!easter.isUsed() || easter.isTest()) {
            return;
        }
        add(model, easter(spec), "easter", ComponentType.CalendarEffect, coefficients);
    }

    private void initializeOutliers(ModelDescription model, IOutlier[] outliers, Map<String, Parameter[]> coefficients) {
        int freq = model.getAnnualFrequency();
        TransitoryChangeFactory tc = new TransitoryChangeFactory(spec.getOutliers().getDeltaTC());
        PeriodicOutlierFactory so = new PeriodicOutlierFactory(freq, false);
        for (int i = 0; i < outliers.length; ++i) {
            String code = outliers[i].getCode();
            LocalDateTime pos = outliers[i].getPosition();
            IOutlier v;
            ComponentType cmp = ComponentType.Undefined;
            switch (code) {
                case AdditiveOutlier.CODE:
                    v = AdditiveOutlierFactory.FACTORY.make(pos);
                    cmp = ComponentType.Irregular;
                    break;
                case LevelShift.CODE:
                    v = LevelShiftFactory.FACTORY_ZEROSTARTED.make(pos);
                    cmp = ComponentType.Trend;
                    break;
                case PeriodicOutlier.CODE:
                    v = so.make(pos);
                    cmp = ComponentType.Seasonal;
                    break;
                case TransitoryChange.CODE:
                    v = tc.make(pos);
                    cmp = ComponentType.Irregular;
                    break;
                default:
                    v = null;
            }
            if (v != null) {
                String name = IOutlier.defaultName(code, pos, model.getEstimationDomain());
                Parameter[] c = coefficients.get(name);
                Variable var = Variable.builder()
                        .name(name)
                        .core(v)
                        .coefficients(c)
                        .attribute(Utility.PRESPECIFIED)
                        .attribute(cmp.name())
                        .build();
                model.addVariable(var);
            }
        }
    }

//    private void initializeUsers(ModelDescription model, TsVariableDescriptor[] uvars, Map<String, double[]> coefficients) {
//        if (uvars == null) {
//            return;
//        }
//        for (int i = 0; i < uvars.length; ++i) {
//            ITsVariable var = uvars[i].toTsVariable(context);
//            String sname = ITsVariable.shortName(var.getName());
//            if (coefficients.containsKey(sname)) {
//                PreadjustmentVariable pv = PreadjustmentVariable.userVariable(var, uvars[i].getEffect().type(), coefficients.get(sname));
//                model.addPreadjustment(pv);
//            } else {
//                Variable uvar = Variable.userVariable(var, uvars[i].getEffect().type(), RegStatus.Prespecified);
//                model.addVariable(uvar);
//            }
//        }
//    }
//
//    private void initializeInterventions(ModelDescription model, InterventionVariable[] interventionVariables, Map<String, double[]> coefficients) {
//        if (interventionVariables == null) {
//            return;
//        }
//        for (int i = 0; i < interventionVariables.length; ++i) {
//            InterventionVariable var = interventionVariables[i];
//            String sname = ITsVariable.shortName(var.getName());
//            if (coefficients.containsKey(sname)) {
//                PreadjustmentVariable pv = PreadjustmentVariable.userVariable(var, Variable.searchType(var), coefficients.get(sname));
//                model.addPreadjustment(pv);
//            } else {
//                Variable uvar = Variable.userVariable(var, Variable.searchType(var), RegStatus.Prespecified);
//                model.addVariable(uvar);
//            }
//        }
//    }
//
//    private void initializeRamps(ModelDescription model, Ramp[] ramps, Map<String, double[]> coefficients) {
//        if (ramps == null) {
//            return;
//        }
//        for (int i = 0; i < ramps.length; ++i) {
//            Ramp var = ramps[i];
//            String sname = ITsVariable.shortName(var.getName());
//            if (coefficients.containsKey(sname)) {
//                PreadjustmentVariable pv = PreadjustmentVariable.userVariable(var, ComponentType.Trend, coefficients.get(sname));
//                model.addPreadjustment(pv);
//            } else {
//                Variable uvar = Variable.userVariable(var, ComponentType.Trend, RegStatus.Prespecified);
//                model.addVariable(uvar);
//            }
//        }
//    }
//
    private void initializeHolidays(ModelDescription model, TradingDaysSpec td, Map<String, Parameter[]> coefficients) {
        add(model, holidays(td, context), "td", ComponentType.CalendarEffect, coefficients);
        add(model, leapYear(td), "lp", ComponentType.CalendarEffect, coefficients);
    }

    private void initializeUserTradingDays(ModelDescription model, TradingDaysSpec td, Map<String, Parameter[]> coefficients) {
        add(model, userTradingDays(td, context), "usertd", ComponentType.CalendarEffect, coefficients);
    }

    private void initializeDefaultTradingDays(ModelDescription model, TradingDaysSpec td, Map<String, Parameter[]> coefficients) {
        add(model, defaultTradingDays(td), "td", ComponentType.CalendarEffect, coefficients);
        add(model, leapYear(td), "lp", ComponentType.CalendarEffect, coefficients);
    }

    private void initializeStockTradingDays(ModelDescription model, TradingDaysSpec td, Map<String, Parameter[]> coefficients) {
        add(model, stockTradingDays(td), "td", ComponentType.CalendarEffect, coefficients);
    }

    private static ITradingDaysVariable stockTradingDays(TradingDaysSpec td) {
        return new StockTradingDays(td.getStockTradingDays());
    }

    private void add(@NonNull ModelDescription model, @NonNull ITsVariable v, @NonNull String name, @NonNull ComponentType cmp, @NonNull Map<String, Parameter[]> coefficients) {
        Parameter[] c = name == null ? null : coefficients.get(name);
        Variable var = Variable.builder()
                .name(name)
                .core(v)
                .coefficients(c)
                .attribute(cmp.name())
                .attribute(Utility.PRESPECIFIED)
                .build();
        model.addVariable(var);
    }

    public static ITradingDaysVariable tradingDays(TramoSpec spec, ModellingContext context) {
        TradingDaysSpec tdspec = spec.getRegression().getCalendar().getTradingDays();
        if (!tdspec.isUsed()) {
            return null;
        }
        if (tdspec.isStockTradingDays()) {
            return new StockTradingDays(tdspec.getStockTradingDays());
        } else if (tdspec.getHolidays() != null) {
            return holidays(tdspec, context);
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
        } else if (tdspec.getHolidays() != null) {
            GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
            HolidaysCorrectedTradingDays.HolidaysCorrector corrector = HolidaysCorrectionFactory.corrector(tdspec.getHolidays(), context.getCalendars());
            return new HolidaysCorrectedTradingDays(gtd, corrector);
        } else if (tdspec.getUserVariables() != null) {
            return null;
        } else {
            GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
            return new GenericTradingDaysVariable(gtd);
        }
    }

    private static ITradingDaysVariable defaultTradingDays(TradingDaysSpec td) {
        if (td.getTradingDaysType() == TradingDaysType.None) {
            return null;
        }
        TradingDaysType tdType = td.getTradingDaysType();
        if (td.isAutomatic()) {
            tdType = TradingDaysType.WorkingDays;
        }
        DayClustering dc = tdType == (TradingDaysType.TradingDays) ? DayClustering.TD7 : DayClustering.TD2;
        GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
        return new GenericTradingDaysVariable(gtd);
    }

    private static ITradingDaysVariable holidays(TradingDaysSpec td, ModellingContext context) {
        if (td.getTradingDaysType() == TradingDaysType.None) {
            return null;
        }
        TradingDaysType tdType = td.getTradingDaysType();
        if (td.isAutomatic()) {
            tdType = TradingDaysType.WorkingDays;
        }
        DayClustering dc = tdType == (TradingDaysType.TradingDays) ? DayClustering.TD7 : DayClustering.TD2;
        GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
        HolidaysCorrectedTradingDays.HolidaysCorrector corrector = HolidaysCorrectionFactory.corrector(td.getHolidays(), context.getCalendars());
        return new HolidaysCorrectedTradingDays(gtd, corrector);
    }

    private static ITradingDaysVariable userTradingDays(TradingDaysSpec td, ModellingContext context) {
        String[] userVariables = td.getUserVariables();
        return UserTradingDays.of(userVariables, context);
    }

    public static ILengthOfPeriodVariable leapYear(TradingDaysSpec tdspec) {
        if (!tdspec.isAutomatic() && tdspec.getLengthOfPeriodType() == LengthOfPeriodType.None) {
            return null;
        } else {
            return new LengthOfPeriod(tdspec.getLengthOfPeriodType());
        }
    }

    public static IEasterVariable easter(TramoSpec spec) {
        EasterSpec espec = spec.getRegression().getCalendar().getEaster();
        if (!espec.isUsed()) {
            return null;
        }
        if (espec.isJulian()) {
            return new JulianEasterVariable(espec.getDuration(), true);
        } else {
            int endpos;
            switch (espec.getType()) {
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
