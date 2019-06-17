/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.ssf.multivariate;

import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.ssf.ISsfRoot;
import jdplus.maths.matrices.FastMatrix;

/**
 *
 * @author palatej
 */
public interface ISsfErrors extends ISsfRoot{
    
//<editor-fold defaultstate="collapsed" desc="description">

    /**
     *
     * @return True if there is no error term or if the covariance matrices of the
 errors are diagonal for each position (and of course if the model is univariate)
     */
    boolean areIndependent();

    /**
     * Gets the variance of the measurements error at a given position
     *
     * @param pos
     * @param h The matrix that will contain the variance. Should be 0 on entry.
 The matrix must have the size of the measurements (=getCount(pos)).
     */
    void H(int pos, FastMatrix h);

    /**
     * Gets the Cholesky factor of the variance of the measurements error at a
 given position
     *
     * @param pos
     * @param r The matrix that will contain the cholesky factor. Should be 0 on
 entry. The matrix must have the size of the measurements
 (=getCount(pos)).
     */
    void R(int pos, FastMatrix r);
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="forward operations">

    /**
     * Computes V = V + H
     *
     * @param pos
     * @param V
     */
    void addH(int pos, FastMatrix V);
//</editor-fold>    

}
