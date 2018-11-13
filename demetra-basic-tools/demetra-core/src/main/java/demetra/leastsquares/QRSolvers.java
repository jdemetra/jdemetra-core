/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.leastsquares;

import demetra.maths.matrices.spi.QRSolver;
import demetra.leastsquares.internal.QRSolverImpl;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.internal.Householder;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class QRSolvers {

    public QRSolver fastSolver() {
        return FAST_FACTORY.get().get();
    }

    public QRSolver robustSolver() {
        return ROBUST_FACTORY.get().get();
    }

    public void setFastSolver(Supplier<QRSolver> factory) {
        FAST_FACTORY.set(factory);
    }

    public void setRobustSolver(Supplier<QRSolver> factory) {
        ROBUST_FACTORY.set(factory);
    }

    private AtomicReference<Supplier<QRSolver>> FAST_FACTORY = new AtomicReference<>(()
            -> QRSolverImpl.builder(new Householder()).build());
    private AtomicReference<Supplier<QRSolver>> ROBUST_FACTORY = new AtomicReference<>(()
            -> QRSolverImpl.builder(new Householder()).iterative(3).simpleIteration(true).build());
}
