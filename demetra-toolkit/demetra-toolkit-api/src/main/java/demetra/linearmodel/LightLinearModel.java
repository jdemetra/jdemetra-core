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
 *
 * @author palatej
 */

@Development(status = Development.Status.Release)
@lombok.Value
class LightLinearModel implements LinearModelType{
    @lombok.NonNull
    /**
     * Exogenous variable
     */
    private DoubleSequence y;

    /**
     * Mean correction
     */
    private boolean meanCorrection;

    /**
     * 
     */
    private MatrixType X;
    
}
