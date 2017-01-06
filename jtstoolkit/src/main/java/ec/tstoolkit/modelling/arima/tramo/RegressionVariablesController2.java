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

/**
 *
 * @author gianluca
 */
public class RegressionVariablesController2 extends AbstractTramoModule implements IPreprocessingModule {

    public static final double DEF_TVAL = 1.96;

    private final double pftd_;
    private double tval_ = DEF_TVAL;

    public RegressionVariablesController2(double pftd) {
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
        if (context.description.contains(var -> var.isCalendar() || var.isMovingHoliday())) {
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

        if (context.description.contains(var -> var.isCalendar())) {
            boolean mean = Math.abs(cxt0.estimation.getLikelihood().getTStats()[0]) > tval_;
            context.description = backModel(context, TradingDaysType.None, LengthOfPeriodType.None, checkEE(cxt0), mean);
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
        double SSmc6 = ll.getSsqErr() / (ll.getN() - ll.getNx());
        double Ftd = (SS0 - SS6) / (SSmc6 * 6);
        double pFtd6 = 0.0;
        if (Ftd >= 0) {
            F f0 = new F();
            f0.setDFDenom(ll.getN() - ll.getNx());
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
        double SSmc1 = ll.getSsqErr() / (ll.getN() - ll.getNx());
        Ftd = (SS0 - SS1) / SSmc1;
        double pFtd1 = 0.0;
        if (Ftd >= 0) {
            F f1 = new F();
            f1.setDFDenom(ll.getN() - ll.getNx());
            f1.setDFNum(1);
            pFtd1 = f1.getProbability(Ftd, ProbabilityType.Lower);
        }

// Check over the 3 cases        
        if ((pFtd6 > pFtd1) && (pFtd6 > 1 - pftd_)) {
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
        } else if (pFtd1 < 1 - pftd_) {
            boolean mean = Math.abs(cxt0.estimation.getLikelihood().getTStats()[0]) > tval_;
            context.description = backModel(context, TradingDaysType.None, LengthOfPeriodType.None, checkEE(cxt0), mean);
        } else {
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
        }
        return ProcessingResult.Changed;
    }

    private ModelDescription createTestModel(ModellingContext context, TradingDaysType td, LengthOfPeriodType lp) {
        ModelDescription model = context.description.clone();
        model.setAirline(context.hasseas);
        model.setMean(true);
        model.setOutliers(null);
        model.setPrespecifiedOutliers(null);
// remove previous calendar effects 
        model.removeVariable(var->var.isCalendar());
        if (td != TradingDaysType.None) {
            model.addVariable(Variable.calendarVariable(GregorianCalendarVariables.getDefault(td), RegStatus.Accepted));
        }
        if (lp != LengthOfPeriodType.None) {
            model.addVariable(Variable.calendarVariable(new LeapYearVariable(lp), RegStatus.Accepted));
        }
        return model;
    }

    private ModelDescription backModel(ModellingContext context, TradingDaysType td, LengthOfPeriodType lp, boolean Ee, boolean mean) {
        ModelDescription model = context.description.clone();
        if (context.automodelling) {
            model.setMean(mean);
        }
        model.setOutliers(null);
        model.removeVariable(var->var.isCalendar());
        if (!Ee) {
            model.removeVariable(var->var.isMovingHoliday());
        } else {
            TsVariableList x = model.buildRegressionVariables();
            TsVariableSelection sel = x.selectCompatible(IMovingHolidayVariable.class);
            TsVariableSelection.Item<ITsVariable>[] items = sel.elements();
            items = sel.elements();
            for (int i = 0; i < items.length; ++i) {
                Variable search = model.searchVariable(items[i].variable);
                search.status = RegStatus.Accepted;
            }
        }
        if (td != TradingDaysType.None) {
            model.addVariable(Variable.calendarVariable(GregorianCalendarVariables.getDefault(td), RegStatus.Accepted));
        }
        if (lp != LengthOfPeriodType.None) {
            model.addVariable(Variable.calendarVariable(new LeapYearVariable(lp), RegStatus.Accepted));
        }
        return model;
    }

    private boolean checkLY(ModellingContext model) {
        boolean retval = true;
        ConcentratedLikelihood ll = model.estimation.getLikelihood();
        int start = model.description.getRegressionVariablesStartingPosition();
        TsVariableList x = model.description.buildRegressionVariables();
        TsVariableSelection sel = x.selectCompatible(ICalendarVariable.class);
        TsVariableSelection.Item<ITsVariable>[] items = sel.elements();
        double[] Tstat = ll.getTStats(true, 2);//airline
        if (Math.abs(Tstat[start + items[items.length - 1].position]) < 2.0) {
            retval = false;
        }
        return retval;
    }

    private boolean checkEE(ModellingContext model) {
        boolean retval = true;
        if (model.description.contains(var->var.isMovingHoliday())) {
            return false;
        }
        TsVariableList x = model.description.buildRegressionVariables();
        TsVariableSelection sel = x.selectCompatible(IMovingHolidayVariable.class);
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
        if (Math.abs(Tstat[start + items[items.length - 1].position]) < 2.2) {
            retval = false;
        }
        return retval;
    }
}
