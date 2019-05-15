/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.linearmodel;

import demetra.eco.EcoException;
import lombok.NonNull;
import demetra.leastsquares.internal.AdvancedQRSolver;
import jd.maths.matrices.SymmetricMatrix;
import jd.maths.matrices.UpperTriangularMatrix;
import demetra.maths.matrices.internal.Householder;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.openide.util.lookup.ServiceProvider;
import demetra.leastsquares.QRSolver;
import demetra.data.DoubleSeq;
import jd.maths.matrices.FastMatrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class Ols {

    private static AtomicReference<Supplier<QRSolver>> QR_FACTORY = new AtomicReference<>(()
            -> AdvancedQRSolver.builder(new Householder()).build());

    public static void setDefaultSolver(Supplier<QRSolver> factory) {
        QR_FACTORY.set(factory);
    }

    private final QRSolver solver;

    public Ols() {
        solver = QR_FACTORY.get().get();
    }

    public Ols(@NonNull final QRSolver solver) {
        this.solver = solver;
    }

    public LeastSquaresResults compute(LinearModel model) {
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
