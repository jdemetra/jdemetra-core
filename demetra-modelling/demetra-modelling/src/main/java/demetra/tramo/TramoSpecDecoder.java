/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramo;

import demetra.information.InformationSet;
import demetra.modelling.ComponentType;
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
import demetra.regarima.ami.IArmaModule;
import demetra.regarima.ami.IDifferencingModule;
import demetra.regarima.ami.IModelBuilder;
import demetra.regarima.ami.ISeasonalityDetector;
import demetra.regarima.ami.ModelDescription;
import demetra.regarima.ami.SarimaComponent;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDataSupplier;
import demetra.timeseries.TsDomain;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.GenericTradingDays;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.simplets.TsDataToolkit;
import java.time.LocalDateTime;
import javax.annotation.Nonnull;

/**
 * The Tramo processing builder initializes the regarima processing, which
 * contains the initial model and the possible AMI modules. It starts from a
 * time series and from a Tramo specification. In a first step, we create the
 * initial model. In a second step, we define the processor itself
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
class TramoSpecDecoder {

    private final TramoSpec spec;
    private final ModellingContext context;

    public TramoSpecDecoder(@Nonnull TramoSpec spec, ModellingContext context) {
        this.spec = spec;
        if (context != null) {
            this.context = context;
        } else {
            this.context = ModellingContext.getActiveContext();
        }
    }

    IModelBuilder modelBuider() {
        return (TsData series, InformationSet log) -> buildInitialModel(series);
    }

    LogLevelModule transformation() {
        // creates the initialmodel
        TransformSpec tspec = spec.getTransform();
        EstimateSpec espec = spec.getEstimate();
        if (tspec.getFunction() == TransformationType.Auto) {
            return LogLevelModule.builder()
                    .logPreference(tspec.getFct())
                    .estimationPrecision(espec.getTol())
                    .build();
        } else {
            return null;
        }
    }

    ISeasonalityDetector seasonality() {
        return spec.isUsingAutoModel() ? new SeasonalityDetector() : null;
    }

    IArmaModule arma() {
        if (!spec.isUsingAutoModel()) {
            return null;
        }
        AutoModelSpec amiSpec = spec.getAutoModel();
        return null;
    }

    IDifferencingModule differencing() {
        if (!spec.isUsingAutoModel()) {
            return null;
        }
        AutoModelSpec amiSpec = spec.getAutoModel();
        return DifferencingModule.builder()
                .cancel(amiSpec.getCancel())
                .ub1(amiSpec.getUb1())
                .ub2(amiSpec.getUb2())
                .build();

    }

    ModelDescription buildInitialModel(TsData series) {
        TsData nseries = TsDataToolkit.select(series, spec.getTransform().getSpan());
        ModelDescription model = new ModelDescription(TsDataToolkit.select(nseries, spec.getEstimate().getSpan()));
        initializeTransformation(model);
        initializeArima(model);
        initializeTradingDays(model);
        initializeEaster(model);
        InitializeOutliers(model);
        InitializeOtherVariables(model);
        return model;
    }

    private void initializeTransformation(ModelDescription model) {
        if (spec.getTransform().getFunction() == TransformationType.Log) {
            model.setLogTransformation(true);
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
            // should be changed...
            ArimaSpec arima = spec.getArima();
            SarimaComponent cmp = new SarimaComponent(freq);
            cmp.setMu(arima.getMu());
            cmp.setPhi(arima.getPhi());
            cmp.setTheta(arima.getTheta());
            cmp.setD(arima.getD());
            if (!yearly) {
                cmp.setBPhi(arima.getBPhi());
                cmp.setBTheta(arima.getBTheta());
                cmp.setBD(arima.getBd());
            }
            model.setArimaComponent(cmp);
        }
    }

    private void initializeTradingDays(ModelDescription model) {
        ITradingDaysVariable td = tradingDays(true);
        if (td != null){
            
        }
//        if model.addVariable(tradingDays());
//        model.addVariable(leapYear());
    }

    private void initializeEaster(ModelDescription model) {
//        model.addVariable(easter(model.getAnnualFrequency()));
    }

    private void InitializeOutliers(ModelDescription model) {
        if (spec.getRegression().getOutliersCount() == 0) {
            return;
        }
        OutlierDefinition[] outliers = spec.getRegression().getOutliers();
//        Variable[] po = prespecifiedOutliers(model.getDomain(), outliers);
//        model.addVariable(po);
    }

    private void InitializeOtherVariables(ModelDescription model) {
        // TODO
    }

    private double[] fixedCoefficients(String name) {
        return spec.getRegression().getAllFixedCoefficients().get(name);
    }

    /**
     * Creates the easter variable, if any
     *
     * @param freq
     * @return
     */
    IEasterVariable easter(int freq, boolean prespecified) {
        EasterSpec espec = spec.getRegression().getCalendar().getEaster();
        if (!espec.isUsed()) {
            return null;
        }
        if (espec.isTest() != prespecified) {
            return null;
        }
        if (espec.isJulian()) {
            if (freq < 3) {
                return null;
            }
            return JulianEasterVariable.builder()
                    .duration(espec.getDuration())
                    .gregorianDates(true)
                    .build();
        } else {
            if (freq < 4) {
                return null;
            }
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

    public ITradingDaysVariable tradingDays(boolean prespecified) {
        TradingDaysSpec td = spec.getRegression().getCalendar().getTradingDays();
        if (!td.isUsed()) {
            return null;
        }
        if (td.isTest() != prespecified)
            return null;
        if (td.isStockTradingDays()) {
            return stockTradingDays(td);
        } else if (td.getHolidays() != null) {
            return holidays(td);
        } else if (td.getUserVariables() != null) {
            return userHolidays(td);
        } else {
            return defaultTradingDays(td);
        }
    }

    private ITradingDaysVariable holidays(TradingDaysSpec td) {
//        IGregorianCalendarProvider cal = context_.getGregorianCalendars().get(td.getHolidays());
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
        return null;
    }

    private ITradingDaysVariable stockTradingDays(TradingDaysSpec td) {
        return new StockTradingDaysVariables(td.getStockTradingDays(), null);
    }

    private ITradingDaysVariable defaultTradingDays(TradingDaysSpec td) {
        TradingDaysType tdType = td.getTradingDaysType();
        if (tdType == TradingDaysType.None) {
            return null;
        }
        DayClustering dc = tdType == (TradingDaysType.TradingDays) ? DayClustering.TD7 : DayClustering.TD2;
        GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
        return new GenericTradingDaysVariables(gtd);
    }

    public ILengthOfPeriodVariable leapYear() {

        TradingDaysSpec td = spec.getRegression().getCalendar().getTradingDays();
        if (!td.isLeapYear()) {
            return null;
        }
        return new LengthOfPeriodVariable(LengthOfPeriodType.LeapYear);
    }

    private ITradingDaysVariable userHolidays(TradingDaysSpec td) {
        String[] userVariables = td.getUserVariables();
        if (userVariables == null || userVariables.length == 0) {
            return null;
        }
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

 //           vars[i] = new TsVariable(s, userVariables[i], null);
        }

 //       return TsVariableBox.tradingDays(new TsVariableGroup(vars, "usertd", null));
 return null;
    }

    private IOutlier[] prespecifiedOutliers(TsDomain domain, OutlierDefinition[] outliers) {
        int freq = domain.getAnnualFrequency();
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
                    v = LevelShift.FACTORY_ZEROENDED.make(pos);
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
                v = (IOutlier) v.rename(IOutlier.defaultName(code, pos, domain));
                vars[i] = v;
            }
        }
        return vars;
    }

//
//    @Override
//    public boolean build(RegArimaContext context) {
//        initializeTransformation(context.getDescription(), spec.getTransform());
//        initializeArima(context.getDescription());
//        initializeVariables(context.getDescription(), spec.getRegression());
//
//        return true;
//    }
//
//
//    private void initializeVariables(ModelDescription model, RegressionSpec regSpec) {
//
//        if (!regSpec.isUsed()) {
//            return;
//        }
//        Map<String, double[]> preadjustment = regSpec.getAllFixedCoefficients();
//        initializeCalendar(model, regSpec.getCalendar(), preadjustment);
//        if (regSpec.getOutliersCount() > 0) {
//            initializeOutliers(model, regSpec.getOutliers(), preadjustment);
//        }
//        if (regSpec.getUserDefinedVariablesCount() > 0) {
//            initializeUsers(model, regSpec.getUserDefinedVariables(), preadjustment);
//        }
//        if (regSpec.getInterventionVariablesCount() > 0) {
//            initializeInterventions(model, regSpec.getInterventionVariables(), preadjustment);
//        }
//        if (regSpec.getRampsCount() > 0) {
//            initializeRamps(model, regSpec.getRamps(), preadjustment);
//        }
//    }
//
//    private void initializeTransformation(@Nonnull ModelDescription model, @Nonnull TransformSpec fnSpec) {
//        model.setLogTransformation(fnSpec.getFunction() == TransformationType.Log);
//    }
//
//    private void initializeCalendar(TsDomain domain, CalendarSpec calendar, Map<String, double[]> preadjustment) {
//        if (calendar == null) {
//            return;
//        }
//        initializeTradingDays(domain, calendar.getTradingDays(), preadjustment);
//        initializeEaster(domain, calendar.getEaster(), preadjustment);
//    }
//
//    private void initializeTradingDays(ModelDescription model, TradingDaysSpec td, Map<String, double[]> preadjustment) {
//        if (!td.isUsed()) {
//            return;
//        }
//        if (td.isStockTradingDays()) {
//            initializeStockTradingDays(model, td, preadjustment);
//        } else if (td.getHolidays() != null) {
//            initializeHolidays(model, td, preadjustment);
//        } else if (td.getUserVariables() != null) {
//            initializeUserHolidays(model, td, preadjustment);
//        } else if (td.isUsed()) {
//            initializeDefaultTradingDays(model, td, preadjustment);
//        }
//    }
//
//    private void initializeUsers(ModelDescription model, TsVariableDescriptor[] uvars, Map<String, double[]> preadjustment) {
//        if (uvars == null) {
//            return;
//        }
//        for (int i = 0; i < uvars.length; ++i) {
//            ITsVariable var = uvars[i].toTsVariable(context_);
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
//
//
}
