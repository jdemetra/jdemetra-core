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
import demetra.data.LogSign;
import demetra.maths.matrices.internal.Householder;

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
     * Solves the least squares problem 
     * @param y
     * @param x
     * @return
     */
    boolean solve(Doubles y, Matrix X);

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
    
    LogSign covarianceLogDeterminant();
}

class LS_Factory {

    static AtomicReference<Supplier<LeastSquaresSolver>> FAST_FACTORY = new AtomicReference<>(() -> 
            QRSolver.builder(new Householder()).normalize(false).build());
    static AtomicReference<Supplier<LeastSquaresSolver>> ROBUST_FACTORY = new AtomicReference<>(() -> 
            QRSolver.builder(new Householder()).normalize(true).iterative(3).simpleIteration(true).build());
}
