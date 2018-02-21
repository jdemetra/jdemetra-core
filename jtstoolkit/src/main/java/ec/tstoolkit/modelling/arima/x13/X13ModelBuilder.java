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
import ec.tstoolkit.modelling.*;
import ec.tstoolkit.modelling.arima.IModelBuilder;
import ec.tstoolkit.modelling.arima.ModelDescription;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.PreadjustmentType;
import ec.tstoolkit.sarima.SarimaComponent;
import ec.tstoolkit.timeseries.calendars.IGregorianCalendarProvider;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.*;
import java.util.ArrayList;
import java.util.Map;

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
            //model.setMu(true); // to be checked. 28/1/2013
        } else if (spec_.getArima() == null) {
            model.setAirline(true);
        } else {
            // should be changed...
            ArimaSpec arima = spec_.getArima();
            SarimaComponent cmp = new SarimaComponent();
            cmp.setFrequency(model.getFrequency());
            cmp.setMu(arima.getMu());
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
        Map<String, double[]> preadjustment = regSpec.getAllFixedCoefficients();
        if (regSpec.getTradingDays().isUsed()) {
            initializeTradingDays(model, regSpec.getTradingDays(), preadjustment);
        }
        if (regSpec.getMovingHolidays() != null) {
            initializeMovingHolidays(model, regSpec.getMovingHolidays(), preadjustment);
        }
        if (regSpec.getOutliersCount() > 0) {
            initializeOutliers(model, regSpec.getOutliers(), preadjustment);
        }
        if (regSpec.getUserDefinedVariablesCount() > 0) {
            initializeUsers(model, regSpec.getUserDefinedVariables(), preadjustment);
        }
        if (regSpec.getInterventionVariablesCount() > 0) {
            initializeInterventions(model, regSpec.getInterventionVariables(), preadjustment);
        }
        if (regSpec.getRampsCount() > 0) {
            initializeRamps(model, regSpec.getRamps(), preadjustment);
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

    private void initializeTradingDays(ModelDescription model, TradingDaysSpec tradingDays, Map<String, double[]> preadjustment) {
        if (tradingDays.isStockTradingDays()) {
            initializeStockTradingDays(model, tradingDays, preadjustment);
        }
        if (tradingDays.getHolidays() != null) {
            initializeHolidays(model, tradingDays, preadjustment);
        } else if (tradingDays.getUserVariables() != null) {
            initializeUserHolidays(model, tradingDays, preadjustment);
        } else {
            initializeDefaultTradingDays(model, tradingDays, preadjustment);
        }
    }

    private void initializeMovingHolidays(ModelDescription model, MovingHolidaySpec[] mh, Map<String, double[]> preadjustment) {
        for (int i = 0; i < mh.length; ++i) {
            if (mh[i].getType() == MovingHolidaySpec.Type.Easter) {
                EasterVariable var = new EasterVariable();
                var.setType(EasterVariable.Correction.PreComputed);
                var.setDuration(mh[i].getW());
                String sname = ITsVariable.shortName(var.getName());
                if (preadjustment.containsKey(sname)) {
                    PreadjustmentVariable pvar = PreadjustmentVariable.movingHolidayVariable(var, preadjustment.get(sname));
                    model.addPreadjustment(pvar);
                } else {
                    Variable tvar = Variable.movingHolidayVariable(var, RegStatus.Undefined);
                    if (null != mh[i].getTest()) {
                        switch (mh[i].getTest()) {
                            case Add:
                                tvar.status = RegStatus.ToAdd;
                                break;
                            case Remove:
                                tvar.status = RegStatus.ToRemove;
                                break;
                            default:
                                tvar.status = RegStatus.Prespecified;
                                break;
                        }
                    }
                    model.addVariable(tvar);
                }
            } else if (mh[i].getType() == MovingHolidaySpec.Type.JulianEaster) {
                JulianEasterVariable var = new JulianEasterVariable();
                var.setDuration(mh[i].getW());
                String sname = ITsVariable.shortName(var.getName());
                if (preadjustment.containsKey(sname)) {
                    PreadjustmentVariable pvar = PreadjustmentVariable.movingHolidayVariable(var, preadjustment.get(sname));
                    model.addPreadjustment(pvar);
                } else {
                    Variable tvar = Variable.movingHolidayVariable(var, RegStatus.Undefined);
                    if (null != mh[i].getTest()) {
                        switch (mh[i].getTest()) {
                            case Add:
                                tvar.status = RegStatus.ToAdd;
                                break;
                            case Remove:
                                tvar.status = RegStatus.ToRemove;
                                break;
                            default:
                                tvar.status = RegStatus.Prespecified;
                                break;
                        }
                    }
                    model.addVariable(tvar);
                }
            }
        }
    }

    private void initializeOutliers(ModelDescription model, OutlierDefinition[] outliers, Map<String, double[]> preadjustment) {
        ArrayList<IOutlierVariable> pvar = new ArrayList<>();
        for (int i = 0; i < outliers.length; ++i) {
            IOutlierVariable v = RegArimaSpecification.fac.make(outliers[i]);
            String sname = ITsVariable.shortName(v.getName());
            if (preadjustment.containsKey(sname)) {
                PreadjustmentVariable pv = PreadjustmentVariable.outlier(v, preadjustment.get(sname));
                model.addPreadjustment(pv);
            } else {
                pvar.add(v);
            }
        }
        model.addPrespecifiedOutliers(pvar);
    }

    private void initializeUsers(ModelDescription model, TsVariableDescriptor[] uvars, Map<String, double[]> preadjustment) {
        if (uvars == null) {
            return;
        }
        for (int i = 0; i < uvars.length; ++i) {
            ITsVariable var = uvars[i].toTsVariable(context_);
            String sname = ITsVariable.shortName(var.getName());
            if (preadjustment.containsKey(sname)) {
                PreadjustmentVariable pv = PreadjustmentVariable.userVariable(var, uvars[i].getEffect().type(), preadjustment.get(sname));
                model.addPreadjustment(pv);
            } else {
                Variable uvar = Variable.userVariable(var, uvars[i].getEffect().type(), RegStatus.Prespecified);
                model.addVariable(uvar);
            }
        }
    }

    private void initializeInterventions(ModelDescription model, InterventionVariable[] interventionVariables, Map<String, double[]> preadjustment) {
        if (interventionVariables == null) {
            return;
        }
        for (int i = 0; i < interventionVariables.length; ++i) {
            InterventionVariable var = interventionVariables[i];
            String sname = ITsVariable.shortName(var.getName());
            if (preadjustment.containsKey(sname)) {
                PreadjustmentVariable pv = PreadjustmentVariable.userVariable(var, Variable.searchType(var), preadjustment.get(sname));
                model.addPreadjustment(pv);
            } else {
                Variable uvar = Variable.userVariable(var, Variable.searchType(var), RegStatus.Prespecified);
                model.addVariable(uvar);
            }
        }
    }

    private void initializeRamps(ModelDescription model, Ramp[] ramps, Map<String, double[]> preadjustment) {
        if (ramps == null) {
            return;
        }
        for (int i = 0; i < ramps.length; ++i) {
            Ramp var = ramps[i];
            String sname = ITsVariable.shortName(var.getName());
            if (preadjustment.containsKey(sname)) {
                PreadjustmentVariable pv = PreadjustmentVariable.userVariable(var, ComponentType.Trend, preadjustment.get(sname));
                model.addPreadjustment(pv);
            } else {
                Variable uvar = Variable.userVariable(var, ComponentType.Trend, RegStatus.Prespecified);
                model.addVariable(uvar);
            }
        }
    }

    private void initializeHolidays(ModelDescription model, TradingDaysSpec td, Map<String, double[]> preadjustment) {
        IGregorianCalendarProvider cal = context_.getGregorianCalendars().get(td.getHolidays());
        if (cal == null) {
            return;
        }
        TradingDaysType ttd = td.getTradingDaysType();
        LengthOfPeriodType tlp = td.getLengthOfPeriod();

        boolean auto = td.isAutoAdjust();

        if (ttd != TradingDaysType.None) {
            GregorianCalendarVariables var = new GregorianCalendarVariables(cal, ttd);
            String sname = ITsVariable.shortName(var.getName());
            if (preadjustment.containsKey(sname)) {
                PreadjustmentVariable pvar = PreadjustmentVariable.calendarVariable(var, preadjustment.get(sname));
                model.addPreadjustment(pvar);
            } else {
                Variable tvar = Variable.calendarVariable(var, RegStatus.Undefined);
                if (null != td.getTest()) {
                    switch (td.getTest()) {
                        case Add:
                            tvar.status = RegStatus.ToAdd;
                            break;
                        case Remove:
                            tvar.status = RegStatus.ToRemove;
                            break;
                        default:
                            tvar.status = RegStatus.Prespecified;
                            break;
                    }
                }
                model.addVariable(tvar);
            }
        }
        if (tlp != LengthOfPeriodType.None) {
            LeapYearVariable var = new LeapYearVariable(tlp);
            String sname = ITsVariable.shortName(var.getName());
            if (preadjustment.containsKey(sname)) {
                PreadjustmentVariable pvar = PreadjustmentVariable.calendarVariable(var, preadjustment.get(sname));
                model.addPreadjustment(pvar);
            } else {
                Variable tvar = Variable.calendarVariable(var, RegStatus.Undefined);
                if (null != td.getTest()) {
                    switch (td.getTest()) {
                        case Add:
                            tvar.status = RegStatus.ToAdd;
                            break;
                        case Remove:
                            tvar.status = RegStatus.ToRemove;
                            break;
                        default:
                            tvar.status = RegStatus.Prespecified;
                            break;
                    }
                }
                model.addVariable(tvar);
            }
        }

        if (auto) {
            model.setTransformation(PreadjustmentType.Auto);
        }
    }

    private void initializeUserHolidays(ModelDescription model, TradingDaysSpec td, Map<String, double[]> preadjustment) {
        String[] userVariables = td.getUserVariables();
        if (userVariables == null || userVariables.length == 0) {
            return;
        }

        ArrayList<ITsVariable> nonUserFixedVariables = new ArrayList<>();
        for (int i = 0; i < userVariables.length; ++i) {
            userVariables[i] = userVariables[i].replace("td|", "");
            String userVariableName = "td|".concat(userVariables[i]);
            ITsVariable var = context_.getTsVariable(userVariables[i]);
            if (var == null) {
                throw new X13Exception(userVariables[i] + " not found");
            }

            if (preadjustment.containsKey(ITsVariable.validName(userVariableName))) {
                userVariables[i] = userVariableName;

                DecoratedTsVariable temp = new DecoratedTsVariable(var, userVariableName);
                PreadjustmentVariable pvar = PreadjustmentVariable.tdVariable(temp, preadjustment.get(ITsVariable.validName(userVariables[i])));
                model.addPreadjustment(pvar);
            } else {
                nonUserFixedVariables.add(var);
            }
        }
        if (!nonUserFixedVariables.isEmpty()) {
            TsVariableGroup var = new TsVariableGroup("User-defined calendar variables", nonUserFixedVariables.toArray(new ITsVariable[nonUserFixedVariables.size()]));// hiermit ist der Name der Zeitreihe mit drin
            var.setName("usertd");

            // it has to be checked if td is fixed too. this might be the case if Refresh/Partial Concurrent adjustment/Current adjustment is done
            if (preadjustment.containsKey("td")) {
                ITradingDaysVariable tdvar_fixed = AbstractTsVariableBox.tradingDays(var);
                PreadjustmentVariable pv_td = PreadjustmentVariable.tdVariable(tdvar_fixed, preadjustment.get("td"));
                model.addPreadjustment(pv_td);
            } else {
                Variable tvar = Variable.tdVariable(var, RegStatus.Undefined);
                switch (td.getTest()) {
                    case Add:
                        tvar.status = RegStatus.ToAdd;
                        break;
                    case Remove:
                        tvar.status = RegStatus.ToRemove;
                        break;
                    default:
                        tvar.status = RegStatus.Prespecified;
                        break;
                }
                model.addVariable(tvar);
            }
        }
    }

    private void initializeDefaultTradingDays(ModelDescription model, TradingDaysSpec td, Map<String, double[]> preadjustment) {
        TradingDaysType ttd = td.getTradingDaysType();
        LengthOfPeriodType tlp = td.getLengthOfPeriod();

        boolean auto = td.isAutoAdjust();

        if (ttd != TradingDaysType.None) {
            GregorianCalendarVariables var = GregorianCalendarVariables.getDefault(ttd);
            String sname = ITsVariable.shortName(var.getName());
            if (preadjustment.containsKey(sname)) {
                PreadjustmentVariable pvar = PreadjustmentVariable.calendarVariable(var, preadjustment.get(sname));
                model.addPreadjustment(pvar);
            } else {
                Variable tvar = Variable.calendarVariable(var, RegStatus.Undefined);
                if (null != td.getTest()) {
                    switch (td.getTest()) {
                        case Add:
                            tvar.status = RegStatus.ToAdd;
                            break;
                        case Remove:
                            tvar.status = RegStatus.ToRemove;
                            break;
                        default:
                            tvar.status = RegStatus.Prespecified;
                            break;
                    }
                }
                model.addVariable(tvar);
            }
        }
        if (tlp != LengthOfPeriodType.None) {
            LeapYearVariable var = new LeapYearVariable(tlp);
            String sname = ITsVariable.shortName(var.getName());
            if (preadjustment.containsKey(sname)) {
                PreadjustmentVariable pvar = PreadjustmentVariable.calendarVariable(var, preadjustment.get(sname));
                model.addPreadjustment(pvar);
            } else {
                Variable tvar = Variable.calendarVariable(var, RegStatus.Undefined);
                if (null != td.getTest()) {
                    switch (td.getTest()) {
                        case Add:
                            tvar.status = RegStatus.ToAdd;
                            break;
                        case Remove:
                            tvar.status = RegStatus.ToRemove;
                            break;
                        default:
                            tvar.status = RegStatus.Prespecified;
                            break;
                    }
                }
                model.addVariable(tvar);
            }
        }

        if (auto) {
            model.setTransformation(PreadjustmentType.Auto);
        }
    }

    private void initializeStockTradingDays(ModelDescription model, TradingDaysSpec td, Map<String, double[]> preadjustment) {
        StockTradingDaysVariables var = new StockTradingDaysVariables(td.getStockTradingDays());
        String sname = ITsVariable.shortName(var.getName());
        if (preadjustment.containsKey(sname)) {
            PreadjustmentVariable pvar = PreadjustmentVariable.calendarVariable(var, preadjustment.get(sname));
            model.addPreadjustment(pvar);
        } else {
            Variable tvar = Variable.calendarVariable(var, RegStatus.Undefined);
            switch (td.getTest()) {
                case Add:
                    tvar.status = RegStatus.ToAdd;
                    break;
                case Remove:
                    tvar.status = RegStatus.ToRemove;
                    break;
                default:
                    tvar.status = RegStatus.Prespecified;
                    break;
            }
            model.addVariable(tvar);
        }
    }

}
