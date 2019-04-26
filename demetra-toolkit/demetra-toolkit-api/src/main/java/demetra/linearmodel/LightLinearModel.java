/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.linearmodel;

import demetra.design.Development;
import demetra.data.DoubleSeq;
import demetra.maths.matrices.Matrix;

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
    private DoubleSeq y;

    /**
     * Mean correction
     */
    private boolean meanCorrection;

    /**
     * 
     */
    private Matrix X;
    
}
