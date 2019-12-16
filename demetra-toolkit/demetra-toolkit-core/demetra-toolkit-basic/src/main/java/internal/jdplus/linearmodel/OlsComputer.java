/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package internal.jdplus.linearmodel;

import demetra.data.DoubleSeq;
import demetra.eco.EcoException;
import jdplus.leastsquares.QRSolution;
import jdplus.leastsquares.QRSolver;
import jdplus.linearmodel.LeastSquaresResults;
import jdplus.linearmodel.LinearModel;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.MatrixException;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.math.matrices.UpperTriangularMatrix;

/**
 *
 * @author palatej
 */
public class OlsComputer implements jdplus.linearmodel.Ols.Processor {

    @Override
    public LeastSquaresResults compute(LinearModel model) {
        try {
            DoubleSeq y = model.getY();
            Matrix x = model.variables();
            QRSolution solution = QRSolver.robustLeastSquares(y, x);
            Matrix R = solution.getQr().rawR();
            Matrix bvar = SymmetricMatrix.UUt(UpperTriangularMatrix
                    .inverse(R));
            return LeastSquaresResults.builder(y, x)
                    .mean(model.isMeanCorrection())
                    .estimation(solution.getB(), bvar)
                    .ssq(solution.getSsqErr())
                    .residuals(solution.getE())
                    .build();
        } catch (MatrixException err) {
            throw new EcoException(EcoException.OLS_FAILED);
        }
    }

}
