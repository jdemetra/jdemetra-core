/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramo;

import jdplus.data.DataBlock;
import jdplus.linearmodel.LeastSquaresResults;
import jdplus.linearmodel.LinearModel;
import jdplus.linearmodel.Ols;
import demetra.modelling.regression.Variable;
import jdplus.regarima.regular.ModelDescription;
import jdplus.regarima.regular.PreprocessingModel;
import jdplus.regarima.regular.ProcessingResult;
import jdplus.regarima.regular.RegArimaModelling;
import demetra.timeseries.TsDomain;
import java.util.List;
import java.util.Optional;
import demetra.modelling.regression.ITradingDaysVariable;
import jdplus.modelling.regression.Regression;
import demetra.data.DoubleSeq;
import jdplus.maths.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
class TradingDaysController extends ModelController {

    private final ITradingDaysVariable td;
//    private final ILengthOfPeriodVariable lp;
    private double ptd = 0.01;

//    TradingDaysController(final ITradingDaysVariable td, ILengthOfPeriodVariable lp, double ptd) {
    TradingDaysController(final ITradingDaysVariable td, double ptd) {
        this.td = td;
//        this.lp = lp;
        this.ptd = ptd;
    }

    @Override
    ProcessingResult process(RegArimaModelling modelling, TramoProcessor.Context context) {
        // find td variables
        ModelDescription desc = modelling.getDescription();
        boolean hascal = desc.variables().anyMatch(var ->(!var.isPrespecified()) && var.isCalendar());
        // nothing to do if td is prespecified
        if (hascal) {
            return ProcessingResult.Unchanged;
        }
        if (!needProcessing(modelling)) {
            return ProcessingResult.Unchanged;
        }
        ModelDescription nmodel = newModel(modelling);
        nmodel.removeVariable(var->var.isOutlier(false));
        // compute the corresponding airline model.
        RegArimaModelling ncontext = new RegArimaModelling();
        ncontext.setDescription(nmodel);
        if (!estimate(ncontext, true)) {
            return ProcessingResult.Failed;
        }
        PreprocessingModel current = modelling.build();
        PreprocessingModel ncurrent = ncontext.build();
        ModelComparator mcmp = ModelComparator.builder().build();
        int cmp = mcmp.compare(current, ncurrent);
        if (cmp < 1) {
//            setReferenceModel(current);
            return ProcessingResult.Unchanged;
        } else {
//            setReferenceModel(ncurrent);
            transferInformation(ncontext, modelling);
            return ProcessingResult.Changed;
        }
    }

    private boolean needProcessing(RegArimaModelling context) {
        DoubleSeq res = context.getEstimation().getConcentratedLikelihood().e();
        LinearModel.Builder builder = LinearModel.builder();
        builder.y(res);
        
        TsDomain domain = context.getDescription().getDomain();
        // drop the number of data corresponding to the number of regression variables 
        domain = domain.drop(domain.getLength() - res.length(), 0);
        if (td != null){
            FastMatrix mtd = Regression.matrix(domain, td);
            builder.addX(mtd);
        }
            
        LinearModel lm = builder.build();
        
        Ols ols = new Ols();
        LeastSquaresResults lsr = ols.compute(lm);
        if (lsr == null) {
            return false;
        }
        
        return lsr.Ftest().getPValue()<ptd;
    }

    private ModelDescription newModel(RegArimaModelling context) {
        ModelDescription ndesc = new ModelDescription(context.getDescription());
        ndesc.removeVariable(var -> var.isCalendar());
        ndesc.addVariable(new Variable(td, "td", false));
        return ndesc;
    }

}
