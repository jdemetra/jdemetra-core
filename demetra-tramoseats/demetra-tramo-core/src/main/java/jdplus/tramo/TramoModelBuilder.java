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

import jdplus.data.interpolation.AverageInterpolator;
import demetra.design.Development;
import demetra.information.InformationSet;
import demetra.modelling.regression.PreadjustmentVariable;
import demetra.modelling.TransformationType;
import demetra.modelling.regression.Variable;
import demetra.modelling.regression.AdditiveOutlier;
import jdplus.modelling.regression.AdditiveOutlierFactory;
import demetra.modelling.regression.EasterVariable;
import demetra.modelling.regression.GenericTradingDaysVariable;
import demetra.modelling.regression.HolidaysCorrectedTradingDays;
import jdplus.modelling.regression.HolidaysCorrectionFactory;
import demetra.modelling.regression.ITsVariable;
import demetra.modelling.regression.JulianEasterVariable;
import demetra.modelling.regression.LengthOfPeriod;
import demetra.modelling.regression.LevelShift;
import demetra.modelling.regression.ModellingContext;
import demetra.modelling.regression.PeriodicOutlier;
import demetra.modelling.regression.StockTradingDays;
import demetra.modelling.regression.TradingDaysType;
import demetra.modelling.regression.TransitoryChange;
import jdplus.regarima.regular.IModelBuilder;
import jdplus.regarima.regular.ModelDescription;
import jdplus.regarima.regular.SarimaComponent;
import demetra.timeseries.TsData;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.LengthOfPeriodType;
import jdplus.timeseries.simplets.TsDataToolkit;
import java.time.LocalDateTime;
import java.util.Map;
import demetra.modelling.regression.ILengthOfPeriodVariable;
import demetra.modelling.regression.ITradingDaysVariable;
import demetra.modelling.regression.IEasterVariable;
import demetra.modelling.regression.IOutlier;
import jdplus.modelling.regression.LevelShiftFactory;
import jdplus.modelling.regression.PeriodicOutlierFactory;
import jdplus.modelling.regression.TransitoryChangeFactory;
import demetra.modelling.regression.UserTradingDays;
import demetra.modelling.regarima.SarimaSpec;
import demetra.timeseries.calendars.GenericTradingDays;
import demetra.tramo.CalendarSpec;
import demetra.tramo.EasterSpec;
import demetra.tramo.RegressionSpec;
import demetra.tramo.TradingDaysSpec;
import demetra.tramo.TramoSpec;
import demetra.tramo.TransformSpec;

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
        Map<String, double[]> preadjustment = regSpec.getFixedCoefficients();
        initializeCalendar(model, regSpec.getCalendar(), preadjustment);
        if (regSpec.getOutliers().size() > 0) {
            initializeOutliers(model, regSpec.getOutliers().toArray(new IOutlier[0]), preadjustment);
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
        } else if (td.getHolidays() != null) {
            initializeHolidays(model, td, preadjustment);
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
        add(model, easter(spec), "easter", preadjustment);
    }

    private void initializeOutliers(ModelDescription model, IOutlier[] outliers, Map<String, double[]> preadjustment) {
        int freq = model.getAnnualFrequency();
        TransitoryChangeFactory tc = new TransitoryChangeFactory(spec.getOutliers().getDeltaTC());
        PeriodicOutlierFactory so = new PeriodicOutlierFactory(freq, false);
        for (int i = 0; i < outliers.length; ++i) {
            String code = outliers[i].getCode();
            LocalDateTime pos = outliers[i].getPosition();
            IOutlier v;
            switch (code) {
                case AdditiveOutlier.CODE:
                    v = AdditiveOutlierFactory.FACTORY.make(pos);
                    break;
                case LevelShift.CODE:
                    v = LevelShiftFactory.FACTORY_ZEROSTARTED.make(pos);
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
                String name = IOutlier.defaultName(code, pos, model.getDomain());
                double[] c = preadjustment.get(name);
                if (c != null) {
                    model.addPreadjustmentVariable(new PreadjustmentVariable(v, name, c));
                } else {
                    model.addVariable(new Variable(v, name, true));
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
    private void initializeHolidays(ModelDescription model, TradingDaysSpec td, Map<String, double[]> preadjustment) {
        add(model, holidays(td, context), "td", preadjustment);
        add(model, leapYear(td), "lp", preadjustment);
    }

    private void initializeUserTradingDays(ModelDescription model, TradingDaysSpec td, Map<String, double[]> preadjustment) {
        add(model, userTradingDays(td, context), "usertd", preadjustment);
    }

    private void initializeDefaultTradingDays(ModelDescription model, TradingDaysSpec td, Map<String, double[]> preadjustment) {
        add(model, defaultTradingDays(td), "td", preadjustment);
        add(model, leapYear(td), "lp", preadjustment);
    }

    private void initializeStockTradingDays(ModelDescription model, TradingDaysSpec td, Map<String, double[]> preadjustment) {
        add(model, stockTradingDays(td), "td", preadjustment);
    }

    private static ITradingDaysVariable stockTradingDays(TradingDaysSpec td) {
        return new StockTradingDays(td.getStockTradingDays());
    }

    private void add(ModelDescription model, ITsVariable var, String name, Map<String, double[]> preadjustment) {
        if (var == null) {
            return;
        }
        double[] c = name == null ? null : preadjustment.get(name);
        if (c != null) {
            model.addPreadjustmentVariable(new PreadjustmentVariable(var, name, c));
        } else {
            model.addVariable(new Variable(var, name, true));
        }

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
        String[] userVariables = td.getUserVariables().toArray(new String[0]);
        return UserTradingDays.of(userVariables, context);
    }

    public static ILengthOfPeriodVariable leapYear(TradingDaysSpec tdspec) {
        if (! tdspec.isAutomatic() && !tdspec.isLeapYear()) {
            return null;
        } else {
            return new LengthOfPeriod(LengthOfPeriodType.LeapYear);
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
