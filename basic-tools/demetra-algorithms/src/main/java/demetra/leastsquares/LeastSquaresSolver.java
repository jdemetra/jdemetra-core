/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.leastsquares;

import demetra.maths.matrices.Matrix;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import demetra.data.Doubles;
import demetra.maths.matrices.impl.Householder;
import demetra.maths.matrices.impl.RobustHouseholder;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface LeastSquaresSolver {

    public static LeastSquaresSolver fastSolver() {
        return LS_Factory.FAST_FACTORY.get().get();
    }

    public static LeastSquaresSolver robustSolver() {
        return LS_Factory.ROBUST_FACTORY.get().get();
    }

    public static void setFastSolver(Supplier<LeastSquaresSolver> factory) {
        LS_Factory.FAST_FACTORY.set(factory);
    }

    public static void setRobustSolver(Supplier<LeastSquaresSolver> factory) {
        LS_Factory.ROBUST_FACTORY.set(factory);
    }

    /**
     *
     * @param y
     * @param x
     * @return
     */
    boolean compute(Doubles y, Matrix x);

    /**
     *
     * @return
     */
    Matrix covariance();

    /**
     *
     * @return
     */
    Doubles coefficients();

    /**
     *
     * @return
     */
    Doubles residuals();

    double ssqerr();

}

class LS_Factory {

    static AtomicReference<Supplier<LeastSquaresSolver>> FAST_FACTORY = new AtomicReference<>(() -> 
            QRSolver.builder(new Householder()).normalize(false).build());
    static AtomicReference<Supplier<LeastSquaresSolver>> ROBUST_FACTORY = new AtomicReference<>(() -> 
            QRSolver.builder(new Householder()).normalize(true).iterative(3).simpleIteration(true).build());
}
