/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.bayes;

import demetra.data.DoubleSeq;
import demetra.math.matrices.Matrix;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder
public class BayesRegularizedRegressionModel {

    public static enum ModelType {
        GAUSSIAN, LAPLACE, T, BINOMIAL, POISSON, GEOMETRIC;
    }

    public static enum Prior {
        G,
        RIDGE,
        LASSO,
        HORSESHOE,
        HORSESHOEPLUS,
        LOGT;
    }
    
    public static enum Tau2Prior{
        HC,
        SB,
        UNIFORM;
    }
    
    @lombok.NonNull
    DoubleSeq y;
    @lombok.NonNull
    Matrix X;

    @lombok.NonNull
    ModelType model;
    
    int tdof;
    
    @lombok.NonNull
    Prior prior;
    
    @lombok.NonNull
    Tau2Prior tau2Prior;

    /**
     * Positions of the categorical variables
     */
    int[] categoricalVariables;
    
    

}
