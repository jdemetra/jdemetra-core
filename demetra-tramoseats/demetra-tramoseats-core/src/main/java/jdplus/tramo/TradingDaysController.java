/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramo;

import jdplus.linearmodel.LeastSquaresResults;
import jdplus.linearmodel.LinearModel;
import jdplus.linearmodel.Ols;
import demetra.timeseries.regression.Variable;
import jdplus.regsarima.regular.ModelDescription;
import jdplus.regsarima.regular.ModelEstimation;
import jdplus.regsarima.regular.ProcessingResult;
import jdplus.regsarima.regular.RegSarimaModelling;
import demetra.timeseries.TsDomain;
import demetra.timeseries.regression.ITradingDaysVariable;
import jdplus.modelling.regression.Regression;
import demetra.data.DoubleSeq;
import demetra.sa.ComponentType;
import demetra.sa.SaVariable;
import jdplus.math.matrices.Matrix;
import jdplus.regarima.ami.ModellingUtility;

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
    ProcessingResult process(RegSarimaModelling modelling, TramoContext context) {
        // find td variables
        ModelDescription desc = modelling.getDescription();
        boolean hascal = desc.variables().anyMatch(var ->(ModellingUtility.isAutomaticallyIdentified(var)) && (ModellingUtility.isDaysRelated(var)));
        // nothing to do if td is prespecified
        if (hascal) {
            return ProcessingResult.Unchanged;
        }
        if (!needProcessing(modelling)) {
            return ProcessingResult.Unchanged;
        }
        ModelDescription nmodel = newModel(modelling);
        nmodel.removeVariable(var->ModellingUtility.isOutlier(var, true));
        // compute the corresponding airline model.
        RegSarimaModelling ncontext = RegSarimaModelling.of(nmodel);
        if (!estimate(ncontext, true)) {
            return ProcessingResult.Failed;
        }
        ModelEstimation current = modelling.build();
        ModelEstimation ncurrent = ncontext.build();
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

    private boolean needProcessing(RegSarimaModelling context) {
        DoubleSeq res = context.getEstimation().getConcentratedLikelihood().e();
        LinearModel.Builder builder = LinearModel.builder();
        builder.y(res);
        
        TsDomain domain = context.getDescription().getEstimationDomain();
        // drop the number of data corresponding to the number of regression variables 
        domain = domain.drop(domain.getLength() - res.length(), 0);
        if (td != null){
            Matrix mtd = Regression.matrix(domain, td);
            builder.addX(mtd);
        }
            
        LinearModel lm = builder.build();
        
        LeastSquaresResults lsr = Ols.compute(lm);
        if (lsr == null) {
            return false;
        }
        
        return lsr.Ftest().getPValue()<ptd;
    }

    private ModelDescription newModel(RegSarimaModelling context) {
        ModelDescription ndesc = ModelDescription.copyOf(context.getDescription());
        ndesc.removeVariable(var -> ModellingUtility.isDaysRelated(var));
        ndesc.addVariable(Variable.variable("td", td, TramoModelBuilder.calendarAMI));
        return ndesc;
    }

}
