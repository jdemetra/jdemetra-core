/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.linearmodel;

import demetra.data.DoubleSeq;
import demetra.eco.EcoException;
import demetra.math.Constants;
import jdplus.leastsquares.QRSolution;
import jdplus.leastsquares.QRSolver;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.MatrixException;
import demetra.math.matrices.Matrix;

/**
 *
 * @author palatej
 */
public class OlsComputer implements jdplus.linearmodel.Ols.Processor {

    @Override
    public LeastSquaresResults compute(LinearModel model) {
        try {
            DoubleSeq y = model.getY();
            if (y.norm2() < Constants.getEpsilon()) {
                return null;
            }
            if (model.getVariablesCount() > 0) {
                FastMatrix x = model.variables();
                QRSolution solution = QRSolver.robustLeastSquares(y, x);
                FastMatrix bvar = solution.unscaledCovariance();
                return LeastSquaresResults.builder(y, x)
                        .mean(model.isMeanCorrection())
                        .estimation(solution.getB(), bvar)
                        .ssq(solution.getSsqErr())
                        .residuals(solution.getE())
                        .build();
            } else {
                return LeastSquaresResults.builder(y, null)
                        .mean(model.isMeanCorrection())
                        .ssq(y.ssq())
                        .residuals(y)
                        .build();

            }
        } catch (MatrixException err) {
            throw new EcoException(EcoException.OLS_FAILED);
        }
    }
}
