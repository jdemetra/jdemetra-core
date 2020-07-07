/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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

import ec.tstoolkit.arima.estimation.LikelihoodStatistics;
import ec.tstoolkit.dstats.F;
import ec.tstoolkit.dstats.ProbabilityType;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.modelling.RegStatus;
import ec.tstoolkit.modelling.Variable;
import ec.tstoolkit.modelling.arima.*;
import ec.tstoolkit.timeseries.calendars.IGregorianCalendarProvider;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.*;
import java.util.Optional;

/**
 * * @author gianluca, jean Correction 22/7/2014. pre-specified Easter effect
 * was not handled with auto-td
 */
public class RegressionTestTD extends AbstractTramoModule implements IPreprocessingModule {

    private static final String REGS = "Regression variables";

    private static final double DEF_MODEL_EPS = .01, DEF_CONSTRAINT_EPS = .03;
    private PreprocessingModel tdModel, td6Model;
    private LikelihoodStatistics ntdStats, td1Stats, td6Stats;
    private final double pftd_, pfwd_;
    private double pdel, pwd, ptd;//, pdel2;
    private double sigma;
    private static final double DEF_TVAL = 1.96;
    private double tval_ = DEF_TVAL;
    private final IGregorianCalendarProvider calendar;

    public RegressionTestTD(IGregorianCalendarProvider calendar, double pftd) {
        pftd_ = pftd;
        pfwd_ = pftd;
        this.calendar=calendar;
    }

    public RegressionTestTD(IGregorianCalendarProvider calendar, double pftd, double pfwd) {
        pftd_ = pftd;
        pfwd_ = pfwd;
        this.calendar=calendar;
    }

    public double getPftd() {
        return pftd_;
    }

    public double getPfwd() {
        return pfwd_;
    }

    public double getTValue() {
        return tval_;
    }

    public void setTvalue(double tval) {
        tval_ = tval;
    }

    @Override
    public ProcessingResult process(ModellingContext context) {
        if (!context.description.contains(var -> var.isCalendar() || var.isMovingHoliday())) {
            return ProcessingResult.Unprocessed;
        }
        // Step 1: choose between TD and WD
        td6Model = createModel(context, TradingDaysType.TradingDays, LengthOfPeriodType.None);
        if (td6Model == null) {
            return ProcessingResult.Failed;
        }
        ConcentratedLikelihood ll = td6Model.estimation.getLikelihood();
        td6Stats = td6Model.estimation.getStatistics();
        int nhp = td6Model.description.getArimaComponent().getFreeParametersCount();
        int df = ll.getN() - ll.getNx() - nhp;
        sigma = ll.getSsqErr() / df;

        GregorianCalendarVariables vars = tdvars(context);
        td1Stats = check(vars, TradingDaysType.WorkingDays, LengthOfPeriodType.None);
        ntdStats = check(vars, TradingDaysType.None, LengthOfPeriodType.None);
        calcProb();
        TradingDaysType td = TradingDaysType.None;
        LengthOfPeriodType lp = LengthOfPeriodType.LeapYear;

        int sel = 0;
        if (pdel < pfwd_ && ptd < pftd_) {// Prefer TD
            td = TradingDaysType.TradingDays;
            sel = 6;
        } // Prefer WD
        else if (pwd < pftd_) {
            td = TradingDaysType.WorkingDays;
            sel = 1;
        } else {
            td = TradingDaysType.None;
            lp = LengthOfPeriodType.None;
        }
//        addTDInfo(context, 1 - pwd, 1 - ptd, 1 - pdel, sel);

        tdModel = createModel(context, td, lp);
        if (td == TradingDaysType.None || !checkLY(tdModel)) {
            boolean mean = Math.abs(tdModel.estimation.getLikelihood().getTStats(true, 2)[0]) > 1.96;
            context.description = backModel(context, td, LengthOfPeriodType.None, checkEE(tdModel), mean);
        } else {
            boolean mean = Math.abs(tdModel.estimation.getLikelihood().getTStats(true, 2)[0]) > 1.96;
            context.description = backModel(context, td, LengthOfPeriodType.LeapYear, checkEE(tdModel), mean);
        }
        context.estimation = null;
        transferLogs(tdModel, context);
        return ProcessingResult.Changed;
    }

    private void calcProb() {
        F fstat = new F();
        pdel = 1;
        ptd = 1;
        pwd = 1;

        int nhp = td6Model.description.getArimaComponent().getFreeParametersCount();
        ConcentratedLikelihood ll = td6Model.estimation.getLikelihood();
        int df = ll.getN() - ll.getNx() - nhp;
        fstat.setDFDenom(df);
        if (td6Stats != null && td1Stats != null) {
            double fdel = (td1Stats.SsqErr - td6Stats.SsqErr) / (5 * sigma);
            if (fdel > 0) {
                fstat.setDFNum(5);
                pdel = fstat.getProbability(fdel, ProbabilityType.Upper);
            }
        }
        if (ntdStats == null) {
            if (td6Stats != null) {
                ptd = 0;
            }
            if (td1Stats != null) {
                pwd = 0;
            }
        } else {

            if (td6Stats != null) {
                double ftd = (ntdStats.SsqErr - td6Stats.SsqErr) / (6 * sigma);
                if (ftd > 0) {
                    fstat.setDFNum(6);
                    ptd = fstat.getProbability(ftd, ProbabilityType.Upper);
                }
            }

            if (td1Stats != null) {
                double fwd = (ntdStats.SsqErr - td1Stats.SsqErr) / sigma;
                if (fwd > 0) {
                    fstat.setDFNum(1);
                    pwd = fstat.getProbability(fwd, ProbabilityType.Upper);
                }
            }
        }
    }

    private PreprocessingModel createModel(ModellingContext context, TradingDaysType td, LengthOfPeriodType lp) {
        ModelDescription model = context.description.clone();
        model.setAirline(context.hasseas);
        model.setMean(true);
        model.setOutliers(null);

        // remove previous calendar effects 
        model.removeVariable(var ->var.isCalendar());
        if (td != TradingDaysType.None) {
            GregorianCalendarVariables vars = tdvars(context);
            vars.setDayOfWeek(td);
            model.addVariable(Variable.calendarVariable(vars, RegStatus.Accepted));
        }
        if (lp != LengthOfPeriodType.None) {
            model.addVariable(Variable.calendarVariable(new LeapYearVariable(lp), RegStatus.Accepted));
        }
        ModellingContext cxt = new ModellingContext();
        cxt.description = model;
        ModelEstimation estimation = new ModelEstimation(model.buildRegArima());
        int nhp = model.getArimaComponent().getFreeParametersCount();
        estimation.compute(getMonitor(), nhp);
        cxt.estimation = estimation;
        return cxt.current(true);
    }

//    private PreprocessingModel createWdModel(ModellingContext context) {
//        ModelDescription model = context.description.clone();
//        model.setAirline(context.hasseas);
//        model.setMean(true);
//        model.setOutliers(null);
//
//        // remove previous calendar effects 
//        GregorianCalendarVariables vars = tdvars(context);
//        vars.setDayOfWeek(TradingDaysType.WorkingDays);
//        model.getCalendars().clear();
//        model.getCalendars().add(new Variable(vars, ComponentType.CalendarEffect, RegStatus.Accepted));
//        model.getCalendars().add(new Variable(new LeapYearVariable(LengthOfPeriodType.LeapYear), ComponentType.CalendarEffect, RegStatus.Accepted));
//
//        ModellingContext cxt = new ModellingContext();
//        cxt.description = model;
//        ModelEstimation estimation = new ModelEstimation(model.buildRegArima());
//        int nhp = model.getArimaComponent().getFreeParametersCount();
//        estimation.compute(getMonitor(), nhp);
//        cxt.estimation = estimation;
//        return cxt.current(true);
//    }
//    
//    private PreprocessingModel createNtdModel(ModellingContext context) {
//        ModelDescription model = context.description.clone();
//        model.setAirline(context.hasseas);
//        model.setMean(true);
//        model.setOutliers(null);
//
//        // remove previous calendar effects 
//        model.getCalendars().clear();
//
//        ModellingContext cxt = new ModellingContext();
//        cxt.description = model;
//        ModelEstimation estimation = new ModelEstimation(model.buildRegArima());
//        int nhp = model.getArimaComponent().getFreeParametersCount();
//        estimation.compute(getMonitor(), nhp);
//        cxt.estimation = estimation;
//        return cxt.current(true);
//    }
//
    private LikelihoodStatistics check(GregorianCalendarVariables tdvar, TradingDaysType td, LengthOfPeriodType lp) {
        ModelDescription model = td6Model.description.clone();

        // remove previous calendar effects 
        model.removeVariable(var -> var.isCalendar());
        if (td != TradingDaysType.None) {
            tdvar.setDayOfWeek(td);
            model.addVariable(Variable.calendarVariable(tdvar, RegStatus.Accepted));
        }
        if (lp != LengthOfPeriodType.None) {
            model.addVariable(Variable.calendarVariable(new LeapYearVariable(lp), RegStatus.Accepted));
        }
        ModelEstimation estimation = new ModelEstimation(model.buildRegArima());
        int nhp = model.getArimaComponent().getFreeParametersCount();
        estimation.computeLikelihood(nhp);
        return estimation.getStatistics();
    }

    private ModelDescription backModel(ModellingContext context, TradingDaysType td, LengthOfPeriodType lp, boolean Ee, boolean mean) {
        ModelDescription model = context.description.clone();
        if (context.automodelling) {
            model.setMean(mean);
        }
        model.setOutliers(null);
        model.removeVariable(var -> var.isCalendar());
        if (!Ee) {
            model.removeVariable(var-> var.isMovingHoliday());
        } else {
            TsVariableList x = model.buildRegressionVariables();
            TsVariableSelection sel = x.selectCompatible(IMovingHolidayVariable.class);
            TsVariableSelection.Item<ITsVariable>[] items = sel.elements();
            for (int i = 0; i < items.length; ++i) {
                Variable search = model.searchVariable(items[i].variable);
                if (search.status.needTesting()) {
                    search.status = RegStatus.Accepted;
                }
            }
        }
        if (td != TradingDaysType.None) {
            GregorianCalendarVariables vars = tdvars(context);
            vars.setDayOfWeek(td);
            model.addVariable(Variable.calendarVariable(vars, RegStatus.Accepted));
        }
        if (lp != LengthOfPeriodType.None) {
            model.addVariable(Variable.calendarVariable(new LeapYearVariable(lp), RegStatus.Accepted));
        }
        return model;
    }

    private boolean checkLY(PreprocessingModel model) {
        boolean retval = true;
        ConcentratedLikelihood ll = model.estimation.getLikelihood();
        int start = model.description.getRegressionVariablesStartingPosition();
        TsVariableList x = model.description.buildRegressionVariables();
        TsVariableSelection sel = x.selectCompatible(ILengthOfPeriodVariable.class);
        TsVariableSelection.Item<ITsVariable>[] items = sel.elements();
        double[] Tstat = ll.getTStats(true, 2);//airline
        double t = Tstat[start + items[items.length - 1].position];
        if (Math.abs(t) < tval_) {
            addLPInfo(model, t);
            retval = false;
        }
        return retval;
    }

    private boolean checkEE(PreprocessingModel model) {
        boolean retval = true;
        TsVariableList x = model.description.buildRegressionVariables();
        TsVariableSelection sel = x.selectCompatible(EasterVariable.class);
        if (sel.isEmpty()) {
            return false;
        }
        TsVariableSelection.Item<ITsVariable>[] items = sel.elements();
        Variable search = model.description.searchVariable(items[items.length - 1].variable);
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

    private GregorianCalendarVariables tdvars(ModellingContext context) {
        Optional<Variable> found = context.description.variables()
                .filter(var -> var.getVariable() instanceof GregorianCalendarVariables)
                .findAny();
        if (found.isPresent()) {
            GregorianCalendarVariables gv = (GregorianCalendarVariables) found.get().getVariable();
            return gv.clone();
        } else {
            return new GregorianCalendarVariables(calendar, TradingDaysType.None);
        }
    }

    private void addLPInfo(PreprocessingModel model, double tstat) {
//        StringBuilder builder = new StringBuilder();
//        builder.append("Mean not significant (T=").append(tstat).append(')');
//        model.addProcessingInformation(ProcessingInformation.info(REGS,
//                RegressionTestTD.class.getName(), builder.toString(), null));
    }

    private void addEasterInfo(PreprocessingModel model, double tstat) {
//           StringBuilder builder = new StringBuilder();
//            builder.append("Easter not significant (T=").append(tstat).append(')');
//            model.addProcessingInformation(ProcessingInformation.info(REGS,
//                    RegressionTestTD.class.getName(), builder.toString(), null));
    }

    private void addTDInfo(ModellingContext context, double pwd, double ptd, double pdel, int sel) {
//        if (context.processingLog != null) {
//            StringBuilder builder = new StringBuilder();
//            builder.append("F-test on TD (P-value): ").append(ptd);
//            context.processingLog.add(ProcessingInformation.info(REGS,
//                    RegressionTestTD.class.getName(), builder.toString(), null));
//            builder = new StringBuilder();
//            builder.append("F-test on WD (P-value): ").append(pwd);
//            context.processingLog.add(ProcessingInformation.info(REGS,
//                    RegressionTestTD.class.getName(), builder.toString(), null));
//            builder = new StringBuilder();
//            builder.append("F-test on the restriction from TD to WD (P-value): ").append(pdel);
//            context.processingLog.add(ProcessingInformation.info(REGS,
//                    RegressionTestTD.class.getName(), builder.toString(), null));
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
//                    RegressionTestTD.class.getName(), msg, null));
//        }
    }
}
