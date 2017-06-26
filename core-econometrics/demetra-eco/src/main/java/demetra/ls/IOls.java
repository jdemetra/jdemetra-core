/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.ls;

import demetra.eco.LinearModel;
import demetra.data.Doubles;
import demetra.maths.matrices.Matrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface IOls {
    LinearModel getModel();
    Doubles coefficients();
    double ser();
    double r2();
    double adjustedR2();
    Matrix covariance();
    
 }
