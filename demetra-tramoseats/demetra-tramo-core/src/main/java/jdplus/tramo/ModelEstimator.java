/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramo;

import demetra.design.Development;
import jdplus.regsarima.regular.IModelEstimator;
import jdplus.regsarima.regular.RegArimaModelling;
import jdplus.regsarima.RegSarimaProcessor;
import jdplus.tramo.internal.OutliersDetectionModule;
import jdplus.math.functions.levmar.LevenbergMarquardtMinimizer;

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
        RegSarimaProcessor processor = RegSarimaProcessor.builder()
                .minimizer(LevenbergMarquardtMinimizer.builder())
                .precision(eps)                
//                .startingPoint(RegSarimaProcessor.StartingPoint.Multiple)
                .build();
        context.estimate(processor);
        return true;
    }
}
