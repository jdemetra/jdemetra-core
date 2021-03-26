/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramo;

import nbbrd.design.Development;
import jdplus.regsarima.regular.IModelEstimator;
import jdplus.regsarima.regular.RegSarimaModelling;
import jdplus.regsarima.RegSarimaComputer;
import jdplus.tramo.internal.OutliersDetectionModule;
import jdplus.math.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.regarima.ami.ModellingUtility;

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
    public boolean estimate(RegSarimaModelling context) {
        context.getDescription().removeVariable(var -> ModellingUtility.isOutlier(var, true));
        if (outliers != null) {
            outliers.process(context, va);
        }
        if (!calc(context)) {
            return false;
        } else {
            return true;
        }
    }

    private boolean calc(RegSarimaModelling context) {
        RegSarimaComputer processor = RegSarimaComputer.builder()
                .minimizer(LevenbergMarquardtMinimizer.builder())
                .precision(eps)                
//                .startingPoint(RegSarimaComputer.StartingPoint.Multiple)
                .build();
        context.estimate(processor);
        return true;
    }
}
