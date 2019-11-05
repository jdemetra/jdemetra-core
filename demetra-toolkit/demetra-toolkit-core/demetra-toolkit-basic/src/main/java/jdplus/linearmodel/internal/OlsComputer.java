/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.linearmodel.internal;

import demetra.data.DoubleSeq;
import demetra.eco.EcoException;
import jdplus.leastsquares.internal.DefaultQRSolver;
import jdplus.linearmodel.LeastSquaresResults;
import jdplus.linearmodel.LinearModel;
import jdplus.maths.matrices.FastMatrix;
import jdplus.maths.matrices.SymmetricMatrix;
import jdplus.maths.matrices.UpperTriangularMatrix;

/**
 *
 * @author palatej
 */
public class OlsComputer implements jdplus.linearmodel.Ols.Processor{
    
    @Override
    public LeastSquaresResults compute(LinearModel model) {
        DefaultQRSolver solver=new DefaultQRSolver();
        DoubleSeq y = model.getY();
        FastMatrix x = model.variables();
        if (!solver.solve(y, x)) {
            throw new EcoException(EcoException.OLS_FAILED);
        }
        FastMatrix R = solver.R();
        FastMatrix bvar = SymmetricMatrix.UUt(UpperTriangularMatrix
                .inverse(R));
        return LeastSquaresResults.builder(y, x)
                .mean(model.isMeanCorrection())
                .estimation(solver.coefficients(), bvar)
                .ssq(solver.ssqerr())
                .residuals(solver.residuals())
                .build();
    }

}
