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

import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.dstats.F;
import ec.tstoolkit.dstats.ProbabilityType;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.RegStatus;
import ec.tstoolkit.modelling.Variable;
import ec.tstoolkit.modelling.arima.*;
import ec.tstoolkit.sarima.estimation.GlsSarimaMonitor;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.GregorianCalendarVariables;
import ec.tstoolkit.timeseries.regression.ICalendarVariable;
import ec.tstoolkit.timeseries.regression.IMovingHolidayVariable;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.LeapYearVariable;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.regression.TsVariableSelection;
import java.util.List;

/**
 *
 * @author gianluca
 */
public class RegressionTestTD2 extends AbstractTramoModule implements IPreprocessingModule {

    public static final double DEF_TVAL = 1.96;

    private final double pftd_;
    private double tval_ = DEF_TVAL;

    public RegressionTestTD2(double pftd) {
        pftd_ = pftd;
    }

    public double getPftd() {
        return pftd_;
    }

    public double getTValue() {
        return tval_;
    }

    public void setTvalue(double tval) {
        tval_ = tval;
    }

    @Override
    public ProcessingResult process(ModellingContext context) {
        if ((context.description.getCalendars() == null || context.description.getCalendars().isEmpty())
                && (context.description.getMovingHolidays() == null || context.description.getMovingHolidays().isEmpty())) {
            return ProcessingResult.Unprocessed;
        }

//      First case TD=0 or Just test EE
        ModelDescription test0 = createTestModel(context, TradingDaysType.None, LengthOfPeriodType.None);
        ModellingContext cxt0 = new ModellingContext();
        cxt0.description = test0;

        ModelEstimation regarima0 = new ModelEstimation(test0.buildRegArima());
        int nhp = test0.getArimaComponent().getFreeParametersCount();
        GlsSarimaMonitor monitor = getMonitor();
        regarima0.compute(monitor, nhp);
        cxt0.estimation = regarima0;
        Double SS0 = regarima0.getLikelihood().getSsqErr();
        Double SSmc0 = regarima0.getLikelihood().getSigma();

        if (context.description.getCalendars() == null || context.description.getCalendars().isEmpty()) {
            boolean mean = Math.abs(cxt0.estimation.getLikelihood().getTStats()[0]) > tval_;
            context.description = backModel(context, TradingDaysType.None, LengthOfPeriodType.None, checkEE(cxt0), mean);
            transferLogs(cxt0, context);
            return ProcessingResult.Changed;
        }
//      Second case TD=TradindDay+Leapyear
        ModelDescription test1 = createTestModel(context, TradingDaysType.TradingDays, LengthOfPeriodType.None);
        ModellingContext cxt1 = new ModellingContext();
        cxt1.description = test1;
        ModelEstimation regarima1 = new ModelEstimation(test1.buildRegArima());
        nhp = test1.getArimaComponent().getFreeParametersCount();
        regarima1.compute(monitor, nhp);
        cxt1.estimation = regarima1;
        ConcentratedLikelihood ll = cxt1.estimation.getLikelihood();
        double SS6 = regarima1.getLikelihood().getSsqErr();
        int df = ll.getN() - ll.getNx() - nhp;
        double SSmc6 = ll.getSsqErr() / df;
        double Ftd = (SS0 - SS6) / (SSmc6 * 6);
        double pFtd6 = 0.0;
        if (Ftd >= 0) {
            F f0 = new F();
            f0.setDFDenom(df);
            f0.setDFNum(6);
            pFtd6 = f0.getProbability(Ftd, ProbabilityType.Lower);
        }

//      Third case TD=WorkingDay+LeapYear
        ModelDescription test2 = createTestModel(context, TradingDaysType.WorkingDays, LengthOfPeriodType.None);
        ModellingContext cxt2 = new ModellingContext();
        cxt2.description = test2;
        ModelEstimation regarima2 = new ModelEstimation(test2.buildRegArima());
        nhp = test1.getArimaComponent().getFreeParametersCount();
        regarima2.compute(monitor, nhp);
        cxt2.estimation = regarima2;
        ll = cxt2.estimation.getLikelihood();
        double SS1 = cxt2.estimation.getLikelihood().getSsqErr();
        df = ll.getN() - ll.getNx() - nhp;
        double SSmc1 = ll.getSsqErr() / df;
        Ftd = (SS0 - SS1) / SSmc1;
        double pFtd1 = 0.0;
        if (Ftd >= 0) {
            F f1 = new F();
            f1.setDFDenom(df);
            f1.setDFNum(1);
            pFtd1 = f1.getProbability(Ftd, ProbabilityType.Lower);
        }

// Check over the 3 cases        
        if ((pFtd6 > pFtd1) && (pFtd6 > 1 - pftd_)) {
            addTDInfo(context, pFtd1, pFtd6, 6);
            ModelDescription all = createTestModel(context, TradingDaysType.TradingDays, LengthOfPeriodType.LeapYear);
            cxt1.description = all;
            ModelEstimation regarima = new ModelEstimation(all.buildRegArima());
            regarima.compute(monitor, nhp);
            cxt1.estimation = regarima;
            if (!checkLY(cxt1)) {
                boolean mean = Math.abs(cxt1.estimation.getLikelihood().getTStats()[0]) > tval_;
                context.description = backModel(context, TradingDaysType.TradingDays, LengthOfPeriodType.None, checkEE(cxt1), mean);
            } else {
                boolean mean = Math.abs(cxt1.estimation.getLikelihood().getTStats()[0]) > tval_;
                context.description = backModel(context, TradingDaysType.TradingDays, LengthOfPeriodType.LeapYear, checkEE(cxt1), mean);
            }
            transferLogs(cxt1, context);
        } else if (pFtd1 < 1 - pftd_) {
            addTDInfo(context, pFtd1, pFtd6, 0);
            boolean mean = Math.abs(cxt0.estimation.getLikelihood().getTStats()[0]) > tval_;
            context.description = backModel(context, TradingDaysType.None, LengthOfPeriodType.None, checkEE(cxt0), mean);
            transferLogs(cxt0, context);
        } else {
            addTDInfo(context, pFtd1, pFtd6, 1);
            ModelDescription all = createTestModel(context, TradingDaysType.WorkingDays, LengthOfPeriodType.LeapYear);
            cxt2.description = all;
            ModelEstimation regarima = new ModelEstimation(all.buildRegArima());
            regarima.compute(monitor, nhp);
            cxt2.estimation = regarima;
            if (!checkLY(cxt2)) {
                boolean mean = Math.abs(cxt2.estimation.getLikelihood().getTStats(true, 2)[0]) > tval_;
                context.description = backModel(context, TradingDaysType.WorkingDays, LengthOfPeriodType.None, checkEE(cxt2), mean);
            } else {
                boolean mean = Math.abs(cxt2.estimation.getLikelihood().getTStats(true, 2)[0]) > tval_;
                context.description = backModel(context, TradingDaysType.WorkingDays, LengthOfPeriodType.LeapYear, checkEE(cxt2), mean);
            }
            transferLogs(cxt2, context);
        }
        return ProcessingResult.Changed;
    }

    private ModelDescription createTestModel(ModellingContext context, TradingDaysType td, LengthOfPeriodType lp) {
        ModelDescription model = context.description.clone();
        model.setAirline(context.hasseas);
        model.setMean(true);
        model.setOutliers(null);
//        model.setPrespecifiedOutliers(null);
// remove previous calendar effects 
        model.getCalendars().clear();
        if (td != TradingDaysType.None) {
            model.getCalendars().add(new Variable(GregorianCalendarVariables.getDefault(td), ComponentType.CalendarEffect, RegStatus.Accepted));
        }
        if (lp != LengthOfPeriodType.None) {
            model.getCalendars().add(new Variable(new LeapYearVariable(lp), ComponentType.CalendarEffect, RegStatus.Accepted));
        }
        return model;
    }

    private ModelDescription backModel(ModellingContext context, TradingDaysType td, LengthOfPeriodType lp, boolean Ee, boolean mean) {
        ModelDescription model = context.description.clone();
        if (context.automodelling) {
            model.setMean(mean);
        }
        model.setOutliers(null);
        model.getCalendars().clear();
        if (!Ee) {
            model.getMovingHolidays().clear();
        } else {
            TsVariableList x = model.buildRegressionVariables();
            TsVariableSelection sel = x.selectCompatible(IMovingHolidayVariable.class);
            TsVariableSelection.Item<ITsVariable>[] items = sel.elements();
            for (int i = 0; i < items.length; ++i) {
                Variable search = Variable.search(model.getMovingHolidays(), items[i].variable);
                if (search.status.needTesting()) {
                    search.status = RegStatus.Accepted;
                }
            }
        }
        if (td != TradingDaysType.None) {
            GregorianCalendarVariables vars = tdvars(context);
            vars.setDayOfWeek(td);
            model.getCalendars().add(new Variable(vars, ComponentType.CalendarEffect, RegStatus.Accepted));
        }
        if (lp != LengthOfPeriodType.None) {
            model.getCalendars().add(new Variable(new LeapYearVariable(lp), ComponentType.CalendarEffect, RegStatus.Accepted));
        }
        return model;
    }

    private GregorianCalendarVariables tdvars(ModellingContext context) {
        List<Variable> calendars = context.description.getCalendars();
        for (Variable var : calendars) {
            if (var.isCompatible(GregorianCalendarVariables.class)) {
                return ((GregorianCalendarVariables) var.getVariable()).clone();
            }
        }
        return GregorianCalendarVariables.getDefault(TradingDaysType.None);
    }

    private boolean checkLY(ModellingContext model) {
        boolean retval = true;
        ConcentratedLikelihood ll = model.estimation.getLikelihood();
        int start = model.description.getRegressionVariablesStartingPosition();
        TsVariableList x = model.description.buildRegressionVariables();
        TsVariableSelection sel = x.selectCompatible(ICalendarVariable.class);
        TsVariableSelection.Item<ITsVariable>[] items = sel.elements();
        double[] Tstat = ll.getTStats(true, 2);//airline
        double t = Tstat[start + items[items.length - 1].position];
        if (Math.abs(t) < 2.0) {
            addLPInfo(model, t);
            retval = false;
        }
        return retval;
    }

    private boolean checkEE(ModellingContext model) {
        boolean retval = true;
        if (model.description.getMovingHolidays() == null || model.description.getMovingHolidays().isEmpty()) {
            return false;
        }
        TsVariableList x = model.description.buildRegressionVariables();
        TsVariableSelection sel = x.selectCompatible(IMovingHolidayVariable.class);
        if (sel.isEmpty()) {
            return false;
        }
        TsVariableSelection.Item<ITsVariable>[] items = sel.elements();
        Variable search = Variable.search(model.description.getMovingHolidays(), items[items.length - 1].variable);
        if (search == null) { // should never happen
            return false;
        }
        if (!search.status.needTesting()) {
            return true;
        }
        ConcentratedLikelihood ll = model.estimation.getLikelihood();
        int start = model.description.getRegressionVariablesStartingPosition();
        double[] Tstat = ll.getTStats(true, 2);//airline
        double t = Tstat[start + items[items.length - 1].position];
        if (Math.abs(t) < 2.2) {
            addEasterInfo(model, t);
            retval = false;
        }
        return retval;
    }

    private void addLPInfo(ModellingContext context, double tstat) {
//        if (context.processingLog != null) {
//            StringBuilder builder = new StringBuilder();
//            builder.append("Mean not significant (T=").append(tstat).append(')');
//            context.processingLog.add(ProcessingInformation.info(REGS,
//                    RegressionTestTD2.class.getName(), builder.toString(), null));
//        }
    }

    private void addEasterInfo(ModellingContext context, double tstat) {
//        if (context.processingLog != null) {
//            StringBuilder builder = new StringBuilder();
//            builder.append("Easter not significant (T=").append(tstat).append(')');
//            context.processingLog.add(ProcessingInformation.info(REGS,
//                    RegressionTestTD2.class.getName(), builder.toString(), null));
//        }
    }

    private void addTDInfo(ModellingContext context, double pwd, double ptd, int sel) {
//        if (context.processingLog != null) {
//            StringBuilder builder = new StringBuilder();
//            builder.append("F-test on TD (P-value): ").append(ptd);
//            context.processingLog.add(ProcessingInformation.info(REGS,
//                    RegressionTestTD2.class.getName(), builder.toString(), null));
//            builder = new StringBuilder();
//            builder.append("F-test on WD (P-value): ").append(pwd);
//            context.processingLog.add(ProcessingInformation.info(REGS,
//                    RegressionTestTD2.class.getName(), builder.toString(), null));
//            String msg;
//            switch (sel) {
//                case 1:
//                    msg = "WD selected";
//                    break;
//                case 6:
//                    msg = "TD selected";
//                    break;
//                default:
//                    msg = "No trading days effects";
//                    break;
//            }
//            context.processingLog.add(ProcessingInformation.info(REGS,
//                    RegressionTestTD2.class.getName(), msg, null));
//        }
    }

    private static final String TD = "TD F-Test", REGS = "Regression variables";


}
