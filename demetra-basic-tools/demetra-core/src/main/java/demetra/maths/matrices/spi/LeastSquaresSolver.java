/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths.matrices.spi;

import demetra.maths.matrices.Matrix;
import demetra.design.Algorithm;
import demetra.data.DoubleSequence;
import demetra.design.Development;
import demetra.design.ServiceDefinition;
import demetra.design.ThreadSafe;

/**
 * 
 * @author Jean Palate <jean.palate@nbb.be>
 */
@ThreadSafe
@Algorithm
@ServiceDefinition(isSingleton = true)
@Development(status = Development.Status.Beta)
public interface LeastSquaresSolver {


    /**
     * Solves the least squares problem: min || y - Xb ||.
     * <br>
     * b = (X'X)^-1 X'y
     * <br>
     * e = y - Xb = [I-X(X'X)^-1 X']y = My, e'e = y'My
     * @param y 
     * @param X 
     * @return True if the system was successfully solved
     */
    boolean solve(DoubleSequence y, Matrix X);

    /**
     * Returns b
     * @return 
     */
    DoubleSequence coefficients();

    /**
     * Returns e
     * @return 
     */
    DoubleSequence residuals();

    /**
     * Computes e'*e = y'My
     * @return 
     */
    double ssqerr();
}

