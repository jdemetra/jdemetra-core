/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.eco.Ols;
import ec.tstoolkit.eco.RegModel;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.RegStatus;
import ec.tstoolkit.modelling.Variable;
import ec.tstoolkit.modelling.arima.AbstractModelController;
import ec.tstoolkit.modelling.arima.JointRegressionTest;
import ec.tstoolkit.modelling.arima.ModelDescription;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.ProcessingResult;
import ec.tstoolkit.modelling.arima.RegArimaEstimator;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.GregorianCalendarVariables;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class TDController extends AbstractModelController {

    private TradingDaysType td;
    private LengthOfPeriodType lp;
    private double ptd = 0.01;

    public TDController(double ptd) {
        this.ptd=ptd;
        td = TradingDaysType.WorkingDays;
        lp = LengthOfPeriodType.None;
    }

    public TDController(TradingDaysType td, LengthOfPeriodType lp, double ptd) {
        this.td = td;
        this.lp = lp;
        this.ptd=ptd;
    }

    @Override
    public ProcessingResult process(ModellingContext context) {
        if (Variable.isUsed(context.description.getCalendars())) {
            return ProcessingResult.Unchanged;
        }
        if (!needProcessing(context)) {
            return ProcessingResult.Unchanged;
        }
        ModelDescription nmodel = newModel(context);
        // compute the corresponding airline model.
        ModellingContext ncontext = new ModellingContext();
        ncontext.description = nmodel;
        ncontext.description.setOutliers(null);

        if (!estimate(ncontext, true)) {
            return ProcessingResult.Failed;
        }
        PreprocessingModel current = context.tmpModel();
        PreprocessingModel ncurrent = ncontext.tmpModel();
        int cmp = new ModelComparator().compare(current, ncurrent);
        if (cmp < 1) {
//            setReferenceModel(current);
            return ProcessingResult.Unchanged;
        } else {
//            setReferenceModel(ncurrent);
            transferInformation(ncontext, context);
            return ProcessingResult.Changed;
        }
    }

    private boolean needProcessing(ModellingContext context) {
        double[] res = context.estimation.getLikelihood().getResiduals();
        RegModel reg = new RegModel();
        reg.setY(new DataBlock(res));
        GregorianCalendarVariables tdvars = tdvars(context);
        tdvars.setDayOfWeek(td);
        int ntd = td.getVariablesCount();
        TsDomain edomain = context.description.getEstimationDomain();
        // drop the number of data corresponding to the number of regression variables 
        edomain=edomain.drop(edomain.getLength()-res.length, 0);
        List<DataBlock> bvars = new ArrayList<DataBlock>(ntd);
        for (int i = 0; i < ntd; ++i) {
            bvars.add(new DataBlock(edomain.getLength()));
        }
        tdvars.data(edomain, bvars);
 //       BackFilter ur = context.description.getArimaComponent().getDifferencingFilter();
        for (int i = 0; i < ntd; ++i) {
            DataBlock cur = bvars.get(i);
//            if (ur.getDegree() > 0) {
//                DataBlock dcur = new DataBlock(cur.getLength() - ur.getDegree());
//                ur.filter(cur, dcur);
//                reg.addX(dcur);
//            } else {
                reg.addX(cur);
//            }
        }
        Ols ols = new Ols();
        if (!ols.process(reg)) {
            return false;
        }
        JointRegressionTest test = new JointRegressionTest(ptd);
        return test.accept(ols.getLikelihood(), 0, 0, ntd, null);
//        DataBlock t = new DataBlock(ols.getLikelihood().getTStats());
//        double fval = t.ssq();
//        F f = new F();
//        f.setDFNum(ntd);
//        f.setDFDenom(res.getLength() - ntd);
//        return f.getProbability(fval, ProbabilityType.Upper) < ptd;
    }

    private ModelDescription newModel(ModellingContext context) {
        ModelDescription ndesc = context.description.clone();
        GregorianCalendarVariables tdvars = tdvars(context);
        tdvars.setDayOfWeek(td);
        ndesc.getCalendars().clear();
        ndesc.getCalendars().add(new Variable(tdvars, ComponentType.CalendarEffect, RegStatus.Accepted));
        return ndesc;
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
}
