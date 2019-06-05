/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.leastsquares;

import demetra.design.Algorithm;
import demetra.design.Development;
import demetra.data.DoubleSeq;
import jdplus.maths.matrices.FastMatrix;

/**
 * 
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Algorithm
@Development(status = Development.Status.Release)
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
    boolean solve(DoubleSeq y, FastMatrix X);

    /**
     * Returns b
     * @return 
     */
    DoubleSeq coefficients();

    /**
     * Returns e
     * @return 
     */
    DoubleSeq residuals();

    /**
     * Computes e'*e = y'My
     * @return 
     */
    double ssqerr();
}

