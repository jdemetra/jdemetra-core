/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.leastsquares;

import demetra.maths.matrices.Matrix;
import demetra.data.Doubles;

/**
 * 
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface ILeastSquaresSolver {


    /**
     * Solves the least squares problem: min || y - Xb ||.
     * <br>
     * b = (X'X)^-1 X'y
     * <br>
     * e = y - Xb = [I-X(X'X)^-1 X']y = My
     * @param y 
     * @param X 
     * @return
     */
    boolean solve(Doubles y, Matrix X);

    /**
     * Returns b
     * @return 
     */
    Doubles coefficients();

    /**
     * Computes e*e'
     * @return 
     */
    double ssqerr();
}

