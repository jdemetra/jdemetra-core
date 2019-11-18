/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.leastsquares;

import demetra.data.DoubleSeq;
import jdplus.math.matrices.Matrix;

/**
 * Solution to the least squares problem: min || y - X*b ||
 * by means of the QR algorithm:
 * 
 * X = Q*R
 * || y - X*b || = || Q'*y - R*b || 
 * 
 * Q'*y = z = (z0', z1')'
 * z0 = R*b <=> R^-1*z0 = b
 * 
 * || y - X*b || = || z1 ||
 * 
 * z1 = e (residuals)
 * z1'z1 = ssqerr
 * 
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
public class QRSolution{
    private DoubleSeq b;
    private DoubleSeq e;
    private double ssqErr;
    
    private Matrix R;
}
