/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramo;

import demetra.design.Development;
import demetra.maths.functions.IParametricMapping;
import demetra.regarima.regular.IModelEstimator;
import demetra.regarima.regular.RegArimaModelling;
import demetra.sarima.RegSarimaProcessor;
import demetra.sarima.SarimaModel;
import demetra.tramo.internal.OutliersDetectionModule;

@Development(status = Development.Status.Beta)
class ModelEstimator implements IModelEstimator {

    private final OutliersDetectionModule outliers;
    private final double eps, va;

    ModelEstimator(double eps, double va, OutliersDetectionModule outliers) {
        this.eps = eps;
        this.va = va;
        this.outliers = outliers;
    }

    @Override
    public boolean estimate(RegArimaModelling context) {
        context.getDescription().removeVariable(var -> var.isOutlier(false));
        if (outliers != null) {
            outliers.process(context, va);
        }
        if (!calc(context)) {
            return false;
        } else {
            return true;
        }
    }

    private boolean calc(RegArimaModelling context) {
        IParametricMapping<SarimaModel> mapping = context.getDescription().getArimaComponent().defaultMapping();
        RegSarimaProcessor processor = RegSarimaProcessor.builder()
                .precision(eps)
                .startingPoint(RegSarimaProcessor.StartingPoint.HannanRissanen)
                .build();
        context.estimate(processor);
        return true;
    }
}
