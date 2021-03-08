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
package jdplus.x13.regarima;

import demetra.data.Parameter;
import demetra.regarima.EasterSpec;
import demetra.regarima.RegressionSpec;
import demetra.regarima.RegArimaSpec;
import demetra.regarima.TradingDaysSpec;
import demetra.regarima.TransformSpec;
import jdplus.data.interpolation.AverageInterpolator;
import nbbrd.design.Development;
import demetra.information.InformationSet;
import demetra.regarima.RegressionTestSpec;
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
import demetra.timeseries.regression.ModellingContext;
import demetra.timeseries.regression.PeriodicOutlier;
import demetra.timeseries.regression.StockTradingDays;
import demetra.timeseries.calendars.TradingDaysType;
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
import demetra.sa.ComponentType;
import demetra.sa.SaVariable;
import demetra.timeseries.TsDomain;
import demetra.timeseries.calendars.GenericTradingDays;
import demetra.timeseries.regression.InterventionVariable;
import demetra.timeseries.regression.Ramp;
import demetra.timeseries.regression.TsContextVariable;
import java.util.List;
import jdplus.regarima.ami.Utility;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
class X13ModelBuilder implements IModelBuilder {

    private final RegArimaSpec spec;
    private final ModellingContext context;

    public X13ModelBuilder(RegArimaSpec spec, ModellingContext context) {
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
        Map<String, Parameter[]> preadjustment = regSpec.getCoefficients();
        initializeCalendar(model, regSpec, preadjustment);
        if (regSpec.getOutliersCount() > 0) {
            initializeOutliers(model, regSpec.getOutliers(), preadjustment);
        }
        initializeUsers(model, regSpec.getUserDefinedVariables());
        initializeInterventions(model, regSpec.getInterventionVariables());
        initializeRamps(model, regSpec.getRamps());
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
        model.setPreadjustment(fnSpec.getAdjust());
    }

    private void initializeCalendar(ModelDescription model, RegressionSpec calendar, Map<String, Parameter[]> preadjustment) {
        initializeTradingDays(model, calendar.getTradingDays(), preadjustment);
        initializeEaster(model, calendar.getEaster(), preadjustment);
    }

    private void initializeTradingDays(ModelDescription model, TradingDaysSpec td, Map<String, Parameter[]> preadjustment) {
        if (!td.isUsed() || td.getRegressionTestType() == RegressionTestSpec.Add) {
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

    private void initializeEaster(ModelDescription model, EasterSpec easter, Map<String, Parameter[]> preadjustment) {
        if (!easter.isUsed() || easter.getTest() == RegressionTestSpec.Add) {
            return;
        }
        add(model, easter(easter.getType(), easter.getDuration()), "easter", easter.getTest() == RegressionTestSpec.None, ComponentType.CalendarEffect, preadjustment);
    }

    private void initializeOutliers(ModelDescription model, List<IOutlier> outliers, Map<String, Parameter[]> preadjustment) {
        int freq = model.getAnnualFrequency();
        TransitoryChangeFactory tc = new TransitoryChangeFactory(spec.getOutliers().getMonthlyTCRate());
        PeriodicOutlierFactory so = new PeriodicOutlierFactory(freq, false);
        for (int i = 0; i < outliers.size(); ++i) {
            String code = outliers.get(i).getCode();
            LocalDateTime pos = outliers.get(i).getPosition();
            IOutlier v;
            ComponentType cmp = ComponentType.Undefined;
            switch (code) {
                case AdditiveOutlier.CODE:
                    v = AdditiveOutlierFactory.FACTORY.make(pos);
                    cmp = ComponentType.Irregular;
                    break;
                case LevelShift.CODE:
                    v = LevelShiftFactory.FACTORY_ZEROENDED.make(pos);
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
                Parameter[] c = preadjustment.get(name);
                Variable var = Variable.builder()
                        .name(name)
                        .core(v)
                        .coefficients(c)
                        .attribute(Utility.PRESPECIFIED, "true")
                        .attribute(SaVariable.REGEFFECT, cmp.name())
                        .build();
                model.addVariable(var);
            }
        }
    }

    private void initializeUsers(ModelDescription model, List< Variable<TsContextVariable>> uvars) {
        for (Variable<TsContextVariable> user : uvars) {
            model.addVariable(user.withCore(user.getCore().instantiateFrom(context, user.getName())));
        }
    }

    private void initializeInterventions(ModelDescription model, List<Variable<InterventionVariable>> interventionVariables) {
        for (Variable<InterventionVariable> iv : interventionVariables) {
            model.addVariable(iv);
        }
    }

    private void initializeRamps(ModelDescription model, List<Variable<Ramp>> ramps) {
        for (Variable<Ramp> r : ramps) {
            model.addVariable(r);
        }
    }

    private void initializeUserTradingDays(ModelDescription model, TradingDaysSpec td, Map<String, Parameter[]> preadjustment) {
        add(model, userTradingDays(td, context), "td", td.getRegressionTestType() == RegressionTestSpec.None, ComponentType.CalendarEffect, preadjustment);
    }

    private void initializeDefaultTradingDays(ModelDescription model, TradingDaysSpec td, Map<String, Parameter[]> preadjustment) {
        add(model, defaultTradingDays(td), "td", td.getRegressionTestType() == RegressionTestSpec.None, ComponentType.CalendarEffect, preadjustment);
        add(model, leapYear(td), "lp", td.getRegressionTestType() == RegressionTestSpec.None, ComponentType.CalendarEffect, preadjustment);
    }

    private void initializeStockTradingDays(ModelDescription model, TradingDaysSpec td, Map<String, Parameter[]> preadjustment) {
        add(model, stockTradingDays(td), "td", td.getRegressionTestType() == RegressionTestSpec.None, ComponentType.CalendarEffect, preadjustment);
    }

    private static ITradingDaysVariable stockTradingDays(TradingDaysSpec td) {
        return new StockTradingDays(td.getStockTradingDays());
    }

    private void add(ModelDescription model, ITsVariable var, String name, boolean prespecified, ComponentType cmp, Map<String, Parameter[]> preadjustment) {
        if (var == null) {
            return;
        }
        Parameter[] c = preadjustment.get(name);
        if (prespecified || c != null) {
            model.addVariable(Variable.builder().name(name).core(var).coefficients(c).attribute(Utility.PRESPECIFIED, "true").attribute(SaVariable.REGEFFECT, cmp.name()).build());
        } else {
            model.addVariable(Variable.builder().name(name).core(var).coefficients(c).attribute(SaVariable.REGEFFECT, cmp.name()).build());
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
        if (td.getTradingDaysType() == TradingDaysType.None) {
            return null;
        }
        TradingDaysType tdType = td.getTradingDaysType();
        DayClustering dc = tdType == (TradingDaysType.TradingDays) ? DayClustering.TD7 : DayClustering.TD2;
        GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
        return new GenericTradingDaysVariable(gtd);
    }

    private static ITradingDaysVariable userTradingDays(TradingDaysSpec td, ModellingContext context) {
        String[] userVariables = td.getUserVariables();
        return UserTradingDays.of(userVariables, context);
    }

    public static ILengthOfPeriodVariable leapYear(TradingDaysSpec tdspec) {
        if (tdspec.getLengthOfPeriodType() == LengthOfPeriodType.None) {
            return null;
        } else {
            return new LengthOfPeriod(tdspec.getLengthOfPeriodType());
        }
    }

    public static IEasterVariable easter(Type type, int w) {
        switch (type) {
            case JulianEaster:
                return new JulianEasterVariable(w, true);
            case Easter:
                return EasterVariable.builder()
                        .duration(w)
                        .meanCorrection(EasterVariable.Correction.PreComputed)
                        .endPosition(-1)
                        .build();
            default:
                return null;
        }
    }

}
