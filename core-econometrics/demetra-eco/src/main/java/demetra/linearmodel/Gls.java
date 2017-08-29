/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.linearmodel;

import demetra.data.DataBlock;
import demetra.eco.EcoException;
import lombok.NonNull;
import demetra.leastsquares.IQRSolver;
import demetra.leastsquares.internal.QRSolver;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.maths.matrices.UpperTriangularMatrix;
import demetra.maths.matrices.internal.Householder;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import demetra.data.LogSign;
import demetra.maths.matrices.LowerTriangularMatrix;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@ServiceProvider(service = IGls.class)
public class Gls implements IGls {

    private static AtomicReference<Supplier<IQRSolver>> QR_FACTORY = new AtomicReference<>(()
            -> QRSolver.builder(new Householder()).build());

    public static void setDefaultSolver(Supplier<IQRSolver> factory) {
        QR_FACTORY.set(factory);
    }

    private final IQRSolver solver;

    public Gls() {
        solver = QR_FACTORY.get().get();
    }

    public Gls(@NonNull final IQRSolver solver) {
        this.solver = solver;
    }

    @Override
    public LeastSquaresResults compute(LinearModel model, Matrix cov) {

        Matrix L = cov.deepClone();
        try {
            SymmetricMatrix.lcholesky(L);
        } catch (Exception err) {
            throw new EcoException(EcoException.GLS_FAILED);
        }
        // yl = L^-1*y <-> L*yl = y
        DataBlock yl = DataBlock.of(model.getY());
        LowerTriangularMatrix.rsolve(L, yl);

        Matrix xl = model.variables();
        LowerTriangularMatrix.rsolve(L, xl);

        if (!solver.solve(yl, xl)) {
            throw new EcoException(EcoException.GLS_FAILED);
        }
        Matrix R = solver.R();
        Matrix bvar = SymmetricMatrix.UUt(UpperTriangularMatrix
                .inverse(R));
        return LeastSquaresResults.builder(yl, xl)
                .mean(model.isMeanCorrection())
                .estimation(solver.coefficients(), bvar)
                .ssq(solver.ssqerr())
                .residuals(solver.residuals())
                .logDeterminant(2 * LogSign.of(L.diagonal()).value)
                .build();
    }

}
