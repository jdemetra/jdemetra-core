/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.linearmodel;

import demetra.data.DoubleSequence;
import demetra.design.Development;
import demetra.maths.MatrixType;

/**
 * Describes the linear model: y = a + b * X
 * 
 * 
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Development(status = Development.Status.Release)
public interface LinearModelType {
    
    public static LinearModelType of(DoubleSequence y, boolean mean, MatrixType x){
        return new LightLinearModel(y, mean, x);
    }

    DoubleSequence getY();

    /**
     * Mean correction
     * @return 
     */
    boolean isMeanCorrection();

    /**
     * 
     * @return 
     */
    MatrixType getX();

}
