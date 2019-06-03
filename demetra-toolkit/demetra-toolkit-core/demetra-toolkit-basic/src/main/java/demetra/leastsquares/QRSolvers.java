/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.leastsquares;

import demetra.leastsquares.internal.AdvancedQRSolver;
import jdplus.maths.matrices.decomposition.Householder;
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

    private static final AtomicReference<Supplier<QRSolver>> FAST_FACTORY = new AtomicReference<>(()
            -> AdvancedQRSolver.builder(new Householder()).build());
    private static final AtomicReference<Supplier<QRSolver>> ROBUST_FACTORY = new AtomicReference<>(()
            -> AdvancedQRSolver.builder(new Householder()).iterative(3).simpleIteration(true).build());
}
