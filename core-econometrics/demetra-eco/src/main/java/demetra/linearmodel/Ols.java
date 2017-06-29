/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.linearmodel;

import demetra.data.Doubles;
import demetra.eco.EcoException;
import lombok.NonNull;
import demetra.leastsquares.IQRSolver;
import demetra.leastsquares.internal.QRSolver;
import demetra.likelihood.ConcentratedLikelihood;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.maths.matrices.UpperTriangularMatrix;
import demetra.maths.matrices.internal.Householder;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class Ols implements IOls {

    private static AtomicReference<Supplier<IQRSolver>> QR_FACTORY = new AtomicReference<>(()
            -> QRSolver.builder(new Householder()).build());

    public static void setDefaultSolver(Supplier<IQRSolver> factory) {
        QR_FACTORY.set(factory);
    }

    private final IQRSolver solver;

    public Ols() {
        solver = QR_FACTORY.get().get();
    }

    public Ols(@NonNull final IQRSolver solver) {
        this.solver = solver;
    }

    @Override
    public OlsResults compute(LinearModel model) {
        Doubles y = model.getY();
        Matrix x = model.variables();
        if (!solver.solve(y, x)) {
            throw new EcoException(EcoException.OLS_FAILED);
        }
        Matrix R = solver.R();
        Matrix bvar = SymmetricMatrix.UUt(UpperTriangularMatrix
                .inverse(R));

        return new OlsResults(y, x, model.isMeanCorrection(), solver.coefficients(), bvar, solver.ssqerr());
    }

}
