/*
 * Copyright 2019 National Bank of Belgium
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

import demetra.data.ParameterSpec;
import demetra.regarima.EasterSpec;
import demetra.regarima.RegressionSpec;
import demetra.regarima.RegArimaSpec;
import demetra.regarima.TradingDaysSpec;
import demetra.regarima.TransformSpec;
import jdplus.data.interpolation.AverageInterpolator;
import demetra.design.Development;
import demetra.information.InformationSet;
import demetra.timeseries.regression.PreadjustmentVariable;
import demetra.modelling.RegressionTestSpec;
import demetra.modelling.TransformationType;
import demetra.timeseries.regression.Variable;
import demetra.timeseries.regression.AdditiveOutlier;
import jdplus.modelling.regression.AdditiveOutlierFactory;
import demetra.timeseries.regression.EasterVariable;
import demetra.timeseries.regression.GenericTradingDaysVariable;
import demetra.timeseries.regression.ITsVariable;
import demetra.timeseries.regression.JulianEasterVariable;
import demetra.timeseries.regression.LengthOfPeriod;
import demetra.timeseries.regression.LevelShift;
import demetra.timeseries.regression.modelling.ModellingContext;
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
import demetra.regarima.EasterSpec.Type;
import demetra.arima.SarimaSpec;
import demetra.timeseries.TsDomain;
import demetra.timeseries.calendars.GenericTradingDays;
import java.util.List;
import jdplus.data.Parameter;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
class X12ModelBuilder implements IModelBuilder {

    private final RegArimaSpec spec;
    private final ModellingContext context;

    public X12ModelBuilder(RegArimaSpec spec, ModellingContext context) {
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
//            model.setMean(true);
        } else if (spec.getArima() == null) {
            model.setAirline(!yearly);
        } else {
            SarimaComponent cmp = model.getArimaComponent();
            SarimaSpec arima = spec.getArima();
            cmp.setPeriod(freq);
            cmp.setPhi(toParameters(arima.getPhi()));
            cmp.setTheta(toParameters(arima.getTheta()));
            cmp.setD(arima.getD());
            if (!yearly) {
                cmp.setBphi(toParameters(arima.getBphi()));
                cmp.setBtheta(toParameters(arima.getBtheta()));
                cmp.setBd(arima.getBd());
            }
        }
    }

    private Parameter[] toParameters(ParameterSpec[] p){
        if (p == null)
            return null;
        Parameter[] np=new Parameter[p.length];
        for (int i=0; i<np.length; ++i){
            np[i]=new Parameter(p[i].getValue(), p[i].getType());
        }
        return np;
    }

    private void initializeVariables(ModelDescription model, RegressionSpec regSpec) {

        if (!regSpec.isUsed()) {
            return;
        }
        model.setMean(regSpec.isMean());
        Map<String, double[]> preadjustment = regSpec.getFixedCoefficients();
        initializeCalendar(model, regSpec, preadjustment);
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
        TsData nseries = TsDataToolkit.select(series, spec.getBasic().getSpan());
        TsDomain edom = nseries.getDomain().select(spec.getEstimate().getSpan());
        ModelDescription cur = new ModelDescription(nseries, edom);

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

    private void initializeCalendar(ModelDescription model, RegressionSpec calendar, Map<String, double[]> preadjustment) {
        initializeTradingDays(model, calendar.getTradingDays(), preadjustment);
        initializeEaster(model, calendar.getEaster(), preadjustment);
    }

    private void initializeTradingDays(ModelDescription model, TradingDaysSpec td, Map<String, double[]> preadjustment) {
        if (!td.isUsed() || td.getTest() == RegressionTestSpec.Add) {
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
        if (easter == null || easter.getTest() == RegressionTestSpec.Add) {
            return;
        }
        add(model, easter(easter.getType(), easter.getDuration()), "easter", easter.getTest() == RegressionTestSpec.None, preadjustment);
    }

    private void initializeOutliers(ModelDescription model, List<IOutlier> outliers, Map<String, double[]> preadjustment) {
        int freq = model.getAnnualFrequency();
        IOutlier[] vars = new IOutlier[outliers.size()];
        TransitoryChangeFactory tc = new TransitoryChangeFactory(spec.getOutliers().getMonthlyTCRate());
        PeriodicOutlierFactory so = new PeriodicOutlierFactory(freq, false);
        for (int i = 0; i < outliers.size(); ++i) {
            String code = outliers.get(i).getCode();
            LocalDateTime pos = outliers.get(i).getPosition();
            IOutlier v;
            switch (code) {
                case AdditiveOutlier.CODE:
                    v = AdditiveOutlierFactory.FACTORY.make(pos);
                    break;
                case LevelShift.CODE:
                    v = LevelShiftFactory.FACTORY_ZEROENDED.make(pos);
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
                String name = IOutlier.defaultName(code, pos, model.getEstimationDomain());
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
        add(model, userTradingDays(td, context), "td", td.getTest() == RegressionTestSpec.None, preadjustment);
    }

    private void initializeDefaultTradingDays(ModelDescription model, TradingDaysSpec td, Map<String, double[]> preadjustment) {
        add(model, defaultTradingDays(td), "td", td.getTest() == RegressionTestSpec.None, preadjustment);
        add(model, leapYear(td), "lp", td.getTest() == RegressionTestSpec.None, preadjustment);
        if (td.isAutoAdjust()) {
            model.setTransformation(td.getLengthOfPeriodTime());
        } else {
            add(model, leapYear(td), "lp", td.getTest() == RegressionTestSpec.None, preadjustment);
        }
    }

    private void initializeStockTradingDays(ModelDescription model, TradingDaysSpec td, Map<String, double[]> preadjustment) {
        add(model, stockTradingDays(td), "td", td.getTest() == RegressionTestSpec.None, preadjustment);
    }

    private static ITradingDaysVariable stockTradingDays(TradingDaysSpec td) {
        return new StockTradingDays(td.getStockTradingDays());
    }

    private void add(ModelDescription model, ITsVariable var, String name, boolean prespecified, Map<String, double[]> preadjustment) {
        if (var == null) {
            return;
        }
        double[] c = preadjustment.get(name);
        if (c != null) {
            model.addPreadjustmentVariable(new PreadjustmentVariable(var, name, c));
        } else {
            model.addVariable(new Variable(var, name, prespecified));
        }

    }

    public static ITradingDaysVariable tradingDays(RegArimaSpec spec, ModellingContext context) {
        TradingDaysSpec tdspec = spec.getRegression().getTradingDays();
        if (!tdspec.isUsed()) {
            return null;
        }
        if (tdspec.isStockTradingDays()) {
            return new StockTradingDays(tdspec.getStockTradingDays());
//        } else if (tdspec.getHolidays() != null) {
//            initializeHolidays(model, td, preadjustment);
        } else if (tdspec.getUserVariables() != null) {
            return userTradingDays(tdspec, context);
        } else {
            return defaultTradingDays(tdspec);
        }
    }

    static ITradingDaysVariable td(RegArimaSpec spec, DayClustering dc, ModellingContext context) {
        TradingDaysSpec tdspec = spec.getRegression().getTradingDays();
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
            return new GenericTradingDaysVariable(gtd);
        }
    }

    private static ITradingDaysVariable defaultTradingDays(TradingDaysSpec td) {
        if (td.getType() == TradingDaysType.None) {
            return null;
        }
        TradingDaysType tdType = td.getType();
        DayClustering dc = tdType == (TradingDaysType.TradingDays) ? DayClustering.TD7 : DayClustering.TD2;
        GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
        return new GenericTradingDaysVariable(gtd);
    }

    private static ITradingDaysVariable userTradingDays(TradingDaysSpec td, ModellingContext context) {
        String[] userVariables = td.getUserVariables().toArray(new String[0]);
        return UserTradingDays.of(userVariables, context);
    }

    public static ILengthOfPeriodVariable leapYear(TradingDaysSpec tdspec) {
        if (tdspec.getLengthOfPeriodTime() == LengthOfPeriodType.None) {
            return null;
        } else {
            return new LengthOfPeriod(tdspec.getLengthOfPeriodTime());
        }
    }

    public static IEasterVariable easter(Type type, int w) {
        switch (type) {
            case JulianEaster:
                return new JulianEasterVariable(w, true);
            case Easter:
                return EasterVariable.builder()
                        .duration(w)
                        .meanCorrection(EasterVariable.Correction.Simple)
                        .endPosition(-1)
                        .build();
            default:
                return null;
        }
    }

}
