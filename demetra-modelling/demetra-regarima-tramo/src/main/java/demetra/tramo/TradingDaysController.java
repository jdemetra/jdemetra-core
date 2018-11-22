/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramo;

import demetra.data.DoubleSequence;
import demetra.modelling.Variable;
import demetra.modelling.regression.GenericTradingDaysVariables;
import demetra.modelling.regression.TradingDaysType;
import demetra.regarima.regular.ModelDescription;
import demetra.regarima.regular.PreprocessingModel;
import demetra.regarima.regular.ProcessingResult;
import demetra.regarima.regular.RegArimaModelling;
import demetra.timeseries.calendars.LengthOfPeriodType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 *
 * @author Jean Palate
 */
public class TradingDaysController extends ModelController {

    private TradingDaysType td;
    private LengthOfPeriodType lp;
    private double ptd = 0.01;

    public TradingDaysController(double ptd) {
        this.ptd = ptd;
        td = TradingDaysType.WorkingDays;
        lp = LengthOfPeriodType.None;
    }

    public TradingDaysController(TradingDaysType td, LengthOfPeriodType lp, double ptd) {
        this.td = td;
        this.lp = lp;
        this.ptd = ptd;
    }

    @Override
    public ProcessingResult process(RegArimaModelling context) {
        // find td variables
        ModelDescription desc = context.getDescription();
        Optional<Variable> findAny = desc.variables().filter(var ->var.isPrespecified() && var.isPrespecified()).findAny();
        // nothing to do if td is prespecified
        if (findAny.isPresent()) {
            return ProcessingResult.Unchanged;
        }
        if (!needProcessing(context)) {
            return ProcessingResult.Unchanged;
        }
        ModelDescription nmodel = newModel(context);
        nmodel.removeVariable(var->var.isOutlier(false));
        // compute the corresponding airline model.
        RegArimaModelling ncontext = new RegArimaModelling();
        ncontext.setDescription(nmodel);
        if (!estimate(ncontext, true)) {
            return ProcessingResult.Failed;
        }
        PreprocessingModel current = context.build();
        PreprocessingModel ncurrent = ncontext.build();
        ModelComparator mcmp = ModelComparator.builder().build();
        int cmp = mcmp.compare(current, ncurrent);
        if (cmp < 1) {
//            setReferenceModel(current);
            return ProcessingResult.Unchanged;
        } else {
//            setReferenceModel(ncurrent);
            transferInformation(ncontext, context);
            return ProcessingResult.Changed;
        }
    }

    private boolean needProcessing(RegArimaModelling context) {
        DoubleSequence res = context.getEstimation().getConcentratedLikelihood().e();
        RegModel reg = new RegModel();
        reg.setY(new DataBlock(res));
        GregorianCalendarVariables tdvars = tdvars(context);
        tdvars.setDayOfWeek(td);
        int ntd = td.getVariablesCount();
        TsDomain edomain = context.description.getEstimationDomain();
        // drop the number of data corresponding to the number of regression variables 
        edomain = edomain.drop(edomain.getLength() - res.length, 0);
        List<DataBlock> bvars = new ArrayList<DataBlock>(ntd);
        for (int i = 0; i < ntd; ++i) {
            bvars.add(new DataBlock(edomain.getLength()));
        }
        tdvars.data(edomain, bvars);
        //       BackFilter ur = context.description.getArimaComponent().getDifferencingFilter();
        for (int i = 0; i < ntd; ++i) {
            DataBlock cur = bvars.get(i);
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

    private ModelDescription newModel(RegArimaModelling context) {
        ModelDescription ndesc = new ModelDescription(context.getDescription());
        GenericTradingDaysVariables tdvars = tdvars(context);
        ndesc.removeVariable(var -> var.isCalendar());
        ndesc.addVariable(new Variable(tdvars, false));
        return ndesc;
    }

    private GenericTradingDaysVariables tdvars(RegArimaModelling context) {
        Optional<Variable> found = context.description.variables().filter(var -> var.isCalendar()).findAny();
        if (found.isPresent()) {
            return ((GregorianCalendarVariables) found.get().getVariable()).clone();
        } else {
            return GregorianCalendarVariables.getDefault(TradingDaysType.None);
        }
    }
}
